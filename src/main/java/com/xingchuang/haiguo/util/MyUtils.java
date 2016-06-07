package com.xingchuang.haiguo.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2016/5/5 0005.
 */
public class MyUtils {

    public static void close(InputStream inputStream) {
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void close(OutputStream out) {
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close(BufferedInputStream bufferedInputStream) {
        try {
            bufferedInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close(BufferedOutputStream bufferedOutputStream) {
        try {
            bufferedOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
