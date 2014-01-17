package com.example.utils;

public class PareString {
	// 解码重定向
	public static String pareString(String string) {
		String[] str = string.split(" ");
		int d = string.lastIndexOf("[");
		int t = string.indexOf("]");

		System.out.println(string + "  " + string.length());
		System.out.println("d:" + d);
		System.out.println("t:" + t);
		System.out.println("--->" + str[0]);
		String file = str[1].substring(d + 1, t);
		if (str[0].equals("#REDIRECT")) {
			return file;
		}
		return "no";
	}
}
