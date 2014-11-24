package org.moegirl.moegirlview;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

public class SearchSuggestionsProvider extends SearchRecentSuggestionsProvider {
	static final String TAG = SearchSuggestionsProvider.class.getSimpleName();
	public static final String AUTHORITY = SearchSuggestionsProvider.class
			.getName();
	public static final int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;
	private static final String[] COLUMNS = {
			"_id", // must include this column
			SearchManager.SUGGEST_COLUMN_TEXT_1,
			SearchManager.SUGGEST_COLUMN_TEXT_2,
			SearchManager.SUGGEST_COLUMN_INTENT_DATA,
			SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
			SearchManager.SUGGEST_COLUMN_SHORTCUT_ID };

	public SearchSuggestionsProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		String query = uri.getLastPathSegment();

		// Log.i("MoeGirlss", selectionArgs.toString());
		if (query == null || query.length() == 0) {
			return null;
		}

		MatrixCursor cursor = new MatrixCursor(COLUMNS);

		if (query.equals("search_suggest_query")) {
			cursor.addRow(createRow(0, "搜索中...", "", ""));
			return cursor;
		}

		try {
			String urladdr = String
					.format("http://zh.moegirl.org/api.php?action=query&list=search&srwhat=title&srsearch=%1$s&format=json",
							URLEncoder.encode(query, "utf-8"));

			HttpClient httpClient1 = new DefaultHttpClient();
			HttpGet httpRequest1 = new HttpGet(urladdr);
			HttpResponse response = httpClient1.execute(httpRequest1);
			HttpEntity entity = response.getEntity();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					entity.getContent(), "utf-8"));

			StringBuffer sb = new StringBuffer();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + '\n');
			}
			reader.close();

			JSONTokener JsonParser = new JSONTokener(sb.toString());
			JSONObject all = (JSONObject) JsonParser.nextValue();
			JSONObject queryJson = all.getJSONObject("query");
			JSONArray searchList = queryJson.getJSONArray("search");

			for (int i = 0; i < searchList.length(); i++) {
				String pageTitle = searchList.getJSONObject(i).getString(
						"title");
				String snippet = searchList.getJSONObject(i).getString(
						"snippet");

				Pattern pattern = null;
				try {
					pattern = Pattern.compile("<[^>]+>");
					Matcher m = pattern.matcher(snippet);
					snippet = m.replaceAll("");
					snippet = snippet.replace("#REDIRECT", "跳转至");
				} catch (Exception e) {
					Log.e(TAG, "take pattern fail " + query, e);
				}

				int kwoffset = snippet.indexOf(query);
				if (kwoffset >= 6)
					kwoffset -= 6;
				else if (kwoffset < 0)
					kwoffset = 0;
				snippet = snippet.substring(kwoffset);

				cursor.addRow(createRow(i, pageTitle, snippet));
			}
			if (searchList.length() == 0) {
				Log.i("MoeGirlSearchSuggest", "no suggest query: " + query);
				cursor.addRow(createRow(0, "没有找到相关内容", "", ""));
			}

		} catch (Exception e) {
			Log.e(TAG, "Failed to lookup " + query, e);
		}
		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	private Object[] createRow(Integer id, String text1, String text2) {
		return createRow(id, text1, text2, text1);
	}

	private Object[] createRow(Integer id, String text1, String text2,
			String intentData) {
		return new Object[] { id, // _id
				text1, text2, intentData, "android.intent.action.VIEW", // action
				SearchManager.SUGGEST_NEVER_MAKE_SHORTCUT };
	}

}