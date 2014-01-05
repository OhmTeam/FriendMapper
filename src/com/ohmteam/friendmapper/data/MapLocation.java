package com.ohmteam.friendmapper.data;

import java.util.LinkedList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class MapLocation {
	public final LatLng location;
	public final List<FacebookFriend> friends = new LinkedList<FacebookFriend>();

	public MapLocation(LatLng location) {
		this.location = location;
	}

	public MapLocation(LatLng location, List<FacebookFriend> friends) {
		this.location = location;
		this.friends.addAll(friends);
	}
}
