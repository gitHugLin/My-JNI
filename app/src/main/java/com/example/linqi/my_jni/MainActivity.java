package com.example.linqi.my_jni;
//armeabi-v7a

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.imageview.PinchImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class MainActivity extends Activity
{
    TextView mTextView;
    PinchImageView mOriImageView;
    PinchImageView mImageView;
    Button mButton;
    ProgressDialog mProcessingDialog;
    Button mCaptureButton;
    Bitmap mFinalBitmap;
    Bitmap m_Tmpbmp;
    boolean threadExist = false;

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
        m_Tmpbmp = BitmapFactory.decodeFile("/mnt/obb/Capture/3.jpg");
        mOriImageView.setImageBitmap(m_Tmpbmp);
        mFinalBitmap = Bitmap.createBitmap(m_Tmpbmp.getWidth() ,m_Tmpbmp.getHeight() ,Bitmap.Config.ARGB_8888);

        mButton = (Button) this.findViewById(R.id.button);
        mTextView.setText("addPicture Demo!");
        mCaptureButton = (Button)findViewById(R.id.capture_btn);
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComponentName cn = new ComponentName("com.example.linqi.my_jni", "com.example.camera.CameraActivity");
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
        mProcessingDialog.setMessage("intialize environment...");
        mProcessingDialog.show();

        threadExist = true;
        mWorkThread = new WorkThread();
        mWorkThread.setPriority(10);
        mWorkThread.start();

        mWorkThread.setMsg(WorkThread.STATE_INIT);
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

            while (msg != STATE_EXIT) {
                if (msg == STATE_INIT) {
                    mWorkThread.setMsg(WorkThread.STATE_NONE);
                    addProcess.initOpenGLES();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProcessingDialog.dismiss();
                            //WorkThread.this.setMsg(STATE_NONE);
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
                            m_Tmpbmp = BitmapFactory.decodeFile("/mnt/obb/Capture/3.jpg");
                            mOriImageView.setImageBitmap(m_Tmpbmp);
                            mImageView.setImageBitmap(mFinalBitmap);    //设置Bitmap
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
