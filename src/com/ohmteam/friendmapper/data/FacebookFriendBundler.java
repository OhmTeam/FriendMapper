package com.ohmteam.friendmapper.data;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

public class FacebookFriendBundler {

	public static Bundle friendToBundle(FacebookFriend friend) {
		Bundle b = new Bundle();
		b.putString("id", friend.getId());
		b.putString("name", friend.getName());
		LatLng loc = friend.getLocation();
		b.putDouble("lat", loc.latitude);
		b.putDouble("long", loc.longitude);
		return b;
	}

	public static FacebookFriend friendFromBundle(Bundle b) {
		String id = b.getString("id");
		String name = b.getString("name");
		double lat = b.getDouble("lat");
		double lng = b.getDouble("long");
		LatLng loc = new LatLng(lat, lng);
		return new FacebookFriend(id, name, loc);
	}

	public static Bundle friendsToBundle(List<FacebookFriend> friends) {
		Bundle b = new Bundle();
		b.putInt("numFriends", friends.size());
		int i = 0;
		for (FacebookFriend friend : friends) {
			b.putBundle("friend" + i, friendToBundle(friend));
			++i;
		}
		return b;
	}

	public static List<FacebookFriend> friendsFromBundle(Bundle b) {
		int numFriends = b.getInt("numFriends");
		List<FacebookFriend> friends = new ArrayList<FacebookFriend>(numFriends);
		for (int i = 0; i < numFriends; i++) {
			Bundle fb = b.getBundle("friend" + i);
			FacebookFriend friend = friendFromBundle(fb);
			friends.add(friend);
		}
		return friends;
	}
}
