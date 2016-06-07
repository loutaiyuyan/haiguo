package com.xingchuang.haiguo.activity;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.VolleyError;
import com.xingchuang.haiguo.R;
import com.xingchuang.haiguo.volley.VolleyInterface;
import com.xingchuang.haiguo.volley.VolleyRequest;

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
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {
    private EditText mEditTextLoginPhone;
    private EditText mEditTextLoginPassword;
    private Button mBtnLogin;
    private Button mBtnCreate;
    private Button mBtnFindPassword;
    private VolleyRequest mVolleyRequest;
    
    private SharedPreferences mSharedPreferences;
    
    public static final int MSG_LOGIN_RESULT = 0;
    
    public String mServerUrl = "http://192.168.253.4:8080/androidWeb/servlet/loadMessage";
    
    private Handler mHandler = new Handler() {
    	
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    		case MSG_LOGIN_RESULT:
    			JSONObject json = (JSONObject) msg.obj;
    			handleLoginResult(json);
    			break;
    		}
    	};
    };
	
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_activity_layout);
        Log.i("LoginActivity", "onCreate");
        mVolleyRequest=new VolleyRequest();
        mSharedPreferences=getSharedPreferences("userInfo", MODE_PRIVATE);
        initViews();
        String phone=mSharedPreferences.getString("phone", null);
		if(phone!=null){
		mEditTextLoginPhone.setText(phone);
		}
    }
	private void initViews() {
		mEditTextLoginPhone = (EditText)findViewById(R.id.id_et_phone);
		mEditTextLoginPassword = (EditText)findViewById(R.id.id_et_password);
		mBtnLogin   = (Button)findViewById(R.id.id_btn_login);
		mBtnFindPassword=(Button) findViewById(R.id.id_btn_forgetpassword);
		mBtnCreate  = (Button)findViewById(R.id.id_btn_create);
		
		mBtnLogin.setOnClickListener(this);
		mBtnFindPassword.setOnClickListener(this);
		mBtnCreate.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.id_btn_login:
			handleLogin();
			break;
		case R.id.id_btn_create:
			handleCreateCount();
			break;
		case R.id.id_btn_forgetpassword:
			Intent intent=new Intent(this,FindPasswordActivity.class);
			startActivity(intent);
			break;	
		}
		
	}
	private void handleLogin() {
		final String phone = mEditTextLoginPhone.getText().toString();
		final String password = mEditTextLoginPassword.getText().toString();
		mVolleyRequest.postLoginJsonData(mServerUrl, "login",phone,password, new VolleyInterface() {
			
			@Override
			public void onMySuccess(String result) {
				Log.i("scx", "result="+result);
				String jsonString = result.substring(result.indexOf("{"));
				JSONObject json;
				try {
					json = new JSONObject(jsonString);
					sendMessage(MSG_LOGIN_RESULT, json);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			@Override
			public void onMyError(VolleyError error) {
				//NoConnectionError是因为客户端没有网络连接
				Log.i("scx", "请求出错");
				Log.e("scx", error.toString());
			}
		});


	}
	
	private void handleCreateCount() {
		Intent intent = new Intent(this, CreateUserActivity.class);
		startActivity(intent);
	}
	
	private void handleLoginResult(JSONObject json){
		/*
		 * login_result:
		 * -1：登陆失败，未知错误！
		 * 0: 登陆成功！
		 * 1：登陆失败，用户名或密码错误！
		 * 2：登陆失败，用户名不存在！
		 * */
		int resultCode = -1;
		try {
			resultCode = json.getInt("result_code");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		switch(resultCode) {
		case 0:
			onLoginSuccess(json);
			break;
		case 1:
			Toast.makeText(this, "用户名或密码错误！", Toast.LENGTH_LONG).show();
			break;
		case 2:
			Toast.makeText(this, "用户名不存在！", Toast.LENGTH_LONG).show();
			break;
		case -1:
			Toast.makeText(this, "登陆失败！未知错误！", Toast.LENGTH_LONG).show();
			break;
		}
	}
	
	private void onLoginSuccess(JSONObject json) {
		Intent intent = new Intent(this, MainActivity.class);
		try {
			intent.putExtra("phone", json.getString("phone"));
			Editor editor=mSharedPreferences.edit();
			editor.putString("phone",  json.getString("phone"));
			editor.putString("school", json.getString("school"));
			Log.e("scx", "json.getSchool="+json.getString("school"));
			editor.commit();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Log.i("scx", "LoginActivity intent="+intent);
		startActivity(intent);
		this.finish();
	}
	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
	@Override
	protected void onResume() {
		super.onResume();
		Intent intent=getIntent();
		String phone=intent.getStringExtra("phone");
		if(phone!=null){
		mEditTextLoginPhone.setText(phone);
		}
		Log.i("LoginActivity", "onResume");
	}
}