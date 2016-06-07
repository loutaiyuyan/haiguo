package com.xingchuang.haiguo.util;

import android.os.CountDownTimer;
import android.widget.Button;

public class TimeCount extends CountDownTimer {
	private Button mBtnTimeCount;
	public TimeCount(long millisInFuture, long countDownInterval,Button btn) {
		super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
		this.mBtnTimeCount=btn;
	}

	@Override
	public void onFinish() {// 计时完毕时触发
		mBtnTimeCount.setText("重新验证");
		mBtnTimeCount.setClickable(true);
	}

	@Override
	public void onTick(long millisUntilFinished) {// 计时过程显示
		mBtnTimeCount.setClickable(false);
		mBtnTimeCount.setText(millisUntilFinished / 1000 + "秒");
	}
}