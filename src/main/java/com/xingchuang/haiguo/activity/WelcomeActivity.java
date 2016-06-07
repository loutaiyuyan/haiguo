package com.xingchuang.haiguo.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import com.xingchuang.haiguo.R;

public class WelcomeActivity extends Activity {

	private static final int TIME=0;
	private static final int GO_HOME=1000;
	private static final int GO_LOGIN=1001;
	public static boolean isLogined;

	@SuppressLint("HandlerLeak")
	private Handler mHandler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			//不应该在主线程当中沉睡
			switch (msg.what) {
			case GO_HOME:
				goHome();
				break;

			case GO_LOGIN:
				goLogin();
				break;
			}
				
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.welcome);
		init();
	}
	private void init() {
		SharedPreferences mSharedPreferences = getSharedPreferences("judgeLogin", MODE_PRIVATE);
		isLogined= mSharedPreferences.getBoolean("isLogined", false);
		if(isLogined){
			mHandler.sendEmptyMessageDelayed(GO_HOME, TIME);
		}else {
			mHandler.sendEmptyMessageDelayed(GO_LOGIN, TIME);
		}
	}
	private void goHome() {
		Intent intent=new Intent(this,MainActivity.class);
		startActivity(intent);
		finish();
	}
	private void goLogin(){
		Intent intent=new Intent(this,LoginActivity.class);
		startActivity(intent);
		finish();
	}
}
