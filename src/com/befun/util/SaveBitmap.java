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
     * ����ͼƬ�ķ��� ���浽sdcard 
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
            Log.v("�ļ�ɾ���ˣ�",imageName);
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
     * ��ȡsd���Ļ���·���� һ���ڿ���sdCard�������Ŀ¼ 
     *  
     * @return SDPath 
     */  
    public static String getSDPath() {  
        File sdDir = null;  
        boolean sdCardExist = Environment.getExternalStorageState().equals(  
                android.os.Environment.MEDIA_MOUNTED); // �ж�sd���Ƿ����  
        if (sdCardExist) {  
            sdDir = Environment.getExternalStorageDirectory();// ��ȡ��Ŀ¼  
        } else {  
            Log.e("ERROR", "û���ڴ濨");  
        }  
        return sdDir.toString();  
    }  
  
    /** 
     * ��ȡ�����ļ���Ŀ¼ ��������ڴ��� �����򴴽��ļ��� 
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
     * ��ȡSDCard�ļ� 
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