package com.ohmteam.friendmapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;

/**
 * Provides a means of requesting a large number of locations at once, via
 * {@link #requestLocations(Session, Set, Callback)}. Locations are returned as
 * instances of {@link FriendLocation}, which contains an id, name, latitude,
 * and longitude.
 * 
 * This class is not meant to be instantiated. Client code should only use the
 * static methods of this class.
 * 
 * @author Dylan
 */
public class FriendLocationRequest {

	private final static String TAG = "FriendLocationRequest";
	private final static int requestBatchMaxSize = 50;

	private FriendLocationRequest() {
		// do not instantiate
	}

	public static interface Callback {
		/**
		 * Called when the request completes for all location requests.
		 * 
		 * @param friendLocations
		 *            A mapping of location IDs to locations
		 */
		void onCompleted(Map<String, FriendLocation> friendLocations);
	}

	public static void requestLocations(Session session, Set<String> locationIds, Callback callback) {
		Log.d(TAG, "requesting info for " + locationIds.size() + " locations");

		final RequestInProgress rip = new RequestInProgress(callback, locationIds.size());

		// Translate each locationId to a request for that location
		final Set<Request> allRequests = new HashSet<Request>();
		for (String locationId : locationIds) {
			Bundle params = new Bundle();
			params.putString("fields", "location,name");
			Request request = new Request(session, locationId, params, null, new SingleRequestCallback(rip));
			allRequests.add(request);
		}

		// Group location requests into batches.
		// Note that there is a maximum size for a single batch, so ensure
		// that no single batch exceeds that size.
		final List<RequestBatch> batches = new ArrayList<RequestBatch>();
		RequestBatch batch = null;
		for (Request request : allRequests) {
			// if needed, create a new batch and add it to the list
			if (batch == null) {
				batch = new RequestBatch();
				batches.add(batch);
			}

			// add a request to the current batch
			batch.add(request);

			// if the batch is at capacity, nullify the reference
			// so that a new batch will be created next time
			if (batch.size() >= requestBatchMaxSize) {
				batch = null;
			}
		}

		// Execute each batch request
		for (RequestBatch b : batches) {
			b.executeAsync();
		}
	}

	private static class RequestInProgress {
		private final Callback callback;
		private final Map<String, FriendLocation> results;
		private final int numRequests;
		private int size;

		public RequestInProgress(Callback callback, int numRequests) {
			this.callback = callback;
			this.results = new HashMap<String, FriendLocation>();
			this.numRequests = numRequests;
			this.size = 0;
		}

		public synchronized void add(FriendLocation location, FacebookRequestError error) {
			size += 1;

			if (error != null) {
				Log.d(TAG, "location request error: " + error);
			}
			else if (location != null) {
				results.put(location.getId(), location);
			}

			if (size == numRequests) {
				callback.onCompleted(results);
			}
		}
	}

	private static class SingleRequestCallback implements Request.Callback {
		private final RequestInProgress rip;

		public SingleRequestCallback(RequestInProgress rip) {
			this.rip = rip;
		}

		@Override
		public void onCompleted(Response response) {
			FacebookRequestError error = response.getError();
			GraphObject obj = response.getGraphObject();
			if (error != null) {
				rip.add(null, error);
			} else if (obj != null) {
				JSONObject responseJson = obj.getInnerJSONObject();
				FriendLocation loc = FriendLocation.fromJson(responseJson);
				rip.add(loc, null);
			}
		}
	}

}
