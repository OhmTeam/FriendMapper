package com.ohmteam.friendmapper.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ohmteam.friendmapper.util.ResultCallback;

/**
 * A Runnable that loads an image from the internet, then sends it to a result
 * callback.
 * 
 * @author Dylan
 */
public class ImageLoaderTask implements Runnable {

	private final String link;
	private final ResultCallback<Bitmap> callback;

	/**
	 * Constructor.
	 * 
	 * @param link
	 *            The String form of the image's URL
	 * @param callback
	 *            A callback function that will be called when the image loading
	 *            succeeds or fails.
	 */
	public ImageLoaderTask(String link, ResultCallback<Bitmap> callback) {
		this.link = link;
		this.callback = callback;
	}

	@Override
	public void run() {
		try {
			URL url = new URL(link);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoInput(true);
			con.connect();

			InputStream in = con.getInputStream();
			Bitmap bm = BitmapFactory.decodeStream(in);

			callback.onSuccess(bm);
		} catch (IOException e) {
			callback.onFailure(e);
		}
	}

}
