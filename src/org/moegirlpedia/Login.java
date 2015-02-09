package org.moegirlpedia;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;
import org.moegirlpedia.util.JsonUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.baidu.mobstat.StatService;

public class Login extends Activity
{
	private Handler mHandler = new Handler();
	private EditText edtUsername;
	private EditText edtPassword;
	private Button btnLogin;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		ImageButton btnReturn = (ImageButton) findViewById(R.id.login_btnReturn);
		btnReturn.setOnClickListener(new OnClickListener() {
				public void onClick(View v)
				{
					finish();
				}
			});
			
		Button btnReg = (Button) findViewById(R.id.loginBtnReg);
		btnReg.setOnClickListener(new OnClickListener() {
				public void onClick(View v)
				{
					Intent intent = new Intent(Intent.ACTION_VIEW);
					Uri content_url = Uri.parse(getString(R.string.baseurl) + "Special:%E7%94%A8%E6%88%B7%E7%99%BB%E5%BD%95?type=signup");
					intent.setData(content_url);
					startActivity(intent);
				}
			});

		edtUsername = (EditText) findViewById(R.id.loginEditTextUserName);
		edtPassword = (EditText) findViewById(R.id.loginEditTextPassword);
		btnLogin = (Button) findViewById(R.id.loginBtnLogin);
		
		final Login that = this;
		btnLogin.setOnClickListener(new OnClickListener() {
			ProgressDialog pdialog;
			
				public void onClick(View v)
				{
					String strUsername = "";
					String strPassword = "";
					try
					{
						strUsername = URLEncoder.encode(edtUsername.getText().toString(), "utf-8").replace("+", "%20");
						strPassword = URLEncoder.encode(edtPassword.getText().toString(), "utf-8").replace("+", "%20");
					}
					catch (UnsupportedEncodingException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
					
					pdialog = new ProgressDialog(that);
					pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					pdialog.setMessage("请稍候");
					pdialog.setIndeterminate(true);
					pdialog.setCancelable(false);
					pdialog.show();
					
					//没有token的请求
					final String strPost = "format=json&action=login&lgname="
						+ strUsername + "&lgpassword=" + strPassword;
					new Thread(new Runnable() {
							@Override
							public void run()
							{
								String strResult = postData(strPost);
								
								if (strResult.isEmpty())
								{
									callFailed();
									return;
								}
								try {
									List ret = (List) JsonUtil.getObjectFromJson(strResult);
									List objlist = (List) ((List) ret.get(0)).get(1);
									String token = "";
									for (int i=0;i<objlist.size();i++)
									{
										List obj = (List) objlist.get(i);
										String name = obj.get(0).toString();
										String value = obj.get(1).toString();
										if (name.equals("token"))
										{
											token = value;
											break;
										}
									}
									
									strResult = postData(strPost + "&lgtoken=" + token);
									
									ret = (List) JsonUtil.getObjectFromJson(strResult);
									objlist = (List) ((List) ret.get(0)).get(1);
									String result = "";
									for (int i=0;i<objlist.size();i++)
									{
										List obj = (List) objlist.get(i);
										String name = obj.get(0).toString();
										String value = obj.get(1).toString();
										if (name.equals("result"))
										{
											result = value;
											break;
										}
									}
									
									if (result.equals("Success"))
									{
										mHandler.post(new Runnable() {
												@Override
												public void run() {
													pdialog.dismiss();
													Toast.makeText(that, "登录成功", Toast.LENGTH_LONG).show();
													setResult(20);
													finish();
												}
											});
									}
									else
										callFailed();
								} catch (Exception e) {
									e.printStackTrace();
									callFailed();
								}
								
							}

							private void callFailed()
							{
								mHandler.post(new Runnable() {
										@Override
										public void run() {
											Toast.makeText(that, "登录失败", Toast.LENGTH_LONG).show();
											pdialog.dismiss();
										}
									});
							}
						}).start();
				}
			});
			
		edtPassword.setOnEditorActionListener(new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId,
											  KeyEvent event) {
					// TODO Auto-generated method stub
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						btnLogin.callOnClick();
						return true;
					}
					return false;
				}
			});
		edtUsername.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before,
										  int count) {
					detectEditText();
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
											  int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});
		edtPassword.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before,
										  int count) {
					detectEditText();
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
											  int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});
	}

	private void detectEditText()
	{
		if ((edtUsername.getText().toString().isEmpty())||(edtPassword.getText().toString().isEmpty()))
			btnLogin.setEnabled(false);
		else
			btnLogin.setEnabled(true);
	}
	
	private String postData(String data)
	{
		String myString = "";
		try
		{
			// 定义获取文件内容的URL
			URL myURL = new URL(
				getString(R.string.baseurl)
				+ "api.php");
			// 打开URL链接
			HttpURLConnection ucon = (HttpURLConnection) myURL.openConnection();
			ucon.setConnectTimeout(10000);
			ucon.setReadTimeout(20000);
			ucon.addRequestProperty("User-Agent",
									getString(R.string.useragent));
			ucon.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
			ucon.setDoOutput(true);
			ucon.setUseCaches(false);
			ucon.setRequestMethod("POST");

			OutputStream outStrm = ucon.getOutputStream();
			outStrm.write(data.getBytes());
			outStrm.flush();
			outStrm.close();

			// 使用InputStream，从URLConnection读取数据
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(
				is);
			// 用ByteArrayBuffer缓存
			ByteArrayBuffer baf = new ByteArrayBuffer(50);
			int current = 0;
			while ((current = bis.read()) != -1)
			{
				baf.append((byte) current);
			}
			// 将缓存的内容转化为String,用UTF-8编码
			myString = EncodingUtils.getString(
				baf.toByteArray(), "UTF-8");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return myString;
	}

	public void onResume()
	{
		super.onResume();

		/**
		 * 页面起始（每个Activity中都需要添加，如果有继承的父Activity中已经添加了该调用，那么子Activity中务必不能添加）
		 * 不能与StatService.onPageStart一级onPageEnd函数交叉使用
		 */
		StatService.onResume(this);
	}

	public void onPause()
	{
		super.onPause();

		/**
		 * 页面结束（每个Activity中都需要添加，如果有继承的父Activity中已经添加了该调用，那么子Activity中务必不能添加）
		 * 不能与StatService.onPageStart一级onPageEnd函数交叉使用
		 */
		StatService.onPause(this);
	}
}
