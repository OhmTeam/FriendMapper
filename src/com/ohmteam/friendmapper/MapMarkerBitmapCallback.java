package com.ohmteam.friendmapper;

import java.net.MalformedURLException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ohmteam.friendmapper.data.MapMarker;
import com.ohmteam.friendmapper.util.ResultCallback;

/**
 * A callback function for Bitmaps that sets the Bitmap as the icon on a marker and then adds that
 * marker to a GoogleMap. This class is intended to be used in conjunction with ImageLoaderTask for
 * loading images from the internet to be put in markers on a GoogleMap.
 * 
 * @author Dylan
 */
public class MapMarkerBitmapCallback implements ResultCallback<Bitmap> {
	private final Activity activity;
	private final GoogleMap map;
	private MarkerOptions markerOptions;

	private Marker marker = null;

	private boolean markerLoaded = false;

	public MapMarkerBitmapCallback(Activity activity, GoogleMap map, MarkerOptions markerOptions) {
		this.activity = activity;
		this.map = map;
		this.markerOptions = markerOptions;
		markerLoaded = false;
	}

	public MapMarkerBitmapCallback(Activity activity, GoogleMap map, MapMarker mapMarker) {
		this.activity = activity;
		this.map = map;
		try {
			this.markerOptions = mapMarker.getMarkerOptions();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			this.markerOptions = new MarkerOptions();
			e.printStackTrace();
		}
		markerLoaded = true;
	}

	@Override
	public void onFailure(Throwable cause) {
		Log.e("MyMap", "Something went wrong getting a bigmap for the map marker", cause);
	}

	@Override
	public void onSuccess(Bitmap result) {
		markerOptions.icon(BitmapDescriptorFactory.fromBitmap(result));
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				marker = map.addMarker(markerOptions);

			}
		});
	}

	/**
	 * @return the marker that was added to the GoogleMap when onSuccess was called. If that hasn't
	 *         happened yet, it returns null. If onFailure was called instead of onSuccess, this
	 *         method will always return null.
	 */
	public Marker getMarker() {
		return marker;
	}
}
