package com.xingchuang.haiguo.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.VolleyError;
import com.xingchuang.haiguo.R;
import com.xingchuang.haiguo.util.TimeCount;
import com.xingchuang.haiguo.volley.VolleyInterface;
import com.xingchuang.haiguo.volley.VolleyRequest;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class CreateUserActivity extends Activity implements OnClickListener, OnItemSelectedListener {

	public static final String CREATE_ACCOUNT_URL = "http://192.168.253.4:8080/androidWeb/servlet/NewAccount";

//	private String APPKEY = "5887b8134af8";
	private String APPKEY = "d5f6b75591a3";
//	private String APPKEY = "22";
	// d5f6b75591a3
//	private String APPSECRET = "9cd46d6cf9908a297fbafeb75b7b19d3";
	private String APPSECRET = "551c92abb280bcb234ccc2363fd1111c";
	// 551c92abb280bcb234ccc2363fd1111c
	/**
	 * 电话号码
	 */
	private EditText mEditPhone;
	/**
	 * ????
	 */
	private EditText mEditPassword1;
	/**
	 * ????????????
	 */
	private EditText mEditPassword2;
	/**
	 *验证码编辑框
	 */
	private EditText mEditCode;
	/**
	 * ????????
	 */
	private Button mBtnGetIdentifyCode;
	/**
	 * ??
	 */
	private Button mBtnSubmit;
	/**
	 * ??????????
	 */
	private Button mBtnBack;

	/**
	 * ????????????????????У
	 */
	private String mPhone;
	private String mSchool;

	/**
	 * ?У?б?
	 */
	private List<String> mList = new ArrayList<String>();
	/**
	 * ??????
	 */
	private Spinner mSpinner;
	/**
	 * list????????
	 */
	private ArrayAdapter<String> mAdapter;
	/**
	 * ??????????
	 */
	private SharedPreferences mSharedPreferences;
	/**
	 * ???????????
	 */
	private VolleyRequest mVolleyRequest;

	private TimeCount mTimeCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.create_user_layout);
		SMSSDK.initSDK(CreateUserActivity.this, APPKEY, APPSECRET);
		initViews();
		mTimeCount=new TimeCount(0, 0, mBtnGetIdentifyCode);
		mVolleyRequest=new VolleyRequest();
		mSharedPreferences=getSharedPreferences("userInfo", MODE_PRIVATE);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			// Log.i("CreateUserActivity", "msg.what=" + msg.what);
			Log.i("scx", "CreateUserActivity mHandler handleMessage()");
			JSONObject json = (JSONObject) msg.obj;
			hanleCreateAccountResult(json);
		}
	};

	/**
	 *根据服务器返回的结果处理注册消息
	 * @param json
	 */
	private void hanleCreateAccountResult(JSONObject json) {
		/*
		 * result_code: 0 注册成功 1 用户名已存在 2 数据库操作异常
		 */
		// progress.cancel();
		int result;
		try {
			result = json.getInt("result_code");
		} catch (JSONException e) {
			Toast.makeText(this, "网络没有响应", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			return;
		}

		if (result == 1) {
			Toast.makeText(this, "用户名已存在", Toast.LENGTH_SHORT).show();
			return;
		}

		if (result == 2) {
			Toast.makeText(this, "数据库操作异常", Toast.LENGTH_SHORT).show();
			return;
		}

		if (result == 0) {
			Toast.makeText(this, "注册成功！", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(this, LoginActivity.class);
			intent.putExtra("phone", mEditPhone.getText().toString());
			startActivity(intent);
			finish();
			return;
		}

	};

	/**
	 * ??????????handler??????????????????
	 */
	private void SMSEventSendMessage() {
		EventHandler eh = new EventHandler() {

			@Override
			public void afterEvent(int event, int result, Object data) {

				Message msg = new Message();
				msg.arg1 = event;
				msg.arg2 = result;
				msg.obj = data;
				msg.what = 2;
				SmsHandler.sendMessage(msg);
				Log.i("scx", "CreateUserActivity SMSEventSendMessage()");
			}
		};
		SMSSDK.registerEventHandler(eh);
	}
	/**
	 * ?????????????ü??????
	 */

	private void initViews() {
		initList();
		mEditPhone = (EditText) findViewById(R.id.new_phone);
		mEditPassword1 = (EditText) findViewById(R.id.new_password_1);
		mEditPassword2 = (EditText) findViewById(R.id.new_password_2);
		mEditCode = (EditText) findViewById(R.id.id_et_verificationcode);
		mBtnGetIdentifyCode = (Button) findViewById(R.id.btn_getverificationcode);
		mBtnSubmit = (Button) findViewById(R.id.new_btn_submit);
		mBtnBack = (Button) findViewById(R.id.btn_BackToLogin);
		mSpinner = (Spinner) findViewById(R.id.spinner_school);
		// 用学校列表给spinner设置适配器
		mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mList);
		// 给spinner设置下拉列表的格式
		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 给spinner设置监听器
		mSpinner.setAdapter(mAdapter);
		mSpinner.setOnItemSelectedListener(this);
		mBtnSubmit.setOnClickListener(this);
		mBtnBack.setOnClickListener(this);
		mBtnGetIdentifyCode.setOnClickListener(this);
	}
	/**
	 *给spinner设置文字列表
	 */
	private void initList() {
		mList.add("南京工程学院");
		mList.add("南京晓庄学院");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.new_btn_submit:
			handleCreateAccount();
			break;
		case R.id.btn_getverificationcode:
			if ((mEditPhone.getText().toString()).length() == 11) {
				mTimeCount.start();
				SMSSDK.getVerificationCode("86", mEditPhone.getText().toString());
				mPhone = mEditPhone.getText().toString();
				SMSEventSendMessage();
			} else if ((mEditPhone.getText().toString()).length() != 11) {
				Toast.makeText(this, "电话号码错误", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "电话号码不能为空", Toast.LENGTH_SHORT).show();

			}
			break;
		case R.id.btn_BackToLogin:
			Intent intent = new Intent(CreateUserActivity.this, LoginActivity.class);
			if ((mEditPhone.getText().toString()).length() == 11) {
				mPhone = mEditPhone.getText().toString();
				intent.putExtra("phone", mPhone);
			}
			startActivity(intent);
			finish();
			break;
		}

	}
	/**
	 *点击提交按钮后
	 */

	private void handleCreateAccount() {
		Log.i("scx", "CreateUserActivity handleCreateAccount()");
		boolean isPhoneValid = checkPhone();
		if (!isPhoneValid) {
			Toast.makeText(this, "手机号码不正确，请重新输入", Toast.LENGTH_SHORT).show();
			return;
		}

		int pwdResult = checkPassword();
		if (pwdResult == 1) {
			Toast.makeText(this, "两次输入的密码不一致，请确认！", Toast.LENGTH_SHORT).show();
			return;
		}
		if (pwdResult == 2) {
			Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
			return;
		}

		if (TextUtils.isEmpty(mEditPhone.getText().toString())) {
			Toast.makeText(this, "请输入电话号码", Toast.LENGTH_SHORT).show();
			return;
		}

		if (TextUtils.isEmpty(mEditCode.getText().toString())) {
			Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
			return;
		}
		if (!TextUtils.isEmpty(mEditCode.getText().toString())) {
			// 提交验证码并向sms的handler发送消息
			SMSSDK.submitVerificationCode("86", mPhone, mEditCode.getText().toString());
			Log.i("scx", "CreateUserActivity submitVerificationCode");
		}
	}

	/**
	 * 向服务器请求创建用户
	 */
	private void createAccount() {
		String phone = mEditPhone.getText().toString();
		String password = mEditPassword1.getText().toString();
		mVolleyRequest.postCreateJsonData(CREATE_ACCOUNT_URL, "create",phone,password, mSchool,new VolleyInterface() {

			@Override
			public void onMySuccess(String result) {
				String jsonString = result.substring(result.indexOf("{"));
				Log.d("scx", "entity = " + jsonString);
				JSONObject json;
				try {
					json = new JSONObject(jsonString);
					sendJsonMessage(json);
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
	 *mob的handler
	 */
	private Handler SmsHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Log.i("scx", "CreateUserActivity SmsHandler handleMessage()");
			int event = msg.arg1;
			int result = msg.arg2;
			Object data = msg.obj;
			Log.i("scx", "CreateUserActivity event=" + event);
			if (result == SMSSDK.RESULT_COMPLETE) {
				// ??????????????MainActivity,???????????
				if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {// ?????????
					createAccount();
					Editor editor=mSharedPreferences.edit();
					editor.putString("phone", mEditPhone.getText().toString());
					editor.putString("password", mEditPassword1.getText().toString());
					editor.commit();
//					Toast.makeText(getApplicationContext(), "?????????", Toast.LENGTH_SHORT).show();
				} else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
					Toast.makeText(getApplicationContext(), "验证码已发送", Toast.LENGTH_SHORT).show();
				} else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {// ????????????????????б?
					Toast.makeText(getApplicationContext(), "获取国家列表成功", Toast.LENGTH_SHORT).show();
				}
			} else {
				((Throwable) data).printStackTrace();
				// int resId = getStringRes(CreateUserActivity.this,
				// "smssdk_network_error");
				Toast.makeText(CreateUserActivity.this, "短信发送失败", Toast.LENGTH_SHORT).show();
				// if (resId > 0) {
				// Toast.makeText(CreateUserActivity.this, resId,
				// Toast.LENGTH_SHORT).show();
				// }
			}
		}

	};

	/**
	 *检查手机号码输入是否正确
	 * @return
	 */
	private boolean checkPhone() {
		String phone = mEditPhone.getText().toString();
		if (phone.length() != 11) {
			return false;
		}
		return true;
	}

	private int checkPassword() {
		/*
		 * return value: 0 password valid 1 password not equal 2 inputs 2
		 * password empty
		 */
		String pwd1 = mEditPassword1.getText().toString();
		String pwd2 = mEditPassword2.getText().toString();
		if (!pwd1.equals(pwd2)) {
			return 1;
		} else if (TextUtils.isEmpty(pwd1)) {
			return 2;
		} else {
			return 0;
		}
	}
	/**
	 *给主线程发送消息
	 * @param obj
	 */

	private void sendJsonMessage(Object obj) {

		Log.i("scx", "CreateUserActivity sendJsonMessage()");
		Message msg = Message.obtain();
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}

	/**
	 * activity进入销毁生命周期的时候解除mob的注册
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		SMSSDK.unregisterAllEventHandler();
	}

	/**
	 * Spinner学校选择的监听
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Editor editor=mSharedPreferences.edit();
		mSchool=mList.get(position);
		editor.putString("school", mList.get(position));
		editor.commit();
		parent.setVisibility(View.VISIBLE);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		parent.setVisibility(View.VISIBLE);
	}
}
