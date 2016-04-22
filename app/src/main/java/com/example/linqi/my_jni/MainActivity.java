package com.example.linqi.my_jni;
//armeabi-v7a

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.text.TextUtils;
import com.example.imageview.PinchImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity
{
    private static final  String TAG = "MainActivity";
    TextView mTextView;
    PinchImageView mOriImageView;
    PinchImageView mImageView;
    Button mButton;
    ProgressDialog mProcessingDialog;
    Button mCaptureButton;
    Bitmap mFinalBitmap;
    Bitmap m_Tmpbmp;
    boolean mInitOpenGL = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProcessingDialog = new ProgressDialog(this);
        mProcessingDialog.setIndeterminate(true);
        mProcessingDialog.setCancelable(false);

        mTextView = (TextView) this.findViewById(R.id.text);
        mImageView = (PinchImageView) this.findViewById(R.id.imageView);
        mOriImageView = (PinchImageView) this.findViewById(R.id.imageView_ori);

        mFinalBitmap = Bitmap.createBitmap(3264 ,2448 ,Bitmap.Config.ARGB_8888);

        mButton = (Button) this.findViewById(R.id.button);
        mButton.setEnabled(false);
        mTextView.setText("addPicture Demo!");
        mCaptureButton = (Button)findViewById(R.id.capture_btn);
        String state = Environment.getExternalStorageState(); // 判断是否存在sd卡
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Log.i("MainActivity:", " 手机SD卡已挂载!");
            File parentPath = Environment.getExternalStorageDirectory();
            String storagePath = parentPath.getAbsolutePath() + "/APCamera/";
            File f = new File(storagePath);
            if(!f.exists()){
                f.mkdir();
            }

        } else {
            Toast.makeText(MainActivity.this, "请检查手机是否有SD卡", Toast.LENGTH_LONG).show();
        }

        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFolderFile("/data/isptune",true);
                ComponentName cn = new ComponentName("com.android.camera2", "com.android.camera.CameraLauncher");
                //ComponentName cn = new ComponentName("com.example.linqi.my_jni", "com.example.camera.CameraActivity");
                Intent intent = new Intent();
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProcessingDialog.setMessage("Please wait while processing images...");
                mProcessingDialog.show();
                mButton.setEnabled(false);
                mWorkThread.setMsg(WorkThread.STATE_RUN);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        mWorkThread = new WorkThread();
        mWorkThread.setPriority(10);
        mWorkThread.start();

        //File file = getDir("image", Context.MODE_PRIVATE);
        File file = new File("/data/isptune");
        if (file.isDirectory()) {
            String[] files = file.list();
            if (files.length == 6) {
                mButton.setEnabled(true);
                mInitOpenGL = true;
            }
        }
        if(mInitOpenGL)
        {
            mProcessingDialog.setMessage("intialize environment...");
            mProcessingDialog.show();
            mWorkThread.setMsg(WorkThread.STATE_INIT);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mWorkThread.setMsg(WorkThread.STATE_EXIT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWorkThread.setMsg(WorkThread.STATE_EXIT);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWorkThread.setMsg(WorkThread.STATE_EXIT);
    }


    /**
     * 删除指定目录下文件及目录
     * @param deleteThisPath
     * @param filepath
     * @return
     */
    public void deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {// 处理文件夹
                    File files[] = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        deleteFolderFile(files[i].getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (file.isFile()) {// 如果是文件，删除
                        file.delete();
                    } /*else {//处理文件夹
                        if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除
                            file.delete();
                        }
                    }*/
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void saveBitmap(Bitmap b)
    {
        File parentPath = Environment.getExternalStorageDirectory();
        String path = parentPath.getAbsolutePath();
        long currentTime = System.currentTimeMillis();
        currentTime = currentTime % 1000000;
        String jpegName = path + "/APCamera/" + "IMG_" + currentTime +".jpg";
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            Log.i(TAG, "saveResult success");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.i(TAG, "saveResult fail!");
            e.printStackTrace();
        }

    }

    WorkThread mWorkThread;
    class WorkThread extends Thread {
        public final static int STATE_NONE = 0;
        public final static int STATE_INIT = 1;
        public final static int STATE_RUN = 2;
        public final static int STATE_EXIT = 3;

        int msg;

        NdkUtils addProcess = new NdkUtils();

        public WorkThread() {

            msg = STATE_NONE;
            Log.i("WorkThread", "create a WorkThread!");
        }

        void setMsg(int message) {

            Log.d("thread", "running process pid: " + Process.myTid() + ";msg: "+message);
            msg = message;
        }

        @Override
        public void run() {
            final File parentPath = getDir("image", Context.MODE_PRIVATE);
            while (msg != STATE_EXIT) {
                if (msg == STATE_INIT) {
                    mWorkThread.setMsg(WorkThread.STATE_NONE);

                    String dstPath = parentPath.getAbsolutePath() + "/src.png";
                    final char[] path = dstPath.toCharArray();
                    final int length = dstPath.length();
                    addProcess.initOpenGLES(path,length);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProcessingDialog.dismiss();
                        }
                    });

                } else if(msg == STATE_RUN) {
                    mWorkThread.setMsg(WorkThread.STATE_NONE);
                    long address = addProcess.processing();
                    double retTime = addProcess.time;
                    Mat outMat = new Mat(address);
                    Utils.matToBitmap(outMat, mFinalBitmap); //convert mat to bitmap
                    final String time = retTime + " ms";
                    //Log.i("RunThread", "RunThread");
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //m_Tmpbmp = BitmapFactory.decodeFile("/data/isptune/src.png");
                            m_Tmpbmp = BitmapFactory.decodeFile(parentPath.getAbsolutePath() + "/src.png");
                            mOriImageView.setImageBitmap(m_Tmpbmp);
                            mImageView.setImageBitmap(mFinalBitmap);    //设置Bitmap
                            saveBitmap(mFinalBitmap);
                            mTextView.setText(time);
                            mButton.setEnabled(true);
                            mProcessingDialog.dismiss();
                            //WorkThread.this.setMsg(STATE_NONE);
                        }
                    });
                }
            }
        }

    }
}
