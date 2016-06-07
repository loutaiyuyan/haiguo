package com.xingchuang.haiguo.adapter;

import java.util.List;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.xingchuang.haiguo.R;
import com.xingchuang.haiguo.chooseicon.ImageLoader;
import com.xingchuang.haiguo.customview.LinearListView;
import com.xingchuang.haiguo.volley.VolleyRequest;

public class NewsAdapter extends BaseAdapter {

    private static final String TAG = "NewsAdapter";
    /**
     * volley网络请求对象
     */
    private VolleyRequest mVolleyRequest;
    /**
     * 封装listView每个item的图片地址,标题,内容的对象列表
     */
    private List<NewsBean> mList;
    /**
     * 捕捉布局的对象
     */
    private LayoutInflater mInflater;
    private Context mContext;

    public NewsAdapter(Context context, List<NewsBean> newsBeans) {
        mList = newsBeans;
        mContext = context;
        //布局应用于哪个Activity就用哪个上下文
        mInflater = LayoutInflater.from(context);
        mVolleyRequest = new VolleyRequest();

    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            int height = (int) (LinearListView.getTotalHeight() * 0.25);
            convertView = mInflater.inflate(R.layout.each_goods_item, null);
            LinearLayout ll = (LinearLayout) convertView.findViewById(R.id.id_ll);
            LayoutParams params = (LayoutParams) ll.getLayoutParams();
            //此处不可以在构方中获取高度，因为每设置一个view都要设置item的高度
            params.height = height;
            Log.i(TAG, "params.height=" + params.height);
            ll.setLayoutParams(params);
            viewHolder.iv_icon = (ImageView) convertView.findViewById(R.id.pic);
            viewHolder.tv_title = (TextView) convertView.findViewById(R.id.title);
            viewHolder.tv_content = (TextView) convertView.findViewById(R.id.content);
            convertView.setTag(viewHolder);
//			Log.i("scx", "convertView.getTag().ArrayList="+convertView.getTag());
        } else {
//			Log.i("scx", "else convertView.getTag().ArrayList="+convertView.getTag());
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //从网络获取数据之前的默认图片资源（可以自己随意设置）#################################
//		viewHolder.iv_icon.setImageResource(R.drawable.ic_launcher);
        String mUrl = mList.get(position).newsIconUrl;
        viewHolder.iv_icon.setTag(mUrl);
//		new ImageLoader().showImageByThread(viewHolder.iv_icon, mUrl);
        //每次new一个对象都将创建一个缓存机制而不能达到缓存重复利用的效果,所一在构造方法里面初始化
//		mImageLoader.showImageByAsyncTask(viewHolder.iv_icon, mUrl);
//		mVolleyRequest.showImageByVolley(viewHolder.iv_icon, mUrl);

        com.xingchuang.haiguo.util.ImageLoader.getInstance(mContext, 8).bindBitmap(mUrl, viewHolder.iv_icon);
        viewHolder.tv_title.setText(mList.get(position).newsTitle);
        viewHolder.tv_content.setText(mList.get(position).newsContent);

        return convertView;
    }

    class ViewHolder {
        public TextView tv_title, tv_content;
        public ImageView iv_icon;
    }

}
