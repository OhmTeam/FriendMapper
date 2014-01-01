package com.ohmteam.friendmapper.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.android.gms.maps.model.LatLng;

/**
 * This class monitors the content loaded from facebook and content pushed to
 * google maps. It is part of the domain, and can be accessed by both maps and
 * facebook related objects to help make decisions on what information needs to
 * be loaded or displayed.
 * 
 * @author Rob
 */
public class ContentOverseer {

	private final Map<LatLng, Set<String>> peepsInLocs;

	public ContentOverseer() {
		peepsInLocs = new HashMap<LatLng, Set<String>>();
	}

	/**
	 * Adds a used LatLng coordinate to and name to the peepsInLocs Map. Adds
	 * friends name to Set if coordinate is already used.
	 * 
	 * @param coordinates
	 * @param name
	 */
	public void addUsedLocation(LatLng coordinates, String name) {
		Set<String> newPeepsSet;

		if (locationUsed(coordinates)) {
			newPeepsSet = peepsInLocs.get(coordinates);
		} else {
			newPeepsSet = new HashSet<String>();
		}

		newPeepsSet.add(name);

		peepsInLocs.put(coordinates, newPeepsSet);

	}

	/**
	 * 
	 * @param coordinates
	 * @return True if locations used, False if not
	 */
	public boolean locationUsed(LatLng coordinates) {
		return peepsInLocs.containsKey(coordinates);
	}

	/**
	 * 
	 * @param coordinates
	 * @return String of all friend names
	 */
	public String getFriendsFromLoc(LatLng coordinates) {
		String friends = "";

		Set<String> friendsSet = peepsInLocs.get(coordinates);
		for (String s : friendsSet) {
			friends += "\n";
			friends += s;
		}

		return friends;
	}

	/**
	 * 
	 * @return Set of all used locations
	 */
	public Set<LatLng> getUsedLocations() {
		return peepsInLocs.keySet();
	}

}
