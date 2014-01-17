package com.example.android.moegirl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class SearchResult extends Activity {
	private TextView textView;
	private WebView search_webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_result);
		textView = (TextView) this.findViewById(R.id.title_search);
		// imageView = (ImageView) this.findViewById(R.id.imageView1);

		Intent intent = getIntent();
		String name = intent.getStringExtra("name");
		String path = "http://zh.moegirl.org/" + name;
		System.out.println(path);

		textView.setText(name);
		search_webview = (WebView) this.findViewById(R.id.search_webview);
		WebSettings webSettings = search_webview.getSettings();
		webSettings.setJavaScriptEnabled(true);

		search_webview.setWebViewClient(new WebViewClient());
		search_webview.loadUrl(path);
		// search_webview.loadUrl("file:///android_asset/test.html");

	}

}
