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

public class MarkerManager {
	private final Activity activity;
	private GoogleMap map;
	private final int clusterRadius;

	private final List<FacebookFriend> friends = new LinkedList<FacebookFriend>();
	private final List<MapMarker> markers = new LinkedList<MapMarker>();

	public MarkerManager(Activity activity, int clusterRadius) {
		this.activity = activity;
		this.clusterRadius = clusterRadius;
	}

	public void setMap(GoogleMap map) {
		if (map != null && map != this.map) {
			this.map = map;
			this.map.setOnCameraChangeListener(new CameraListener());
			activity.runOnUiThread(new RecalculateMarkersTask());
		}
	}

	public synchronized void setFriends(List<FacebookFriend> friends) {
		this.friends.clear();
		this.friends.addAll(friends);
		activity.runOnUiThread(new RecalculateMarkersTask());
	}

	public void saveTo(Bundle bundle) {
		Bundle friendsList = FacebookFriendBundler.friendsToBundle(friends);
		bundle.putBundle("markerManagerFriends", friendsList);
	}

	public void loadFrom(Bundle bundle) {
		Bundle friendsBundle = bundle.getBundle("markerManagerFriends");
		setFriends(FacebookFriendBundler.friendsFromBundle(friendsBundle));
	}

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

	private class RecalculateMarkersTask implements Runnable {
		@Override
		public void run() {
			if (map != null) {
				recalculateMarkers(map.getProjection());
			}
		}
	}

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
		List<MapLocation> locations = new ArrayList<MapLocation>(locationsMap.size());
		for (Entry<LatLng, List<FacebookFriend>> entry : locationsMap.entrySet()) {
			MapLocation location = new MapLocation(entry.getKey(), entry.getValue());
			locations.add(location);
		}

		// -----------------------------------------------------------------
		// group all of the MapLocations by clusters that are near each other
		// -----------------------------------------------------------------

		GridMap<MapLocation> clusterGrid = new GridMap<MapLocation>(clusterRadius);
		// add all locations to the cluster grid so that they get grouped by
		// what tile they belong in
		for (MapLocation location : locations) {
			Point pixelLocation = mapProjection.toScreenLocation(location.location);
			clusterGrid.add(pixelLocation, location);
		}
		for (Point tileCorner : clusterGrid.getFilledTiles()) {
			// for each tile in the grid, find the locations that were added to
			// that tile
			// scala: tileLocations = clusterGrid.getNearbyEntries(tileCorner,
			// 0).map(_._2)
			List<Tuple<Point, MapLocation>> tileEntries = clusterGrid.getNearbyEntries(tileCorner, 0);
			List<MapLocation> tileLocations = new ArrayList<MapLocation>(tileEntries.size());
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
				Point tileCenterPixel = clusterGrid.roundToGridCenter(tileCorner);
				LatLng tileCenter = mapProjection.fromScreenLocation(tileCenterPixel);
				markerPos = tileCenter;
			}

			// finally, add the marker to the map, and the list of markers.
			MapMarker marker = new MapMarker(markerPos, tileLocations);
			marker.attach(map);
			markers.add(marker);
		}
	}
}
