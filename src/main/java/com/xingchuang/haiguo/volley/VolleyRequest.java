package com.xingchuang.haiguo.volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.xingchuang.haiguo.R;
import com.xingchuang.haiguo.adapter.NewsAdapter;
import com.xingchuang.haiguo.adapter.NewsBean;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class VolleyRequest {

	/**
	 * 请求对象
	 */
	public StringRequest mStringRequest;
	/**
	 * 封装listView每个item的图片地址,标题,内容的对象列表
	 */
	private List<NewsBean> newsBeans;
	/**
	 * 上下文
	 */
	private Context context;
	/**
	 * 需要设置适配器的listView
	 */
	private ListView listView;
	/**
	 * UI线程的handler
	 */
	private Handler mHandler;
	/**
	 * 用于保存缓存中item内容的个数
	 */
	private int newsBeanNum;
	/**
	 * listView适配器对象
	 */
	protected NewsAdapter newsAdapter;
	
	/**
	 * 无参构造方法
	 */
	public VolleyRequest() {
	}
	/**
	 * 设置listView适配器的构造方法
	 * @param context
	 * @param handler
	 * @param listView
	 */
	public VolleyRequest(Context context, Handler handler, ListView listView) {
		this.context = context;
		this.listView = listView;
		this.mHandler = handler;
	}
	/**
	 * 请求获取服务器返回的json数据
	 * 
	 */
	private void getJsonData(String url,String tag,VolleyInterface vif){
		MyApplication.getHttpQueues().cancelAll(tag);
		mStringRequest=new StringRequest(Method.GET, url, vif.successListener(), vif.errorListener());
		mStringRequest.setTag(tag);
		MyApplication.getHttpQueues().add(mStringRequest);
		MyApplication.getHttpQueues().start();
	}
	
	/**
	 * 以POST方式向服务器发送请求登录数据
	 */
	//JsonObjectRequest不能重写getParams来传参
	public void postLoginJsonData(String url,String tag,final String phone,final String password,VolleyInterface vif){
		MyApplication.getHttpQueues().cancelAll(tag);
		Map<String, String> map=new HashMap<String, String>();
		map.put("phone", phone);  
        map.put("password", password); 
		mStringRequest=new StringRequest(Method.POST, url, vif.successListener(), vif.errorListener()){
				@Override
				protected Map<String, String> getParams() throws AuthFailureError {
					Map<String, String> map=new HashMap<String, String>();
					map.put("phone", phone);  
		            map.put("password", password); 
		            Log.i("info", map.toString());
					return map;
			}
		};
		mStringRequest.setTag(tag);
		MyApplication.getHttpQueues().add(mStringRequest);
		MyApplication.getHttpQueues().start();
	}
	/**
	 * 以POST方式向服务器请求创建一个新的账户
	 * @param url
	 * @param tag
	 * @param phone
	 * @param password
	 * @param school
	 * @param vif
	 */
	public void postCreateJsonData(String url,String tag,final String phone,final String password,final String school,VolleyInterface vif){
		MyApplication.getHttpQueues().cancelAll(tag);
		mStringRequest=new StringRequest(Method.POST, url, vif.successListener(), vif.errorListener()){
				@Override
				protected Map<String, String> getParams() throws AuthFailureError {
					Map<String, String> map=new HashMap<String, String>();
					map.put("phone", phone);  
		            map.put("password", password); 
		            map.put("school", school); 
					return map;
			}
		};
		mStringRequest.setTag(tag);
		MyApplication.getHttpQueues().add(mStringRequest);
		MyApplication.getHttpQueues().start();
	}
	/**
	 * 向服务器请求修改学校信息
	 */
	public void postChooseSchoolJsonData(String url,String tag,final String whoseRequest,final String phone,final String school,VolleyInterface vif){
		MyApplication.getHttpQueues().cancelAll(tag);
		mStringRequest=new StringRequest(Method.POST, url, vif.successListener(), vif.errorListener()){
				@Override
				protected Map<String, String> getParams() throws AuthFailureError {
					Map<String, String> map=new HashMap<String, String>();
					map.put("whoseRequest", whoseRequest);
					map.put("phone", phone);
		            map.put("school", school); 
					return map;
			}
		};
		mStringRequest.setTag(tag);
		MyApplication.getHttpQueues().add(mStringRequest);
		MyApplication.getHttpQueues().start();
	}
	/**
	 * 给imageView设置对应的url图片
	 */
	public void showImageByVolley(ImageView iv,String url) {
		ImageLoader imageLoader=new ImageLoader(MyApplication.getHttpQueues(), new BitmapCache());
		ImageListener listener=ImageLoader.getImageListener(iv, R.mipmap.ic_launcher, R.mipmap.ic_launcher);
		imageLoader.get(url, listener);
	}
	/**
	 * 以GET方式向服务器请求并处理服务器返回的结果(给listView设置适配器)
	 * @param preferences
	 * @param url
	 * @param tag
	 */
	public void handleGetJsonData(final SharedPreferences preferences,final String url,String tag){
		
		newsBeanNum=preferences.getInt("newsBeanNum", 1);
		newsBeans = getDataFromCache(preferences);
		if(newsBeans==null){
			Log.i("scx", "newsBeans="+newsBeans);
			getJsonData(url, tag, new VolleyInterface() {
				
				@Override
				public void onMySuccess(String result) {
					newsBeans=addJsonDataToNewsBean(preferences,result);
					newsAdapter = new NewsAdapter(context, newsBeans);
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							listView.setAdapter(newsAdapter);
						}
					});
				}
				@Override
				public void onMyError(VolleyError error) {
					Toast.makeText(context, "连接服务器失败", Toast.LENGTH_SHORT);
				}
			});
		}else{
			newsAdapter = new NewsAdapter(context, newsBeans);
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					listView.setAdapter(newsAdapter);
				}
			});
		}
	}
	/**
	 * 从缓存中获取每个item的内容
	 * @param preferences
	 * @return
	 */
	private List<NewsBean> getDataFromCache(SharedPreferences preferences) {
		Log.e("scx", "getDataFromCache()");
		List<NewsBean> newsBeanList = new ArrayList<NewsBean>();

		for (int i = 0; i < newsBeanNum; i++) {
			// 此处用log
			NewsBean newsBean = new NewsBean();
			newsBean.newsIconUrl = preferences.getString("imageUrl" + i, null);
			newsBean.newsTitle = preferences.getString("title" + i, null);
			newsBean.newsContent = preferences.getString("content" + i, null);
			newsBeanList.add(newsBean);
			Log.e("scx", "newsBean.newsIconUrl=" + newsBean.newsIconUrl);
			Log.e("scx", "newsBean.newsTitle=" + newsBean.newsTitle);
			Log.e("scx", "newsBean.newsContent=" + newsBean.newsContent);
			if (newsBean.newsIconUrl == null || newsBean.newsTitle == null || newsBean.newsContent == null) {
				return null;
			}
		}
		return newsBeanList;
	}
	/**
	 * 将服务器返回的item数据放入NewsBean中，并保存在sharedPreferences中
	 * @param preferences
	 * @param jsonString
	 * @return
	 */
	private List<NewsBean> addJsonDataToNewsBean(SharedPreferences preferences, String jsonString) {
		List<NewsBean> newsBeanList = new ArrayList<NewsBean>();
//		jsonString=jsonString.substring(jsonString.indexOf("re")-2,jsonString.lastIndexOf("]"));
		// json对象需要传入来自网页的json格式的字符串}
		try {
			Log.i("scx", "jsonString="+jsonString);
			JSONObject jsonObject;
			NewsBean newsBean = null;
			jsonObject = new JSONObject(jsonString);
			JSONArray jsonArray = jsonObject.getJSONArray("dataList");
			// 遍历
			for (int i = 0; i < jsonArray.length(); i++) {
				// 每次都要新建一个newsBean用于装入newsBeanList
				newsBean = new NewsBean();
				// JsonArray中每一个元素都是jsonObject,现在用遍历把每一个数组元素即jsonObject取出来
				jsonObject = jsonArray.getJSONObject(i);
				newsBean.newsIconUrl = jsonObject.getString("pic");
				newsBean.newsTitle = jsonObject.getString("title");
				newsBean.newsContent = jsonObject.getString("content");
				newsBeanList.add(newsBean);
				Editor editor = preferences.edit();
				editor.putString("imageUrl" + i, newsBean.newsIconUrl);
				editor.putString("title" + i, newsBean.newsTitle);
				editor.putString("content" + i, newsBean.newsContent);
				editor.commit();
			}
			Editor editor = preferences.edit();
			editor.putInt("newsBeanNum",  jsonArray.length());
//			editor.putInt("newsBeanNum",  1);
			editor.commit();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return newsBeanList;
	}

}
