package com.ohmteam.friendmapper.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.ohmteam.friendmapper.util.GridMap;
import com.ohmteam.friendmapper.util.Tuple;

/**
 * An object that manages a friends list and displays it as a series of markers.
 * Friends in overlapping locations will be joined as a group, and locations
 * that get too close to each other will be joined as a cluster. See
 * {@link MapLocation} and {@link MapMarker} for details of the cluster/group
 * hierarchy.
 * 
 * An instance of this class can be pointed at a GoogleMap, where it will listen
 * for camera change events; it recalculates the groups and updates the marker
 * display as the camera zoom changes.
 * 
 * @author Dylan
 */
public class MarkerManager {
	private final Activity activity;
	private GoogleMap map;
	private final int clusterRadius;

	private final List<FacebookFriend> friends = new LinkedList<FacebookFriend>();
	private final List<MapMarker> markers = new LinkedList<MapMarker>();

	/**
	 * Constructor.
	 * 
	 * @param activity
	 *            A reference to an activity so that this manager can request to
	 *            run tasks on the UI thread (necessary for updating markers).
	 *            TODO: maybe use a Handler instead, for better encapsulation so
	 *            that this class isn't coupled to Activity anymore.
	 * @param clusterRadius
	 *            The number of pixels to use as the location clustering
	 *            threshold. Note: the clustering algorithm is very primitive at
	 *            this time, so the clusterRadius is simply used as a tile size
	 *            for grid-based spacial bucketing.
	 */
	public MarkerManager(Activity activity, int clusterRadius) {
		this.activity = activity;
		this.clusterRadius = clusterRadius;
	}

	/**
	 * Point this manager at a GoogleMap, so that it can listen for changes in
	 * the zoom level and update the friend markers accordingly.
	 * 
	 * @param map
	 *            The map for this manager to use.
	 */
	public void setMap(GoogleMap map) {
		if (map != null && map != this.map) {
			this.map = map;
			this.map.setOnCameraChangeListener(new CameraListener());
			activity.runOnUiThread(new RecalculateMarkersTask());
		}
	}

	/**
	 * Set the friends list, triggering a recalculation of the markers, and a UI
	 * update.
	 * 
	 * @param friends
	 *            A list containing the friends to be displayed.
	 */
	public synchronized void setFriends(List<FacebookFriend> friends) {
		this.friends.clear();
		this.friends.addAll(friends);
		activity.runOnUiThread(new RecalculateMarkersTask());
	}

	/**
	 * Save the state (current friends list) of this manager to a Bundle.
	 * 
	 * @param bundle
	 *            The bundle to save to.
	 */
	public void saveTo(Bundle bundle) {
		Bundle friendsList = FacebookFriendBundler.friendsToBundle(friends);
		bundle.putBundle("markerManagerFriends", friendsList);
	}

	/**
	 * Load a state (friends list) from the given bundle into this manager.
	 * 
	 * @param bundle
	 *            The bundle to load from
	 */
	public void loadFrom(Bundle bundle) {
		Bundle friendsBundle = bundle.getBundle("markerManagerFriends");
		setFriends(FacebookFriendBundler.friendsFromBundle(friendsBundle));
	}

	/**
	 * Listens for changes in the map's camera zoom level. When the zoom
	 * changes, the markers should be recalculated.
	 */
	private class CameraListener implements GoogleMap.OnCameraChangeListener {
		private float latestZoom = 0;

		@Override
		public void onCameraChange(CameraPosition cam) {
			// only do work if the zoom is different from last time
			if (cam.zoom != latestZoom) {
				latestZoom = cam.zoom;
				Projection mapProjection = map.getProjection();
				recalculateMarkers(mapProjection);
			}
		}
	}

	/**
	 * A Runnable that calls `recalculateMarkers`. This exists so that code
	 * running outside of the UI thread can have the markers be updated within
	 * the UI thread.
	 */
	private class RecalculateMarkersTask implements Runnable {
		@Override
		public void run() {
			if (map != null) {
				recalculateMarkers(map.getProjection());
			}
		}
	}

	/**
	 * Based on the current friends list, calculates a hierarchy of clusters and
	 * locations, then adds the appropriate markers to the map.
	 * 
	 * @param mapProjection
	 *            the GoogleMap's projection, used to transform back and forth
	 *            between Latitude/Longitude space and Pixel space.
	 */
	private void recalculateMarkers(Projection mapProjection) {
		// detach all of the previous markers, and clear the list
		for (MapMarker marker : markers) {
			marker.detach();
		}
		markers.clear();

		// -----------------------------------------------------------
		// assemble a list of MapLocations based on all of the friends
		// -----------------------------------------------------------

		Map<LatLng, List<FacebookFriend>> locationsMap = new HashMap<LatLng, List<FacebookFriend>>();
		// put all friends in the locationsMap at their corresponding locations.
		// scala: locationsMap = friends.groupBy(_.getLocation)
		for (FacebookFriend friend : friends) {
			LatLng friendLoc = friend.getLocation();
			List<FacebookFriend> neighbors = locationsMap.get(friendLoc);
			if (neighbors == null) {
				neighbors = new LinkedList<FacebookFriend>();
				locationsMap.put(friendLoc, neighbors);
			}
			neighbors.add(friend);
		}
		// transform the locationsMap that we just created into a List of
		// MapLocations
		// scala: locations = (for{(loc, friends) <- locationsMap} yield new
		// MapLocation(loc, friends)).toList
		List<MapLocation> locations = new ArrayList<MapLocation>(
				locationsMap.size());
		for (Entry<LatLng, List<FacebookFriend>> entry : locationsMap
				.entrySet()) {
			MapLocation location = new MapLocation(entry.getKey(),
					entry.getValue());
			locations.add(location);
		}

		// -----------------------------------------------------------------
		// group all of the MapLocations by clusters that are near each other
		// -----------------------------------------------------------------

		GridMap<MapLocation> clusterGrid = new GridMap<MapLocation>(
				clusterRadius);
		// add all locations to the cluster grid so that they get grouped by
		// what tile they belong in
		for (MapLocation location : locations) {
			Point pixelLocation = mapProjection
					.toScreenLocation(location.location);
			clusterGrid.add(pixelLocation, location);
		}
		for (Point tileCorner : clusterGrid.getFilledTiles()) {
			// for each tile in the grid, find the locations that were added to
			// that tile
			// scala: tileLocations = clusterGrid.getNearbyEntries(tileCorner,
			// 0).map(_._2)
			List<Tuple<Point, MapLocation>> tileEntries = clusterGrid
					.getNearbyEntries(tileCorner, 0);
			List<MapLocation> tileLocations = new ArrayList<MapLocation>(
					tileEntries.size());
			for (Tuple<Point, MapLocation> entry : tileEntries) {
				tileLocations.add(entry._2);
			}

			// Pick the appropriate marker position based on the tile contents:
			// If just one marker was in the tile, use that marker's position.
			// If multiple markers were in the tile, use the tile's center pos.
			LatLng markerPos = null;
			int numLocations = tileLocations.size();
			if (numLocations == 0) {
				continue; // but this shouldn't happen anyway
			} else if (numLocations == 1) {
				// for one location, use its LatLng for the marker
				markerPos = tileLocations.get(0).location;
			} else {
				// for many locations, use the grid center for the marker
				Point tileCenterPixel = clusterGrid
						.roundToGridCenter(tileCorner);
				LatLng tileCenter = mapProjection
						.fromScreenLocation(tileCenterPixel);
				markerPos = tileCenter;
			}

			// finally, add the marker to the map, and the list of markers.
			MapMarker marker = new MapMarker(markerPos, tileLocations);
			marker.attach(map);
			markers.add(marker);
		}
	}

	public void removeAllMarkers() {

		// Clear the list of loaded Facebook friends so that the map is not
		// re-populated when re-sized

		List<FacebookFriend> emptyList = new LinkedList<FacebookFriend>();
		setFriends(emptyList);
		/*
		 * this.friends.clear();
		 * 
		 * // Remove each Marker from the map for (MapMarker marker : markers) {
		 * marker.detach(); }
		 */
	}

	public boolean friendsListIsEmpty() {
		return friends.isEmpty();
	}
}
