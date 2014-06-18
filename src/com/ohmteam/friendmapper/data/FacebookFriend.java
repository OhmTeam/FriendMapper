package com.ohmteam.friendmapper.data;

import com.google.android.gms.maps.model.LatLng;
import com.ohmteam.friendmapper.util.HasLocation;

/**
 * Represents a Friend on Facebook.
 * 
 * @author Dylan
 */
public class FacebookFriend implements HasLocation {
	private final String id;
	private final String name;
	private final LatLng location;
	private boolean loadedImage;

	/**
	 * Constructor.
	 * 
	 * @param id The friend's ID according to Facebook. The ID should be able to be used with the
	 *        Facebook API, and should not be null.
	 * @param name The friend's full name, for display purposes.
	 * @param location The location that the friend provided to Facebook.
	 */
	public FacebookFriend(String id, String name, LatLng location) {
		this.id = id;
		this.name = name;
		this.location = location;
		this.loadedImage = false;

	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public LatLng getLocation() {
		return location;
	}

	public String getProfilePicURL() {
		return "https://graph.facebook.com/" + id + "/picture?type=normal";
	}

	public boolean imageLoaded() {
		return loadedImage;
	}

	public void setImageLoaded(boolean imageLoaded) {
		this.loadedImage = imageLoaded;
	}

	@Override
	public int hashCode() {
		// Auto-generated hashCode method, by Eclipse.
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		// auto-generated equals method, by Eclipse
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FacebookFriend other = (FacebookFriend) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
