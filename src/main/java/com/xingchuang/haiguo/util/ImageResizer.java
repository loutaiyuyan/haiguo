package com.xingchuang.haiguo.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileDescriptor;

/**
 * Created by Administrator on 2016/5/5 0005.
 */
public class ImageResizer {
    private static final String TAG = "ImageResizer";

    // 传入资源文件，计算合适的大小，返回一个Bitmap对象
    public Bitmap decodeSampledBitmapFromResource(Resources res,
                                                  int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }


    /**
     为了加载文件系统的文件避免二次decodeStream返回null而专门调整的方法decode方法
     解释：
     FileInputStream是一种有序的文件流，而两次的decodeStream调用影响了文件流的位置属性，
     导致了第二次decodeStream时得到的是null，为了解决这个问题，可以通过文件流来得到他所对应的文件描述符，
     然后在通过 BitmapFactory.decodeFileDescriptor方法来加载一张缩放过的图片
     */
    public Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fd, int reqWidth, int reqHeight) {


        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //Descriptor:描述符
        //用decodeFileDescriptor()来生成bitmap比decodeFile()省内存
        BitmapFactory.decodeFileDescriptor(fd, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd, null, options);
    }

    /**
     * 根据整体缩放比例直接压缩图片，
     * @param fd
     * @param inSampleSize 取1,2(1/4),4(1/16),8(1/64)
     * @return
     */
    public Bitmap decodeSampledBitmapDependInSampleSizeFromFileDescriptor(FileDescriptor fd, int inSampleSize) {


        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //Descriptor:描述符
        //用decodeFileDescriptor()来生成bitmap比decodeFile()省内存
        BitmapFactory.decodeFileDescriptor(fd, null, options);

        // Calculate inSampleSize
        options.inSampleSize = inSampleSize;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd, null, options);
    }

    public int calculateInSampleSize(BitmapFactory.Options options,
                                     int reqWidth, int reqHeight) {
        if (reqWidth == 0 || reqHeight == 0) {
            return 1;
        }

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        Log.d(TAG, "origin, w= " + width + " h=" + height);
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        Log.d(TAG, "sampleSize:" + inSampleSize);
        return inSampleSize;
    }
}
