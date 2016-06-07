package com.xingchuang.haiguo.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class LocalReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("scx", "LocalReceiver  onReceive()");
		Log.i("scx", "网络连接错误");
		Toast.makeText(context, "网络连接错误", Toast.LENGTH_SHORT).show();
	}

}
