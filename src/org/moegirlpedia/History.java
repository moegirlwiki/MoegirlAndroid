package org.moegirlpedia;

import java.util.ArrayList;
import java.util.HashMap;

import org.moegirlpedia.database.HistoryBean;
import org.moegirlpedia.database.SQLiteHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;

public class History extends Activity
{
	ArrayList<HashMap<String, Object>> history_data_list = new ArrayList<HashMap<String, Object>>();// 用来显示历史的list
	private SQLiteHelper sqliteHelper;
	private Cursor myCursor;
	private ListView history_listview;
	public static String operaString = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.page_history);
		ImageButton btnReturn = (ImageButton) findViewById(R.id.history_btnReturn);
		btnReturn.setOnClickListener(new OnClickListener() {
				public void onClick(View v)
				{
					finish();
				}
			});
		init();
	}

	public void init()
	{
		sqliteHelper = new SQLiteHelper(getApplicationContext());
		history_listview = (ListView) findViewById(R.id.history_list);
		SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(),
												  get_History(), R.layout.history_display_style, new String[] {
													  "title", "url" }, new int[] { R.id.website_name,
													  R.id.website_url });
		history_listview.setAdapter(adapter);

		// 设置ListView的项目按下事件监听
		history_listview.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View v,
										int position, long id)
				{
					String url = history_data_list.get(position).get("url")
						.toString();

					Intent intent = new Intent(History.this, MainActivity.class);  
					intent.putExtra("url", url);
					setResult(RESULT_OK, intent);
					finish();
				}
			});
		// 设置ListView的项目长按下事件监听
		// 设置ListView的项目长按下事件监听
		history_listview.setOnItemLongClickListener(new ListItemLongClick());
		history_listview.setOnCreateContextMenuListener(new ListonCreate());

		final History that = this;
		Button btnClear = (Button) findViewById(R.id.historybtnClear);
		btnClear.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					AlertDialog alertDialog = new AlertDialog.Builder(that).create();
					alertDialog.setTitle("清空历史记录");
					// prevents the user from escaping the dialog by hitting the Back button
					alertDialog.setCancelable(true);
					alertDialog.setMessage("确定吗？");
					alertDialog.setButton("确定",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which)
							{
								//clear history
								try
								{
									sqliteHelper.clear_history();
									Log.e("clear_history", "cleared");
								}
								catch (Exception e)
								{
									Log.e("clear_history", "failed");
								}
								finish();
							}
						});
					alertDialog.setButton2("取消", new DialogInterface.OnClickListener() { 

							@Override 
							public void onClick(DialogInterface dialog, int which) { 
								// TODO Auto-generated method stub  
							} 
						});
					alertDialog.show();
				}
			});
	}

	private ArrayList<HashMap<String, Object>> get_History()
	{
		SQLiteDatabase db = sqliteHelper.getWritableDatabase();
		myCursor = db.query(SQLiteHelper.TB__HISTORY_NAME, new String[] {
								HistoryBean.NAME, HistoryBean.URL }, "isbookmark=0", null,
							null, null, HistoryBean.TIME + " DESC");
		int url = myCursor.getColumnIndex(HistoryBean.URL);
		int name = myCursor.getColumnIndex(HistoryBean.NAME);
		history_data_list.clear();
		if (myCursor.moveToFirst())
		{
			do {
				HashMap<String, Object> item = new HashMap<String, Object>();
				item.put("title", myCursor.getString(name));
				item.put("url", myCursor.getString(url));
				history_data_list.add(item);
			} while (myCursor.moveToNext());
		}
		myCursor.close();
		return history_data_list;
	}
	public boolean onContextItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case 1:
				// 删除操作
				Log.e("opera", operaString);
				sqliteHelper.delete_single_record(operaString);
				finish();
				break;
			default:
				break;
		}
		return super.onContextItemSelected(item);

	}

	// 长按弹出菜单事件类
	private class ListonCreate implements OnCreateContextMenuListener
	{

		public void onCreateContextMenu(ContextMenu menu, View arg1,
										ContextMenuInfo arg2)
		{
			menu.setHeaderTitle(R.string.long_click_history_title);
			//menu.add(0, 0, 0, R.string.modify);
			menu.add(0, 1, 0, R.string.delete);
		}

	}

	// 长按事件类
	private class ListItemLongClick implements OnItemLongClickListener
	{

		public boolean onItemLongClick(AdapterView<?> parent, View view,
									   int position, long arg3)
		{
			Log.e("tag", history_data_list.get(position).get("title").toString());
			operaString = history_data_list.get(position).get("title").toString();
			return false;
		}

	}
}
