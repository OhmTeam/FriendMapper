package com.ohmteam.friendmapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ohmteam.friendmapper.data.FacebookFriend;
import com.ohmteam.friendmapper.data.MarkerManager;
import com.ohmteam.friendmapper.io.ImageLoaderTask;
import com.ohmteam.friendmapper.util.DaemonThreadFactory;
import com.ohmteam.friendmapper.util.ResultCallback;

public class MainActivity extends FragmentActivity {
	private GoogleMap map;
	private MarkerManager markerManager = new MarkerManager(this, 100);
	private boolean needsToLoadFriends = true;
	private MainFragment mainFragment;

	private final String TAG = "MainActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(TAG, "Creating MainActivity " + this.hashCode());

		if (savedInstanceState == null) {
			Log.i(TAG, "onCreate with no saved state");
			// Add the fragment on initial activity setup
			mainFragment = new MainFragment();
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, mainFragment).commit();

		} else {

			Log.i(TAG, "onCreate with a saved state");
			// Or set the fragment from restored state info
			mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);

			markerManager.loadFrom(savedInstanceState);
			needsToLoadFriends = savedInstanceState.getBoolean("needsToLoadFriends");
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		markerManager.saveTo(outState);
		outState.putBoolean("needsToLoadFriends", needsToLoadFriends);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
		setUpMapIfNeeded();
		loadFriendsIfNeeded();
	}

	/**
	 * Updates the state of the friend markers on the map. If friend data has
	 * already been loaded, it simply refreshes the markers. If no data has been
	 * loaded yet, it loads the data and then updates the markers.
	 */
	private void loadFriendsIfNeeded() {
		if (needsToLoadFriends) {
			Log.i(TAG, "loadFriendsIfNeeded: doing a full load of friend data");
			loadFriendsData();
		}
	}

	/**
	 * Triggers a FriendsLoader.loadFriends request at the next available
	 * Facebook login+session. Once friends and locations are resolved, the data
	 * will be put into the contentOverseer, the `needsToLoadFriends` flag will
	 * be reset, and `updateFriendMarkers` will be called.
	 */
	private void loadFriendsData() {
		mainFragment.runOnNextLogin(new Runnable() {
			@Override
			public void run() {
				FriendsLoader loader = new FriendsLoader();

				loader.loadFriends(Session.getActiveSession(), new FriendsLoader.Callback() {
					@Override
					public void onComplete(Map<GraphUser, FriendLocation> friendsLocations) {
						// add the location data into the contentOverseer
						List<FacebookFriend> friends = new ArrayList<FacebookFriend>(friendsLocations.size());

						for (Entry<GraphUser, FriendLocation> entry : friendsLocations.entrySet()) {
							String id = entry.getKey().getId();
							String name = entry.getKey().getName();
							FriendLocation loc = entry.getValue();
							LatLng locLL = new LatLng(loc.getLatitude(), loc.getLongitude());

							friends.add(new FacebookFriend(id, name, locLL));
						}
						needsToLoadFriends = false;
						markerManager.setFriends(friends);
					}
				});
			}
		});
	}

	private void setUpMapIfNeeded() {
		if (map != null) {
			return;
		}
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		if (map == null) {
			return;
		}
		// Initialize map options. For example:
		// mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		map.setMyLocationEnabled(true);

		markerManager.setMap(map);

	}

	public void setupExampleMarker() {
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
		ResultCallback<Bitmap> addMarkerCallback = new MapMarkerBitmapCallback(this, map, marker);
		String imageUrl = "http://3.bp.blogspot.com/-uILNxoeY8Sk/TzMmmd5kvdI/AAAAAAAAAMI/AaOWXbN9E6o/s45/lol-face.gif";
		Runnable loadImageTask = new ImageLoaderTask(imageUrl, addMarkerCallback);
		backgroundTaskService.execute(loadImageTask);

		// Set the Camera (view of the map) to our new marker. This makes it
		// easy to see that the marker was added successfully.
		// CameraPosition cameraPosition = new
		// CameraPosition.Builder().target(home).zoom(10.0f).build();
		// CameraUpdate cameraUpdate =
		// CameraUpdateFactory.newCameraPosition(cameraPosition);
		// mMap.moveCamera(cameraUpdate);
	}
}
