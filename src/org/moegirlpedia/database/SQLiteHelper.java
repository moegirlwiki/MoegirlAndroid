package org.moegirlpedia.database;

import android.database.SQLException;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class SQLiteHelper extends SQLiteOpenHelper
{
	public static String DB_NAME = "History.db";// 数据库名称；处理为：所有的都记为记录，添加bookmark的标签，为true时则为书签，否则则是普通的历史记录
	public static String TB__HISTORY_NAME = "allHistory";// 表名-历史记录和书签
	public static String TB__BOOKMARK_NAME = "allBookmark";// 表名-首页快捷
	private static SQLiteHelper instance = null;
	private Cursor temp_cursor;

	public static SQLiteHelper getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new SQLiteHelper(context);
		}
		return instance;
	}

	public SQLiteHelper(Context context)
	{
		super(context, DB_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		// 创建默认的allHistory表
		db.execSQL("create table " + TB__HISTORY_NAME + " ( "
				   + HistoryBean.NAME + " varchar, " + HistoryBean.URL
				   + " varchar, " + HistoryBean.ISBOOKMARK + " integer, "
				   + HistoryBean.TIME + " integer)");
		db.execSQL("create table " + TB__BOOKMARK_NAME + " ( "
				   + HistoryBean.NAME + " varchar, " + HistoryBean.URL
				   + " varchar, " + HistoryBean.ISBOOKMARK + " integer, "
				   + HistoryBean.TIME + " integer)");
	}

	/*
	 * public void createTable(SQLiteDatabase db, String table) {
	 * db.execSQL("create table " + table + " ( " + HistoryBean.NAME +
	 * " varchar, " + HistoryBean.URL + " varchar, " + HistoryBean.ISBOOKMARK +
	 * " integer, " + HistoryBean.TIME + " integer)"); }
	 */

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// TODO Auto-generated method stub
	}

	// 添加历史记录--isbookmark=0表示非书签，普通历史记录；bookmark=1表示书签
	public void add_history(Context context, String name, String url,
							int isbookmark)
	{
		String SQL = null;
		String TIP = null;
		int time = (int) Math.floor(System.currentTimeMillis() / 1000);
		SQLiteDatabase db = this.getWritableDatabase();
		if (isbookmark == 0)
		{
			temp_cursor = db.rawQuery("select * from " + TB__HISTORY_NAME
									  + " where name=" + "'" + name + "'" + ";", null);
			if (temp_cursor.moveToFirst())
			{
				SQL = "update " + TB__HISTORY_NAME + " set " + HistoryBean.TIME
					+ "=" + time + "," + HistoryBean.ISBOOKMARK + "="
					+ isbookmark + " where name=" + "'" + name + "';";
				TIP = "update";
			}
			else
			{
// 疑问：关于整型引号问题
				SQL = "insert into  " + TB__HISTORY_NAME + "(" + HistoryBean.TIME
					+ "," + HistoryBean.NAME + "," + HistoryBean.URL + ","
					+ HistoryBean.ISBOOKMARK + ")" + "values(" + time + ",'"
					+ name + "','" + url + "'," + isbookmark + ");";
				TIP = "insert";
			}
		}
		else
		{
			temp_cursor = db.rawQuery("select * from " + TB__BOOKMARK_NAME
									  + " where name=" + "'" + name + "'"+ ";", null);
			if (temp_cursor.moveToFirst()) {
				SQL = "update " + TB__BOOKMARK_NAME + " set " + HistoryBean.TIME
					+ "=" + time + "," + HistoryBean.ISBOOKMARK + "="
					+ isbookmark + " where name=" + "'" + name + "';";
				TIP = "update";
			} else {
				// 疑问：关于整型引号问题
				SQL = "insert into  " + TB__BOOKMARK_NAME + "(" + HistoryBean.TIME
					+ "," + HistoryBean.NAME + "," + HistoryBean.URL + ","
					+ HistoryBean.ISBOOKMARK + ")" + "values(" + time + ",'"
					+ name + "','" + url + "'," + isbookmark + ");";
				TIP = "insert";
			}
		}
		try
		{
			db.execSQL(SQL);
			//Toast.makeText(context, TIP + "了记录", Toast.LENGTH_LONG).show();
			Log.e("sqlite", TIP + "了记录");
		}
		catch (SQLException e)
		{
			//Toast.makeText(context, TIP + "记录出错", Toast.LENGTH_LONG).show();
			Log.e("splite", TIP + "了记录");
			return;
		}
	}
	public void delete_single_record(String name)
	{
		String SQL = "delete from " + TB__HISTORY_NAME + " where name=" + "'" + name + "'";
		SQLiteDatabase dbHelper = this.getWritableDatabase();
		try
		{
			dbHelper.execSQL(SQL);
			Log.e("delete_single_record", "success");
		}
		catch (Exception e)
		{
			Log.e("delete_single_record", "failed");
		}
		SQL = "delete from " + TB__BOOKMARK_NAME + " where name=" + "'" + name + "'";
		try
		{
			dbHelper.execSQL(SQL);
			Log.e("delete_single_record", "success");
		}
		catch (Exception e)
		{
			Log.e("delete_single_record", "failed");
		}
	}
	public void clear_history()
	{
		String SQL = "delete from " + TB__HISTORY_NAME + " where isbookmark=0";
		SQLiteDatabase db = this.getWritableDatabase();
		try
		{
			db.execSQL(SQL);
			Log.e("delete_history", "did");
		}
		catch (Exception e)
		{
			Log.e("delete_history", "failed!");
		}
	}

}
