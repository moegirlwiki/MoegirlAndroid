package org.moegirl.moegirlview;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.moegirl.moegirlview.ShakeDetector.OnShakeListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {
	private WebView webview1;
	private ShakeDetector shakeDetector;

	private String webview_html;
	private String title;
	private boolean isFromCache = false;
	private long revid;

	private boolean forceCache = false;
	private Stack<String> pageHistory = new Stack<String>();

	private final String homePage = "Mainpage";

	private ExecutorService executorService = Executors.newFixedThreadPool(3);

	// private final String homePage = "归宅部活动记录";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		// .detectAll().build());

		setTitle("萌娘百科");
		setContentView(R.layout.activity_main);

		getOverflowMenu();

		webview1 = (WebView) findViewById(R.id.webview);
		webview1.getSettings().setJavaScriptEnabled(true);
		webview1.getSettings().setDefaultTextEncodingName("utf-8");
		webview1.getSettings().setBuiltInZoomControls(true);
		webview1.getSettings().setLoadWithOverviewMode(true);
		webview1.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webview1.setWebViewClient(new HelloWebViewClient());

		if (detect() < 2) {
			forceCache = true;
			webview1.getSettings().setCacheMode(
					WebSettings.LOAD_CACHE_ELSE_NETWORK);
		}

		Intent it = this.getIntent();
		String tSchema = it.getScheme();
		Uri myURI = it.getData();
		if (myURI != null) {
			if (savedInstanceState == null) {
				webview1.loadData("hello world", "text/html", "utf-8");
			} else {
				String savedTitle = savedInstanceState.getString("title");
				if (savedTitle.equals(myURI.getLastPathSegment())) {
					loadSaved(savedInstanceState);
				} else {
					webview1.loadData("hello world", "text/html", "utf-8");
				}
			}
		} else if (Intent.ACTION_SEARCH.equals(it.getAction())) {
			String searchString = it.getStringExtra(SearchManager.QUERY).trim();
			// Log.i("MoeGirl", searchString);
			loadtitle(searchString);
		} else if (savedInstanceState == null) {
			loadtitle(homePage);
		} else {
			loadSaved(savedInstanceState);
		}

		shakeDetector = new ShakeDetector(this);
		shakeDetector.registerOnShakeListener(new ShakeListener());
		System.out.println("start_shake_to_random");
		shakeDetector.start();

	}

	private void loadSaved(Bundle savedInstanceState) {
		webview_html = savedInstanceState.getString("html");
		title = savedInstanceState.getString("title");
		revid = savedInstanceState.getLong("revid");

		if (title.toLowerCase(Locale.getDefault()).equals("mainpage")) {
			setTitle("萌娘百科");
		} else {
			setTitle(title);
		}
		webview1.loadDataWithBaseURL("http://m.moegirl.org/", webview_html,
				"text/html", "utf-8", null);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("html", webview_html);
		outState.putString("title", title);
		outState.putLong("revid", revid);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_activity_actions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_search:
			onSearchRequested();
			return true;
		case R.id.action_about:
			// openSettings();
			new AlertDialog.Builder(this)
					.setTitle("关于")
					.setMessage(
							"萌娘百科Android客户端测试版\n\n"
									+ "By：死宅小h\n"
									+ "Dedicated to cwl and this beautiful cruel world\n\n"
									+ "wifi: " + !forceCache)
					.setPositiveButton("确定", null).show();
			return true;
		case R.id.action_refresh:
			GetPageTask task = new GetPageTask(this);
			task.execute(new String[] { title, "1" });
			return true;
		case R.id.action_edit:
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				Uri content_url = Uri
						.parse("http://m.moegirl.org/index.php?action=edit&title="
								+ URLEncoder.encode(title, "utf-8"));
				intent.setData(content_url);
				startActivity(intent);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		case R.id.action_share:
			try {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_SUBJECT, "萌娘百科 - " + title);
				intent.putExtra(
						Intent.EXTRA_TEXT,
						title + " http://zh.moegirl.org/"
								+ URLEncoder.encode(title, "utf-8") + " #萌娘百科#");
				startActivity(Intent
						.createChooser(intent, "分享 - " + getTitle()));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		case R.id.action_viewinbrowser:
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				Uri content_url = Uri.parse("http://m.moegirl.org/"
						+ URLEncoder.encode(title, "utf-8"));
				intent.setData(content_url);
				startActivity(intent);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void getOverflowMenu() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			// Log.i("MoeGirl", query);
			// loadtitle(query);
			startSearch(query, false, null, false);
		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			String query = intent.getDataString();
			if (!query.equals("")) {
				loadtitle(query);
			}
		}
	}

	private void loadtitle(String title) {
		loadtitle(title, true);
	}

	private GetPageTask task;
	private CheckUpdateTask task2;

	private void loadtitle(String title, boolean isPush) {
		if (title.equals(this.title)) {
			return;
		}
		Log.i("MoeGirl", "Try load title: " + title);
		if (isPush) {
			if (pageHistory.isEmpty()) {
				pageHistory.push(this.title);
			} else if (pageHistory.peek() != this.title) {
				pageHistory.push(this.title);
			}
		}

		this.title = title;

		// task.cancel(false);
		task = new GetPageTask(this);
		if (VERSION.SDK_INT >= 11) {
			task.executeOnExecutor(executorService, new String[] { title, "0" });
		} else {
			task.execute(new String[] { title, "0" });
		}
	}

	private class GetPageTask extends AsyncTask<String, Integer, Integer> {
		ProgressDialog pdialog;

		String result;
		String result_extra;

		private JSONArray parse;

		public GetPageTask(Context context) {
			pdialog = new ProgressDialog(context);
			pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pdialog.setMessage("载入中");
			pdialog.setIndeterminate(true);
			pdialog.setCancelable(false);
			pdialog.show();
		}

		private int getCache(String title1) {
			Log.i("MoeGirlCache", "Try Get Cache: " + title1);
			result_extra = title1;
			try {
				File tmp = new File(getCacheDir(), title1);
				if (tmp.exists()) {
					if (forceCache
							|| System.currentTimeMillis() - tmp.lastModified() < 432000000) {

						FileInputStream inStream = new FileInputStream(tmp);
						byte[] revid_b = new byte[8];
						byte[] buffer_len_b = new byte[8];
						inStream.read(revid_b, 0, 8);
						inStream.read(buffer_len_b, 0, 8);
						revid = bytes2Long(revid_b);
						int buffer_len = (int) bytes2Long(buffer_len_b);

						byte[] buffer = new byte[buffer_len];
						inStream.read(buffer);
						inStream.close();

						// String result = EncodingUtils
						// .getString(buffer, "utf-8");
						String result = new String(buffer, "utf-8");

						Log.i("MoeGirlCache", "hit cache: " + title1
								+ " revid: " + Long.toString(revid)
								+ " length: " + Integer.toString(buffer_len));

						if (revid != 0) {
							isFromCache = true;
							this.result = result;
							return 0;
						} else {
							int getcachetmp = getCache(result);
							if (getcachetmp == 0) {
								return 1;
							} else {
								return -1;
							}
						}
					} else {
						Log.i("MoeGirlCache", "cache expired: " + title1
								+ " revid: " + Long.toString(revid));
						return -1;
					}
				} else {
					Log.i("MoeGirlCache", "no cache: " + title1);
					return -1;
				}
			} catch (Exception e) {
				Log.e("MoeGirlGetCache", e.toString());
				return -1;
			}
		}

		@Override
		protected Integer doInBackground(String... arg0) {
			// Log.i("MoeGirlDebug", "title：" + title);

			title = arg0[0];
			String ForceUpdate = arg0[1];

			if (ForceUpdate == "0") {
				int tmp = getCache(title);
				if (tmp != -1) {
					return tmp;
				}
			}

			String header = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
					+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"zh-CN\" lang=\"zh-CN\" dir=\"ltr\">\n"
					+ "<head>\n"
					+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
					+ "<meta name=\"robots\" content=\"noindex, nofollow\" />\n<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0,maximum-scale=1.0, user-scalable=no\" />\n"
					+ "<title>萌娘百科 万物皆可萌的百科全书 - zh.moegirl.org</title>\n"
					+ "<link rel=\"apple-touch-icon\" href=\"/apple-touch-icon.png\" />\n"
					+ "<link rel=\"stylesheet\" href=\"/skins/wptouch/css/main.css\" type=\"text/css\" media=\"screen\" />\n"
					+ "<link href=\"//zh.moegirl.org/load.php?debug=false&lang=zh&modules=site&only=styles&skin=vector&*\" rel=\"stylesheet\">\n"
					+ "<script src=\"/load.php?debug=false&lang=zh&modules=jquery%2Cmediawiki&only=scripts&skin=vector&version=20131206T221358Z\">\n"
					+ "<script type=\"text/javascript\" src=\"/skins/wptouch/javascript/core.js?ver=1.9\"></script>\n"
					+ "</head>"
					+ "<body><div class=\"content\"><div class=\"post\"><div class=\"mainentry\"><div id=\"mw-content-text\" class=\"mw-content-ltr\" lang=\"zh-CN\" dir=\"ltr\">";

			String addonStyle = "<style>.thumbimage{max-width: 100%} "
					+ ".thumbinner{max-width:100%} tr, td{display: block;}"
					+ "#toc{background-color: #F9F9F9;border: 1px solid #AAAAAA;padding: 7px;width:200px;}"
					+ "@media screen and (max-width: 400px) {p{clear: both;}}"
					+ ".mw-editsection{display:none;}.mw-headline{font-weight:bold;}"
					+ ".nomobile{display:none;} .heimu{background-color: #000;}"
					+ "</style>";

			String urladdr = "";
			try {
				// redirects重定向
				urladdr = String
						.format("http://zh.moegirl.org/api.php?action=parse&format=json&redirects&page=%1$s",
								URLEncoder.encode(title, "utf-8"));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				Log.e("MoeGirl", e1.toString());
				return null;
			}

			String footer = "</div>"
					+ "<hr/><p>本站全部内容禁止商业使用<br/>文本内容除另有声明外,均在"
					+ "<b>知识共享(Creative Commons) 署名-非商业性使用-相同方式共享 3.0 协议</b>"
					+ "下提供,附加条款亦可能应用<br/>其他类型作品版权归属原作者，如有授权遵照授权协议使用</p>"
					+ "</div></div></div>"
					+ "<script>/*<![CDATA[*/window.jQuery && jQuery.ready();/*]]>*/</script>"
					+ "<script>if(window.mw){\nmw.loader.state({\"site\":\"loading\",\"user\":\"ready\",\"user.groups\":\"ready\"});\n}</script>\n"
					+ "<script>if(window.mw){\nmw.loader.load([\"mediawiki.action.view.postEdit\",\"mediawiki.user\",\"mediawiki.hidpi\",\"mediawiki.page.ready\",\"mediawiki.searchSuggest\",\"ext.gadget.Force_preview\",\"ext.gadget.Searchbox-popout\",\"ext.gadget.Backtotop\",\"ext.FancyBoxThumbs\",\"ext.ajaxpoll\"],null,true);\n}</script>\n"
					+ "<script>\nvar fbtFancyBoxOptions = {\"openEffect\":\"elastic\",\"closeEffect\":\"elastic\",\"helpers\":{\"title\":{\"type\":\"inside\"}}};\n</script>\n"
					+ "<script src=\"//m.moegirl.org/load.php?debug=false&amp;lang=zh-cn&amp;modules=site&amp;only=scripts&amp;skin=wptouch&amp;*\"></script>\n"
					+ "<script>$(\".heimu\").toggle(function(event){if($(event.target).hasClass(\"heimu\"))$(event.target).css(\"background-color\",\"transparent\"); if($(event.target).attr(\"href\")!=undefined)location.href=$(event.target).attr(\"href\");},function(event){if($(event.target).hasClass(\"heimu\"))$(event.target).css(\"background-color\",\"#000\");if($(event.target).attr(\"href\")!=undefined)location.href=$(event.target).attr(\"href\");});</script>"
					+ "</body></html>";

			HttpClient httpClient1 = new DefaultHttpClient();
			httpClient1.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
					getResources().getString(R.string.user_agent));
			HttpGet httpRequest1 = new HttpGet(urladdr);

			try {
				HttpResponse response = httpClient1.execute(httpRequest1);
				HttpEntity entity = response.getEntity();
				// BufferedReader reader = new BufferedReader(
				// new InputStreamReader(entity.getContent(), "utf-8"));
				//
				// StringBuffer sb = new StringBuffer();
				// String line = null;
				// while ((line = reader.readLine()) != null) {
				// sb.append(line + '\n');
				// }
				// reader.close();
				String getResult = EntityUtils.toString(entity);

				JSONTokener JsonParser = new JSONTokener(getResult);
				JSONObject all = (JSONObject) JsonParser.nextValue();
				JSONObject parse = all.getJSONObject("parse");
				JSONObject txt = parse.getJSONObject("text");

				String returnTitle = parse.getString("title");
				revid = parse.getLong("revid");
				String result = txt.getString("*");
				// String result = txt.getString("*").replace(
				// "<div class=\"thumbinner\" style=\"width:419px;\">",
				// "<div class=\"thumbinner\" style=\"width:100%;\">");

				result = header + addonStyle + result + footer;

				try {
					if (returnTitle.equals(title)) {
						File tmp = new File(getCacheDir(), title);

						FileOutputStream outStream = new FileOutputStream(tmp);
						outStream.write(long2Bytes(revid));
						byte[] buffertmp = result.getBytes("utf-8");
						outStream.write(long2Bytes(buffertmp.length));
						outStream.write(buffertmp);
						outStream.close();

						Log.i("MoeGirl", "cache: " + title + " Length: "
								+ Integer.toString(buffertmp.length));

					} else {
						File tmp = new File(getCacheDir(), title);
						FileOutputStream outStream = new FileOutputStream(tmp);
						outStream.write(long2Bytes(0l));
						byte[] buffertmp = returnTitle.getBytes("utf-8");
						outStream.write(long2Bytes(buffertmp.length));
						outStream.write(buffertmp);
						outStream.close();

						tmp = new File(getCacheDir(), returnTitle);
						if (!tmp.exists()) {
							outStream = new FileOutputStream(tmp);
							outStream.write(long2Bytes(revid));
							buffertmp = result.getBytes("utf-8");
							outStream.write(long2Bytes(buffertmp.length));
							outStream.write(buffertmp);
							outStream.close();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("MoeGirlGetTxt", e.toString());
				}
				isFromCache = false;
				this.result = result;

				if (!returnTitle.equals(title)) {
					this.result_extra = returnTitle;
					return 1;
				} else {
					return 0;
				}
				// return sb.toString();
			} catch (org.json.JSONException e) {
				// String errhtml = "-1";
				return -1;
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("MoeGirl", e.toString());
			}
			return -2;
		}

		@Override
		protected void onPostExecute(Integer result) {
			boolean isErr = false;
			if (result == -1) {
				webview1.loadDataWithBaseURL(null, "没有这个词条", "text/html",
						"utf-8", null);
				setTitle("萌娘百科");
				isErr = true;
			} else if (result == null || result == -1) {
				webview1.loadDataWithBaseURL(null, "发生错误!", "text/html",
						"utf-8", null);
				setTitle("萌娘百科");
				isErr = true;
			} else {
				if (result == 1) {
					title = this.result_extra;
				}

				Log.i("MoeGirlView",
						"title: " + title.toLowerCase(Locale.getDefault()));
				webview1.loadDataWithBaseURL("http://m.moegirl.org/",
						this.result, "text/html", "utf-8", null);

				if (title.toLowerCase(Locale.getDefault()).equals("mainpage")) {
					setTitle("萌娘百科");
				} else {
					setTitle(title);
				}
				webview_html = this.result;
			}

			pdialog.dismiss();

			if (!isErr && isFromCache && !forceCache && revid > 0) {
				task2 = new CheckUpdateTask();
				task2.execute(new String[] { title, Long.toString(revid) });
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() < 5) {
			if (title != homePage) {
				String target = goback();
				while (target.equals(this.title)) {
					target = goback();
				}
				loadtitle(target, false);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private String goback() {
		if (pageHistory.isEmpty()) {
			return homePage;
		} else {
			return pageHistory.pop();
		}
	}

	private class HelloWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// Log.i("MoeGirl", url);
			if (url.indexOf("http://m.moegirl.org/") >= 0) {
				String newtitle = url.replace("http://m.moegirl.org/", "");
				if (url.indexOf("index.php") < 0
						&& newtitle.indexOf("File:") < 0
						&& newtitle.indexOf("Category:") < 0) {
					try {
						loadtitle(URLDecoder.decode(newtitle, "utf-8"));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					Uri content_url = Uri.parse(url.replace("redlink=1", ""));
					intent.setData(content_url);
					startActivity(intent);
					// view.loadUrl(url.replace("redlink=1", ""));
					// setTitle("萌娘百科");
				}
				return true;
			} else {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				Uri content_url = Uri.parse(url);
				intent.setData(content_url);
				startActivity(intent);
				return true;
			}

		}
	}

	private class CheckUpdateTask extends AsyncTask<String, Integer, Boolean> {

		String arg_title;

		@Override
		protected Boolean doInBackground(String... arg0) {
			arg_title = arg0[0];

			long arg_revid = Long.parseLong(arg0[1]);

			String urladdr = "";
			try {
				urladdr = String
						.format("http://zh.moegirl.org/api.php?action=query&prop=revisions&titles=%1$s&format=json",
								URLEncoder.encode(arg_title, "utf-8"));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return false;
			}

			HttpClient httpClient1 = new DefaultHttpClient();
			httpClient1.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
					getResources().getString(R.string.user_agent));
			HttpGet httpRequest1 = new HttpGet(urladdr);

			try {
				HttpResponse response = httpClient1.execute(httpRequest1);
				HttpEntity entity = response.getEntity();

				String getResult = EntityUtils.toString(entity);
				// BufferedReader reader = new BufferedReader(
				// new InputStreamReader(entity.getContent(), "utf-8"));
				//
				// StringBuffer sb = new StringBuffer();
				// String line = null;
				// while ((line = reader.readLine()) != null) {
				// sb.append(line + '\n');
				// }
				// reader.close();
				// // Log.i("MoeGirl", sb.toString());

				JSONTokener JsonParser = new JSONTokener(getResult);
				JSONObject all = (JSONObject) JsonParser.nextValue();
				JSONObject query = all.getJSONObject("query").getJSONObject(
						"pages");
				if (query.length() > 0) {
					JSONObject page = query.getJSONObject(query.names()
							.getString(0));
					JSONArray revisions = page.getJSONArray("revisions");
					long nowRevid = revisions.getJSONObject(0).getLong("revid");

					Log.i("MoeGirlUpdateCheck",
							"NewRevid: " + Long.toString(nowRevid)
									+ " CacheRevid: "
									+ Long.toString(arg_revid));
					if (nowRevid > arg_revid) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
				// return sb.toString();
			} catch (org.json.JSONException e) {
				Log.e("MoeGirlUpdateCheck", e.toString());
				return false;
			} catch (Exception e) {
				Log.e("MoeGirlUpdateCheck", e.toString());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// Log.i("MoeGirl", result.toString());
			if (result && arg_title.equals(title)) {
				Toast.makeText(getApplicationContext(), "词条已经更新，点击右上角刷新查看",
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	public int detect() {
		ConnectivityManager manager = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null) {
			return 0;
		}
		NetworkInfo networkinfo = manager.getActiveNetworkInfo();

		if (networkinfo == null || !networkinfo.isAvailable()) {
			return 0;
		}
		if (networkinfo.getType() == ConnectivityManager.TYPE_MOBILE) {
			return 1;
		} else if (networkinfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return 2;
		}
		return 0;
	}

	public static byte[] long2Bytes(long num) {
		byte[] byteNum = new byte[8];
		for (int ix = 0; ix < 8; ++ix) {
			int offset = 64 - (ix + 1) * 8;
			byteNum[ix] = (byte) ((num >> offset) & 0xff);
		}
		return byteNum;
	}

	public static long bytes2Long(byte[] byteNum) {
		long num = 0;
		for (int ix = 0; ix < 8; ++ix) {
			num <<= 8;
			num |= (byteNum[ix] & 0xff);
		}
		return num;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		shakeDetector.stop();
	}

	private class ShakeListener implements OnShakeListener {

		@Override
		public void onShake() { // TODO Auto-generated method stub
			webview1.loadUrl("http://m.moegirl.org/Special:%E9%9A%8F%E6%9C%BA%E9%A1%B5%E9%9D%A2");
		}
	}

}
