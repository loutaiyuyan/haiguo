package com.xingchuang.haiguo.volley;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
	
	public static RequestQueue sQueues;
	/**
	 * 全局对象，但是dialog不能用此context
	 */
	private static Context sContext;
	@Override
	public void onCreate() {
		super.onCreate();
		sContext=getApplicationContext();
		sQueues=Volley.newRequestQueue(getApplicationContext());
	}
	public static RequestQueue getHttpQueues(){
		return sQueues;
	}
	
	public static Context getContext() {
		return sContext;
	}
}
