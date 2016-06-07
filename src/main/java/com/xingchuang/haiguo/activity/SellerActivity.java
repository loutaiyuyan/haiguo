package com.xingchuang.haiguo.activity;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.xingchuang.haiguo.R;
import com.xingchuang.haiguo.volley.VolleyRequest;

public class SellerActivity extends Activity implements OnItemClickListener, OnRefreshListener {

	/**
	 * 下拉刷新对象
	 */
	private SwipeRefreshLayout mSwipeRefreshLayout;
	
	private ListView mListView;
	private Handler mHandler;
	private VolleyRequest mVolleyRequest;
	private SharedPreferences mSharedPreferences;
	private String mSchool;
	private String mTag;
	private TextView mTextSellerLocalTitle;
	private String mUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("scx", "onCreate()");
		setContentView(R.layout.inschool_activity);
		initUI();
		initRefresh();
	}

	public void initUI() {
		mHandler = new Handler();
		mSharedPreferences=getSharedPreferences("userInfo", MODE_PRIVATE);
		mTextSellerLocalTitle=(TextView) findViewById(R.id.sellerLocalTitle);
		mListView = (ListView) findViewById(R.id.lvInschool);
		mListView.setOnItemClickListener(this);
		mVolleyRequest = new VolleyRequest(SellerActivity.this, mHandler, mListView);
		mSchool=mSharedPreferences.getString("school", null);
		
		Intent intent=getIntent();
		mTag=intent.getStringExtra("tag");
		mUrl=intent.getStringExtra("url");
		Log.e("scx", "url="+mUrl);
		if(mTag.substring(0, 1).equals("i")){
			mSharedPreferences=getSharedPreferences("inSchoolSellerInfo", MODE_PRIVATE);
			mTextSellerLocalTitle.setText(mSchool);
		}else if(mTag.substring(0, 1).equals("o")){
			mSharedPreferences=getSharedPreferences("outSchoolSellerInfo", MODE_PRIVATE);
			mTextSellerLocalTitle.setText(mSchool+"的校外商家");
		}
		mVolleyRequest.handleGetJsonData(mSharedPreferences,mUrl, mTag);
		
	}
	
	private void initRefresh() {
		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
		// 闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归崚鐑芥晸閺傘倖瀚归弮鍫曟晸閺傘倖瀚归柨鐔告灮閹风兘鏁撻弬銈嗗闁跨喐鏋婚幏鐤闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归柨鐔告灮閹风兘鏁撻弬銈嗗闁跨喐鏋婚幏锟�4闁跨喐鏋婚幏锟�
		mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light,
				android.R.color.holo_orange_light, android.R.color.holo_green_light);
		mSwipeRefreshLayout.setOnRefreshListener(this);
	}

	/**
//	 * @param parent
	 * @param view
	 * @param position
	 * @param id
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (position) {
		case 0:
//			Intent intent = new Intent(this, PayDemoActivity.class);
//			intent.putExtra("sellerName", "閸犳粓鏁撻弬銈嗗闁跨噦鎷�");
//
//			startActivity(intent);
//			break;
		}
	}

	@Override
	public void onRefresh() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mVolleyRequest.handleGetJsonData(mSharedPreferences,mUrl, mTag);
				mSwipeRefreshLayout.setRefreshing(false);
			}
		}, 0);
	}
}
