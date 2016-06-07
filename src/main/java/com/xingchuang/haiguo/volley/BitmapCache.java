package com.xingchuang.haiguo.volley;

import com.android.volley.toolbox.ImageLoader.ImageCache;

import android.graphics.Bitmap;
import android.util.LruCache;

public class BitmapCache implements ImageCache {
	
	public LruCache<String, Bitmap> mCache;
	private int max=10*1024*1024;
	
	
	public BitmapCache() {
		mCache=new LruCache<String, Bitmap>(max){
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes()*value.getHeight();
			}
		};
	}
	@Override
	public Bitmap getBitmap(String arg0) {
		return mCache.get(arg0);
	}

	@Override
	public void putBitmap(String arg0, Bitmap arg1) {
		mCache.put(arg0, arg1);
	}

}
