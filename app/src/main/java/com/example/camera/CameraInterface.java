package com.example.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraInterface {
	private static final String TAG = "CameraInterface";
	private Context mContext;
	private Camera mCamera = null;
	private Parameters mParams;
	private boolean isPreviewing = false;
	private boolean isReleased = false;
	private float mPreviwRate = -1f;
	private static CameraInterface mCameraInterface;
	private CamOpenOverCallback mCallback;
	private static int frameIndex = 1;
	public interface CamOpenOverCallback{
		public void cameraHasOpened();
		public void pictureHasTaken();
	}

	private CameraInterface(){

	}
	public static synchronized CameraInterface getInstance(){
		if(mCameraInterface == null){
			mCameraInterface = new CameraInterface();
		}
		return mCameraInterface;
	}

	public void doOpenCamera(CamOpenOverCallback callback){
		mCallback = callback;
		Log.i(TAG, "Camera open....");
		mCamera = Camera.open();
		if(mCamera == null)
			Log.e(TAG, "Camera open fial....");
		mCallback.cameraHasOpened();
	}

	public void doStartPreview(SurfaceHolder holder, float previewRate){
		Log.i(TAG, "doStartPreview...");
		if(isPreviewing){
			mCamera.stopPreview();
			return;
		}
		if(mCamera != null){
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			initCamera(previewRate);
		}
	}

	public void doStartPreview(SurfaceTexture surface, float previewRate){
		Log.i(TAG, "doStartPreview...");
		if(isPreviewing){
			mCamera.stopPreview();
			return;
		}
		if(mCamera != null){
			try {
				mCamera.setPreviewTexture(surface);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			initCamera(previewRate);
		}
		
	}


	public void doStopCamera(){
		if(null != mCamera)
		{
			Log.i(TAG, "release camera....");
			isReleased = true;
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview(); 
			isPreviewing = false; 
			mPreviwRate = -1f;
			mCamera.release();
			mCamera = null;
		}
	}
	
	public class AutoFocusCallbackimpl implements AutoFocusCallback{  
        public void onAutoFocus(boolean success,Camera camera){
        	Log.v(TAG, "onAutoFocus..." + success);
            if(success){
				if(frameIndex == 6)
				{
					Toast.makeText(mContext, "6 frames is ready!", Toast.LENGTH_SHORT).show();
					frameIndex = 1;
				}
				else
				{
					Toast.makeText(mContext, ""+frameIndex+"th Frame", Toast.LENGTH_SHORT).show();
					frameIndex++;
				}
            	mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);
            }
        }  
    } 
	

	public void doTakePicture(Context context){
		mContext = context;
		mCamera.enableShutterSound(true);
		if(isPreviewing && (mCamera != null)){
			Log.i(TAG, "doTakePicture....");
			//mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);
			mCamera.autoFocus(new AutoFocusCallbackimpl());

		}
	}



	private void initCamera(float previewRate){
		if(mCamera != null){

			mParams = mCamera.getParameters();
			mParams.setPictureFormat(PixelFormat.JPEG);
			//CamParaUtil.getInstance().printSupportPictureSize(mParams);
			//CamParaUtil.getInstance().printSupportPreviewSize(mParams);
			
			// check if there is picture size supported large or equal
			// than 1280, or else we will check if 800 is available. For
			// most device this can be ok.
			Size pictureSize = CamParaUtil.getInstance().getPropPictureSize(
					mParams.getSupportedPictureSizes(),previewRate, 3200);
/*			if (pictureSize.width < 1280) {
				pictureSize = CamParaUtil.getInstance().getPropPictureSize(
						mParams.getSupportedPictureSizes(),previewRate, 800);
			}*/
			//w = 1600 h = 1200;192w
			//w = 2048 h = 1536;369w
			//w = 2592 h = 1944;503w
			//w = 3264 h = 2448;799w
			mParams.setPictureSize(pictureSize.width, pictureSize.height);
			Size previewSize = CamParaUtil.getInstance().getPropPreviewSize(
					mParams.getSupportedPreviewSizes(), previewRate, 800);
			mParams.setPreviewSize(previewSize.width, previewSize.height);

			//mCamera.setDisplayOrientation(90);
			/*
			CamParaUtil.getInstance().printSupportFocusMode(mParams);
			List<String> focusModes = mParams.getSupportedFocusModes();
			if(focusModes.contains("continuous-picture")){
				//mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
				//mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			}
			*/
			mParams.setFocusMode(Parameters.FOCUS_MODE_AUTO);
			mCamera.setParameters(mParams);	
			mCamera.startPreview();


			isPreviewing = true;
			mPreviwRate = previewRate;

			mParams = mCamera.getParameters();
			Log.i(TAG, "PreviewSize--With = " + mParams.getPreviewSize().width
					+ "Height = " + mParams.getPreviewSize().height);
			Log.i(TAG, "PictureSize--With = " + mParams.getPictureSize().width
					+ "Height = " + mParams.getPictureSize().height);
		}
	}

    private void pointFocus(int x, int y) {
    	Point p = DisplayUtil.getScreenMetrics(mContext);
    	int screenWidth = p.x;
    	int screenHeight = p.y;
    	
        if (mParams.getMaxNumMeteringAreas() > 0) {
                List<Camera.Area> areas = new ArrayList<Camera.Area>();
                Rect area1 = new Rect(x - 100, x - 100, x + 100, x + 100);
                areas.add(new Camera.Area(area1, 600));
                Rect area2 = new Rect(0, screenWidth,0,screenHeight); 
                areas.add(new Camera.Area(area2, 400));
                mParams.setMeteringAreas(areas);
        }
        mCamera.cancelAutoFocus();
        mParams.setFocusMode(Parameters.FOCUS_MODE_AUTO);
        mCamera.setParameters(mParams);
        //mCamera.autoFocus(autoFocusCallBack);
    }

	ShutterCallback mShutterCallback = new ShutterCallback() 
	{
		public void onShutter() {
			Log.i(TAG, "myShutterCallback:onShutter...");
		}
	};
	PictureCallback mRawCallback = new PictureCallback() 
	{
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.i(TAG, "myRawCallback:onPictureTaken...");
		}
	};
	PictureCallback mJpegPictureCallback = new PictureCallback() 
	{
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Log.i(TAG, "myJpegCallback:onPictureTaken...");
			Bitmap b = null;
			if(null != data){
				b = BitmapFactory.decodeByteArray(data, 0, data.length);
				mCamera.stopPreview();
				isPreviewing = false;
			}
			if(null != b)
			{
				//Bitmap rotaBitmap = ImageUtil.getRotateBitmap(b, 90.0f);
				//Bitmap bmp = zoomImage(b, 1536, 500);
				FileUtil.saveBitmap(mContext, b);
				mCallback.pictureHasTaken();
				b.recycle();
			}

			mCamera.startPreview();
			isPreviewing = true;
		}
	};
	
	public void doStopPreview() {
		if (mCamera != null && isPreviewing && !isReleased) {
			mCamera.stopPreview();
			isPreviewing = false;
		} else {
			Log.d(TAG, "doStopPreview, camera released!");
		}
	}
	
	public void doRestartPreview() {
		if (mCamera != null && !isPreviewing && !isReleased) {
			mCamera.startPreview();
			isPreviewing = true;
		} else {
			Log.d(TAG, "doRestartPreview, camera released!");
		}
	}


}
