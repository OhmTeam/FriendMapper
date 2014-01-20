package com.ohmteam.friendmapper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ohmteam.friendmapper.data.FacebookFriend;
import com.ohmteam.friendmapper.data.ProfilePicManager;
import com.ohmteam.friendmapper.util.ResultCallback;

/**
 * A callback function for Bitmaps that sets the profile picture bitmap in the
 * Map<Facebookfriend, Bitmap> in ProfilePic Manager.
 * 
 * This class is based off of MapMarkerBitmapCallback, but works in conjunction
 * with the ProfilePicManager.
 * 
 * It currently expects access to the main Activity so that it can run UIthreads
 * to place markers. However, this may not be in the next iteration of this
 * class, and may only be responsible for storing the profile picture in
 * ProfilePicManager.
 * 
 * @author Rob
 */
public class ProfilePicCallback implements ResultCallback<Bitmap> {
	private final Activity activity;
	private final GoogleMap map;
	private final MarkerOptions markerOptions;
	private final ProfilePicManager manager;
	private final FacebookFriend friend;

	private Marker marker = null;

	public ProfilePicCallback(Activity activity, GoogleMap map,
			MarkerOptions markerOptions, ProfilePicManager pics,
			FacebookFriend friend) {
		this.activity = activity;
		this.map = map;
		this.markerOptions = markerOptions;
		this.manager = pics;
		this.friend = friend;
	}

	@Override
	public void onFailure(Throwable cause) {
		Log.e("MyMap",
				"Something went wrong getting a bigmap for the map marker",
				cause);
	}

	@Override
	public void onSuccess(Bitmap result) {
		markerOptions.icon(BitmapDescriptorFactory.fromBitmap(result));
		manager.addEntry(friend, result);
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				marker = map.addMarker(markerOptions);
			}
		});
	}

	/**
	 * @return the marker that was added to the GoogleMap when onSuccess was
	 *         called. If that hasn't happened yet, it returns null. If
	 *         onFailure was called instead of onSuccess, this method will
	 *         always return null.
	 */
	public Marker getMarker() {
		return marker;
	}

}
