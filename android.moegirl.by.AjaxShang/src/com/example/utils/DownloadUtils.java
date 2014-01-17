package com.example.utils;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

public class DownloadUtils {
	public interface Callback {
		public void getBitmap(Bitmap bitmap);

	}

	public static void getImageFromNet(final String path,
			final Callback callback) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if (msg.what == 1) {
					Bitmap bitmap = (Bitmap) msg.obj;
					callback.getBitmap(bitmap);
				}
			}
		};
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpPost = new HttpGet(path);
				Bitmap bitmap = null;
				try {
					HttpResponse response = httpClient.execute(httpPost);
					if (response.getStatusLine().getStatusCode() == 200) {
						byte[] data = EntityUtils.toByteArray(response
								.getEntity());
						bitmap = BitmapFactory.decodeByteArray(data, 0,
								data.length);
						Message message = Message.obtain();
						message.what = 1;
						message.obj = bitmap;
						handler.sendMessage(message);
					}
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					httpClient.getConnectionManager().shutdown();
				}
			}
		}).start();
	}
}
