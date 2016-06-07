package com.xingchuang.haiguo.chooseicon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.xingchuang.haiguo.R;

import java.util.List;

public class ListImageDirPopupWindow extends PopupWindow {

	private int mWidth;
	private int mHeight;
	private View mConvertView;
	private ListView mListView;
	private List<FolderBean> mDatas;
	public OnDirSelectedListener mListener;
	
	
	public interface OnDirSelectedListener{
		void onSelected(FolderBean folderBean);
	}
	public void setOnDirSelectedListener(OnDirSelectedListener selectedListener){
		this.mListener=selectedListener;
	}

	@SuppressWarnings("deprecation")
	@SuppressLint({ "InflateParams", "ClickableViewAccessibility" })
	public ListImageDirPopupWindow(Context context, List<FolderBean> datas) {
		this.mDatas = datas;
		this.mConvertView = LayoutInflater.from(context).inflate(R.layout.popup_main, null);
		calWidthAndHeight(context);
		setContentView(mConvertView);
		setWidth(mWidth);
		setHeight(mHeight);
		setFocusable(true);
		setTouchable(true);
		setOutsideTouchable(true);
		setBackgroundDrawable(new BitmapDrawable());
		setTouchInterceptor(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					dismiss();
					return true;
				}
				return false;
			}
		});
		initViews(context);
		initEvent();

	}

	private void initEvent() {
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
				if(mListener!=null)
				{
					mListener.onSelected(mDatas.get(position));
				}
			}
		});
	}
	private void initViews(Context context) {
		mListView=(ListView) mConvertView.findViewById(R.id.listDir);
		mListView.setAdapter(new ListDirAdapter(context, mDatas));
	}

	private void calWidthAndHeight(Context context) {
		//获取屏幕的宽高，此处的方法是固定的
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		mWidth = outMetrics.widthPixels;
		mHeight = (int) (outMetrics.heightPixels * 0.7);
	}
	
	private class ListDirAdapter extends ArrayAdapter<FolderBean>{

		
		private LayoutInflater mInflater;
		private List<FolderBean>mDatas;
		//此处的objects好像没用？？？？？？？
		public ListDirAdapter(Context context, List<FolderBean> objects) {
			super(context, 0, objects);
			mInflater=LayoutInflater.from(context);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder=null;
			if(convertView==null){
				viewHolder=new ViewHolder();
				convertView=mInflater.inflate(R.layout.item_popup_main, parent, false);
				viewHolder.mImg=(ImageView) convertView.findViewById(R.id.dirItemImage);
				viewHolder.mDirName=(TextView) convertView.findViewById(R.id.dirItemName);
				viewHolder.mDirCount=(TextView) convertView.findViewById(R.id.dirItemCount);
				convertView.setTag(viewHolder);
			}else{
				viewHolder=(ViewHolder) convertView.getTag();
			}
			FolderBean bean=getItem(position);
			//重置图片
			viewHolder.mImg.setImageResource(R.mipmap.ic_launcher);
			ImageLoader.getInstance(3, ImageLoader.Type.LIFO).loadSampledImage(bean.getFirstImgPath(), viewHolder.mImg);
			viewHolder.mDirName.setText(bean.getName());
			viewHolder.mDirCount.setText(bean.getCount()+"");
			
			return convertView;
		}
		
		private class ViewHolder{
			ImageView mImg;
			TextView mDirName;
			TextView mDirCount;
		}
		
	}
}
