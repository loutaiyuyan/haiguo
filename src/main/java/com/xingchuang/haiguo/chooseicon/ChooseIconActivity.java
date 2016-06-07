package com.xingchuang.haiguo.chooseicon;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xingchuang.haiguo.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChooseIconActivity extends Activity {


    private static final String TAG = "ChooseIconActivity";
    protected static final int DATA_LOADED = 0x110;
    private GridView mGridView;
    private ImageAdapter mImageAdapter;
    /**
     * 我仿佛退却了
     */
    private RelativeLayout mBottomLy;
    /**
     * 图片所在文件夹的名称
     */
    private TextView mTvDirName;
    /**
     * 图片所在文件夹图片的个数
     */
//    private TextView mTvDirCount;
    /**
     * 图片的路径列表
     */
    private List<String> mImgs;
    /**
     * 所有图片扫描结束时候显示的文件夹路径，还有切换文件夹显示图片的时候当前文件夹路径
     */
    private File mCurrentDir;
    /**
     * 图片所在文件夹列表的弹出窗口
     */
    private ListImageDirPopupWindow mDirPopupWindow;
    /**
     * 图片的总个数
     */
    private int mAllCount;
    /**
     * 将图片及文件夹信息封装到一起的类的列表(顺序表)
     */
    private List<FolderBean> mFolderBeans = new ArrayList<FolderBean>();
    /**
     * 用集合mDirPaths存储遍历过的父目录绝对路径
     */
    private ArrayList<String> mDirPaths = new ArrayList<String>();

    private String mFirstImgPathOfAll;

    private ProgressDialog mProgressDialog;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == DATA_LOADED) {
                mProgressDialog.dismiss();
                // 绑定数据到View中
                dataToView();
                //图片遍历完毕之后初始化popupWindow
                initDirPopupWindow();

            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_icon);


        initViews();
        initDatas();
        initEvent();

    }

    protected void initDirPopupWindow() {
        if (mFolderBeans != null) {
            FolderBean allPicFolderBean = new FolderBean();
            allPicFolderBean.setFirstImgPath(mFirstImgPathOfAll);
            allPicFolderBean.setName("所有图片");
            allPicFolderBean.setCount(mAllCount);
            mFolderBeans.add(0, allPicFolderBean);
        }
        mDirPopupWindow = new ListImageDirPopupWindow(this, mFolderBeans);
        mDirPopupWindow.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                lightOn();
            }
        });

        mDirPopupWindow.setOnDirSelectedListener(new ListImageDirPopupWindow.OnDirSelectedListener() {

            @Override
            public void onSelected(FolderBean folderBean) {
                if(folderBean.getName()=="所有图片"){
                    mImageAdapter = new ImageAdapter(ChooseIconActivity.this,mDirPaths);
                }else{
                    mCurrentDir = new File(folderBean.getDir());
                    mImgs = filterImgPathsListFromFile(mCurrentDir);
                    mImageAdapter = new ImageAdapter(ChooseIconActivity.this, mImgs, mCurrentDir.getAbsolutePath());
                }
                mGridView.setAdapter(mImageAdapter);

                mTvDirName.setText(folderBean.getName());
                mDirPopupWindow.dismiss();
            }
        });

    }

    /**
     * 内容区域变亮
     */
    protected void lightOn() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1.0f;
        getWindow().setAttributes(lp);
    }

    /**
     * 内容区域变暗
     */
    protected void lightOff() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = .3f;
        getWindow().setAttributes(lp);
    }

    /**
     * 第一次为显示扫描到的所有图片
     */
    protected void dataToView() {
        //如果包含图片的文件列表为空
        if (mDirPaths == null) {
            Toast.makeText(this, "未扫描到任何图片", Toast.LENGTH_SHORT).show();
            return;
        }
        // 将文件夹中图片的路径包装成list返回,File.list()返回File文件夹中的所有文件路径,但是可以加过滤器
//        mImgs = filterImgPathsListFromFile(mCurrentDir);
//        mImageAdapter = new ImageAdapter(ChooseIconActivity.this, mImgs, mCurrentDir.getAbsolutePath());
        mImageAdapter = new ImageAdapter(ChooseIconActivity.this, mDirPaths);
        //gridView只需要设置当前需要显示的文件夹中的图片就可以了，当从弹出窗口切换文件夹的时候mDirName会改变为当前需要显示的文件夹
        mGridView.setAdapter(mImageAdapter);
//        mTvDirName.setText(mCurrentDir.getName());
    }

    /**
     * 初始化控件
     */
    private void initViews() {
        mGridView = (GridView) findViewById(R.id.gridView);
        mBottomLy = (RelativeLayout) findViewById(R.id.bottomLy);
        mTvDirName = (TextView) findViewById(R.id.dirName);
    }

    /**
     * 利用ContetnProvider开启单独线程遍历手机中所有图片
     */
    private void initDatas() {

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "当前存储卡不可用！", Toast.LENGTH_SHORT).show();
            return;
        }
        mProgressDialog = ProgressDialog.show(this, null, "正在加载...");
        new Thread() {
            @Override
            public void run() {

                //创建并初始化Uri对象为图片uri的对象,相当于数据库的图片表名,意味着查询的是本地的图片文件
                ///mImgUri=content://external/images/media
                Uri mImgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                //内容提供器ContentProvider的具体实现:(ContentResolver)内容解析器
                ContentResolver contentResolver = ChooseIconActivity.this.getContentResolver();
                //mime:Multipurpose Internet Mail Extensions:多用途互联网邮件扩展类型可表示扩展文件类型
                // 此处的语句必须要有空格，null表示检索所有的列（select *）
                //select * From mImageUri where mime_type =image/jpeg or mime_type=image/png
                //mimeType指媒体类型，image/jpeg,image/png都是媒体类型，其他比如还有:audio/mpeg4-generic(文本),video/*等
                // image/*表示所有图片类型  cursor:游标
                //最新被修改的文件最先展示，即查询的结果按文件的最后修改时间排序
                //给查询到的本地所有图片路径的结果设置一个从第一行之前开始的游标
                Cursor cursor = contentResolver.query(mImgUri, null,
                        MediaStore.Images.Media.MIME_TYPE + " = ? or " + MediaStore.Images.Media.MIME_TYPE + " = ? ",
                        new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);

//                Log.e(TAG, "run: 游标的总行数cursor.getCount()：" + cursor.getCount());
//                Log.e(TAG, "run: 游标的总列数cursor.getColumnCount()：" + cursor.getColumnCount());
//                Log.e(TAG, "run: cursor.getColumnIndex(MediaStore.Images.Media.DATA)=" + cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                //遍历每个列的名称，就是图片的id、名称、路径、尺寸、描述等等一些属性
                for (int i = 0; i < cursor.getColumnCount(); i++) {

                    String getColumnName = cursor.getColumnName(i);
//                    Log.e(TAG, "run: getColumnName=" + getColumnName);
                }
                mAllCount=cursor.getCount();
                boolean isFirstImagePath = true;


                //游标的初始位置在第一个结果的上一个位置，所以第一次moveToNext()是游标移动到第一个位置，第一列？？？？
                while (cursor.moveToNext()) {

                    // Path是每一张图片的路径
                    //cursor.getColumnIndex(MediaStore.Images.Media.DATA)=1,MediaStore.Images.Media.DATA="_data"表示图片路径那一列
                    //游标的总行数表示图片的总个数
                    //此处的getString必须要在光标所在行的列有值得情况下才有结果这里获得的是图片的外部存储器存储路径
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

                    if (isFirstImagePath) {
                        mFirstImgPathOfAll = path;
                        isFirstImagePath = false;
                    }
                    // 游标所对应的每行所对应的图片的父目录
                    File parentFile = new File(path).getParentFile();
                    if (parentFile == null)
                        continue;
                    // 图片的父目录绝对路径
                    String dirPath = parentFile.getAbsolutePath();
                    FolderBean folderBean = null;
                    //若此父目录文件夹路径已经添加过，那么光标移到下一行，所以目的只有一个，那就是找出所有包含图片的文件夹的目录
                    if (mDirPaths.contains(dirPath))
                        continue;
                    else {
                        //将图片父目录的文件夹路径加入到所有包含图片的文件夹目录集合中
                        mDirPaths.add(dirPath);
                        folderBean = new FolderBean();
                        //设置图片所在文件夹的路径
                        folderBean.setDir(dirPath);
                        //之所以图片是文件夹中的第一张(时间最新)，是因为遍历到同一个文件夹中的其他图片的时候就直接执行下一次循环了
                        folderBean.setFirstImgPath(path);
                    }
                    //父目录文件夹中所包含的文件列表，文件不一定就是图片
                    if (parentFile.list() == null)
                        continue;

                    // 图片所在文件夹下包含的图片个数
                    int picSize = getImgsCountFromFile(parentFile);

                    //设置文件夹中图片的个数
                    folderBean.setCount(picSize);
                    mFolderBeans.add(folderBean);
                    //这里是为了显示包含最多图片的文件夹
//                    if (picSize > mMaxCount) {
//                        mMaxCount = picSize;
//                        mCurrentDir = parentFile;
//                    }
                }
                cursor.close();

                mHandler.sendEmptyMessage(DATA_LOADED);
            }

        }.start();

    }

    /**
     * 过滤出当前文件夹中所有图片的不隐藏文件扩展名(文件名+扩展名)列表
     * 比如:girl.png,boy.jpg等等
     *
     * @param currentDir 当前文件夹对象
     * @return
     */
    private List<String> filterImgPathsListFromFile(File currentDir) {
        String currentDirPath = currentDir.getAbsolutePath();
        List<String> imgNamesList = Arrays.asList(currentDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png"))
                    return true;
                return false;
            }
        }));
        List<String> imgPathsList = new ArrayList<>();
        for (int i = 0; i < imgNamesList.size(); i++) {
            imgPathsList.add(currentDirPath + "/" + imgNamesList.get(i));
        }
        return imgPathsList;
    }

    /**
     * 获取当前文件夹图片的个数
     *
     * @param currentDir 当前文件夹对象
     * @return
     */
    private int getImgsCountFromFile(File currentDir) {
        int count = currentDir.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png"))
                    return true;
                return false;
            }
        }).length;
        return count;
    }

    /**
     * RelativeLayout点击事件
     */
    private void initEvent() {
        Log.e(TAG, "mBottomLy=" + mBottomLy);
        mBottomLy.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mDirPopupWindow.showAsDropDown(mBottomLy, 0, 0);
                mDirPopupWindow.setAnimationStyle(R.style.dir_popupwindow_anim);
                lightOff();

            }
        });
    }
}
