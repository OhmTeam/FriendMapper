package com.ohmteam.friendmapper.data.clustering;

import com.google.android.gms.maps.model.LatLng;

/**
 * An event that states a Clusteree's location changed.
 * 
 * @author Dylan
 */
public class LocationChange {
	private final LatLng fromLocation;
	private final LatLng toLocation;

	public LocationChange(LatLng fromLocation, LatLng toLocation) {
		this.fromLocation = fromLocation;
		this.toLocation = toLocation;
	}

	public LatLng getFromLocation() {
		return fromLocation;
	}

	public LatLng getToLocation() {
		return toLocation;
	}

	@Override
	public String toString() {
		return "LocationChange(" + getFromLocation() + " to " + getToLocation() + ")";
	}
}