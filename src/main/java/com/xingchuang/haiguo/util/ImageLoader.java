package com.xingchuang.haiguo.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import com.jakewharton.disklrucache.DiskLruCache;
import com.xingchuang.haiguo.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by scx on 2016/5/4 0004.
 */
public class ImageLoader {
    private static final String TAG = "ImageLoader";

    /**
     * 主线程的handler向主线程发送的消息
     */
    private static final int MESSAGE_POST_RESULT = 1;
    /**
     *不直接按照缩放比例缩放bitmap,而是按照请求的宽高
     */
    private static final int NOT_DEPEND_INSAMPLESIZE = 0;

    //Runtime.getRuntime()用于获取Runtime类的实例
    /**
     * java虚拟机可用的处理器的个数
     */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * 线程池的核心线程数为处理器的个数+1
     */
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    /**
     * 线程池所能容纳的最大线程数
     */
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    /**
     * 非核心线程闲置的超时时长（10秒），同样可设置作用于核心线程
     */
    private static final long KEEP_ALIVE = 10;
    private static final int TAG_KEY_URI = R.id.imageloader_uri;

    /**
     * 磁盘缓存大小50M
     */
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;
    /**
     * IO流缓存的大小为8Kb
     */
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    /**
     * DiskLruCache的open方法中设置了一个节点只能有一个数据
     * 即open方法的第三个参数设为1
     */
    private static final int DISK_CACHE_INDEX = 0;
    private int mInSampleSize;
    /**
     * 判断是否创建了磁盘缓存目录
     */
    private boolean mIsDiskLruCacheCreated = false;

    /**
     * 线程工厂，为线程池提供创建新线程的功能。ThreadFactory是一个接口
     * 它只有一个方法:Thread new Thread(Runnable r)
     * 不允许创建接口的实例，但允许定义接口类型的引用变量，该变量指向了实现接口的类的实例。
     */
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        //atomic:原子的，原子能的；Integer:整数
        //程序的原子性即要么执行完整成功，要么完全不执行保持原来的状态
        //开启多个线程时候用volatile修饰变量也会发生线程并发问题，那样会导致变量值同时在两个线程改变相同的值
        //从而没有连续改变带来的效果，比如count++
        //volatile对线程内存可见，表示对一个volatile变量的读，总是能看到（任意线程）对这个volatile变量最后的写入
        //对单个volatile读/写具有原子性，而类似于volatile++这种复合操作不具有原子性
        //AtomicInteger是一个提供原子操作的Integer类，可防止线程并发
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            //Increment：增量，增长
            //getAndIncrement方法表示对在构造方法中传入的数值进行+1操作
            return new Thread(r, "ImageLoader#" + mCount.getAndIncrement());
        }
    };
    /**
     * 我是接口的一个例子
     */
    private static final View.OnClickListener onclick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

        }
    };
    /**
     * ThreadPoolExecutor是线程池的真正实现
     */
    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS
            , new LinkedBlockingQueue<Runnable>(), sThreadFactory);
    /**
     * 作用于主线程的handler,用于更新UI
     */
    private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            LoaderResult result = (LoaderResult) msg.obj;
            ImageView imageView = result.imageView;
//            imageView.setImageBitmap(result.bitmap);
            String uri = (String) imageView.getTag(TAG_KEY_URI);
            if (uri.equals(result.uri)) {
                imageView.setImageBitmap(result.bitmap);

            } else {
                Log.w(TAG, "set image bitmap,but url has changed,ignored!");
            }
        }
    };
    private Context mContext;
    private ImageResizer mImageResizer = new ImageResizer();
    /**
     * 内存缓存（当前进程可用内存的1/8）
     */
    private LruCache<String, Bitmap> mMemoryCache;
    /**
     * 磁盘缓存（50M）
     */
    private DiskLruCache mDiskLruCache;

    private ImageLoader(Context context,int inSampleSize) {

        mContext = context.getApplicationContext();
        mInSampleSize = inSampleSize;
        //单位是KB
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
        //在缓存目录下获取名称为bitmap的文件夹
        File diskCacheDir = getDiskCacheDir(mContext, "bitmap");
        if (!diskCacheDir.exists()) {
            //mkdir():创建此抽象路径名指定的目录。
            //mkdirs():创建此抽象路径名指定的路径，包括所有必须但不存在的父目录
            //如果此文件夹不存在则创建
            diskCacheDir.mkdirs();
        }
        //如果可用磁盘内存空间大于需要的磁盘缓存空间，则创建缓存大小为50M的磁盘缓存对象
        if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
            try {
                //缓存目录为默认缓存目录下的bitmap文件夹，版本号为1，每个节点所对应的数据的个数为1，缓存的总大小为50M
                mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
                //磁盘缓存全部创建成功
                mIsDiskLruCacheCreated = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * build a new instance of ImageLoader
     *
     * @param context
     * @return a new instance of ImageLoader
     */
    public static ImageLoader getInstance(Context context,int inSampleSize) {
        return new ImageLoader(context,inSampleSize);
    }

    /**
     * 将bitmap添加到内存缓存中,这里只在从磁盘缓存加载bitmap中进行内存缓存
     * 因为磁盘缓存过程中从网络将图片解析成了bitmap,方便进行缓存
     *
     * @param key    图片url转化过后的到的值
     * @param bitmap 需要缓存的bitmap
     */
    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * 从内存缓存中通过key获取bitmap,在loadBitmapFromMemCache中实现了此方法
     *
     * @param key
     * @return
     */
    private Object getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    /**
     * load bitmap from memory cache or disk cache or network async,then bind
     * imageView and bitmap
     * NOTE THAT: should run in UI Thread
     *
     * @param uri       http url
     * @param imageView bitmap's bind object
     */
    /**
     * 通过url将bitmap与imageView绑定,即显示bitmap到ImageView
     *
     * @param uri
     * @param imageView
     */
    public void bindBitmap(final String uri, final ImageView imageView) {
        bindBitmap(uri, imageView, 0, 0);
    }

    /**
     * ImageLoader的外界调用的方法
     * 先加载相应的bitmap再给主线程发送消息将bitmap设置给imageView
     * @param uri
     * @param imageView
     * @param reqWidth
     * @param reqHeight
     */
    public void bindBitmap(final String uri, final ImageView imageView, final int reqWidth, final int reqHeight) {
        //给imageView设置tag为图片的url
        imageView.setTag(TAG_KEY_URI, uri);
        //通过url从内存缓存中加载bitmap，其中要先将uri转化成字符数组再转化成16进制格式的字符串再getBitmapFromMemCache
        Bitmap bitmap = loadBitmapFromMemCache(uri);
        //如果内存缓存中存在此bitmap则直接设置给imageView
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }
        //若内存缓存中没有则通过线程池开启新的线程任务加载bitmap
        Runnable loadBitmapTask = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmap(uri, reqWidth, reqHeight);
                if (bitmap != null) {
                    LoaderResult result = new LoaderResult(imageView, uri, bitmap);
                    mMainHandler.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget();
                }
            }
        };
        THREAD_POOL_EXECUTOR.execute(loadBitmapTask);

    }

    /**
     * 从内存缓存或磁盘缓存或者网络加载bitmap
     * 1.将url转化为他的md5值key
     * 2.通过key获取缓存中的bitmap
     *
     * @param uri       http url
     * @param reqWidth  the width ImageVIew desired
     * @param reqHeight the height ImageView desired
     * @return bitmap, maybe null
     */
    private Bitmap loadBitmap(String uri, int reqWidth, int reqHeight) {
        //从内存缓存中获取bitmap,其实在bindBitmap已经判断过内存缓存中没有对应的bitmap了
        Bitmap bitmap = loadBitmapFromMemCache(uri);
        if (bitmap != null) {
            Log.d(TAG, "loadBitmapFromMemCache,url:" + uri);
            return bitmap;
        }
        try {
            bitmap = loadBitmapFromDiskCache(uri, reqWidth, reqHeight);
            if (bitmap != null) {
                Log.d(TAG, "loadBitmapFromDisk,url:" + uri);
                return bitmap;
            }
            //内存磁盘缓存中都没有时从网络加载bitmap，实际上内部还是先从网络下载图片输入流存储到磁盘再从磁盘缓存中加载bitmap
            bitmap = loadBitmapFromHttp(uri, reqWidth, reqHeight);
            Log.d(TAG, "loadBitmapFromHttp,url:" + uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //若是bitmap从网络下载好了但是磁盘缓存目录没有创建成功则直接从网络下载图片并解析为bitmap
        if (bitmap != null && !mIsDiskLruCacheCreated) {
            Log.w(TAG, "encounter error,DiskLruCache is not created.");
            bitmap = downloadBitmapFromUrl(uri);
        }
        return bitmap;


    }

    private Bitmap loadBitmapFromMemCache(String url) {
        //hash:散列，哈希，把...搞乱
        //图片的url很可能还有特殊字符，这将影响url在Android中直接使用，一般采用url的md5值作为key
        final String key = hashKeyFromUrl(url);
        Bitmap bitmap = (Bitmap) getBitmapFromMemCache(key);
        return bitmap;
    }

    /**
     * 从网络加载bitmap：先将bitmap添加到磁盘缓存中再磁盘缓存中加载bitmap
     * @param url
     * @param reqWidth
     * @param reqHeight
     * @return
     * @throws IOException
     */
    private Bitmap loadBitmapFromHttp(String url, int reqWidth, int reqHeight) throws IOException {
        //不能在主线程请求网络数据
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("can not visit network from UI Thread.");
        }
        //前提要创建磁盘缓存
        if (mDiskLruCache == null) {
            return null;
        }
        String key = hashKeyFromUrl(url);
        //DiskLruCache的缓存添加通过Editor来完成
        //一个key(缓存)只能有一个Editor对象，多次DiskLruCache.edit(key)则会返回空
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if (editor != null) {
            //通过editor对象得到key对应bitmap的文件输出流，而且一个节点只有一个数据所以传入0
            OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
            if (downloadUrlToStream(url, outputStream)) {
                editor.commit();
            } else {
                //回退整个操作
                editor.abort();
            }
            mDiskLruCache.flush();
        }

        return loadBitmapFromDiskCache(url, reqWidth, reqHeight);
    }

    /**
     * 从磁盘缓存中获取缓存好的bitmap，若没有则返回null,若有则将bitmap添加到内存缓存中并返回
     * @param url
     * @param reqWidth
     * @param reqHeight
     * @return
     * @throws IOException
     */
    private Bitmap loadBitmapFromDiskCache(String url, int reqWidth, int reqHeight) throws IOException {
        //不推荐在UI线程中进行IO文件操作
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.w(TAG, "load bitmap from UI Thread,it is not recommended!");
        }
        if (mDiskLruCache == null) {
            return null;
        }
        Bitmap bitmap = null;
        String key = hashKeyFromUrl(url);
        //Snapshot：数据快照，可通过他的对象的到缓存的文件输入流
        DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
        if (snapShot != null) {
            FileInputStream fileInputStream = (FileInputStream) snapShot.getInputStream(DISK_CACHE_INDEX);
            //通过文件流得到文件所对应的文件描述符
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            //通过BitmapFactory.Options对象来加载一张缩放后的图片此方法对FileInputStream的缩放存在问题，原因是FileInputStream
            //是一种有序的文件流，而两次decodeStream调用影响了文件流的位置属性
            if (mInSampleSize==NOT_DEPEND_INSAMPLESIZE) {
                Log.e(TAG, "loadBitmapFromDiskCache: mInSampleSize==NOT_DEPEND_INSAMPLESIZE");
            bitmap = mImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor, reqWidth, reqHeight);
            } else if (mInSampleSize >= 1) {
                bitmap = mImageResizer.decodeSampledBitmapDependInSampleSizeFromFileDescriptor(fileDescriptor, mInSampleSize);
            } else {
                Log.e(TAG, "loadBitmapFromDiskCache: 缩放比例输入有误！");
            }
            if (bitmap != null) {
                addBitmapToMemoryCache(key, bitmap);
            }
        }
        return bitmap;
    }

    //通过key对应的输出流将从网络获取的图片的输入流（whilie(in.read()!=-1)）写入到文件系统上，即缓存目录下
    //因为key对应的输出流就在缓存目录的bitmap目录下
    private boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
        HttpURLConnection urlConnection = null;
        //比outputStream提高了输出效率，因为有一个在内存的缓冲区(8kb),凑够缓冲区大小才向磁盘写入数据
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

            int b;
            //将输入流的内容全部读完并通过输出流写入磁盘
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;

        } catch (IOException e) {
            Log.e(TAG, "downloadBitmap failed." + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            MyUtils.close(out);
            MyUtils.close(in);
        }
        return false;
    }


    /**
     * 通过BitmapFactory的decodeStream方法将网络输入流解析为bitmap
     * @param urlString
     * @return
     */
    private Bitmap downloadBitmapFromUrl(String urlString) {
        Bitmap bitmap = null;
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            //通过BitmapFactory的decodeStream方法将输入流解析为bitmap
            bitmap = BitmapFactory.decodeStream(in);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "Error in downloadBitmap: " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            MyUtils.close(in);

        }

        return bitmap;
    }

    /**
     * 通过url的md5值(是一个byte数组)，还需要转化成16进制的字符串格式从而得到缓存bitmap对应的key
     * @param url
     * @return
     */
    private String hashKeyFromUrl(String url) {
        String cacheKey;
        try {
            //Digest:摘要
            //java.security.MessageDigest类用于为应用程序提供信息摘要算法的功能，如 MD5 或 SHA 算法。简单点说就是用于生成散列码
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            //使用指定的字符数组更新摘要，我理解为将字符数组放入MessageDigest的对象mDigest中，方便继续操作（生成字符数组的md5值）
            mDigest.update(url.getBytes());
            //mDigest.digest():通过执行诸如填充之类的最终操作完成哈希计算,返回经过计算的字节数组
            cacheKey = bytesToHexString(mDigest.digest());
            //抛出没有此算法异常
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    /**
     * 将字节数组转化成16进制的字符串格式
     * @param bytes
     * @return
     */
    private String bytesToHexString(byte[] bytes) {
        //String 字符串常量,因此每次改变String类型的值时其实都在创建一个新的String对象，然后将指针指向新的String对象，所以会剩余很多
        //的无引用对象，这对系统性能影响是很不好的
        //StringBuffer 字符串变量（线程安全）
        //StringBuilder 字符串变量（非线程安全）,用在字符串缓冲区被单个线程使用的时候，它比StringBuffer要快，两者的方法基本相同
        StringBuilder sb = new StringBuilder();
        //将byte数组转化为16进制的字符串,其中0xFF&bytes[i]是将byte[i]转化成int类型的数据，byte8位，int32位，0xFF就是0x000000FF故0xFF&bytes[i]
        // 可将int的前24位清零，因为byte只有8位
        //122 23 46 98
        for (int i = 0; i < bytes.length; i++) {
            //toHexString里的参数是int
            //返回int型数据的16进制字符串表示形式
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }

        return sb.toString();
    }


    /**
     * 从磁盘缓存获取具体的缓存好的文件而不是文件夹
     *
     * @param context
     * @param uniqueName 唯一的名字
     * @return
     */

    private File getDiskCacheDir(Context context, String uniqueName) {
        //判断外部存储路径是否可用，Environment.MEDIA_MOUNTED表示安装好的
        boolean externalStorageAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        //磁盘缓存路径，首选外部缓存路径，因为是磁盘缓存嘛不占系统内存最好
        final String cachePath;
        if (externalStorageAvailable) {
            //获取外部缓存路径即/sdcard/Android/data/<application package>/cache这个路径
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            //获取内部缓存路径即 /data/data/<application package>/cache 这个路径
            cachePath = context.getCacheDir().getPath();
        }

        //specified:指定的；separator：分离器
        //File.separator是与系统有关的默认名称（路径）分隔符，windows下是"\",Unix下是"/"
        //File对象既可以是文件夹也可以是具体的文件
        //这里返回的File对象是具体的文件
        return new File(cachePath + File.separator + uniqueName);
    }

    private long getUsableSpace(File path) {
        //如果安卓SDK版本大于等于2.3则可以直接通过文件获取可用的磁盘内存空间
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return path.getUsableSpace();
        }
        //StatFs类主要用于获取文件系统的状态，能够获取sd卡的大小和剩余空间
        //这里应该就是获取当前文件所在文件夹的剩余存储空间
        final StatFs stats = new StatFs(path.getPath());
        //block：块；障碍物
        return stats.getBlockSize() * stats.getAvailableBlocks();
    }


    /**
     * 将相应的ImageView、uri、bitmap进行绑定防止错乱
     */
    private static class LoaderResult {
        public ImageView imageView;
        public String uri;
        public Bitmap bitmap;

        public LoaderResult(ImageView imageView, String uri, Bitmap bitmap) {
            this.imageView = imageView;
            this.uri = uri;
            this.bitmap = bitmap;
        }
    }

}
