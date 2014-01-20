package com.ohmteam.friendmapper.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;

import com.ohmteam.friendmapper.ProfilePicCallback;
import com.ohmteam.friendmapper.io.ImageLoaderTask;
import com.ohmteam.friendmapper.util.DaemonThreadFactory;

/**
 * This class loads and keeps tracks of profile pictures for facebook friends
 * that have been loaded into the application.
 * 
 * Need to figure out where we are instantiating this class, and who has acces
 * to it.
 * 
 * @author Rob
 * 
 */
public class ProfilePicManager {

	private Map<FacebookFriend, Bitmap> profilePics = new HashMap<FacebookFriend, Bitmap>();

	/**
	 * Checks to see if Facebookfriend is already in the map. If not, initiate
	 * HTTP request to get it, then store Friend<->Bitmap pair in the map.
	 * 
	 * @param friend
	 * @param callback
	 * @return Bitmap
	 */
	public Bitmap getProfilePic(FacebookFriend friend,
			ProfilePicCallback callback) {

		Bitmap pic = profilePics.get(friend);
		if (pic != null) {
			return pic;
		} else {
			ExecutorService backgroundTaskService = Executors
					.newFixedThreadPool(3, new DaemonThreadFactory());
			// HTTP Request stuff
			String profileURL = friend.getProfilePicURL();
			ImageLoaderTask loadImage = new ImageLoaderTask(profileURL,
					callback);
			backgroundTaskService.execute(loadImage);
			return profilePics.get(friend);
		}
	}

	/**
	 * Adds a Facebookfriend<->Bitmap pair to the profilePics map
	 * 
	 * @param friend
	 * @param result
	 */
	public void addEntry(FacebookFriend friend, Bitmap result) {
		profilePics.put(friend, result);
	}
	/**
	 * Add caching to filesystem in phone
	 */

}
