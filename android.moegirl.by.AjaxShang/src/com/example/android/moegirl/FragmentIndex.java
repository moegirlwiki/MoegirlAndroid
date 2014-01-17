package com.example.android.moegirl;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.utils.HttpUtils;
import com.example.utils.JSONTools;

public class FragmentIndex extends Fragment {
	private ListView listView;
	private MyAdapter adapter;
	private ProgressDialog dialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_1, null);
		final EditText editText = (EditText) view.findViewById(R.id.editText1);
		final Button button = (Button) view.findViewById(R.id.button1);
		listView = (ListView) view.findViewById(R.id.listView1);
		dialog = new ProgressDialog(getActivity());
		dialog.setTitle("提示");
		dialog.setMessage("正在很努力的加载中....");
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(getActivity(), SearchResult.class);
				String name = editText.getText().toString().trim()
						.toUpperCase();
				final String Searchpath = "http://zh.moegirl.org/api.php?action=opensearch&search="
						+ name + "&limit=10&namespace=0&format=json";
				new MyTask().execute(Searchpath);
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				TextView textView = (TextView) arg1
						.findViewById(R.id.textView1);
				String str = textView.getText().toString();
				Toast.makeText(getActivity(), str, 1).show();
				Intent intent = new Intent();
				intent.putExtra("name", str);
				intent.setClass(getActivity(), SearchResult.class);
				startActivity(intent);
			}
		});
		return view;
	}

	public class MyAdapter extends BaseAdapter {
		private List<String> list = new ArrayList<String>();
		private LayoutInflater inflater;

		public void BindData(List<String> list) {
			this.list = list;
			inflater = LayoutInflater.from(getActivity());
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view = null;
			if (convertView == null) {
				view = inflater.inflate(R.layout.result, null);
			} else {
				view = convertView;
			}
			TextView textView = (TextView) view.findViewById(R.id.textView1);
			textView.setText(list.get(position));
			return view;
		}
	}

	class MyTask extends AsyncTask<String, Void, List<String>> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog.show();
		}

		@Override
		protected List<String> doInBackground(String... params) {
			// TODO Auto-generated method stub
			return JSONTools.getSearchList(HttpUtils.getJsonString(params[0]));
		}

		@SuppressWarnings("unused")
		@Override
		protected void onPostExecute(List<String> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			// MyAdapter adapter = new MyAdapter();
			System.out.println(result.size());
			for (int i = 0; i < result.size(); i++) {
				System.out.println(i + "  " + result.get(i));
			}
			if (result.size() != 0) {
				adapter = new MyAdapter();
				adapter.BindData(result);
				listView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
			} else {
				Toast.makeText(getActivity(), "您搜索的条目不存在", Toast.LENGTH_SHORT)
						.show();
			}
			dialog.dismiss();

		}
	}
}
