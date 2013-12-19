package com.ohmteam.friendmapper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

public class MainFragment extends Fragment {

	private final static String TAG = "MainFragment";
	private final List<Runnable> loginRunnables = new LinkedList<Runnable>();

	private UiLifecycleHelper uiHelper;

	// Button loadFriendsButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main, container, false);

		LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
		authButton.setFragment(this);
		authButton.setReadPermissions(Arrays.asList("friends_location"));

		// loadFriendsButton = (Button) view.findViewById(R.id.friendsButton);
		// loadFriendsButton.setOnClickListener(friendsClickListener);
		// updateFriendsButtonVisibility(Session.getActiveSession());

		return view;
	}

	// private void updateFriendsButtonVisibility(Session session) {
	// int visibility = View.GONE;
	// if (session != null && session.isOpened())
	// visibility = View.VISIBLE;
	// if (loadFriendsButton != null) {
	// loadFriendsButton.setVisibility(visibility);
	// }
	// }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate...");
		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume...");

		// For secnarios where the main activity is launched and user session is
		// not null, the session state change notification may not be triggered.
		// Trigger it if it's open/closed.
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			onSessionStateChange(session, session.getState(), null);
		}

		uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult...");
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause...");
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy...");
		uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(TAG, "onSaveInstanceState...");
		uiHelper.onSaveInstanceState(outState);
	}

	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (state.isOpened()) {
			Log.i(TAG, "Logged in...");
			tryRunLoginTasks();
		} else if (state.isClosed()) {
			Log.i(TAG, "Logged out...");
		}

		// updateFriendsButtonVisibility(session);
	}

	private final Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	// private boolean didLoadFriends = false;
	//
	// private void onLoginLoadFriends() {
	// if (didLoadFriends) {
	// Log.i(TAG, "already loaded friends from earlier");
	// } else {
	// Log.i(TAG, "loading friends...");
	// FriendsLoader loader = new FriendsLoader();
	//
	// loader.loadFriends(Session.getActiveSession(), new
	// FriendsLoader.Callback() {
	// @Override
	// public void onComplete(Map<GraphUser, FriendLocation> friendsLocations) {
	// for (Entry<GraphUser, FriendLocation> entry :
	// friendsLocations.entrySet()) {
	// Log.i(TAG, "friend: " + entry.getKey().getName() + " at " +
	// entry.getValue());
	// }
	// didLoadFriends = true;
	// }
	// });
	// }
	// }

	public void runOnNextLogin(Runnable task) {
		loginRunnables.add(task);
		tryRunLoginTasks();
	}

	private void tryRunLoginTasks() {
		Session session = Session.getActiveSession();
		if (session == null)
			return;

		if (session.isOpened()) {
			for (Runnable task : loginRunnables) {
				task.run();
			}
			loginRunnables.clear();
		}
	}
}
