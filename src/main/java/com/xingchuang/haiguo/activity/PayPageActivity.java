package com.xingchuang.haiguo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.xingchuang.haiguo.R;

public class PayPageActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.paypage_activity);
	}
}
