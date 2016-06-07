package com.xingchuang.haiguo.volley;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

public abstract class VolleyInterface {
	
	private Listener<String> mListener;
	private ErrorListener mErrorListener;

	public Listener<String> successListener(){
		mListener=new Listener<String>() {

			@Override
			public void onResponse(String arg0) {
				onMySuccess(arg0);
			}
		};
		return mListener;
	}
	public ErrorListener errorListener(){
		mErrorListener=new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError arg0) {
				onMyError(arg0);
			}
		};
		return mErrorListener;
	}
	/**
	 * 网络请求失败
	 * @param error
	 */
	public abstract void onMyError(VolleyError error);
	/**
	 * 网络请求成功
	 * @param result
	 */
	public abstract void onMySuccess(String result);
}
