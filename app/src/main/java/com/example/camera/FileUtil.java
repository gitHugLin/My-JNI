package com.example.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtil {
	private static final  String TAG = "FileUtil";
	/*private static final File parentPath = Environment.getExternalStoragePublicDirectory(
			Environment.DIRECTORY_DCIM);*/
	private static final File parentPath = Environment.getExternalStorageDirectory();
	private static   String storagePath = "";
	private static final String DST_FOLDER_NAME = "PlayCamera";
	private static final String IMAGE_FOLDER_NAME = "image";
	public final static String DST_FILE = parentPath.getAbsolutePath()+"/" + DST_FOLDER_NAME + "/" + "plate_locate.jpg";
	private static int picIndex = 0;
	private static String initPath(){
		if(storagePath.equals("")){
			storagePath = parentPath.getAbsolutePath();
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


	public static void saveBitmap(Context ctx, Bitmap b)
	{
		File dir = ctx.getDir(IMAGE_FOLDER_NAME, Context.MODE_PRIVATE);
		//Log.i(TAG, "DIR = " + dir);
		String currentPath = dir.getAbsolutePath();
		String dstPath = initPath();
		Log.i(TAG, "currentPath = " + currentPath);

		if( picIndex == 6) {
			picIndex = 1;
			copyFolder(currentPath,dstPath);
		}
		else
			picIndex++;
		String num = "" + picIndex;
		String jpegName = currentPath + "/" + num + ".jpg";
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


	 //复制整个文件夹内容
	 //@param oldPath String 原文件路径
	 //@param newPath String 复制后路径
	 //@return boolean
	public static void copyFolder(String oldPath, String newPath) {

		try {
			(new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
			File a = new File(oldPath);
			String[] file=a.list();
			File temp=null;
			for (int i = 0; i < file.length; i++) {
				if(oldPath.endsWith(File.separator)){
					temp = new File(oldPath+file[i]);
				}
				else{
					temp = new File(oldPath+File.separator+file[i]);
				}

				if(temp.isFile()){
					long currentTime = System.currentTimeMillis();
					currentTime = currentTime % 1000000;
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath + "/"
							+"IMG_" + currentTime +".jpg");
					/*FileOutputStream output = new FileOutputStream(newPath + "/" +
							(temp.getName()).toString());*/
					byte[] b = new byte[1024 * 5];
					int len;
					while ( (len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
/*				if(temp.isDirectory()){//如果是子文件夹
					copyFolder(oldPath+"/"+file[i],newPath+"/"+file[i]);
				}*/
			}
		}
		catch (Exception e) {
			System.out.println("复制整个文件夹内容操作出错");
			e.printStackTrace();

		}

	}

}
