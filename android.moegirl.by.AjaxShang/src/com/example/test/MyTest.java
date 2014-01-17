package com.example.test;

import android.test.AndroidTestCase;

import com.example.utils.HttpUtils;

public class MyTest extends AndroidTestCase {
	public void test() {
		String searchString = "http://zh.moegirl.org/api.php?action=opensearch&limit=30&namespace=0&format=json&search=CC";
		String string = HttpUtils.getJsonString(searchString);
		System.out.println("result : " + string);
	}

	// public void testJson() {
	// String searchString =
	// "http://zh.moegirl.org/api.php?action=opensearch&limit=30&namespace=0&format=json&search=CC";
	// List<String> list = JSONTools.getList(HttpUtils
	// .getJsonString(searchString));
	// System.out.println("list.siez:" + list.size());
	// for (int i = 0; i < list.size(); i++) {
	// System.out.println("--->i:" + list.get(i));
	// }
	// }
}
