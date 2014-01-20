package com.ohmteam.friendmapper.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import android.graphics.Bitmap;

import com.ohmteam.friendmapper.io.ImageLoaderTask;
import com.ohmteam.friendmapper.util.ResultCallback;

/**
 * This class loads and keeps tracks of profile pictures for facebook friends that have been loaded
 * into the application.
 * 
 * Need to figure out where we are instantiating this class, and who has acces to it.
 * 
 * @author Rob
 * 
 */
public class ProfilePicManager {

	private final Map<FacebookFriend, Bitmap> profilePics = new HashMap<FacebookFriend, Bitmap>();

	/**
	 * Checks to see if Facebookfriend is already in the map. If not, initiate HTTP request to get
	 * it, then store Friend<->Bitmap pair in the map.
	 * 
	 * @param friend
	 * @param callback
	 * @return Bitmap
	 */
	public void getProfilePic(final FacebookFriend friend, final ResultCallback<Bitmap> callback,
			ExecutorService threadpool) {

		Bitmap pic = profilePics.get(friend);
		if (pic != null) {
			callback.onSuccess(pic);
		} else {
			// HTTP Request stuff

			String profileURL = friend.getProfilePicURL();
			ImageLoaderTask loadImage = new ImageLoaderTask(profileURL, new ResultCallback<Bitmap>() {
				@Override
				public void onSuccess(Bitmap result) {
					profilePics.put(friend, result);
					callback.onSuccess(result);
				}

				@Override
				public void onFailure(Throwable cause) {
					callback.onFailure(cause);

				}
			});
			threadpool.execute(loadImage);
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
