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
import com.google.android.gms.maps.model.LatLng;

/**
 * Utility class containing static methods for finding the latitude and
 * longitude for facebook locations. Uses request batching to minimize the HTTP
 * overhead of Facebook API calls, while enabline many locations to be requested
 * at once.
 * 
 * @author Dylan
 */
public class FriendLocationRequest {

	// Tag for Android Logging
	private final static String TAG = "FriendLocationRequest";

	/**
	 * A limitation of the Facebook API. Batched requests can only have up to 50
	 * requests at a time. Any more than that and we need to create multiple
	 * batches.
	 */
	private final static int requestBatchMaxSize = 50;

	private FriendLocationRequest() {
		// do not instantiate
	}

	public static interface Callback {
		/**
		 * Called when the request completes for all location requests.
		 * 
		 * @param friendsById A map of friend with their IDs as keys
		 */
		void onCompleted(Map<String, LatLng> friendsById);
	}

	/**
	 * Using the Facebook Web API, request location details for a set of
	 * locations. Details for a location are its Latitude and Longitude. The
	 * requested locations are specified by their Facebook IDs, which will
	 * generally have been returned by some previous API call.
	 * 
	 * @param session The Facebook Session, required for making Facebook API
	 *        calls.
	 * @param locationIds A Set containing the IDs for all of the locations
	 *        being looked up.
	 * @param callback A callback object whose onCompleted method will be called
	 *        once the request has been completed.
	 */
	public static void requestLocations(Session session, Set<String> locationIds, Callback callback) {

		final RequestInProgress rip = new RequestInProgress(callback, locationIds.size());

		// Translate each locationId to a request for that location
		final Set<Request> allRequests = new HashSet<Request>();
		for (String locationId : locationIds) {
			Bundle params = new Bundle();
			params.putString("fields", "location");
			Request request = new Request(session, locationId, params, null, new SingleRequestCallback(locationId, rip));
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

	/**
	 * An instance of this class is used to contain the results of many
	 * individual location detail requests. As a request for a location's
	 * details is fulfilled, it adds its result to an instance of this class.
	 * Once the number of expected requests has been reached, it invokes a
	 * callback function with the accumulated results.
	 * 
	 * @author Dylan
	 */
	private static class RequestInProgress {
		private final Callback callback;
		private final Map<String, LatLng> results;
		private final int numRequests;
		private int size;

		/**
		 * Constructor.
		 * 
		 * @param callback The callback function to be called once all of the
		 *        expected requests have completed.
		 * @param numRequests The number of expected requests.
		 */
		public RequestInProgress(Callback callback, int numRequests) {
			this.callback = callback;
			this.results = new HashMap<String, LatLng>();
			this.numRequests = numRequests;
			this.size = 0;
		}

		/**
		 * Adds the results of one location request to the results map. This
		 * method can specify either a location or an error; either case will
		 * increment the "satisfied requests" counter. If the counter reaches
		 * the expected number of requests, the results callback will be
		 * invoked.
		 * 
		 * @param id The ID of the location whose details were requested
		 * @param location The details (Latitude, Longitude) of the location.
		 *        May be null.
		 * @param error An error that occurred while requesting location
		 *        details. May be null.
		 */
		public synchronized void add(String id, LatLng location, FacebookRequestError error) {
			size += 1;

			if (error != null) {
				Log.d(TAG, "location request error: " + error);
			} else if (location != null) {
				results.put(id, location);
			}

			if (size == numRequests) {
				callback.onCompleted(results);
			}
		}
	}

	/**
	 * Callback for a location request. It puts the LatLng location into the
	 * results map for a RequestInProgress when the request has completed.
	 * 
	 * @author Dylan
	 */
	private static class SingleRequestCallback implements Request.Callback {
		private final String id;
		private final RequestInProgress rip;

		/**
		 * Constructor.
		 * 
		 * @param id The ID of the location being requested
		 * @param rip The object to which the request's completed results are
		 *        reported
		 */
		public SingleRequestCallback(String id, RequestInProgress rip) {
			this.id = id;
			this.rip = rip;
		}

		@Override
		public void onCompleted(Response response) {
			// response could be an error, or it could have JSON
			FacebookRequestError error = response.getError();
			GraphObject obj = response.getGraphObject();

			if (error != null) {
				// if the response was an error, report the error to "rip"
				rip.add(id, null, error);
			} else if (obj != null) {
				// if the response was JSON, extract the location details
				// from the JSON and report them to "rip" with no error.
				JSONObject responseJson = obj.getInnerJSONObject();
				LatLng loc = extractLocation(responseJson);
				rip.add(id, loc, null);
			}
		}

		/**
		 * Extract a LatLng object from the given "response" object. Expected
		 * format for the response JSON is
		 * <code>{ location: { latitude: XX.YY, longitude: XX.YY } }</code>.
		 * 
		 * @param response The JSON from a Facebook API response.
		 * @return A LatLng instance containing the latitude and longitude from
		 *         the response, or null.
		 */
		private LatLng extractLocation(JSONObject response) {
			if (response == null)
				return null;
			JSONObject loc = response.optJSONObject("location");
			if (loc == null)
				return null;

			double lat = loc.optDouble("latitude");
			double lon = loc.optDouble("longitude");
			return new LatLng(lat, lon);
		}
	}

}
