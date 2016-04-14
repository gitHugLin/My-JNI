package com.example.camera;

import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

public class CameraTextureView extends TextureView implements TextureView.SurfaceTextureListener, CameraInterface.CamOpenOverCallback {
	private static final String TAG = "CameraTextureView";
	Context mContext;
	SurfaceTexture mSurface;
	
	public CameraTextureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
		this.setSurfaceTextureListener(this);
	}
	
	@Override
	public void cameraHasOpened() {
		// TODO Auto-generated method stub
		SurfaceTexture surface = this._getSurfaceTexture();
		CameraInterface.getInstance().doStartPreview(surface, DisplayUtil.getScreenRate(mContext));
	}
	
	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
			int height) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onSurfaceTextureAvailable...");
		mSurface = surface;
		
		Thread openThread = new Thread(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				CameraInterface.getInstance().doOpenCamera(CameraTextureView.this);
			}
		};
		openThread.start();
		
//		CameraInterface.getInstance().doStartPreview(surface, 1.33f);
	}
	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onSurfaceTextureDestroyed...");
		CameraInterface.getInstance().doStopCamera();
		return true;
	}
	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onSurfaceTextureSizeChanged...");
	}
	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		// TODO Auto-generated method stub
		//Log.i(TAG, "onSurfaceTextureUpdated...");
		
	}
	

	public SurfaceTexture _getSurfaceTexture(){
		return mSurface;
	}

	
	@Override
	public void pictureHasTaken() {
		/*
		new Thread(new Runnable(){
			public void run(){
				CameraInterface.getInstance().doStopPreview();

				CameraInterface.getInstance().doRestartPreview();
			}
		}).start();
		*/
	}
}
