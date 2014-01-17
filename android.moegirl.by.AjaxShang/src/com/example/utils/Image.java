package com.example.utils;

import java.util.List;

public class Image {
	private int pageid;
	private int ns;
	private String title;
	private String imagerepository;
	private List<Imageinfo> list;

	public int getPageid() {
		return pageid;
	}

	public void setPageid(int pageid) {
		this.pageid = pageid;
	}

	public int getNs() {
		return ns;
	}

	public void setNs(int ns) {
		this.ns = ns;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImagerepository() {
		return imagerepository;
	}

	public void setImagerepository(String imagerepository) {
		this.imagerepository = imagerepository;
	}

	public List<Imageinfo> getList() {
		return list;
	}

	public void setList(List<Imageinfo> list) {
		this.list = list;
	}

}
