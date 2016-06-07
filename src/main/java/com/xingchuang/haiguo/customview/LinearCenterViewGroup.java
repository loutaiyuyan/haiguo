package com.xingchuang.haiguo.customview;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.xingchuang.haiguo.util.ScreenParams;

public class LinearCenterViewGroup extends LinearLayout {

	private static final String TAG = "LinearCenterViewGroup";
	private ScreenParams mScreenParams;
	private CustomViewAttributes mCustomViewAttributes;
	private float mWidthPercent;

	public LinearCenterViewGroup(Context context) {
		this(context, null);
	}

	public LinearCenterViewGroup(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LinearCenterViewGroup(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mScreenParams = new ScreenParams(context);
		mCustomViewAttributes = new CustomViewAttributes(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(mScreenParams.getScreenWidthPixels(),
				(int) (mScreenParams.getScreenHeightPixels() * mCustomViewAttributes.getHeightPercent()));
		//此处必须要测量子view，并计算出子view的宽高，以便在子view中获取自己的宽高
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		
		final int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
		final int childCount = getChildCount();
//		Log.i(TAG, "childCount=" + childCount);
		for (int i = 0; i < childCount; i++) {
			View childView = getChildAt(i);
			ViewGroup.LayoutParams params = childView.getLayoutParams();
			if (params instanceof CustomLayoutParams) {
				mWidthPercent = ((CustomLayoutParams) params).getWidthPercent();
				// heightPercent = ((CustomLayoutParams)
				// params).getHeightPercent();
			}
//			Log.i(TAG, "widthPercent=" + mWidthPercent);
			if (mWidthPercent != 0) {
//				Log.i(TAG, "widthPercent=" + mWidthPercent);
				params.width = (int) (measuredWidth * mWidthPercent);
				// params.height = (int) (measuredHeight * heightPercent);
			}
		}

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		Log.i("LinearCenterViewGroup", "getWidth()=" + getWidth());
		Log.i("LinearCenterViewGroup", "getHeight()=" + getHeight());
	}

	/**
	 * 子view获取自己的布局参数
	 * 
	 * @param attrs
	 *            子view的布局参数
	 * @return
	 */
	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new CustomLayoutParams(getContext(), attrs);
	}

}
