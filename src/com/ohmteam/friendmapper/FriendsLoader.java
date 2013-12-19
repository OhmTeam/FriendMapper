package com.ohmteam.friendmapper;

import java.util.HashMap;
import java.util.HashSet;
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

public class FriendsLoader {
	private final static String TAG = "FriendsLoader";

	public static interface Callback {
		void onComplete(Map<GraphUser, FriendLocation> friendsLocations);
	}

	public void loadFriends(final Session session, final Callback callback) {
		Log.d(TAG, "Load friends...");
		if (session == null || session.isClosed())
			return;

		Request req = Request.newMyFriendsRequest(session, new Request.GraphUserListCallback() {
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
					public void onCompleted(Map<String, FriendLocation> friendLocations) {

						Map<GraphUser, FriendLocation> results = new HashMap<GraphUser, FriendLocation>();
						for (GraphUser friend : users) {
							String locId = getLocationId(friend);
							if (locId != null) {
								FriendLocation loc = friendLocations.get(locId);
								if (loc != null)
									results.put(friend, loc);
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
	 * @param user
	 *            The user. May be null.
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
