package com.example.utils;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONTools {

	public JSONTools() {
		// TODO Auto-generated constructor stub
	}

	// FIXME 添加一个解析搜索结构的方法
		// 解析这种json[
		// "御姐",
		// [
		// "御姐",
		// "御姐控"
		// ]
		// ]

	public static List<String> getSearchList(String json) {
		List<String> list = new ArrayList<String>();
		try {
			JSONArray jArray = new JSONArray(json);
			String String = jArray.getString(0);
			JSONArray subJsonArray = jArray.getJSONArray(1);
			for (int i = 0; i < subJsonArray.length(); i++) {
				list.add(subJsonArray.getString(i));
			}

			return list;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public static Image pareImageJson(String json) {
		List<Imageinfo> list = new ArrayList<Imageinfo>();
		String pageid = "-1";
		try {
			JSONObject jo = new JSONObject(json);
			JSONObject jsonObject = jo.getJSONObject("query");
			JSONObject jObject = jsonObject.getJSONObject("pages");
			Iterator<?> iterator = jObject.keys();
			while (iterator.hasNext()) {
				pageid = (String) iterator.next();
			}
			if (pageid.equals("-1")) {
				return null;
			} else {
				JSONObject jsonObject2 = jObject.getJSONObject(pageid);
				Image image = new Image();
				image.setPageid(jsonObject2.getInt("pageid"));
				image.setNs(jsonObject2.getInt("ns"));
				image.setTitle(jsonObject2.getString("title"));
				JSONArray jsonArray = jsonObject2.getJSONArray("imageinfo");
				for (int i = 0; i < jsonArray.length(); i++) {
					Imageinfo imageinfo = new Imageinfo();
					JSONObject jsonObject3 = jsonArray.getJSONObject(i);
					imageinfo.setUrl(URLDecoder.decode(jsonObject3
							.getString("url")));
					imageinfo.setDescriptionurl(URLDecoder.decode(jsonObject3
							.getString("descriptionurl")));
					list.add(imageinfo);
				}
				image.setList(list);
				return image;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Pages pareJson(String json) {
		List<Revisions> list = new ArrayList<Revisions>();
		String pageid = "";
		try {
			JSONObject jo = new JSONObject(json);
			JSONObject jsonObject = jo.getJSONObject("query");
			JSONObject jObject = jsonObject.getJSONObject("pages");
			Iterator<?> iterator = jObject.keys();
			while (iterator.hasNext()) {
				pageid = (String) iterator.next();
			}
			if (pageid.equals("")) {
				return null;
			} else {
				JSONObject jsonObject2 = jObject.getJSONObject(pageid);
				Pages pages = new Pages();
				pages.setPageid(jsonObject2.getInt("pageid"));
				pages.setNs(jsonObject2.getString("ns"));
				pages.setTitle(jsonObject2.getString("title"));
				JSONArray jsonArray = jsonObject2.getJSONArray("revisions");
				for (int i = 0; i < jsonArray.length(); i++) {
					Revisions revisions = new Revisions();
					JSONObject jsonObject3 = jsonArray.getJSONObject(i);
					revisions.setContentformat(jsonObject3
							.getString("contentformat"));
					revisions.setContentmodel(jsonObject3
							.getString("contentmodel"));
					revisions.setContent(jsonObject3.getString("*"));
					list.add(revisions);
				}
				pages.setList(list);
				return pages;
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
