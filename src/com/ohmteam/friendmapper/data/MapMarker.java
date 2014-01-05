package com.ohmteam.friendmapper.data;

import java.util.List;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapMarker {
	private List<MapLocation> locations;
	private Marker marker = null;
	private final LatLng markerPos;

	public MapMarker(LatLng markerPos, List<MapLocation> locations) {
		this.markerPos = markerPos;
		this.locations = locations;
	}

	public int getNumFriends() {
		int count = 0;
		for (MapLocation loc : locations) {
			count += loc.friends.size();
		}
		return count;
	}

	public FacebookFriend getFirstFriend() {
		for (MapLocation loc : locations) {
			if (loc.friends.size() > 0) {
				return loc.friends.get(0);
			}
		}
		return null;
	}

	public MarkerOptions getMarkerOptions() {
		MarkerOptions mo = new MarkerOptions();

		// set the marker's Position
		mo.position(markerPos);

		// set the marker's Title
		int numFriends = getNumFriends();
		String title = "?";
		if (numFriends > 1) {
			title = numFriends + " friends...";
		} else if (numFriends == 1) {
			title = getFirstFriend().getName();
		}
		mo.title(title);

		// set the marker's visuals
		BitmapDescriptor icon = null;
		if (locations.size() > 1) {
			icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
		} else if (numFriends > 1) {
			icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
		} else {
			icon = BitmapDescriptorFactory.defaultMarker();
		}
		mo.icon(icon);

		return mo;
	}

	public void attach(GoogleMap toMap) {
		detach();
		marker = toMap.addMarker(getMarkerOptions());
	}

	public void detach() {
		if (marker != null) {
			marker.remove();
			marker = null;
		}
	}
}
