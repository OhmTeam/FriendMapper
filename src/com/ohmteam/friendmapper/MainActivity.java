package com.ohmteam.friendmapper;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

		// Create the marker. Default marker type is the standard google marker
		// Modified through accessor functions as shown below
		MarkerOptions marker = new MarkerOptions();
		marker.position(home);
		marker.title("Rob's Mo'fuckin' TOWN!");

		// Add the marker to our MapFragment
		mMap.addMarker(marker);

		// Set the Camera (view of the map) to our new marker. This makes it
		// easy
		// to see that the marker was added successfully
		CameraPosition cameraPosition = new CameraPosition.Builder().target(home).zoom(10.0f).build();
		CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
		mMap.moveCamera(cameraUpdate);

	}
}
