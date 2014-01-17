package com.example.utils;

import java.util.List;


public class Pages {
	private int pageid;
	private String ns;
	private String title;
	private List<Revisions> list;

	public List<Revisions> getList() {
		return list;
	}

	public void setList(List<Revisions> list) {
		this.list = list;
	}

	public int getPageid() {
		return pageid;
	}

	public void setPageid(int pageid) {
		this.pageid = pageid;
	}

	public String getNs() {
		return ns;
	}

	public void setNs(String ns) {
		this.ns = ns;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Pages() {
		// TODO Auto-generated constructor stub
	}

}
