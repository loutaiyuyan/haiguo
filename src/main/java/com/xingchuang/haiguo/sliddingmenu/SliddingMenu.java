package com.xingchuang.haiguo.sliddingmenu;


import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.xingchuang.haiguo.R;
import com.xingchuang.haiguo.slideshowview.SlideShowView;

public class SliddingMenu extends HorizontalScrollView {

	private static final String TAG = "SliddingMenu";
	private LinearLayout mWapper;
	/**
	 * 侧滑菜单
	 */
	private ViewGroup mMenu;
	private ViewGroup mContent;

	private RelativeLayout rightPage;

	/**
	 * 屏幕宽度
	 */
	private int mScreenWidth;
	/**
	 * 屏幕高度
	 */
	private int mScreenHeight;
	/**
	 * 侧滑菜单右边距
	 */
	private int mMenuRightPadding = 50;
	/**
	 * 判断是否第一次进入应用,用于初始化侧滑是否显示
	 */
	private boolean once;
	/**
	 * 菜单的宽度
	 */
	private int mMenuWidth;

	private boolean isOpen = false;
	/**
	 * 滑动速度捕获
	 */
	private VelocityTracker mVelocityTracker;

	private int mLastXInterceptX = 0;
	private int mLastXInterceptY = 0;

	private LinearLayout rightFirstView;
	
	private SlideShowView slideShowView;

	private Handler mHandler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			
			slideShowView.stopPlay();
		};
		
	};
	/**
	 * 未使用自定义属性时候调用2个参数的构造方法
	 * 
	 * @param context
	 * @param attrs
	 */
	public SliddingMenu(Context context, AttributeSet attrs) {
		// 调用构造方法没有"."这个符号,也没有方法名
		this(context, attrs, 0);
	}

	/**
	 * 当使用自定义属性时候调用3个参数的构造方法
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyleAttr
	 */
	public SliddingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		slideShowView=new SlideShowView(context, attrs, defStyleAttr);
		// 获取自定义属性
		TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SlidingMenu, defStyleAttr, 0);
		int n = array.getIndexCount();
		for (int i = 0; i < n; i++) {
			int attr = array.getIndex(i);
			switch (attr) {
			case R.styleable.SlidingMenu_rightPadding:
				mMenuRightPadding = array.getDimensionPixelSize(attr, (int) TypedValue
						.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, context.getResources().getDisplayMetrics()));
				break;

			default:
				break;
			}
		}
		array.recycle();
		mVelocityTracker = VelocityTracker.obtain();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		mScreenWidth = outMetrics.widthPixels;
		mScreenHeight = outMetrics.heightPixels;
		// 把dp改为px
		// mMenuRightPadding = (int)
		// TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50,
		// context.getResources().getDisplayMetrics());
	}

	public SliddingMenu(Context context) {
		this(context, null);
	}

	/**
	 * 决定内部View（子view）的宽和高，以及自己的宽和高
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (!once) {
			// 获取decorVIew中的第一个view也就是整个侧滑菜单还有右边的页面
			mWapper = (LinearLayout) getChildAt(0);
			// LinearLayout里面的第一个元素，也就是menu界面
			mMenu = (ViewGroup) mWapper.getChildAt(0);
			mContent = (ViewGroup) mWapper.getChildAt(1);
			// Log.i(TAG, "mWapper="+mWapper);
			// Log.i(TAG, "mMenu="+mMenu);
			// Log.i(TAG, "mContent="+mContent);
			// Log.i(TAG, "mContent.getChildCount()"+mContent.getChildCount());
			// Log.i(TAG, "mContent.getChildAt(0)"+mContent.getChildAt(0));
			// Log.i(TAG, "mContent.getChildAt(1)"+mContent.getChildAt(1));
			rightPage = (RelativeLayout) mContent.getChildAt(1);
			// Log.i(TAG,
			// "rightPage.getChildCount()"+rightPage.getChildCount());
			// Log.i(TAG,
			// "rightPage.getChildAt(0).getLayoutParams().height="+rightPage.getChildAt(0).getLayoutParams().height);
			// Log.i(TAG,
			// "rightPage.getChildAt(0).getWidth()"+rightPage.getChildAt(0).getWidth());
			// Log.i(TAG,
			// "rightPage.getChildAt(0).getHeight()"+rightPage.getChildAt(0).getHeight());
			//
			// Log.i(TAG, "rightPage.getChildAt(1)"+rightPage.getChildAt(1));

			// Log.i(TAG,
			// "mSlideShowView.getWidth()"+mSlideShowView.getWidth());
			// Log.i(TAG, ""+mSlideShowView.getLayoutParams().height);
			mMenuWidth = mMenu.getLayoutParams().width = mScreenWidth - mMenuRightPadding;
			mContent.getLayoutParams().width = mScreenWidth;
			once = true;
		}
	}

	// 决定子view的摆放位置
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed) {
			this.scrollTo(mMenuWidth, 0);
		}
	}

	// 判断手指的滑动状态
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		mVelocityTracker.addMovement(ev);
		switch (action) {

		case MotionEvent.ACTION_UP:
			// 隐藏在左边的宽度,左边隐藏的越多这个值越大,以整个view左上角为原点，手机屏幕左上角的坐标
			int scrollX = getScrollX();
			//5毫秒之内的速度
			mVelocityTracker.computeCurrentVelocity(500);
			float xVelocity = mVelocityTracker.getXVelocity();
			Log.i(TAG, "xVelocity=" + xVelocity);
			if (scrollX >= mMenuWidth / 2 || xVelocity <= -50) {
				// 隐藏
				Log.e("info", "scrollX>=mMenuWidth/2");
				this.smoothScrollTo(mMenuWidth, 0);
			} else if (scrollX < mMenuWidth / 2 || xVelocity >= 50) {
				Log.e("info", "scrollX<mMenuWidth/2");
				new Thread(new Runnable() {
					@Override
					public void run() {
						mHandler.sendEmptyMessage(0);
//						slideShowView.stopPlay();
					}
				});
				this.smoothScrollTo(0, 0);
				
			}
			// 这里一定要return true
			return true;
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		int action = event.getAction();
		boolean intercepted = false;
		Log.i("info", "SliddingMenu  onInterceptTouchEvent()");
		int x = (int) event.getX();
		int y = (int) event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			int deltaX = x - mLastXInterceptX;
			int deltaY = y - mLastXInterceptY;
			// 若侧滑菜单已经显示,并且不触摸在滚动图内
			if ((!isOpen && (event.getRawY() < SlideShowView.topY || event.getRawY() > SlideShowView.bottomY)
					&& Math.abs(deltaX) > Math.abs(deltaY))) {

				intercepted = true;
			} else {
				// 必须返回false
				intercepted = false;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			intercepted = false;
			break;
		case MotionEvent.ACTION_UP:
			intercepted = false;
			break;

		default:
			break;
		}
		mLastXInterceptX = x;
		mLastXInterceptY = y;
		return intercepted;
	}

	/**
	 * 切换侧滑菜单显示
	 */
	public void toggle() {
		if (isOpen) {
			closeMenu();
		} else {
			openMenu();
		}
	}

	public void openMenu() {
		if (isOpen)
			return;
		this.smoothScrollTo(0, 0);
		isOpen = true;
	}

	public void closeMenu() {

		if (!isOpen)
			return;
		this.smoothScrollTo(mMenuWidth, 0);
		isOpen = false;
	}
	
	@Override
	protected void onDetachedFromWindow() {
		mVelocityTracker.recycle();
		super.onDetachedFromWindow();
	}
}
