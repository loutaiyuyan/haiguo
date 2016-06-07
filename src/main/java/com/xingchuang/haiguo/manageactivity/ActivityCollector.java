package com.xingchuang.haiguo.manageactivity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

public class ActivityCollector {

	//所有的activity都在运行主线程中
	public static List<Activity> activities=new ArrayList<Activity>();
	
	/**
	 * 添加当前activity到队列中
	 * @param activity
	 */
	public static void addActivity(Activity activity){
		activities.add(activity);
	}
	/**
	 * 将当前activity从队列中移除
	 * @param activity
	 */
	public static void removeActivity(Activity activity){
		activities.remove(activity);
	}
	/**
	 * 销毁所有activity
	 */
	public static void finishAll(){
		for(Activity activity : activities){
			if(!activity.isFinishing()){
				activity.finish();
			}
		}
	}
	
}
