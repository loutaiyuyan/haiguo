package com.xingchuang.haiguo.customview;//package com.haiguo.newhaiguo.customview;
//
//import com.haiguo.newhaiguo.util.ScreenParams;
//
//import android.content.Context;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.widget.LinearLayout;
//
//public class LinearListViewItem extends LinearLayout {
//
//	private static final String TAG = "LinearListViewItem";
//	private CustomViewAttributes customViewAttributes;
//	public LinearListViewItem(Context context) {
//		this(context,null);
//	}
//	
//	public LinearListViewItem(Context context, AttributeSet attrs) {
//		this(context, attrs,0);
//	}
//
//
//	public LinearListViewItem(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//		customViewAttributes=new CustomViewAttributes(context, attrs);
//	}
//
//	@Override
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		measureChildren(widthMeasureSpec, heightMeasureSpec);
//	    final int heightPercent=(int) customViewAttributes.getHeightPercent();
//	    final int measuredHeight=MeasureSpec.getSize(heightMeasureSpec);
//	    final int measuredWidth=MeasureSpec.getSize(widthMeasureSpec);
//	    Log.i(TAG, "measuredHeight="+measuredHeight);
//	    Log.i(TAG, "measuredWidth="+measuredWidth);
//	    Log.i(TAG, "measuredHeight*heightPercent="+measuredHeight*heightPercent);
//	    setMeasuredDimension(widthMeasureSpec, measuredHeight*heightPercent);
//		
//	}
//
//}
