package com.befun.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class SaveBitmap {  
  
    private final static String CACHE = "/befunImage";  
    private static int   IMAGE_MAX_WIDTH  = 250;
    private static int   IMAGE_MAX_HEIGHT = 250;

  
    /** 
     * 保存图片的方法 保存到sdcard 
     *  
     * @throws Exception 
     *  
     */  
    public static void saveImage(ByteArrayOutputStream byteArrayOutputStream, String imageName)  
            throws Exception {  
        String filePath = isExistsFilePath();  
        FileOutputStream fos = null;  
        File file = new File(filePath, imageName);  
        try {  
            fos = new FileOutputStream(file);
            if (null != fos) { 
                byteArrayOutputStream.writeTo(fos);
                fos.flush();  
                fos.close();  
            }  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }
    public static void deleteImage(String imageName){
    	String filepath = getSDPath() + CACHE  + "/" + imageName;  
        File file = new File(filepath); 
        if (file.exists()) {  
            file.delete();
            Log.v("文件删除了？",imageName);
        }  
    }
    public static void saveImageRecieve(InputStream inputStream, String imageName)  
            throws Exception {  
        String filePath = isExistsFilePath();  
        FileOutputStream fos = null;  
        File file = new File(filePath, imageName);  
        try {  
            fos = new FileOutputStream(file);
            if (null != fos) { 
            	int ch = 0;
            	while((ch=inputStream.read()) != -1)  
                    fos.write(ch); 
                fos.flush();  
                fos.close();  
            }  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }finally{
        	fos.flush();
        	fos.close();
        	inputStream.close();
        }  
    }
    public static void saveImage0(Bitmap bitmap, String imageName)  
            throws Exception {  
        String filePath = isExistsFilePath();  
        FileOutputStream fos = null;  
        File file = new File(filePath, imageName);  
        try {  
            fos = new FileOutputStream(file);
            if (null != fos) { 
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos); 
                if (bitmap.isRecycled()){
                	bitmap.recycle();
                }
                fos.flush();  
                fos.close();  
            }  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
  
    /** 
     * 获取sd卡的缓存路径， 一般在卡中sdCard就是这个目录 
     *  
     * @return SDPath 
     */  
    public static String getSDPath() {  
        File sdDir = null;  
        boolean sdCardExist = Environment.getExternalStorageState().equals(  
                android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在  
        if (sdCardExist) {  
            sdDir = Environment.getExternalStorageDirectory();// 获取根目录  
        } else {  
            Log.e("ERROR", "没有内存卡");  
        }  
        return sdDir.toString();  
    }  
  
    /** 
     * 获取缓存文件夹目录 如果不存在创建 否则则创建文件夹 
     *  
     * @return filePath 
     */  
    private static String isExistsFilePath() {  
        String filePath = getSDPath() + CACHE;  
        File file = new File(filePath);  
        if (!file.exists()) {  
            file.mkdirs();  
        }  
        return filePath;  
    }  
    /** 
     * 获取SDCard文件 
     *  
     * @return Bitmap 
     */  
    public static Bitmap getImageFromSDCard(String imageName) {  
        String filepath = getSDPath() + CACHE  + "/" + imageName;  
        File file = new File(filepath);  
        if (file.exists()) {  
            Bitmap bm = BitmapFactory.decodeFile(filepath);  
            return bm;  
        }  
        return null;  
    }  
    public static Bitmap getImageFromSDCardlist(String imageName) {
    	String filepath = getSDPath() + CACHE  + "/" + imageName; 
    	File file = new File(filepath);
    	if (file.exists()) {  
    		BitmapFactory.Options option = new BitmapFactory.Options();
    		option.inSampleSize = getImageScale(filepath);
    		option.inPreferredConfig = Bitmap.Config.ARGB_8888;
    		option.inInputShareable = true;
    		option.inJustDecodeBounds = true;
    		Bitmap bm = BitmapFactory.decodeFile(filepath, option);
    		return bm;
        }  
        return null;
    }
    private static int getImageScale(String imagePath) {
    	BitmapFactory.Options option = new BitmapFactory.Options();
    	// set inJustDecodeBounds to true, allowing the caller to query the bitmap info without having to allocate the
    	// memory for its pixels.
    	option.inJustDecodeBounds = true;
    	BitmapFactory.decodeFile(imagePath, option);
    	int scale = 1;
    	while (option.outWidth / scale >= IMAGE_MAX_WIDTH || option.outHeight / scale >= IMAGE_MAX_HEIGHT) {
    		scale += 1;
    	}
    	return scale;
    }
} 