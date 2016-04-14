package com.example.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
	private static final  String TAG = "FileUtil";
	private static final File parentPath = Environment.getExternalStorageDirectory();
	private static   String storagePath = "";
	private static final String DST_FOLDER_NAME = "PlayCamera";
	private static final String IMAGE_FOLDER_NAME = "image";
	public final static String DST_FILE = parentPath.getAbsolutePath()+"/" + DST_FOLDER_NAME + "/" + "plate_locate.jpg";
	private static int picIndex = 0;

	private static String initPath(){
		if(storagePath.equals("")){
			storagePath = parentPath.getAbsolutePath()+"/" + DST_FOLDER_NAME;
			File f = new File(storagePath);
			if(!f.exists()){
				f.mkdir();
			}
		}
		return storagePath;
	}
	
	public static String getFilePath(Context ctx) {
		File dir = ctx.getDir(IMAGE_FOLDER_NAME, Context.MODE_PRIVATE);
		return dir.getAbsolutePath() + "/" +"plate_locate.jpg";
	}
	

	public static void saveBitmap(Bitmap b){

		//String path = initPath();
		//long dataTake = System.currentTimeMillis();
		String path = "/mnt/obb/Capture";
		String jpegName = path + "/" + "1.jpg";
		Log.i(TAG, "saveBitmap:jpegName = " + jpegName);
		try {
			FileOutputStream fout = new FileOutputStream(jpegName);
			BufferedOutputStream bos = new BufferedOutputStream(fout);
			b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
			Log.i(TAG, "saveBitmap success");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "saveBitmap faild");
			e.printStackTrace();
		}

	}


	public static void saveBitmap(Context ctx, Bitmap b){
		File dir = ctx.getDir(IMAGE_FOLDER_NAME, Context.MODE_PRIVATE);
		long dataTake = System.currentTimeMillis();
		//String jpegName = dir.getAbsolutePath() + "/" +"plate_locate.jpg";
		if( picIndex == 6)
			picIndex = 1;
		else
			picIndex++;
		String num = "" + picIndex;
		String jpegName = "/mnt/obb/Capture/" + num + ".jpg";
		Log.i(TAG, "saveBitmap:jpegName = " + jpegName);
		try {
			FileOutputStream fout = new FileOutputStream(jpegName);
			BufferedOutputStream bos = new BufferedOutputStream(fout);
			b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
			Log.i(TAG, "saveBitmap success");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "saveBitmap faild");
			e.printStackTrace();
		}

	}

}
