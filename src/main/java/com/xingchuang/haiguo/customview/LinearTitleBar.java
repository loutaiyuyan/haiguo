package com.xingchuang.haiguo.customview;


import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.xingchuang.haiguo.util.ScreenParams;

public class LinearTitleBar extends LinearLayout {

	private CustomViewAttributes mCustomViewAttributes;
	private ScreenParams mScreenParams;
	private static final String TAG = "LinearTitleBar";
	private int mChildWidth;
	private float mWidthPercent;

	public LinearTitleBar(Context context) {
		this(context,null);
	}

	public LinearTitleBar(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public LinearTitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mCustomViewAttributes=new CustomViewAttributes(context, attrs);
		mScreenParams=new ScreenParams(context);
		//这里肯定是不行的，因为这是一个viewGroup里面包含了很多的子view,这个viewGroup
		//控件没有设置这个属性
//		widthPercent = array.getFloat(R.styleable.TitleBarPercent_widthPercent, 0);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int measuredWidth = 0;
		int measuredHeight = 0;
		// 获取屏幕的宽度(800dp)
		int screenWidth = MeasureSpec.getSize(widthMeasureSpec);
		final int childCount = getChildCount();
		
		for (int i = 0; i < childCount; i++) {
			View childView = getChildAt(i);
			//获取产生的布局参数即genenrateLayoutParams
			ViewGroup.LayoutParams params = childView.getLayoutParams();
			if(params instanceof CustomLayoutParams){
				mWidthPercent= ((CustomLayoutParams) params).getWidthPercent();
			}
			if (mWidthPercent != 0) {
				params.width = (int) (screenWidth * mWidthPercent);
			}
		}
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		//这里设置的是整个viewGroup的宽高
		int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSpaceSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSpaceSize = MeasureSpec.getSize(heightMeasureSpec);
		int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		if (childCount == 0) {
			setMeasuredDimension(0, 0);
		} else if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
			Log.i(TAG, "widthSpecMode==MeasureSpec.AT_MOST&&heightSpecMode==MeasureSpec.AT_MOST");
			// 此处用遍历如何
			final View childView = getChildAt(0);
			measuredWidth = childView.getMeasuredWidth() * childCount;
			measuredHeight = childView.getMeasuredHeight();
			setMeasuredDimension(measuredWidth, measuredHeight);
		} else if (heightSpecMode == MeasureSpec.AT_MOST) {
			Log.i(TAG, "heightSpecMode==MeasureSpec.AT_MOST");
			measuredHeight = (int) (mScreenParams.getScreenHeightPixels()*mCustomViewAttributes.getHeightPercent());
			
//			Log.e(TAG, "measuredHeight=" + measuredHeight);
			setMeasuredDimension(widthSpaceSize, measuredHeight);
		} else if (widthSpecMode == MeasureSpec.AT_MOST) {
			Log.i(TAG, "widthSpecMode==MeasureSpec.AT_MOST");
			final View childView = getChildAt(0);
			measuredWidth = childView.getMeasuredWidth() * childCount;
			setMeasuredDimension(measuredWidth, heightSpaceSize);
		}

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		int childLeft = 0;
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View childView = getChildAt(i);
			int childVisibility = childView.getVisibility();
//			Log.i("onLayout", "childAt" + i);
//			Log.i("onLayout", "getMeasuredWidth=" + childView.getMeasuredWidth());
//			Log.i("onLayout", "getMeasuredHeight=" + childView.getMeasuredHeight());
//			Log.i("onLayout", "childView.getWidth=" + childView.getWidth());
//			Log.i("onLayout", "childView.getHeight=" + childView.getHeight());

			if (childVisibility != View.GONE) {
				final int childWidth = childView.getMeasuredWidth();
				mChildWidth = childWidth;
				// getMeasuredHeight有问题？
				childView.layout(childLeft, 0, childLeft + childWidth, childView.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
	
	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new CustomLayoutParams(getContext(), attrs);
	}

}
