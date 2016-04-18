#include "myjni.h"
#include <android/log.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <GLES2/gl2platform.h>
#include <stdio.h>
#include <stdlib.h>
#include "include/PerspectiveAdd.h"
#include <dirent.h>


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

JNIEXPORT void JNICALL initOpenGLES(JNIEnv *env, jobject obj,jcharArray path,jint length)
{
    g_picVec.clear();
    g_grayVec.clear();
    jchar *array;
    char *buf;
    int i;
    array = env->GetCharArrayElements( path, NULL);//复制数组元素到array内存空间
    if(array == NULL){
        LOGE("initOpenGLES: GetCharArrayElements error.");
    }
    buf = (char *)calloc(length , sizeof(char));
    //开辟jboolean类型的内存空间，jboolean对应的c++类型为unsigned char
    if(buf == NULL){
        LOGE("initOpenGLES: calloc error.");
    }
    for(i=0; i < length; i++){
        //把jcharArray的数据元素复制到buf所指的内存空间
        *(buf + i) = (char)(*(array + i));
        //LOGD("buf[%d]=%c\n",i,*(buf+i));
    }

    char picPath[255];
    int nameLength = sizeof("/0.jpg");
    memset(picPath,0,sizeof(picPath));
    memcpy(picPath,buf,length);

    for(int i = 0; i < 6; i++)
    {
        switch(i)
        {
            case 0:memcpy(picPath+length,"/1.jpg",nameLength);break;
            case 1:memcpy(picPath+length,"/2.jpg",nameLength);break;
            case 2:memcpy(picPath+length,"/3.jpg",nameLength);break;
            case 3:memcpy(picPath+length,"/4.jpg",nameLength);break;
            case 4:memcpy(picPath+length,"/5.jpg",nameLength);break;
            case 5:memcpy(picPath+length,"/6.jpg",nameLength);break;
            default:break;
        }
        //LOGE("LOGE: path = %s \n",picPath);
        Mat temp;
        temp = imread(picPath);
        g_picVec.push_back(temp);
        cvtColor(temp,temp,CV_RGB2GRAY);
        g_grayVec.push_back(temp);
    }
    g_APUnit.initOpenGLES(g_picVec,g_grayVec);
    env->ReleaseCharArrayElements(path, array, 0);//释放资源
    free(buf);//释放内存空间
    buf = NULL;
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
        {"initOpenGLES","([CI)V",(void*)initOpenGLES},
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


