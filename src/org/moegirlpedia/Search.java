package org.moegirlpedia;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;
import org.moegirlpedia.database.HistoryBean;
import org.moegirlpedia.database.SQLiteHelper;
import org.moegirlpedia.util.JsonUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.baidu.mobstat.StatService;

public class Search extends Activity {
	private SQLiteHelper sqliteHelper;
	private Handler mHandler = new Handler();
	private ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
	private EditText edittext;
	private ListView list;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sqliteHelper = new SQLiteHelper(getApplicationContext());
		setContentView(R.layout.search);
		// 绑定Layout里面的ListView
		list = (ListView) findViewById(R.id.search_list);
		ImageButton btnReturn = (ImageButton) findViewById(R.id.search_btnReturn);
		btnReturn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		final Search that = this;
		ImageButton btnClear = (ImageButton) findViewById(R.id.searchBtnClear);
		btnClear.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AlertDialog alertDialog = new AlertDialog.Builder(that)
						.create();
				alertDialog.setTitle("清空搜索历史记录");
				// prevents the user from escaping the dialog by hitting the
				// Back button
				alertDialog.setCancelable(true);
				alertDialog.setMessage("确定吗？");
				alertDialog.setButton("确定",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// clear history
								try {
									sqliteHelper.clear_search_history();
									Log.e("clear_history", "cleared");
								} catch (Exception e) {
									Log.e("clear_history", "failed");
								}
								finish();
							}
						});
				alertDialog.setButton2("取消",
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
		final LinearLayout historyBar = (LinearLayout) findViewById(R.id.searchHistoryBar);
		edittext = (EditText) findViewById(R.id.searchEditText1);

		edittext.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if (actionId == EditorInfo.IME_ACTION_GO) {
					ret();
					return true;
				}
				return false;
			}
		});

		edittext.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				historyBar.setVisibility(View.GONE);
				listItem.clear();
				setList();

				new Thread(new Runnable() {

					@Override
					public void run() {
						String querytext = edittext.getText().toString();
						String myString = "";
						try {
							querytext = URLEncoder.encode(querytext, "utf-8");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							// 定义获取文件内容的URL
							URL myURL = new URL(
									getString(R.string.baseurl)
											+ "api.php?action=opensearch&limit=100&search="
											+ querytext);
							// 打开URL链接
							URLConnection ucon = myURL.openConnection();
							ucon.addRequestProperty("User-Agent",
									getString(R.string.useragent));
							// 使用InputStream，从URLConnection读取数据
							InputStream is = ucon.getInputStream();
							BufferedInputStream bis = new BufferedInputStream(
									is);
							// 用ByteArrayBuffer缓存
							ByteArrayBuffer baf = new ByteArrayBuffer(50);
							int current = 0;
							while ((current = bis.read()) != -1) {
								baf.append((byte) current);
							}
							// 将缓存的内容转化为String,用UTF-8编码
							myString = EncodingUtils.getString(
									baf.toByteArray(), "UTF-8");
						} catch (Exception e) {
							e.printStackTrace();
						}

						try {
							List ret = (List) JsonUtil
									.getObjectFromJson(myString);
							final String word = ret.get(0).toString();
							final List array = (List) ret.get(1);
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									if (!word.equals(edittext.getText()
											.toString()))
										return;
									// 生成动态数组，加入数据
									listItem.clear();
									for (int i = 0; i < array.size(); i++) {
										HashMap<String, Object> map = new HashMap<String, Object>();
										map.put("ItemText",
												(String) array.get(i));
										listItem.add(map);
									}
									setList();
								}
							});
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		// 添加点击
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				String text = (String) listItem.get(arg2).get("ItemText");
				if (edittext.getText().toString().equals(text))
					ret();
				edittext.setText(text);
				edittext.setSelection(text.length());
			}
		});

		get_Search_History();
		setList();

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

	}

	private void ret() {
		String name = edittext.getText().toString();
		sqliteHelper.add_search_history(this, name);

		String url = getString(R.string.baseurl);
		try {
			url += URLEncoder.encode(name, "utf-8").replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		url += "?action=render";
		Intent intent = new Intent(Search.this, MainActivity.class);
		intent.putExtra("url", url);
		setResult(RESULT_OK, intent);
		finish();
	}

	private void get_Search_History() {
		SQLiteDatabase db = sqliteHelper.getWritableDatabase();
		Cursor myCursor = db.query(SQLiteHelper.TB__SEARCH_NAME,
				new String[] { HistoryBean.NAME }, null, null, null, null,
				HistoryBean.TIME + " DESC");
		int name = myCursor.getColumnIndex(HistoryBean.NAME);
		listItem.clear();
		if (myCursor.moveToFirst()) {
			do {
				HashMap<String, Object> item = new HashMap<String, Object>();
				item.put("ItemText", myCursor.getString(name));
				listItem.add(item);
			} while (myCursor.moveToNext());
		}
		myCursor.close();
	}

	private void setList() {
		// 生成适配器的Item和动态数组对应的元素
		SimpleAdapter listItemAdapter = new SimpleAdapter(this, listItem,// 数据源
				R.layout.search_display_style,// ListItem的XML实现
				new String[] { "ItemText" }, new int[] { R.id.ItemText });

		// 添加并且显示
		list.setAdapter(listItemAdapter);
	}

	public void onResume() {
		super.onResume();

		/**
		 * 页面起始（每个Activity中都需要添加，如果有继承的父Activity中已经添加了该调用，那么子Activity中务必不能添加）
		 * 不能与StatService.onPageStart一级onPageEnd函数交叉使用
		 */
		StatService.onResume(this);
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
