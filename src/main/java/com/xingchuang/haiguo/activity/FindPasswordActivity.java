package com.xingchuang.haiguo.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.xingchuang.haiguo.R;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class FindPasswordActivity extends Activity implements OnClickListener {

	/**
	 * 电话号码编辑框
	 */
	private EditText mEditPhone;
	/**
	 * 重置密码编辑框
	 */
	private EditText mEditPassword;
	/**
	 * 验证码编辑框
	 */
	private EditText mEditCode;
	/**
	 * 获取验证码按钮
	 */
	private Button mBtnGetCode;
	/**
	 * 提交按钮
	 */
	private Button mBtnSubmit;
	/**
	 * 找回密码服务器地址
	 */
	public static final String FIND_ACCOUNT_URL = "http://192.168.253.4:8080/androidWeb/servlet/FindPassword";
	/**
	 * 向服务器提交的验证请求，以判断是找回密码还是修改学校的名称
	 */
	public static final String FIND_ACCOUNT="findAccount";

	/**
	 * 短信验证的key
	 */
	private static final String APPKEY = "5887b8134af8";
	// d5f6b75591a3
	/**
	 * 短信验证的secret
	 */
	private static final String APPSECRET = "9cd46d6cf9908a297fbafeb75b7b19d3";
	// 551c92abb280bcb234ccc2363fd1111c

	/**
	 * 用于注册时候保存手机号码
	 */
	public String mPhone;
	/**
	 * 用于注册时候保存学校名称
	 */
	private String mSchool;

	/**
	 * 保存用户信息的存储器
	 */
	private SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.find_password);
		SMSSDK.initSDK(FindPasswordActivity.this, APPKEY, APPSECRET);
		initViews();
		mSharedPreferences=getSharedPreferences("userInfo", MODE_PRIVATE);
	}

	/**
	 * 处理找回密码请求的结果	
	 */

private Handler mHandler = new Handler() {
	public void handleMessage(Message msg) {
		// Log.i("mEditPasswordActivity", "msg.what=" + msg.what);
		Log.i("scx", "mEditPasswordActivity mHandler handleMessage()");
		// progress.dismiss();
		JSONObject json = (JSONObject) msg.obj;
		hanleUpdateAccountResult(json);
	}
};
/**
 * 处理服务器返回的结果
 * @param json 服务器返回的json数据
 */
private void hanleUpdateAccountResult(JSONObject json) {
	/*
	 * result_code: 0 找回密码成功 2 数据库操作异常
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
		Toast.makeText(this, "找回失败！服务端出现异常！", Toast.LENGTH_SHORT).show();
		return;
	}

	if (result == 0) {
		Toast.makeText(this, "找回密码成功！", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra("phone", mEditPhone.getText().toString());
		startActivity(intent);
		finish();
		return;
	}
};
/**
 * 短信验证功能向SmsHandler发送信息
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
			Log.i("scx", "mEditPasswordActivity SMSEventSendMessage()");
		}
	};
	SMSSDK.registerEventHandler(eh);
}
/**
 * 初始化控件
 */
private void initViews() {
	mEditPhone = (EditText) findViewById(R.id.find_phone);
	mEditPassword = (EditText) findViewById(R.id.find_password);
	mEditCode = (EditText) findViewById(R.id.find_verificationcode);
	mBtnGetCode = (Button) findViewById(R.id.find_getverificationcode);
	mBtnSubmit = (Button) findViewById(R.id.find_submit);
	mBtnGetCode.setOnClickListener(this);
	mBtnSubmit.setOnClickListener(this);
}


@Override
public void onClick(View v) {
	switch (v.getId()) {
	case R.id.find_submit:
		handleUpdateAccount();
		break;
	case R.id.find_getverificationcode:
		if ((mEditPhone.getText().toString()).length() == 11) {
			SMSSDK.getVerificationCode("86", mEditPhone.getText().toString());
			mPhone = mEditPhone.getText().toString();
			SMSEventSendMessage();
		} else if ((mEditPhone.getText().toString()).length() != 11) {
			Toast.makeText(this, "电话号码错误", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "电话不能为空", Toast.LENGTH_SHORT).show();
		}
		break;
	}

}

public void handleUpdateAccount() {
	Log.i("scx", "mEditPasswordActivity handleCreateAccount()");
	boolean isPhoneValid = checkPhone();
	if (!isPhoneValid) {
		Toast.makeText(this, "手机号码不正确，请重新输入", Toast.LENGTH_SHORT).show();
		return;
	}

	if (TextUtils.isEmpty(mEditPhone.getText().toString())) {
		Toast.makeText(this, "请输入电话号码！", Toast.LENGTH_SHORT).show();
		return;
	}

	if (TextUtils.isEmpty(mEditCode.getText().toString())) {
		Toast.makeText(this, "验证码不能为空", Toast.LENGTH_SHORT).show();
		return;
	}
	if (!TextUtils.isEmpty(mEditCode.getText().toString())) {
		// 提交验证码的时候会自动启动
		SMSSDK.submitVerificationCode("86", mPhone, mEditCode.getText().toString());
		Log.i("scx", "mEditPasswordActivity submitVerificationCode");
	}
}

/**
 *  更新用户账户密码
 */
private void updateAccount() {
	Log.i("scx", "mEditPasswordActivity createAccount()");
	new Thread(new Runnable() {
		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			Log.i("scx", "mEditPasswordActivity Start Network!");
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(FIND_ACCOUNT_URL);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("whoseRequest",FIND_ACCOUNT));
			params.add(new BasicNameValuePair("phone", mEditPhone.getText().toString()));
			params.add(new BasicNameValuePair("password", mEditPassword.getText().toString()));
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				HttpResponse httpResponse = httpClient.execute(httpPost);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					Log.i("scx", "mEditPasswordActivity Network OK!");
					HttpEntity entity = httpResponse.getEntity();
					String entityStr = EntityUtils.toString(entity);
					Log.e("scx", "entityStr="+entityStr);
					String jsonStr = entityStr.substring(entityStr.indexOf("{"));
					JSONObject json = new JSONObject(jsonStr);
					sendJsonMessage(json);

				} else {
					Log.i("scx", "mEditPasswordActivity Network Failed!");
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}).start();
}
/**
 * 处理mob发送过来的消息
 */
private Handler SmsHandler = new Handler() {

	@Override
	public void handleMessage(Message msg) {
		Log.i("scx", "mEditPasswordActivity SmsHandler handleMessage()");
		int event = msg.arg1;
		int result = msg.arg2;
		Object data = msg.obj;
		Log.i("scx", "mEditPasswordActivity event=" + event);
		if (result == SMSSDK.RESULT_COMPLETE) {
			// 短信注册成功后，返回MainActivity,然后提示新好友
			if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {// 提交验证码成功
				updateAccount();
				Editor editor=mSharedPreferences.edit();
				editor.putString("phone", mEditPhone.getText().toString());
				editor.putString("password", mEditPassword.getText().toString());
				editor.commit();
//				Toast.makeText(getApplicationContext(), "提交验证码成功", Toast.LENGTH_SHORT).show();
			} else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
				Toast.makeText(getApplicationContext(), "验证码已经发送", Toast.LENGTH_SHORT).show();
			} else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {// 返回支持发送验证码的国家列表
				Toast.makeText(getApplicationContext(), "获取国家列表成功", Toast.LENGTH_SHORT).show();
			}
		} else {
			((Throwable) data).printStackTrace();
			Toast.makeText(FindPasswordActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
		}
	}
};

/**
 * 检查手机号码是否规范
 * @return
 */
private boolean checkPhone() {
	String phone = mEditPhone.getText().toString();
	if (phone.length() != 11) {
		return false;
	}
	return true;
}
/**
 * 向主线程发送服务器返回的json格式数据结果
 * @param obj
 */
private void sendJsonMessage(Object obj) {

	Log.i("scx", "mEditPasswordActivity sendJsonMessage()");
	Message msg = Message.obtain();
	msg.obj = obj;
	mHandler.sendMessage(msg);
}

@Override
protected void onDestroy() {
	super.onDestroy();
	SMSSDK.unregisterAllEventHandler();
}
	
}
