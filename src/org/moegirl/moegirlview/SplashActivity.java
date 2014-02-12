package org.moegirl.moegirlview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_splash);

		new Handler().postDelayed(new Runnable() {
			public void run() {
				Intent i = new Intent(SplashActivity.this, MainActivity.class);
				SplashActivity.this.startActivity(i);
				SplashActivity.this.finish();
			}
		}, 800);
	}

	class SplashHandler implements Runnable {
		public void run() {
			startActivity(new Intent(getApplication(), MainActivity.class));
			SplashActivity.this.finish();
		}
	}

}
