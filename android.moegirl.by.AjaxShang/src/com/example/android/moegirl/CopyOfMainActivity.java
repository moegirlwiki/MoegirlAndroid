package com.example.android.moegirl;

import android.app.Activity;

public class CopyOfMainActivity extends Activity {
	// private String title = "";
	// private String path1 =
	// "http://zh.moegirl.org/api.php?action=query&titles=";
	// private String path2 = "&prop=revisions&rvprop=content&format=json";
	// private TextView textView;
	// private EditText editText;
	// private FrameLayout frameLayout1;
	// private Button button;
	// private ProgressDialog dialog;
	//
	// @Override
	// protected void onCreate(Bundle savedInstanceState) {
	// super.onCreate(savedInstanceState);
	// requestWindowFeature(Window.FEATURE_NO_TITLE);
	// setContentView(R.layout.activity_main);
	// textView = (TextView) findViewById(R.id.textViewId);
	// editText = (EditText) findViewById(R.id.editText);
	// frameLayout1 = (FrameLayout) findViewById(R.id.frameLayout1);
	// dialog = new ProgressDialog(this);
	// dialog.setTitle(R.string.notice);
	// dialog.setMessage(getText(R.string.message));
	// dialog.setCancelable(false);
	// button = (Button) findViewById(R.id.button);
	// button.setOnClickListener(new OnClickListener() {
	// @Override
	// public void onClick(View v) {
	// // TODO Auto-generated method stub
	// if (title != null) {
	// title = editText.getText().toString().trim();
	// String path = path1 + title + path2;
	// System.out.println(path);
	// new MyTask().execute(path);
	// }
	// }
	// });
	//
	// }
	//
	// class MyTask extends AsyncTask<String, Void, Pages> {
	// @Override
	// protected void onPreExecute() {
	// // TODO Auto-generated method stub
	// super.onPreExecute();
	// dialog.show();
	// }
	//
	// @Override
	// protected Pages doInBackground(String... params) {
	// // TODO Auto-generated method stub
	// String json = HttpUtils.getJsonString(params[0]);
	// return JSONTools.pareJson(json);
	// }
	//
	// @Override
	// protected void onPostExecute(Pages result) {
	// // TODO Auto-generated method stub
	// super.onPostExecute(result);
	// if (result != null) {
	// textView.setText(result.getList().get(0).getContent());
	// frameLayout1.setVisibility(View.GONE);
	// } else {
	// textView.setText("error page");
	// }
	// dialog.dismiss();
	// }
	// }
	//
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.main, menu);
	// return true;
	// }

}
