package com.xingchuang.haiguo.customview;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.xingchuang.haiguo.R;

public class CustomViewAttributes {

	private float mWidthPercent;
	private float mHeightPercent;
	private AllPercent mAllPercent;
	public CustomViewAttributes(Context context,AttributeSet attrs) {
		mAllPercent=new AllPercent(context, attrs);
	}
	
	public float getWidthPercent() {
		setWidthPercent(mAllPercent.getWidthPercent());
		return mWidthPercent;
	}

	public void setWidthPercent(float widthPercent) {
		this.mWidthPercent = widthPercent;
	}

	public float getHeightPercent() {
		setHeightPercent(mAllPercent.getHeightPercent());
		return mHeightPercent;
	}

	public void setHeightPercent(float heightPercent) {
		this.mHeightPercent = heightPercent;
	}

	public static class AllPercent{
		private float widthPercent;
		private float heightPercent;
		private TypedArray array;
		public AllPercent(Context context,AttributeSet attrs) {
			array=context.obtainStyledAttributes(attrs, R.styleable.SizePercent);
			widthPercent=array.getFloat(R.styleable.SizePercent_widthPercent, 0);
			heightPercent=array.getFloat(R.styleable.SizePercent_heightPercent, 0);
			array.recycle();
		}
		public float getWidthPercent() {
			return widthPercent;
		}
		public float getHeightPercent() {
			return heightPercent;
		}
	}
}
