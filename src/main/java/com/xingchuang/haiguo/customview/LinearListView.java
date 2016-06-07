package com.xingchuang.haiguo.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class LinearListView extends ListView {

	private static final String TAG = "LinearListView";
	private static int sListViewTotalHeight;

	public LinearListView(Context context) {
		this(context, null);
	}

	public LinearListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LinearListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		sListViewTotalHeight = getMeasuredHeight();
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		final int childCount = getChildCount();
//		Log.i(TAG, "childCount=" + childCount);
		for (int i = 0; i < childCount; i++) {
			View childView = getChildAt(i);
			float heightPercent = 0;
			ViewGroup.LayoutParams params = (LayoutParams) childView.getLayoutParams();
			if (params instanceof CustomListViewParams) {
				heightPercent = ((CustomListViewParams) params).getHeightPercent();
			}
//			Log.i(TAG, "getMeasuredHeight()=" + getMeasuredHeight());
//			Log.i(TAG, "heightPercent=" + heightPercent);
			if (heightPercent != 0) {
				params.height = (int) (getMeasuredHeight() * heightPercent);
//				Log.i(TAG, "params.height=" + params.height);
			}
		}
	}

	public static int getTotalHeight() {
//		Log.i(TAG, "getTotalHeight()=" + sListViewTotalHeight);
		return sListViewTotalHeight;
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new CustomListViewParams(getContext(), attrs);
	}

}
