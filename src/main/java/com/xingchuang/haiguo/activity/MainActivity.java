package com.xingchuang.haiguo.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xingchuang.haiguo.R;
import com.xingchuang.haiguo.broadcast.NetworkChangeReceiver;
import com.xingchuang.haiguo.chooseicon.ChooseIconActivity;
import com.xingchuang.haiguo.chooseicon.ImageLoader;
import com.xingchuang.haiguo.sliddingmenu.SliddingMenu;
import com.xingchuang.haiguo.volley.MyApplication;
import com.xingchuang.haiguo.volley.VolleyRequest;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    /**
     * 南京工程学院校内商家列表url
     */
    private static final String IN_GONGCHENG_URL = "http://192.168.1.105:8080/androidWeb/servlet/ShowItemServLet";
    /**
     * 南京工程学院校外商家列表url
     */
    private static final String OUT_GONGCHENG_URL = "http://192.168.1.105:8080/androidWeb/servlet/JsonServlet";
    /**
     * 南京晓庄学院校内商家列表url
     */
    private static final String IN_XIAOZHUANG_URL = "http://192.168.1.105:8080/androidWeb/servlet/JsonServlet";
    /**
     * 南京晓庄学院校外商家列表url
     */
    private static final String OUT_XIAOZHUANG_URL = "http://192.168.1.105:8080/androidWeb/servlet/ShowItemServLet";
    /**
     * ActivityForResult的请求码
     */
    private static final int REQ_CHOOSE_ICON = 0;
    private static final int REQ_START_CAMERA = 1;
    /**
     * 猜你喜欢的工程商家列表url
     */
    private String mGongChengUrl = "http://192.168.253.4:8080/androidWeb/servlet/LikeListServlet";
    /**
     * 猜你喜欢的晓庄商家列表url
     */
    private String mXiaoZhuangUrl = "http://192.168.253.4:8080/androidWeb/servlet/LikeListServlet";

    /**
     * 下拉刷新实现对象
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * 自定义侧滑菜单对象
     */
    private SliddingMenu mSliddingMenu;
    /**
     * 侧滑的左边菜单
     */
    private View mViewLeftMenu;
    /**
     * 侧滑的右边页面
     */
    private View mViewRightPage;
    /**
     * 用于判断再按一次退出程序
     */
    private long mExitTime = 0;
    /**
     * 存储判断是否是第一次登陆flag的共享存储器对象
     */
    private SharedPreferences mPreJudgeLogin;
    /**
     * 用于存储用户的号码密码学校头像url
     */
    private SharedPreferences mPreUserInfo;

    /**
     * 存储用户喜欢的商家列表
     */
    private SharedPreferences mPreLikeInfo;

    /**
     * 显示号码的TextView
     */
    private TextView mTvPhone;
    private ImageButton mImgBtnCamera;
    /**
     * 账户按钮
     */
    private Button mBtnAccount;
    /**
     * 我的卡券按钮
     */
    private Button mBtnCard;
    /**
     *
     */
    private Button mBtnSchool;
    /**
     *
     */
    private Button mBtnExitLogin;
    /**
     *
     */
    private ListView mListView;
    /**
     *
     */
    private String mPhone;
    /**
     *
     */
    private String mSchool;
    /**
     *
     */
    private Handler mHandler;
    /**
     *
     */
    private VolleyRequest mVolleyRequest;

    private ImageView mImgViewUserIcon;
    private boolean mFlag;
    private NetworkChangeReceiver mNetworkChangeReceiver;
    private String mFilePath;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();
        mContext = MyApplication.getContext();
        /**
         * 锟斤拷始锟斤拷锟截硷拷
         */
        initViews();
        /**
         * 锟斤拷锟斤拷listView锟斤拷锟斤拷,tag为first
         */
        mVolleyRequest = new VolleyRequest(mContext, mHandler, mListView);
        mVolleyRequest.handleGetJsonData(mPreLikeInfo, mGongChengUrl, "first");
        initRefresh();
    }

    /**
     * 锟斤拷始锟斤拷锟斤拷锟斤拷刷锟斤拷
     */
    private void initRefresh() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        // 锟斤拷锟斤拷刷锟斤拷时锟斤拷锟斤拷锟斤拷锟斤拷色锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷4锟斤拷
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light,
                android.R.color.holo_orange_light, android.R.color.holo_green_light);
        mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (!isNetworkAvailable(mContext)) {
                    Toast.makeText(mContext, "network is un available", Toast.LENGTH_SHORT).show();
                    mSwipeRefreshLayout.setRefreshing(false);
                    return;
                }
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mVolleyRequest.handleGetJsonData(mPreLikeInfo, mXiaoZhuangUrl, "first");
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 0);
            }
        });
    }

    /**
     * 锟斤拷示锟斤拷锟界不锟斤拷锟斤拷
     *
     * @param context
     */
    private void toastNetworkUnavailable(Context context) {
        if (!isNetworkAvailable(context)) {
            Log.i("scx", "network is unavailable");
            Toast.makeText(context, "network is unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 锟叫讹拷锟斤拷锟斤拷锟角凤拷锟斤拷锟�
     *
     * @param context
     * @return
     */
    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
        } else {
            //锟斤拷锟斤拷锟叫讹拷锟角凤拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
//        	cm.getActiveNetworkInfo().isAvailable();  
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 锟斤拷始锟斤拷锟截硷拷
     */
    private void initViews() {

        mSliddingMenu = (SliddingMenu) findViewById(R.id.sliding);
        mViewLeftMenu = findViewById(R.id.leftMenu);
        mViewRightPage = findViewById(R.id.rightPage);
//        mViewRightPage.setOnTouchListener(this);

        mImgViewUserIcon = (ImageView) mViewLeftMenu.findViewById(R.id.img1);
        mTvPhone = (TextView) mViewLeftMenu.findViewById(R.id.tvPhone);
        mBtnAccount = (Button) mViewLeftMenu.findViewById(R.id.btnAccount);
        mBtnCard = (Button) mViewLeftMenu.findViewById(R.id.btnCard);
        mBtnSchool = (Button) mViewLeftMenu.findViewById(R.id.btnSchool);
        mBtnExitLogin = (Button) mViewLeftMenu.findViewById(R.id.btnExitLogin);
        mImgBtnCamera = (ImageButton) findViewById(R.id.startCamera);
        mListView = (ListView) mViewRightPage.findViewById(R.id.likeListView);

        mPreJudgeLogin = getSharedPreferences("judgeLogin", MODE_PRIVATE);
        mPreUserInfo = getSharedPreferences("userInfo", MODE_PRIVATE);
        mPreLikeInfo = getSharedPreferences("likeInfo", MODE_PRIVATE);
        if (!mPreJudgeLogin.getBoolean("isLogined", false)) {
            Editor editor = mPreJudgeLogin.edit();
            editor.putBoolean("isLogined", true);
            editor.commit();
        }
    }

    /**
     * 侧滑菜单按钮监听事件
     *
     * @param view 侧滑菜单按钮id
     */
    public void onButtonClick(View view) {
        Intent intent = new Intent(this, SecondActivity.class);
        switch (view.getId()) {

            case R.id.btnAccount:
                startActivity(intent);
                break;
            case R.id.btnCard:
                startActivity(intent);
                break;
            case R.id.btnSchool:
                Intent intent3 = new Intent(this, ChooseSchoolActivity.class);
                startActivity(intent3);
                break;
            case R.id.btnExitLogin:
                Intent intent4 = new Intent(this, LoginActivity.class);
                intent4.putExtra("mPhone", mPhone);
                Editor editor = mPreJudgeLogin.edit();
                editor.putBoolean("isLogined", false);
                editor.commit();
                startActivity(intent4);
                finish();
                break;
        }
    }

    public void onEditTextClick(View view) {
    }

    /**
     * 图标按钮
     *
     * @param view
     */
    public void onImageButtonClick(View view) {
        switch (view.getId()) {

            // 选择头像
            case R.id.img1:

                //此处不能用application获取context
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                dialogBuilder.setTitle("选择方式");
                dialogBuilder.setItems(new String[]{"拍照", "本地图片"}, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                startCamera();
                                break;

                            case 1:
                                Intent intent = new Intent(MainActivity.this, ChooseIconActivity.class);
                                startActivity(intent);
                                break;
                        }
                    }
                });
                dialogBuilder.setCancelable(true);
                dialogBuilder.show();

                // Intent intent = new Intent(MainActivity.this,
                // ChooseIconActivity.class);
                // startActivityForResult(intent, REQ_CHOOSE_ICON);
                break;
            case R.id.toggle:
                mSliddingMenu.toggle();
                break;
            case R.id.startCamera:
                startCamera();
                break;

            // 校锟斤拷锟教硷拷
            case R.id.ib1:
                Intent intent1 = new Intent(this, SellerActivity.class);
                intent1.putExtra("url", getInSchoolUrl(mSchool));
                intent1.putExtra("tag", getInSchoolTag(mSchool));
                startActivity(intent1);
                break;
            // 校锟斤拷锟教硷拷
            case R.id.ib2:
                Intent intent2 = new Intent(this, SellerActivity.class);
                intent2.putExtra("url", getOutSchoolUrl(mSchool));
                intent2.putExtra("tag", getOutSchoolTag(mSchool));
                startActivity(intent2);
                break;
            case R.id.ib3:

                break;
            case R.id.ib4:

                break;
        }
    }

    private void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQ_START_CAMERA);
    }

    ////////////////////////////////////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_CHOOSE_ICON:

                    break;

                case REQ_START_CAMERA:
                    Bundle bundle = data.getExtras();
                    Log.i("scx", "bundle=" + bundle);
                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    mImgViewUserIcon.setImageBitmap(bitmap);

                    break;
            }
        }
    }

    /**
     * 锟斤拷取校锟斤拷锟絫ag
     *
     * @param school
     * @return
     */

    private String getOutSchoolTag(String school) {
        switch (school) {
            case "南京工程学院":

                return "outGcSeller";

            case "南京晓庄学院":

                return "outXzSeller";
        }
        return null;
    }

    /**
     * 锟斤拷取校锟节碉拷tag
     *
     * @param school
     * @return
     */
    private String getInSchoolTag(String school) {
        switch (school) {
            case "南京工程学院":

                return "inGcSeller";

            case "南京晓庄学院":

                return "inXzSeller";
        }
        return null;
    }

    /**
     * 锟斤拷取校锟斤拷锟絬rl
     * @param school
     */
    private String getOutSchoolUrl(String school) {
        Log.i("scx", "getOutSchoolUrl()");
        switch (school) {
            case "南京工程学院":
                Log.i("scx", "南京工程学院 OUT_GONGCHENG_URL=" + OUT_GONGCHENG_URL);
                return OUT_GONGCHENG_URL;

            case "南京晓庄学院":
                Log.i("scx", "南京晓庄学院 OUT_XIAOZHUANG_URL=" + OUT_XIAOZHUANG_URL);
                return OUT_XIAOZHUANG_URL;
        }
        return null;
    }

    /**
     * 锟斤拷取校锟节碉拷url
     * @param school
     * @return
     */
    private String getInSchoolUrl(String school) {
        Log.i("scx", "getInSchoolUrl()");
        switch (school) {
            case "南京工程学院":
                Log.i("scx", "南京工程学院 IN_GONGCHENG_URL=" + IN_GONGCHENG_URL);
                return IN_GONGCHENG_URL;

            case "南京晓庄学院":
                Log.i("scx", "南京晓庄学院 IN_XIAOZHUANG_URL=" + IN_XIAOZHUANG_URL);
                return IN_XIAOZHUANG_URL;
        }
        return null;
    }

    /**
     * 判断是否需要改变学校
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mFlag = intent.getBooleanExtra("flag", false);
    }

    /**
     * oncreate只锟斤拷锟斤拷一锟轿ｏ拷onResume锟斤拷锟皆碉拷锟矫讹拷锟�
     */
    @Override
    protected void onResume() {
        super.onResume();
//        Log.e("scx", "onResume()");
        Log.e("scx", "flag=" + mFlag);
        mPhone = mPreUserInfo.getString("phone", null);
        mSchool = mPreUserInfo.getString("school", null);
        mTvPhone.setText(mPhone);
        mBtnSchool.setText(mSchool);
        String userIconPath = mPreUserInfo.getString("imgPath", null);
        if (userIconPath != null) {

            ImageLoader.getInstance(1, ImageLoader.Type.FIFO).loadSampledImage(userIconPath, mImgViewUserIcon);
        }
        toastNetworkUnavailable(mContext);
//		Intent intent=getIntent();
//		if(intent!=null){
//			String path = intent.getStringExtra("imgPath");
//            Log.e(TAG, "onResume: intent="+intent);
//            Log.e(TAG, "onResume: path="+path);
//            if(path!=null){
//
//                ImageLoader.getInstance(1, ImageLoader.Type.FIFO).loadCompleteImage(path,mImgViewUserIcon);
//            }
//		}
        if (mFlag) {
            Log.e("scx", "学校修改成功");
            if (mSchool.equals("南京工程学院")) {
                mVolleyRequest.handleGetJsonData(mPreLikeInfo, mGongChengUrl, "first");
            } else {
                mVolleyRequest.handleGetJsonData(mPreLikeInfo, mXiaoZhuangUrl, "first");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 通过网络接收器获取本地广播管理对象
        if (NetworkChangeReceiver.getLocalBroadcastManager() != null) {
            NetworkChangeReceiver.getLocalBroadcastManager()
                    .unregisterReceiver(NetworkChangeReceiver.getLocalReceiver());
        }
    }

    /**
     * 再按一次退出程序
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
