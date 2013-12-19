package com.ohmteam.friendmapper;

import org.json.JSONObject;

/**
 * Represents a friend's location. Contains an id which is the Facebook id for
 * the location, the name of the location, latitude, and longitude.
 * 
 * @author Dylan
 */
public class FriendLocation {
	private final String id;
	private final String name;
	private final Double latitude;
	private final Double longitude;

	public FriendLocation(String id, String name, Double latitude, Double longitude) {
		this.id = id;
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public static FriendLocation fromJson(JSONObject json) {
		if (json == null)
			return null;

		String id = json.optString("id");
		String name = json.optString("name");
		JSONObject loc = json.optJSONObject("location");

		if (id == null || loc == null)
			return null;
		else {
			double longitude = loc.optDouble("longitude");
			double latitude = loc.optDouble("latitude");
			return new FriendLocation(id, name, latitude, longitude);
		}
	}

	@Override
	public String toString() {
		return new StringBuilder("FriendLocation(")
			.append(getName())
			.append(", {id: ").append(getId())
			.append(", lat: ").append(getLatitude())
			.append(", long: ").append(getLongitude())
			.append("})")
			.toString();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}
}
