package com.xingchuang.haiguo.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {

	private static LocalBroadcastManager sLocalBroadcastManager;
	private static LocalReceiver sLocalReceiver;
	private IntentFilter mIntentFilter;
	private int count=0;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("scx", "onReceive()");
		Log.i("scx", "context=" + context);
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		Log.i("scx", "networkInfo=" + networkInfo);
		if (networkInfo != null && networkInfo.isAvailable()) {
			// Log.i("scx", "已判断网络连接成功");
			// Toast.makeText(context, "网络连接成功", Toast.LENGTH_SHORT).show();
		} else {

			Log.i("scx", "已判断网络连接失败");
			sLocalBroadcastManager=LocalBroadcastManager.getInstance(context);
			mIntentFilter = new IntentFilter();
			mIntentFilter.addAction("com.haiguo.newhaiguo.broadcast.LOCAL_BROADCAST");
			sLocalReceiver = new LocalReceiver();
			sLocalBroadcastManager.registerReceiver(sLocalReceiver, mIntentFilter);
			Intent intent1=new Intent("com.haiguo.newhaiguo.broadcast.LOCAL_BROADCAST");
			
			if(count==0){
				
				sLocalBroadcastManager.sendBroadcast(intent1);
				count++;
			}
//			Toast.makeText(context, "网络连接失败", Toast.LENGTH_SHORT).show();
		}
	}
	public static LocalReceiver getLocalReceiver()
	{
		return sLocalReceiver;
	}
	public static LocalBroadcastManager getLocalBroadcastManager(){
		return sLocalBroadcastManager;
	}
}
