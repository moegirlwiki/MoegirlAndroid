package org.moegirlpedia;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;
import org.moegirlpedia.util.VersionUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;

public class MainActivity extends Activity implements OnClickListener,
		OnMenuItemClickListener {
	private Handler mHandler = new Handler();
	private DrawerLayout drawer;
	private MyWebView mWebView;
	private PopupMenu pop;
	private TextView menuLogin;
	private TextView tvUsername;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.layout_title_bar);

		drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ImageView menuImg = (ImageView) findViewById(R.id.title_bar_menu_btn);
		menuImg.setOnClickListener(this);
		TextView menuRandom = (TextView) findViewById(R.id.menuRandom);
		menuRandom.setOnClickListener(this);
		TextView menuBookmark = (TextView) findViewById(R.id.menuBookmark);
		menuBookmark.setOnClickListener(this);
		TextView menuHistory = (TextView) findViewById(R.id.menuHistory);
		menuHistory.setOnClickListener(this);
		TextView menuQuit = (TextView) findViewById(R.id.menuQuit);
		menuQuit.setOnClickListener(this);
		menuLogin = (TextView) findViewById(R.id.menuLogin);
		menuLogin.setOnClickListener(this);
		TextView menuSettings = (TextView) findViewById(R.id.menuSettings);
		menuSettings.setOnClickListener(this);
		Button btnSearch = (Button) findViewById(R.id.title_bar_search_btn);
		btnSearch.setOnClickListener(this);
		ImageButton btnIndex = (ImageButton) findViewById(R.id.title_bar_index_btn);
		btnIndex.setOnClickListener(this);
		ImageButton btnMore = (ImageButton) findViewById(R.id.title_bar_more_btn);
		btnMore.setOnClickListener(this);
		tvUsername = (TextView) findViewById(R.id.tvUsername);

		pop = new PopupMenu(this, btnMore);
		pop.getMenuInflater().inflate(R.menu.main, pop.getMenu());
		pop.setOnMenuItemClickListener(this);

		ProgressBar mprogressBar = (ProgressBar) this
				.findViewById(R.id.mProgress);
		TextView tvTitle = (TextView) this.findViewById(R.id.layoutindexTitle);
		ListView list = (ListView) this.findViewById(R.id.index_list);
		
		mWebView = (MyWebView) this.findViewById(R.id.web);
		mWebView.setProgressBar(mprogressBar);
		mWebView.setTextViewTitle(tvTitle);
		mWebView.setIndexListView(list);
		

		android.webkit.CookieSyncManager.createInstance(this);// 要保持cookie，需要先运行这句
		android.webkit.CookieManager.getInstance().setAcceptCookie(true);

		// magic starts here
		WebkitCookieManagerProxy coreCookieManager = new WebkitCookieManagerProxy(
				null, java.net.CookiePolicy.ACCEPT_ALL);
		java.net.CookieHandler.setDefault(coreCookieManager);

		if (savedInstanceState == null) {
			mWebView.loadUrl(getString(R.string.baseurl) + "Mainpage");
		} else {
			mWebView.restoreState(savedInstanceState);
		}
		
		checkUpdate();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((!drawer.isDrawerOpen(GravityCompat.START)) && (!drawer.isDrawerOpen(GravityCompat.END))) {
				if (mWebView.canGoBack()) {
					mWebView.goBack();
					return true;
				}
			} else {
				closeDrawerLeft();
				closeDrawerRight();
				return true;
			}
		}
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (!drawer.isDrawerOpen(GravityCompat.START))
			{
				closeDrawerRight();
				drawer.openDrawer(GravityCompat.START);
			}
			else
				closeDrawerLeft();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mWebView.saveState(outState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case RESULT_OK:
			Bundle d = data.getExtras();
			String url = d.getString("url");
			mWebView.loadUrl(url);
			closeDrawerLeft();
			break;
		case 20:
			detectLogin();
			closeDrawerLeft();
			break;
		default:
			break;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.share:
			mWebView.share();
			break;
		case R.id.refresh:
			mWebView.refresh();
			break;
		case R.id.openinbrowser:
			mWebView.openInBrowser();
			break;
		case R.id.edit:
			mWebView.gotoEdit();
			break;
		case R.id.addbookmark:
			mWebView.addBookmark();
			break;
		case R.id.about:
			LayoutInflater inflater = getLayoutInflater();
			View aboutLayout = inflater.inflate(R.layout.about,
					(ViewGroup) findViewById(R.layout.about));
			TextView a1 = (TextView) aboutLayout.findViewById(R.id.textView1);
			a1.setMovementMethod(LinkMovementMethod.getInstance());
			// a1.setText(Html.fromHtml(getResources().getString(
			// R.string.about_text)));

			new AlertDialog.Builder(this).setTitle("关于").setView(aboutLayout)
					.setPositiveButton("确定", null).show();
			break;

		default:
			break;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_bar_menu_btn:
			if (drawer.isDrawerOpen(GravityCompat.START)) {
				closeDrawerLeft();
			} else {
				closeDrawerRight();
				drawer.openDrawer(GravityCompat.START);
			}
			break;
		case R.id.title_bar_index_btn:
			if (drawer.isDrawerOpen(GravityCompat.END)) {
				closeDrawerRight();
			} else {
				closeDrawerLeft();
				drawer.openDrawer(GravityCompat.END);
			}
				break;
		case R.id.menuRandom:
			closeDrawerLeft();
			mWebView.loadUrl(getString(R.string.baseurl)
					+ "Special:%E9%9A%8F%E6%9C%BA%E9%A1%B5%E9%9D%A2?action=render");
			break;
		case R.id.menuBookmark:
			Intent bintent = new Intent(MainActivity.this, Bookmark.class);
			startActivityForResult(bintent, 0);
			break;
		case R.id.menuHistory:
			Intent hintent = new Intent(MainActivity.this, History.class);
			startActivityForResult(hintent, 0);
			break;
		case R.id.menuSettings:
			startActivity(new Intent(MainActivity.this, SettingActivity.class));
			break;
		case R.id.menuQuit:
			mWebView.clear();
			finish();
			break;
		case R.id.menuLogin:
			if (menuLogin.getText().equals(getString(R.string.login)))
			{
				Intent lintent = new Intent(MainActivity.this, Login.class);
				startActivityForResult(lintent, 0);
			}
			else
			{
				menuLogin.setText(R.string.login);
				closeDrawerLeft();
				final String url = getString(R.string.baseurl) + "api.php?action=logout";
				final MainActivity that = this;
				new Thread(new Runnable() {
						@Override
						public void run()
						{
							fetchData(url);
							mHandler.post(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(that,"退出成功",Toast.LENGTH_LONG).show();
										detectLogin();
									}
								});
						}
					}).start();
			}
			break;
		case R.id.title_bar_search_btn:
			Intent sintent = new Intent(MainActivity.this, Search.class);
			startActivityForResult(sintent, 0);
			break;
		case R.id.title_bar_more_btn:
			pop.show();
			break;
		}

	}

	private void closeDrawerLeft() {
		drawer.closeDrawer(GravityCompat.START);
	}
	
	public void closeDrawerRight() {
		drawer.closeDrawer(GravityCompat.END);
	}
	
	private void detectLogin()
	{
		tvUsername.setText("加载中...");
		
		String key = "moegirlSSOUserName=";
		String cookie = android.webkit.CookieManager.getInstance().getCookie(getString(R.string.baseurl));
		if (cookie != null)
		{
			int pos = cookie.indexOf(key);
			if (pos >= 0)
				cookie = cookie.substring(pos + key.length(),cookie.length());
			pos = cookie.indexOf(";");
			if (pos >= 0)
				cookie = cookie.substring(0,pos);
			
			try {
				cookie = URLDecoder.decode(cookie, "utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
			cookie = "";
			
		final String Username = cookie;
		final String url = getString(R.string.baseurl) + "api.php?format=json&action=query&meta=tokens";
		new Thread(new Runnable() {
				@Override
				public void run()
				{
					String strResult = fetchData(url);
					
					if (strResult.indexOf("\"+\\\\\"") < 0)
						{
							//已登录
							mHandler.post(new Runnable() {
									@Override
									public void run() {
										menuLogin.setText(R.string.logout);
										tvUsername.setText(Username);
									}
								});
						}
						else
						{
							mHandler.post(new Runnable() {
									@Override
									public void run() {
										menuLogin.setText(R.string.login);
										tvUsername.setText("");
									}
								});
						}

				}
			}).start();
	}
	
	private void checkUpdate()
	{
		final MainActivity that = this;
		final String versionurl = getString(R.string.versionurl);
		final String downloadurl = getString(R.string.downloadurl);
		new Thread(new Runnable() {
				@Override
				public void run()
				{
					String strResult = fetchData(versionurl).trim();
					if (strResult.isEmpty()) return;
					Integer result = Integer.parseInt(strResult);

					if (result > VersionUtil.getVersionCode(that))
					{
						//有新版本
						mHandler.post(new Runnable() {
								@Override
								public void run() {
									AlertDialog alertDialog = new AlertDialog.Builder(that)
										.create();
									alertDialog.setTitle("有新版本");
									alertDialog.setCancelable(true);
									alertDialog.setMessage("要更新吗？");
									alertDialog.setButton("更新",
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
																int which) {
												Intent intent = new Intent(Intent.ACTION_VIEW);
												Uri content_url = Uri.parse(downloadurl);
												intent.setData(content_url);
												that.startActivity(intent);
											}
										});
									alertDialog.setButton2("不更新",
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(DialogInterface dialog,
																int which) {
												// TODO Auto-generated method stub
											}
										});
									alertDialog.show();
								}
							});
					}
					
				}
			}).start();
	}
	
	private String fetchData(String url)
	{
		String myString = "";
		try
		{
			// 定义获取文件内容的URL
			URL myURL = new URL(url);
			// 打开URL链接
			URLConnection ucon = myURL.openConnection();
			ucon.setConnectTimeout(10000);
			ucon.setReadTimeout(20000);
			ucon.addRequestProperty("User-Agent",
									getString(R.string.useragent));
			ucon.setUseCaches(false);

			// 使用InputStream，从URLConnection读取数据
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(
				is);
			// 用ByteArrayBuffer缓存
			ByteArrayBuffer baf = new ByteArrayBuffer(50);
			int current = 0;
			while ((current = bis.read()) != -1)
			{
				baf.append((byte) current);
			}
			// 将缓存的内容转化为String,用UTF-8编码
			myString = EncodingUtils.getString(
				baf.toByteArray(), "UTF-8");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return myString;
	}

	public void onResume() {
		super.onResume();

		/**
		 * 页面起始（每个Activity中都需要添加，如果有继承的父Activity中已经添加了该调用，那么子Activity中务必不能添加）
		 * 不能与StatService.onPageStart一级onPageEnd函数交叉使用
		 */
		StatService.onResume(this);
		detectLogin();
	}

	public void onPause() {
		super.onPause();

		/**
		 * 页面结束（每个Activity中都需要添加，如果有继承的父Activity中已经添加了该调用，那么子Activity中务必不能添加）
		 * 不能与StatService.onPageStart一级onPageEnd函数交叉使用
		 */
		StatService.onPause(this);
	}

}
