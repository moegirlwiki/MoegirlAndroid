package org.moegirlpedia;

import android.app.*;
import android.content.*;
import android.content.res.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.view.inputmethod.*;
import android.widget.*;
import android.widget.AdapterView.*;
import android.widget.TextView.*;
import java.io.*;
import java.net.*;
import java.util.*;

import android.view.View.OnClickListener;
import android.text.*;
import org.apache.http.util.*;
import org.moegirlpedia.util.*;

public class Search extends Activity
{
	private Handler mHandler = new Handler();
	private ArrayList<HashMap<String, Object>> listItem;
	private EditText edittext;

    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        //绑定Layout里面的ListView
        final ListView list = (ListView) findViewById(R.id.search_list);
		ImageButton btnReturn = (ImageButton) findViewById(R.id.search_btnReturn);
		btnReturn.setOnClickListener(new OnClickListener() {
				public void onClick(View v)
				{
					finish();
				}
			});
		edittext = (EditText) findViewById(R.id.searchEditText1);

		edittext.setOnEditorActionListener(new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId,
											  KeyEvent event)
				{
					// TODO Auto-generated method stub
					if (actionId == EditorInfo.IME_ACTION_GO)
					{
						ret();
						return true;
					}
					return false;
				}
			});

		final Search that = this;
		edittext.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count)
				{
					new Thread(new Runnable() {

							@Override
							public void run()
							{
								// TODO Auto-generated method stub
								String querytext = edittext.getText().toString();
								String myString = "";
								try
								{
									querytext = URLEncoder.encode(querytext, "utf-8");
								}
								catch (UnsupportedEncodingException e)
								{
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								try
								{  
									// 定义获取文件内容的URL  
									URL myURL = new URL(  
										"http://m.moegirl.org/api.php?action=opensearch&limit=100&search=" + querytext);  
									// 打开URL链接  
									URLConnection ucon = myURL.openConnection();  
									// 使用InputStream，从URLConnection读取数据  
									InputStream is = ucon.getInputStream();  
									BufferedInputStream bis = new BufferedInputStream(is);  
									// 用ByteArrayBuffer缓存  
									ByteArrayBuffer baf = new ByteArrayBuffer(50);  
									int current = 0;  
									while ((current = bis.read()) != -1)
									{  
										baf.append((byte) current);  
									}  
									// 将缓存的内容转化为String,用UTF-8编码  
									myString = EncodingUtils.getString(baf.toByteArray(), "UTF-8");  
								}
								catch (Exception e)
								{  
									e.printStackTrace();
								}
								
								try
								{
									List ret = (List) JsonUtil.getObjectFromJson(myString);
									final String word = ret.get(0).toString();
									final List array = (List) ret.get(1);
									mHandler.post(new Runnable() {
											@Override
											public void run()
											{
												if (!word.equals(edittext.getText().toString())) return;
												//生成动态数组，加入数据
												listItem = new ArrayList<HashMap<String, Object>>();
												for (int i=0;i < array.size();i++)
												{
													HashMap<String, Object> map = new HashMap<String, Object>();
													map.put("ItemText", (String) array.get(i));
													listItem.add(map);
												}
												//生成适配器的Item和动态数组对应的元素
												SimpleAdapter listItemAdapter = new SimpleAdapter(that, listItem,//数据源 
																								  R.layout.search_display_style,//ListItem的XML实现
																								  //动态数组与ImageItem对应的子项        
																								  new String[] {"ItemText"}, 
																								  //ImageItem的XML文件里面的一个ImageView,两个TextView ID
																								  new int[] {R.id.ItemText}
																								  );

												//添加并且显示
												list.setAdapter(listItemAdapter);
											}
										});
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}
						}).start();
				}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
											  int after)
				{
				}
				@Override
				public void afterTextChanged(Editable s)
				{
				}
			});

        //添加点击
        list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
										long arg3)
				{
					String text = (String) listItem.get(arg2).get("ItemText");
					if (edittext.getText().toString().equals(text))
						ret();
					edittext.setText(text);
					edittext.setSelection(text.length());
				}
			});


    }

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);

	}
	
	private void ret()
	{
		String url = "http://m.moegirl.org/";
		try
		{
			url += URLEncoder.encode(edittext.getText().toString(), "utf-8");
		}
		catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//url += "?action=render";
		Intent intent = new Intent(Search.this, MainActivity.class);  
		intent.putExtra("url", url);
		setResult(RESULT_OK, intent);
		finish();
	}

}
