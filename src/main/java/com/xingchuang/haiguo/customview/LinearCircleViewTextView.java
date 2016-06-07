package com.xingchuang.haiguo.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class LinearCircleViewTextView extends LinearLayout {

	private static final String TAG = "LinearCircleTextView";
	private float mHeightPercent;
	public LinearCircleViewTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public LinearCircleViewTextView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public LinearCircleViewTextView(Context context) {
		this(context,null);
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		measureChildren(widthMeasureSpec, heightMeasureSpec);
//		final int 
		final int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
//		Log.i(TAG, "measuredHeight=" + measuredHeight);

		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View childView = getChildAt(i);
			ViewGroup.LayoutParams params = childView.getLayoutParams();
			if (params instanceof CustomLayoutParams) {
				mHeightPercent = ((CustomLayoutParams) params).getHeightPercent();
			}
//			Log.e(TAG, "heightPercent=" + mHeightPercent);
			if (mHeightPercent != 0) {
//				Log.i(TAG, "heightPercent=" + mHeightPercent);
				params.height = (int) (measuredHeight * mHeightPercent);
//				Log.i(TAG, "params.height=" + params.height);
			}
		}
	}
	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new CustomLayoutParams(getContext(), attrs);
	}

}
