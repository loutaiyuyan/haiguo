package com.xingchuang.haiguo.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ScreenParams {
	
	private WindowManager mWindowManager;
	private int mScreenWidth;
	private int mScreenHeight;
	public ScreenParams(Context context) {
		mWindowManager=(WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics=new DisplayMetrics();
		mWindowManager.getDefaultDisplay().getMetrics(outMetrics);
		this.mScreenWidth=outMetrics.widthPixels;
		this.mScreenHeight=outMetrics.heightPixels;
	}
	public int getScreenWidthPixels() {
		return mScreenWidth;
	}
	public int getScreenHeightPixels() {
		return mScreenHeight;
	}
	

//	private static GetScreenParams getScreenParams;
//	private static Context context;
//	public static int getScreenWidthPixels() {
//		context=MyApplication.getContext();
//		if(getScreenParams==null){
//			getScreenParams=new GetScreenParams(context);
//		}
//		return getScreenParams.screenWidth;
//	}
//	public static int getScreenHeightPixels() {
//		context=MyApplication.getContext();
//		if(getScreenParams==null){
//			getScreenParams=new GetScreenParams(context);
//		}
//		return getScreenParams.screenHeight;
//	}
//	static class GetScreenParams{
//		
//	}
}
