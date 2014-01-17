package com.example.android.moegirl;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {
	private FragmentTabHost mTabHost;
	private LayoutInflater layoutInflater;
	private Class fragmentArray[] = { FragmentIndex.class, FragmentMy.class,
			FragmentMore.class };
	private int mImageViewArray[] = { R.drawable.tab_home_btn,
			R.drawable.tab_selfinfo_btn, R.drawable.tab_more_btn };
	private String mTextViewArray[] = { "首页", "我的萌百", "更多" };

	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_tab_layout);
		initView();
	}

	private void initView() {
		layoutInflater = LayoutInflater.from(this);
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		int count = fragmentArray.length;
		for (int i = 0; i < count; i++) {
			TabSpec tabSpec = mTabHost.newTabSpec(mTextViewArray[i])
					.setIndicator(getTabItemView(i));
			mTabHost.addTab(tabSpec, fragmentArray[i], null);
			mTabHost.getTabWidget().getChildAt(i)
					.setBackgroundResource(R.drawable.selector_tab_background);
		}
	}

	private View getTabItemView(int index) {
		// TODO Auto-generated method stub
		View view = layoutInflater.inflate(R.layout.tab_item_view, null);
		ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
		imageView.setImageResource(mImageViewArray[index]);
		TextView textView = (TextView) view.findViewById(R.id.textview);
		textView.setText(mTextViewArray[index]);
		return view;
	}
}
