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
			// 为了减少代码使用匿名Handler创建一个延时的调用
			public void run() {
				Intent i = new Intent(SplashActivity.this, MainActivity.class);
				// 通过Intent打开最终真正的主界面Main这个Activity
				SplashActivity.this.startActivity(i); // 启动Main界面
				SplashActivity.this.finish(); // 关闭自己这个开场屏
			}
		}, 800); // 5秒，够用了吧
	}

	class SplashHandler implements Runnable {
		public void run() {
			startActivity(new Intent(getApplication(), MainActivity.class));
			SplashActivity.this.finish();
		}
	}

}
