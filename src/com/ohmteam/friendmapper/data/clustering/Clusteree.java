package com.ohmteam.friendmapper.data.clustering;

import com.google.android.gms.maps.model.LatLng;
import com.ohmteam.friendmapper.util.HasLocation;

/**
 * A participant in the Clusterizer's clustering algorithm. It represents a single input to the
 * algorithm, and has a location where it thinks its cluster lies.
 * 
 * @author Dylan
 * 
 * @param <T> The type of the input that this Clusteree holds.
 */
public class Clusteree<T extends HasLocation> {
	private final T self;
	private LatLng clusterLocation;

	public Clusteree(T self, LatLng markerLocation) {
		this.self = self;
		this.clusterLocation = markerLocation;
	}

	public T getSelf() {
		return self;
	}

	public LatLng getRealLocation() {
		return self.getLocation();
	}

	public LatLng getClusterLocation() {
		return clusterLocation;
	}

	public LocationChange setClusterLocation(LatLng clusterLocation) {
		if (clusterLocation.equals(this.clusterLocation)) {
			return null;
		} else {
			LocationChange change = new LocationChange(this.clusterLocation, clusterLocation);
			this.clusterLocation = clusterLocation;
			return change;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o instanceof Clusteree) {
			Clusteree<?> that = (Clusteree<?>) o;
			return this.self.equals(that.self);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return self.hashCode();
	}
}
