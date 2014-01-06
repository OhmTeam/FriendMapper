package com.ohmteam.friendmapper.data;

import java.util.LinkedList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

/**
 * Represents a single location on a GoogleMap, containing any number of
 * friends. This is the second level of the cluter/group hierarchy; a
 * MapLocation represents a group.
 * 
 * @author Dylan
 */
public class MapLocation {
	public final LatLng location;
	public final List<FacebookFriend> friends = new LinkedList<FacebookFriend>();

	/**
	 * Constructor. Initializes a new MapLocation with no friends attached.
	 * 
	 * @param location The actual location
	 */
	public MapLocation(LatLng location) {
		this.location = location;
	}

	/**
	 * Constructor. Initializes a new MapLocation with a list of friends.
	 * 
	 * @param location The actual location
	 * @param friends A list containing the friends that are at this location
	 */
	public MapLocation(LatLng location, List<FacebookFriend> friends) {
		this.location = location;
		this.friends.addAll(friends);
	}
}
