package com.ohmteam.friendmapper;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphLocation;
import com.facebook.model.GraphUser;
import com.google.android.gms.maps.model.LatLng;
import com.ohmteam.friendmapper.data.FacebookFriend;

public class FriendsLoader {
	private final static String TAG = "FriendsLoader";

	/**
	 * Callback object that is used to pass a list of FacebookFriends back from
	 * an asynchronous request for friends.
	 * 
	 * @author Dylan
	 */
	public static interface Callback {
		void onComplete(List<FacebookFriend> friends);
	}

	/**
	 * Asynchronously load a list of FacebookFriends belonging to the logged-in
	 * user in the given session.
	 * 
	 * @param session A Facebook session, on which a user should be logged in
	 * @param callback A function to be called when the results list is ready
	 */
	public void loadFriends(final Session session, final Callback callback) {
		Log.d(TAG, "Load friends...");
		if (session == null || session.isClosed())
			return;

		// First, a FriendsList request is made via the facebook API.
		Request req = Request.newMyFriendsRequest(session, new Request.GraphUserListCallback() {

			/*
			 * Once the initial request is completed, additionally request
			 * details for all of the locations for all of the friends in the
			 * results list. (The results list will only have IDs
			 */
			@Override
			public void onCompleted(final List<GraphUser> users, Response response) {
				Log.i(TAG, String.format("Loaded %d friends", users.size()));

				Set<String> locationIds = new HashSet<String>();
				for (GraphUser user : users) {
					String locId = getLocationId(user);
					if (locId != null)
						locationIds.add(locId);
				}

				FriendLocationRequest.requestLocations(session, locationIds, new FriendLocationRequest.Callback() {

					@Override
					public void onCompleted(Map<String, LatLng> locations) {

						List<FacebookFriend> results = new LinkedList<FacebookFriend>();
						for (GraphUser friend : users) {
							String locId = getLocationId(friend);
							if (locId != null) {
								LatLng loc = locations.get(locId);
								if (loc != null) {
									String id = friend.getId();
									String name = friend.getName();
									results.add(new FacebookFriend(id, name, loc));
								}
							}
						}
						callback.onComplete(results);
					}
				});
			}
		});

		Bundle reqParams = new Bundle();
		reqParams.putString("fields", "location,name");
		req.setParameters(reqParams);
		req.executeAsync();
	}

	/**
	 * Get the location id for the given user
	 * 
	 * @param user The user. May be null.
	 * @return The location id. May be null.
	 */
	private String getLocationId(GraphUser user) {
		if (user == null)
			return null;
		GraphLocation loc = user.getLocation();
		if (loc == null)
			return null;
		JSONObject json = loc.getInnerJSONObject();
		if (json == null)
			return null;
		return json.optString("id");
	}
}
