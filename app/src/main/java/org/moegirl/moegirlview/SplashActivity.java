package org.moegirl.moegirlview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_splash);

        ImageView imgView = (ImageView) findViewById(R.id.imageView1);

        Animation animation = new ScaleAnimation(0.9f, 1.1f, 0.9f, 1.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(1500);
        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(i);
                SplashActivity.this.finish();
                overridePendingTransition(R.xml.fade_in, R.xml.fade_out);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        imgView.startAnimation(animation);

//		new Handler().postDelayed(new Runnable() {
//			public void run() {
//				Intent i = new Intent(SplashActivity.this, MainActivity.class);
//				SplashActivity.this.startActivity(i);
//				SplashActivity.this.finish();
//			}
//		}, 800);
    }
}
