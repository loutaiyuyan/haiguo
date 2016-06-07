package com.xingchuang.haiguo.chooseicon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class ImageLoader {

    private static final String TAG = "ImageLoader";
    /**
     * 线程池默认线程个数
     */
    private static final int DEFAULT_THREAD_COUNT = 1;
    /**
     * ImageLoader的实例对象
     */
    private static ImageLoader mInstance;

    /**
     * 图片缓存的核心对象
     */
    private LruCache<String, Bitmap> mLruCache;
    /**
     * 线程池，以pool结尾的是线程池
     * 线程池里面实际上是一个Runnable任务队列,他最终执行的还是跟单个thread一样是任务队列
     */
    private ExecutorService mThreadPool;

    /**
     * 队列的调度方式为后进先出
     */
    private static Type mType = Type.LIFO;
    /**
     * 任务队列
     * LinkedList可以从队列的头部和尾部获得对象，采用链表存储方式，方便插入、删除操作
     * ArrayList属于顺序表，只能从前往后回去
     */
    private LinkedList<Runnable> mTaskQueue;
    /**
     * 后台轮询线程，以线程结尾的就是单个线程
     */
    private Thread mAskPoolThread;
    /**
     * 后台轮询线程的handler
     */
    private Handler mAskPoolThreadHandler;
    /**
     * UI线程中的Handler
     * 回调显示图片
     */
    private Handler mUIHandler= new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //获取得到的bitmap并且设置给imageView
            ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
            Bitmap bitmap = holder.bitmap;
            ImageView imageView = holder.imageView;
            String path = holder.path;
            //将path与getTag()存储路径进行比较
            if (imageView.getTag().toString().equals(path)) {
                imageView.setImageBitmap(bitmap);
            }
        }
    };;
    /**
     * 信号量（用于进程或者线程间同步），特别占用系统资源，0指信号量的最多个数
     * 信号量用来限制对某个共享资源进行访问的线程的个数
     */
    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);
    private Semaphore mSemaphorePoolThread;
//	private ImgBeanHolder holder=new ImgBeanHolder();
//	private ImageView imageView;
//	private Bitmap bitmap;

    /**
     * 枚举类型，是先进先出还是后进先出
     */
    public enum Type {
        FIFO, LIFO;
    }

    /**
     * 图片的宽高
     */
    private class ImageSize {
        //图片的宽度
        int width;
        int height;
    }

    /**
     * @param threadCount 线程池中线程的个数
     * @param type        图片加载的方式
     */
    public ImageLoader(int threadCount, Type type) {
        init(threadCount, type);
    }

    /**
     * 初始化
     *
     * @param threadCount
     * @param type
     */
    private void init(int threadCount, Type type) {
        mAskPoolThread = new Thread() {
            @Override
            public void run() {
                //线程轮询开始，保证handler在此线程中执行，looper的作用域是一个线程之内
                Looper.prepare();
                mAskPoolThreadHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        //访问线程池看看是否有任务需要执行，有则在线程池中执行
                        mThreadPool.execute(getTask());

                        try {
                            //信号量许可线程访问
                            mSemaphorePoolThread.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                //线程完成资源访问后向信号量归还许可
                //完成释放一个线程，那么处于等待状态的线程此时就可进来
                mSemaphorePoolThreadHandler.release();
                Looper.loop();
            }
        };
        mAskPoolThread.start();
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }

            ;
        };
        //创建固定的核心线程个数的线程池
        mThreadPool = Executors.newFixedThreadPool(threadCount);
        //初始化任务队列
        mTaskQueue = new LinkedList<Runnable>();
        mType = type;

        //信号量中最大线程个数为传入的threadCount即线程池的核心线程数
        mSemaphorePoolThread = new Semaphore(threadCount);
    }

    /**
     *单例模式，获取ImageLoader的实例,并且ImageLoader只是bitmap加载到ImageView的加载器，不需要知道图片所在文件夹等属性
     */
    public static ImageLoader getInstance(int threadCount, Type type) {
        if (mInstance == null) {
            // 同步,只能同时有一个线程访问此代码块
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(threadCount, type);
                }
            }
        }
        return mInstance;
    }

    /**
     * 从线程池中的任务队列获取任务
     *
     * @return
     */
    private Runnable getTask() {
        if (mType == Type.FIFO) {
            return mTaskQueue.removeFirst();
        } else if (mType == Type.LIFO) {
            return mTaskQueue.removeLast();
        }
        return null;
    }

    ;


    /**
     * 根据path 为imageView设置压缩后的图片
     *
     * @param path
     * @param imageView
     */
    public void loadSampledImage(final String path, final ImageView imageView) {
        Log.i("scx", "loadImage path=" + path);
        imageView.setTag(path);
        Bitmap bm = getBitmapFromCache(path);
        if (bm != null) {
            refreshImageView(path, imageView, bm);
        } else {
            addTasks(new Runnable() {
                @Override
                public void run() {
                    //加载图片
                    //图片的压缩
                    //1.获取图片需要请求显示的大小
                    ImageSize imageSize = getImageViewSize(imageView);
                    //2.压缩图片,将路径为path的bitmap请求压缩为第一步中请求图片显示的大小为
                    //0???????
                    Bitmap bitmap = decodeSampleBitmapFromPath(path, imageSize.width, imageSize.height);
                    //3.将图片加入到缓存
                    addBitmapToLruCache(path, bitmap);
                    refreshImageView(path, imageView, bitmap);
                }
            });
        }
    }

    /**
     * 加载未经任何压缩处理的图片
     * @param path
     * @param imageView
     */
    public void loadCompleteImage(final String path, final ImageView imageView) {
        Log.i("scx", "loadImage path=" + path);
        imageView.setTag(path);
        Bitmap bm = getBitmapFromCache(path);
        if (bm != null) {
            refreshImageView(path, imageView, bm);
        } else {
            addTasks(new Runnable() {
                @Override
                public void run() {
                    //加载图片
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    //3.将图片加入到缓存
                    addBitmapToLruCache(path, bitmap);
                    refreshImageView(path, imageView, bitmap);
                }
            });
        }
    }

    /**
     * 通过mUIHandler向主线程发送消息请求更新ImageView的图片
     * @param path
     * @param imageView
     * @param bm
     */
    private void refreshImageView(final String path, final ImageView imageView, Bitmap bm) {
        Message message = Message.obtain();
        //将bitmap,path,imageView封装好一并发送给mUIHandler
        ImgBeanHolder holder = new ImgBeanHolder();
        holder.bitmap = bm;
        holder.path = path;
        holder.imageView = imageView;
        message.obj = holder;
        mUIHandler.sendMessage(message);
    }

    protected void addBitmapToLruCache(String path, Bitmap bitmap) {
        if (getBitmapFromCache(path) == null) {
            if (bitmap != null) {
                mLruCache.put(path, bitmap);
            }
        }
    }

    /**
     *
     * @param path 图片的完整路径
     * @param width
     * @param height
     * @return
     */
    protected Bitmap decodeSampleBitmapFromPath(String path, int width, int height) {

        Options options = new Options();
        options.inJustDecodeBounds = true;
        //解析路径为path为bitmap并且将bitmap的属性加到options上
        Log.i("scx", "bitmap正在解析图片路径path=" + path);
        BitmapFactory.decodeFile(path, options);

        //计算压缩的倍数（比如inSampleSize=4,那么bitmap的宽高压缩为原来的1/4）
        options.inSampleSize = caculateInsampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    private int caculateInsampleSize(Options options, int reqWidth, int reqHeight) {
        //根据需求的大小和实际大小计算sampleSize
        //获取bitmap的实际大小
        int width = options.outWidth;
        int height = options.outHeight;
        Log.i("scx", "实际width=" + width);
        Log.i("scx", "reqWidth=" + reqWidth);
        Log.i("scx", "图片实际height=" + height);
        Log.i("scx", "reqHeight=" + reqHeight);
        int inSampleSize = 1;
        if (width > reqWidth || height > reqHeight) {
            //获取实际大小与需求大小的比值
            int widthRadio = Math.round(width * 1.0f / reqWidth);
            int heightRadio = Math.round(height * 1.0f / reqHeight);
            inSampleSize = Math.max(widthRadio, heightRadio);
            Log.i("scx", "widthRadio=" + widthRadio);
            Log.i("scx", "heightRadio=" + heightRadio);
            Log.i("scx", "inSampleSize=" + inSampleSize);
        }
        return inSampleSize;
    }

    private ImageSize getImageViewSize(final ImageView imageView) {
        ImageSize imageSize = new ImageSize();
        DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();
        LayoutParams lp = imageView.getLayoutParams();
        int width = imageView.getWidth();
        if (width <= 0) {
            width = lp.width;
            Log.i("info", "lp.width<=0     width=" + width);
        }
        if (width <= 0) {
            width = getImageFieldValue(imageView, "mMaxWidth");
        }
        if (width <= 0) {
            width = displayMetrics.widthPixels;
        }
        imageSize.width = width;
        int height = imageView.getHeight();
        if (height <= 0) {
            height = lp.height;
        }
        if (height <= 0) {
            height = getImageFieldValue(imageView, "mMaxHeight");
        }
        if (height <= 0) {
            height = displayMetrics.heightPixels;
        }
        imageSize.height = height;
        //此处一定要将width与height的值赋给imageSize
        return imageSize;
    }

    private static int getImageFieldValue(Object object, String fileName) {
        Log.i("info", "getImageFieldValue()");
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fileName);
            field.setAccessible(true);
            int fieldValue = field.getInt(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        Log.i("scx", "value=" + value);
        return value;
    }

    //只能同时添加一个任务到任务队列
    private synchronized void addTasks(Runnable runnable) {
        //添加到任务队列
        mTaskQueue.add(runnable);
        if (mAskPoolThreadHandler == null) {
            try {
                mSemaphorePoolThreadHandler.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mAskPoolThreadHandler.sendEmptyMessage(0x110);
        mSemaphorePoolThread.release();

    }

    private Bitmap getBitmapFromCache(String path) {
        return mLruCache.get(path);
    }

    private class ImgBeanHolder {
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }
}
