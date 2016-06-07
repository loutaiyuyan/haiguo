package com.xingchuang.haiguo.activity;

import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.xingchuang.haiguo.R;
import com.xingchuang.haiguo.volley.VolleyInterface;
import com.xingchuang.haiguo.volley.VolleyRequest;

public class ChooseSchoolActivity extends Activity implements OnClickListener {

	/**
	 * 南京工程学院
	 */
	private Button mBtnGongCheng;
	/**
	 * 南京晓庄学院
	 */
	private Button mBtnXiaoZhuang;
	/**
	 * 用于判断是否需要更新学校数据,true为需要更新
	 */
	private boolean mFlag;
	/**
	 * 用户信息preferences
	 */
	private SharedPreferences mSharedPreferences;
	/**
	 * 商户信息preference
	 */
	private SharedPreferences mSellerPreferences;
	/**
	 * 网络请求对象
	 */
	private VolleyRequest mVolleyRequest;
	/**
	 * 选择学校的服务器
	 */
	public static  String CHOOSE_SCHOOL_URL = "http://192.168.1.105:8080/androidWeb/servlet/FindPassword";
	/**
	 * 选择学校的tag
	 */
	private static final String CHOOSE_SCHOOL = "chooseSchool";
	/**
	 * 用于保存用户手机号码
	 */
	private String mPhone;
	/**
	 * 用于获取用户选择之前学校名称
	 */
	private String mOldSchool;
	/**
	 * 用于获取用户后来选择的学校名称
	 */
	private String mNewSchool;
	/**
	 * 处理服务器返回的选择学校的消息
	 */
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			// Log.i("FindPasswordActivity", "msg.what=" + msg.what);
			Log.i("scx", "FindPasswordActivity mHandler handleMessage()");
			JSONObject json = (JSONObject) msg.obj;
			hanleUpdateAccountResult(json);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_school_layout);
		Log.i("scx", "SecondActivity oncreate");
		initWeights();
		mVolleyRequest = new VolleyRequest();
		mSharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
		mSellerPreferences=getSharedPreferences("sellerInfo", MODE_PRIVATE);
		mOldSchool=mSharedPreferences.getString("school", null);
	}

	/**
	 * 初始化控件
	 */
	private void initWeights() {
		mBtnGongCheng = (Button) findViewById(R.id.btn1);
		mBtnXiaoZhuang = (Button) findViewById(R.id.btn2);
		mBtnGongCheng.setOnClickListener(this);
		mBtnXiaoZhuang.setOnClickListener(this);
	}

	/**
	 * button的监听事件
	 */
	@Override
	public void onClick(View v) {
		Editor editor = mSharedPreferences.edit();
		
		switch (v.getId()) {
		case R.id.btn1:
			editor.putString("school", "南京工程学院");
			editor.commit();
			mPhone=mSharedPreferences.getString("phone", null);
			mNewSchool=mSharedPreferences.getString("school", null);
			updateAccount();
			break;

		case R.id.btn2:
			editor.putString("school", "南京晓庄学院");
			editor.commit();
			mPhone=mSharedPreferences.getString("phone", null);
			mNewSchool=mSharedPreferences.getString("school", null);
			updateAccount();
			break;
		}
	}

	/**
	 * 处理服务器返回的修改学校结果
	 * @param json json格式的数据
	 */
	private void hanleUpdateAccountResult(JSONObject json) {
		/*
		 * result_code: 0 修改学校成功 1 用户名已存在 2 数据库操作异常
		 */
		// progress.cancel();
		int result;
		try {
			result = json.getInt("result_code");
		} catch (JSONException e) {
			Toast.makeText(this, "没有获取到网络的响应！", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			return;
		}

		if (result == 2) {
			Toast.makeText(this, "修改失败！服务端出现异常！", Toast.LENGTH_SHORT).show();
			return;
		}

		if (result == 0) {
			Toast.makeText(this, "修改学校成功！", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra("school", mSharedPreferences.getString("school", null));
			intent.putExtra("flag", mFlag);
			Log.i("scx", "flag="+mFlag);
			startActivity(intent);
			finish();
			return;
		}
	};
	/**
	 * 如果现在选择的学校跟原来一样的话不更新数据,不一样则更新数据
	 */
	private void changeListData(){
		if(!mOldSchool.equals(mNewSchool)){
			Log.e("scx", "选择的学校跟原来不一样,更新数据");
			Editor editor2=mSellerPreferences.edit();
			editor2.clear();
			editor2.commit();
			mFlag=true;
		}else{
			Log.e("scx", "选择的学校跟原来一样,不更新数据");
			mFlag=false;
		}
	}
	/**
	 * 向服务器请求修改学校
	 */
	private void updateAccount() {
		
		mVolleyRequest.postChooseSchoolJsonData(CHOOSE_SCHOOL_URL, CHOOSE_SCHOOL, CHOOSE_SCHOOL,mPhone,mNewSchool, new VolleyInterface() {

			@Override
			public void onMySuccess(String result) {
				changeListData();
				String jsonString = result.substring(result.indexOf("{"));
				Log.d("scx", "entity = " + jsonString);
				JSONObject json;
				try {
					json = new JSONObject(jsonString);
					sendJsonMessage( json);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onMyError(VolleyError error) {

			}
		});

	}
	/**
	 * 发送服务器返回的数据信息给handler
	 * @param obj
	 */

	private void sendJsonMessage(Object obj) {

		Log.i("scx", "FindPasswordActivity sendJsonMessage()");
		Message msg = Message.obtain();
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}

}
