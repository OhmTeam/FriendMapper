package com.ohmteam.friendmapper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ohmteam.friendmapper.io.ImageLoaderTask;
import com.ohmteam.friendmapper.util.DaemonThreadFactory;
import com.ohmteam.friendmapper.util.ResultCallback;

public class MainActivity extends FragmentActivity {
	private GoogleMap mMap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setUpMapIfNeeded();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	private void setUpMapIfNeeded() {
		if (mMap != null) {
			return;
		}
		mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		if (mMap == null) {
			return;
		}
		// Initialize map options. For example:
		// mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		mMap.setMyLocationEnabled(true);

		// Set Latitude and Longitude for a new marker here
		// I've called it home, because it currently points at my
		// home town. Makes sense to call it something else though...like
		// "mapPos"
		LatLng home = new LatLng(38.905622, -77.033081);

		/*
		 * Create a thread pool executor that can be used to run arbitrary
		 * background tasks. It uses a thread pool with 3 daemon threads. "3" is
		 * an arbitrary choice, but once we start loading a bunch of different
		 * facebook friends' images, we might want to tweak the number for
		 * better efficiency.
		 * 
		 * TODO: figure out if something needs to be done to this thing when the
		 * activity stops/pauses/resumes.
		 */
		ExecutorService backgroundTaskService = Executors.newFixedThreadPool(3, new DaemonThreadFactory());

		// Create the marker. Default marker type is the standard google marker
		// Modified through accessor functions as shown below
		MarkerOptions marker = new MarkerOptions();
		marker.position(home);
		marker.title("Rob's Mo'fuckin' TOWN!");

		/*
		 * Set up a task that will load an image from a URL, apply it to the
		 * marker, then add that marker to the map; the task will be run on a
		 * background thread via the backgroundTaskService.
		 */
		ResultCallback<Bitmap> addMarkerCallback = new MapMarkerBitmapCallback(this, mMap, marker);
		String imageUrl = "http://3.bp.blogspot.com/-uILNxoeY8Sk/TzMmmd5kvdI/AAAAAAAAAMI/AaOWXbN9E6o/s45/lol-face.gif";
		Runnable loadImageTask = new ImageLoaderTask(imageUrl, addMarkerCallback);
		backgroundTaskService.execute(loadImageTask);

		// Set the Camera (view of the map) to our new marker. This makes it
		// easy to see that the marker was added successfully.
		CameraPosition cameraPosition = new CameraPosition.Builder().target(home).zoom(10.0f).build();
		CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
		mMap.moveCamera(cameraUpdate);

	}
}
