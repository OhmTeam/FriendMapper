package com.ohmteam.friendmapper.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.os.Bundle;

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
	 * Saves the current data in this ContentOverseer to the given
	 * <code>bundle</code>. Fields generated in this method will be prefixed
	 * with the given <code>prefix</code> string, e.g. "myPrefix.entry1 = ..."
	 * 
	 * @param prefix
	 *            A string prepended to fields generated for this
	 *            ContentOverseer to be stored in the bundle.
	 * @param bundle
	 *            The Bundle to which the data in this object is stored
	 */
	public void saveToBundle(String prefix, Bundle bundle) {
		int index = 0;

		// Bundle Save Format:
		// entryCount = <num entries>,
		// entry0: <entry bundle 0>,
		// entry1: <entry bundle 1>,
		// ...
		// entryN: <entry bundle N>

		for (Entry<LatLng, Set<String>> entry : peepsInLocs.entrySet()) {

			// Entry Bundle Format:
			// latitude = <latitude>
			// longitude = <longitude>
			// names = [<name0>, <name1>, ..., <nameN>]
			LatLng loc = entry.getKey();
			Set<String> names = entry.getValue();
			Bundle entryBundle = new Bundle();

			entryBundle.putDouble("latitude", loc.latitude);
			entryBundle.putDouble("longitude", loc.longitude);

			String[] namesArray = new String[names.size()];
			names.toArray(namesArray);
			entryBundle.putStringArray("names", namesArray);

			bundle.putBundle(prefix + ".entry" + index, entryBundle);
			index++;
		}

		bundle.putInt(prefix + ".entryCount", index);
	}

	/**
	 * Loads the ContentOverseer data from the given <code>bundle</code>,
	 * overwriting any data currently stored in this object. The data format
	 * should be the same format that would be stored via
	 * <code>saveToBundle</code>, and will use the same prefix as well.
	 * 
	 * @param prefix
	 * @param bundle
	 */
	public void loadFromBundle(String prefix, Bundle bundle) {
		peepsInLocs.clear();

		int entryCount = bundle.getInt(prefix + ".entryCount");

		for (int i = 0; i < entryCount; i++) {
			Bundle entryBundle = bundle.getBundle(prefix + ".entry" + i);
			double lat = entryBundle.getDouble("latitude");
			double lon = entryBundle.getDouble("longitude");
			LatLng loc = new LatLng(lat, lon);
			String[] namesArray = entryBundle.getStringArray("names");

			Set<String> names = new HashSet<String>();
			for (String name : namesArray) {
				names.add(name);
			}
			peepsInLocs.put(loc, names);
		}
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
