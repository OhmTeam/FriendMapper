package com.ohmteam.friendmapper;

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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ohmteam.friendmapper.io.ImageLoaderTask;
import com.ohmteam.friendmapper.util.ContentOverseer;
import com.ohmteam.friendmapper.util.DaemonThreadFactory;
import com.ohmteam.friendmapper.util.ResultCallback;

public class MainActivity extends FragmentActivity {
	private GoogleMap mMap;

	private MainFragment mainFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.main);
		// setUpMapIfNeeded();
		// super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			// Add the fragment on initial activity setup
			mainFragment = new MainFragment();
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, mainFragment).commit();
		} else {
			// Or set the fragment from restored state info
			mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
		}
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

		mainFragment.runOnNextLogin(new Runnable() {
			@Override
			public void run() {
				Log.i("MainFragment", "Logged in for the first time probably!");
				FriendsLoader loader = new FriendsLoader();

				loader.loadFriends(Session.getActiveSession(), new FriendsLoader.Callback() {
					@Override
					public void onComplete(final Map<GraphUser, FriendLocation> friendsLocations) {
						Log.d("MainFragment", "loaded " + friendsLocations.size()
								+ " friends... now to put them on the map");
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								
								ContentOverseer usedLocationsMap = new ContentOverseer();
								for (Entry<GraphUser, FriendLocation> entry : friendsLocations.entrySet()) {
									// Log.d("MainFragment",
									// "friend: " + entry.getKey().getName() +
									// " at " + entry.getValue());
									String friendName = entry.getKey().getName();
									FriendLocation loc = entry.getValue();
									
									
									LatLng locLL = new LatLng(loc.getLatitude(), loc.getLongitude());
									Log.d("Foo", friendName + " at " + locLL);
									
									//Add Location to location used maps. If if is already a ued location, the friends name will be
									//added to the associated set. 
									usedLocationsMap.addUsedLocation(locLL, friendName);
									
								}
								for (LatLng markerCoord : usedLocationsMap.getUsedLocations())
								{
									
									MarkerOptions mo = new MarkerOptions();
									mo.position(markerCoord);
									mo.title(usedLocationsMap.getFriendsFromLoc(markerCoord));
									mo.icon(BitmapDescriptorFactory.defaultMarker());

									mMap.addMarker(mo);
								}

							}
						});
					}
				});
			}
		});

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
		//CameraPosition cameraPosition = new CameraPosition.Builder().target(home).zoom(10.0f).build();
		//CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
		//mMap.moveCamera(cameraUpdate);

	}
}
