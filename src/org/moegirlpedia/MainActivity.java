package org.moegirlpedia;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.Window;
import android.view.Menu;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.webkit.WebView;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.RenderPriority;
import android.graphics.Bitmap;
import android.text.method.LinkMovementMethod;
import android.content.Intent;
import android.content.res.Configuration;
import org.moegirlpedia.database.SQLiteHelper;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.view.GravityCompat;

public class MainActivity extends Activity implements OnClickListener, OnMenuItemClickListener
{
	private DrawerLayout drawerLeft;
	private MyWebView mWebView;
	private PopupMenu pop;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.layout_title_bar);

		drawerLeft = (DrawerLayout) findViewById(R.id.drawer_layout);
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
		TextView menuLogin = (TextView) findViewById(R.id.menuLogin);
		menuLogin.setOnClickListener(this);
		TextView menuSettings = (TextView) findViewById(R.id.menuSettings);
		menuSettings.setOnClickListener(this);
		Button btnSearch = (Button) findViewById(R.id.title_bar_search_btn);
		btnSearch.setOnClickListener(this);
		ImageButton btnMore = (ImageButton) findViewById(R.id.title_bar_more_btn);
		btnMore.setOnClickListener(this);

		pop = new PopupMenu(this, btnMore);  
		pop.getMenuInflater().inflate(R.menu.main, pop.getMenu());
		pop.setOnMenuItemClickListener(this);

		ProgressBar mprogressBar = (ProgressBar) this.findViewById(R.id.mProgress);

		mWebView = (MyWebView) this.findViewById(R.id.web);
		mWebView.setProgressBar(mprogressBar);

		android.webkit.CookieSyncManager.createInstance(this);// unrelated, just make sure cookies are generally allowed
		android.webkit.CookieManager.getInstance().setAcceptCookie(true);
		
		// magic starts here
		WebkitCookieManagerProxy coreCookieManager = new WebkitCookieManagerProxy(null, java.net.CookiePolicy.ACCEPT_ALL);
		java.net.CookieHandler.setDefault(coreCookieManager);

		if (savedInstanceState == null)
		{
			mWebView.loadUrl(getString(R.string.baseurl)+"Mainpage?action=render");
		}
		else
		{
			mWebView.restoreState(savedInstanceState);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if (!drawerLeft.isDrawerOpen(GravityCompat.START))
			{
				if (mWebView.canGoBack())
				{
					mWebView.goBack();
					return true;
				}
			}
			else
			{
				drawerLeft.closeDrawer(GravityCompat.START);
				return true;
			}
		}
		if (keyCode == KeyEvent.KEYCODE_MENU)
		{
			if (!drawerLeft.isDrawerOpen(GravityCompat.START))
				drawerLeft.openDrawer(GravityCompat.START);
			else
				drawerLeft.closeDrawer(GravityCompat.START);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		mWebView.saveState(outState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{  
		switch (resultCode)
		{
			case RESULT_OK:  
				Bundle d=data.getExtras();
				String url=d.getString("url");
				mWebView.loadUrl(url);
				if (drawerLeft.isDrawerOpen(GravityCompat.START))
				{
					drawerLeft.closeDrawer(GravityCompat.START);
				}
				break;  
			default:  
				break;  
		}  
	}  

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);

	}

	@Override
    public boolean onMenuItemClick(MenuItem item)
	{
        switch (item.getItemId())
		{
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
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.title_bar_menu_btn:
				if (drawerLeft.isDrawerOpen(GravityCompat.START))
				{
					drawerLeft.closeDrawer(GravityCompat.START);
				}
				else
				{
					drawerLeft.openDrawer(GravityCompat.START);
				}
				break;
			case R.id.menuRandom:
				closeDrawerLeft();
				mWebView.loadUrl(getString(R.string.baseurl)+"Special:%E9%9A%8F%E6%9C%BA%E9%A1%B5%E9%9D%A2?action=render");
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
				closeDrawerLeft();
				mWebView.loadUrl("http://zh.moegirl.org/Special:%E7%94%A8%E6%88%B7%E7%99%BB%E5%BD%95?returnto=Mainpage");
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

	private void closeDrawerLeft()
	{
		drawerLeft.closeDrawer(GravityCompat.START);
	}

}
