package com.example.linqi.my_jni;

import org.opencv.core.Mat;


/**
 * Created by linqi on 16-1-18.
 */
public class NdkUtils {

    double time;
    NdkUtils() {
        time = 0;
    }

    public native boolean gray(int[] dstImage,int w,int h);
    public native long processing();
    public native void grayImage(int[][] srcImage,int[] dstImage,int width,int height);
    public native void initOpenGLES();
    public native long addPicture();

    static {
        System.loadLibrary("opencv_java");
        System.loadLibrary("AddPictureLib");   //defaultConfig.ndk.moduleName
    }
}
