#include "myjni.h"
#include <android/log.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <GLES2/gl2platform.h>
#include <stdio.h>
#include <stdlib.h>
#include "include/PerspectiveAdd.h"


/*static const string picPath[] = {
        "/mnt/obb/pic1/01.jpg",
        "/mnt/obb/pic1/02.jpg",
        "/mnt/obb/pic1/03.jpg",
        "/mnt/obb/pic1/04.jpg",
        "/mnt/obb/pic1/05.jpg",
        "/mnt/obb/pic1/06.jpg",
        "\0"
};*/

/*
static const string picPath[] = {
        "/mnt/obb/pic3/01.jpg",
        "/mnt/obb/pic3/02.jpg",
        "/mnt/obb/pic3/03.jpg",
        "/mnt/obb/pic3/04.jpg",
        "/mnt/obb/pic3/05.jpg",
        "/mnt/obb/pic3/06.jpg",
        "\0"
};*/

// final int w = 3264;
//final int h = 2448;

static const string picPath[] = {
        "/mnt/obb/Capture/1.jpg",
        "/mnt/obb/Capture/2.jpg",
        "/mnt/obb/Capture/3.jpg",
        "/mnt/obb/Capture/4.jpg",
        "/mnt/obb/Capture/5.jpg",
        "/mnt/obb/Capture/6.jpg",
        "\0"
};



// final int w = 4160;
//final int h = 3104;

/*
static const string picPath[] = {
        "/mnt/obb/pic5/01.jpg",
        "/mnt/obb/pic5/02.jpg",
        "/mnt/obb/pic5/03.jpg",
        "/mnt/obb/pic5/04.jpg",
        "/mnt/obb/pic5/05.jpg",
        "/mnt/obb/pic5/06.jpg",
        "\0"
};*/



static double work_begin = 0;
static double work_end = 0;
static double gTime = 0;
//开始计时
static void workBegin()
{
    work_begin = getTickCount();
}
//结束计时
static void workEnd()
{
    work_end = getTickCount() - work_begin;
    gTime = work_end /((double)getTickFrequency() )* 1000.0;
    LOGE("TIME = %lf ms \n",gTime);
}

static vector <Mat> g_picVec;
static vector <Mat> g_grayVec;

static PerspectiveAdd g_APUnit;

JNIEXPORT void JNICALL initOpenGLES(JNIEnv *env, jobject obj)
{
    g_picVec.clear();
    g_grayVec.clear();
    for(int i = 0; i < 6; i++)
    {
        Mat temp;
        temp = imread(picPath[i]);
        g_picVec.push_back(temp);
        cvtColor(temp,temp,CV_RGB2GRAY);
        g_grayVec.push_back(temp);
    }
    g_APUnit.initOpenGLES(g_picVec,g_grayVec);
}

JNIEXPORT jlong JNICALL processing(JNIEnv *env, jobject obj)
{
    jfieldID  nameFieldId ;
    jclass cls = env->GetObjectClass(obj);  //获得Java层该对象实例的类引用，即HelloJNI类引用
    nameFieldId = env->GetFieldID(cls ,"time", "D"); //获得属性句柄
    if(nameFieldId == NULL)
    {
        LOGE("LOGE: 没有得到 TIME 的句柄ID \n");
    }

    workBegin();
    Mat outMat;
    int HomMethod = LMEDS; // RHO   RANSAC LMEDS
    g_APUnit.setMode(HomMethod);
    g_APUnit.Progress(outMat);
    Mat *imgData = new Mat(outMat);
    LOGE("SUM TIME COUNT");
    workEnd();

    env->SetDoubleField(obj,nameFieldId ,gTime); // 设置该字段的值
    return (jlong)imgData;
}



static const char *className = "com/example/linqi/my_jni/NdkUtils";

//定义方法隐射关系
static JNINativeMethod methods[] = {
        {"processing","()J",(void*)processing},
        {"initOpenGLES","()V",(void*)initOpenGLES},
};

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
//声明变量
    jint result = JNI_ERR;
    JNIEnv* env = NULL;
    jclass clazz;
    int methodsLenght;

//获取JNI环境对象
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("ERROR: GetEnv failed\n");
        return JNI_ERR;
    }
    assert(env != NULL);

//注册本地方法.Load 目标类
    clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGE("Native registration unable to find class '%s'", className);
        return JNI_ERR;
    }

//建立方法隐射关系
//取得方法长度
    methodsLenght = sizeof(methods) / sizeof(methods[0]);
    if (env->RegisterNatives(clazz, methods, methodsLenght) < 0) {
        LOGE("RegisterNatives failed for '%s'", className);
        return JNI_ERR;
    }

    result = JNI_VERSION_1_4;
    return result;
}

jint JNI_Unload(JavaVM* vm,void* reserved)
{
    jint result = JNI_ERR;
    JNIEnv* env = NULL;

    LOGI("JNI_OnUnload!");
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("ERROR: GetEnv failed");
        return JNI_ERR;
    }
    result = JNI_VERSION_1_4;
    return result;
}


