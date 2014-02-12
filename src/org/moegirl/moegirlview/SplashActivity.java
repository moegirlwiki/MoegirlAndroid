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
			// ΪÁ˼õÉٴúÂëʹÓÃÄäÃûHandler´´½¨һ¸öÑÓʱµĵ÷ÓÃ
			public void run() {
				Intent i = new Intent(SplashActivity.this, MainActivity.class);
				// ͨ¹ýIntent´ò¿ª×îÖÕÕæÕýµÄÖ÷½çÃæMainÕâ¸öActivity
				SplashActivity.this.startActivity(i); // Æô¶¯Main½çÃæ
				SplashActivity.this.finish(); // ¹رÕ×ԼºÕâ¸ö¿ª³¡ÆÁ
			}
		}, 800); // 5Ã룬¹»ÓÃÁ˰É
	}

	class SplashHandler implements Runnable {
		public void run() {
			startActivity(new Intent(getApplication(), MainActivity.class));
			SplashActivity.this.finish();
		}
	}

}
