package com.ohmteam.friendmapper.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.android.gms.maps.model.LatLng;
import com.ohmteam.friendmapper.util.HasLocation;

/**
 * Represents a single location on a GoogleMap, containing any number of friends. This is the second
 * level of the cluter/group hierarchy; a MapLocation represents a group.
 * 
 * @author Dylan
 */
public class MapLocation implements HasLocation {
	private final LatLng location;
	public final List<FacebookFriend> friends = new LinkedList<FacebookFriend>();

	/**
	 * Constructor. Initializes a new MapLocation with no friends attached.
	 * 
	 * @param location The actual location
	 */
	public MapLocation(LatLng location) {
		this.location = location;
	}

	@Override
	public LatLng getLocation() {
		return location;
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

	public static List<MapLocation> groupFriends(List<FacebookFriend> friends) {
		Map<LatLng, List<FacebookFriend>> locationsMap = new HashMap<LatLng, List<FacebookFriend>>();

		// put all friends in the locationsMap at their corresponding locations.
		for (FacebookFriend friend : friends) {
			LatLng friendLoc = friend.getLocation();
			List<FacebookFriend> neighbors = locationsMap.get(friendLoc);
			if (neighbors == null) {
				neighbors = new LinkedList<FacebookFriend>();
				locationsMap.put(friendLoc, neighbors);
			}
			neighbors.add(friend);
		}

		// transform the locationsMap that we just created into a List of MapLocations
		List<MapLocation> locations = new ArrayList<MapLocation>(locationsMap.size());
		for (Entry<LatLng, List<FacebookFriend>> entry : locationsMap.entrySet()) {
			MapLocation location = new MapLocation(entry.getKey(), entry.getValue());
			locations.add(location);
		}

		return locations;
	}
}
