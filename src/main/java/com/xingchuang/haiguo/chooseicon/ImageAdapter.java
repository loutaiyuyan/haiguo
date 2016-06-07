package com.xingchuang.haiguo.chooseicon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.xingchuang.haiguo.R;
import com.xingchuang.haiguo.activity.MainActivity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageAdapter extends BaseAdapter {

    private static final String TAG = "ImageAdapter";
    /**
     * 图片所在文件夹路径,这里只是为了补全图片的绝对路径
     */
    private String mDirPath;

    /**
     * 图片的路径列表
     */
    private List<String> mImgPaths;
    private LayoutInflater mInflater;
    private Context context;
    //可以用contain与remove与add方法，这是一个集合，无重复性
    private static Set<String> mSelectedImg = new HashSet<String>();
    /**
     * 用集合mDirPaths存储遍历过的父目录绝对路径
     */
    List<String> mDirPaths;

    public ImageAdapter(Context context, List<String> mDatas, String dirPath) {
        this.mDirPath = dirPath;
        this.mImgPaths = mDatas;
        this.context = context;
        mInflater = LayoutInflater.from(context);

    }

    public ImageAdapter(Context context, List<String> dirPaths) {
        this.context = context;
        this.mDirPaths = dirPaths;
        mImgPaths=new ArrayList<>();
        mInflater = LayoutInflater.from(context);
        firstInit(dirPaths);
    }

    private void firstInit(List<String> dirPaths) {
        for (int i = 0; i < dirPaths.size(); i++) {
            mDirPath = dirPaths.get(i);
            List<String> imgPaths = filterImgPathsListFromFile(new File(mDirPath));
            for (int j=0;j<imgPaths.size();j++){
                mImgPaths.add(imgPaths.get(j));

            }

        }
    }

    @Override
    public int getCount() {
        return mImgPaths.size();
    }

    @Override
    public Object getItem(int position) {
        return mImgPaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_gridview, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mImg = (ImageView) convertView.findViewById(R.id.itemImage);
            viewHolder.mSelect = (ImageButton) convertView.findViewById(R.id.itemSelected);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // 重置状态
        viewHolder.mImg.setImageResource(R.mipmap.ic_launcher);
        viewHolder.mSelect.setImageResource(R.mipmap.dot_blur);
        viewHolder.mImg.setColorFilter(null);
        //图片的绝对路径
        final String filePath = mImgPaths.get(position);
        ImageLoader.getInstance(3, ImageLoader.Type.LIFO).loadSampledImage(filePath, viewHolder.mImg);

        viewHolder.mImg.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //选择多个图片的时候用此方法
                /*	if(mSelectedImg.contains(filePath))
                    {
						mSelectedImg.remove(filePath);
						viewHolder.mImg.setColorFilter(null);
						viewHolder.mSelect.setImageResource(R.mipmap.dot_blur);
					}else{
						mSelectedImg.add(filePath);
						viewHolder.mImg.setColorFilter(Color.parseColor("#77000000"));
						viewHolder.mSelect.setImageResource(R.mipmap.dot_focus);
					}*/
                //选择一张图片的时候用这个方法
                SharedPreferences mPreUserInfo = context.getSharedPreferences("userInfo", context.MODE_PRIVATE);
                SharedPreferences.Editor editor=mPreUserInfo.edit();
                editor.putString("imgPath",filePath);
                editor.commit();
                Intent intent = new Intent(context, MainActivity.class);
//                intent.putExtra("imgPath", filePath);
                context.startActivity(intent);
            }
        });

		/*	if(mSelectedImg.contains(filePath)){
                viewHolder.mImg.setColorFilter(Color.parseColor("#77000000"));
				viewHolder.mSelect.setImageResource(R.mipmap.dot_focus);
			}*/
        return convertView;
    }

    /**
     * 过滤出当前文件夹中所有图片的不隐藏文件扩展名(文件名+扩展名)列表
     * 比如:girl.png,boy.jpg等等
     *
     * @param currentDir 当前文件夹对象
     * @return
     */
    private List<String> filterImgPathsListFromFile(File currentDir) {
        String currentDirPath=currentDir.getAbsolutePath();
        List<String> imgNamesList = Arrays.asList(currentDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png"))
                    return true;
                return false;
            }
        }));
        List<String> imgPathsList=new ArrayList<>();
        for (int i = 0; i < imgNamesList.size(); i++) {
            imgPathsList.add(currentDirPath+"/"+imgNamesList.get(i));
        }
        return imgPathsList;
    }

    private class ViewHolder {
        private ImageView mImg;
        private ImageButton mSelect;
    }

}