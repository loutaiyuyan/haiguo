package com.xingchuang.haiguo.customview;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.xingchuang.haiguo.R;


public  class CustomLayoutParams extends LinearLayout.LayoutParams {

		private float mWidthPercent;
		private float mHeightPercent;

		public float getWidthPercent() {
			return mWidthPercent;
		}

		public void setWidthPercent(float widthPercent) {
			this.mWidthPercent = widthPercent;
		}

		public float getHeightPercent() {
			return mHeightPercent;
		}

		public void setHeightPercent(float heightPercent) {
			this.mHeightPercent = heightPercent;
		}

		public CustomLayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
			TypedArray array = c.obtainStyledAttributes(attrs, R.styleable.SizePercent);
			mWidthPercent = array.getFloat(R.styleable.SizePercent_widthPercent, 0);
			mHeightPercent = array.getFloat(R.styleable.SizePercent_heightPercent, 0);
			array.recycle();
		}

	}