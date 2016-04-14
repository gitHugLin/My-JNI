package com.example.camera;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.linqi.my_jni.R;

public class CameraActivity extends Activity {
	private static final String TAG = "CameraActivity";
	CameraTextureView textureView = null;
	ImageButton shutterBtn;
	View progressBar;
	float previewRate = -1f;
	TextView noticeText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		initUI();
		initViewParams();
		textureView.setAlpha(1.0f);
		
		shutterBtn.setOnClickListener(new BtnListeners());
		
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
		}
		return super.onKeyUp(keyCode, event);
	}

	private void initUI(){
		textureView = (CameraTextureView)findViewById(R.id.camera_textureview);
		shutterBtn = (ImageButton)findViewById(R.id.btn_shutter);
		progressBar = (View) findViewById(R.id.progress_bar);
		noticeText = (TextView) findViewById(R.id.camera_notice);
	}
	private void initViewParams(){
		LayoutParams params = textureView.getLayoutParams();
		Point p = DisplayUtil.getScreenMetrics(this);
		params.width = p.x;
		params.height = p.y;
		previewRate = DisplayUtil.getScreenRate(this);
		textureView.setLayoutParams(params);

		LayoutParams p2 = shutterBtn.getLayoutParams();
		p2.width = DisplayUtil.dip2px(this, 80);
		p2.height = DisplayUtil.dip2px(this, 80);
		shutterBtn.setLayoutParams(p2);	

	}
	
	private class BtnListeners implements OnClickListener{

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.btn_shutter) {
				//Toast.makeText(CameraActivity.this, "Progressing ...", Toast.LENGTH_SHORT).show();
				CameraInterface.getInstance().doTakePicture(CameraActivity.this);
			}
		}
	}
}
