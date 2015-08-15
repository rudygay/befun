package com.befun.entity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.befun.db.FriendProvider;
import com.befun.db.MessageProvider;
import com.befun.service.MainService;
import com.befun.util.SaveBitmap;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class DownloadImageTask extends AsyncTask<String, Void, Boolean> {
	private long ts;
	private ContentResolver contentResolver;
	private MainService mainService;
	private String fromJID;
	
		public DownloadImageTask(long ts,ContentResolver contentResolver,MainService mainService,String fromJID){
			this.ts = ts;
			this.contentResolver = contentResolver;
			this.mainService = mainService;
			this.fromJID = fromJID;
		}
		//覆写的方法，这个方法将在这个类的对象execute()的时候调用
       protected Boolean doInBackground(String... urls) {
           Log.v("调用asynctask成功！",urls[0]);
		try {
			 this.loadImageFromNetwork(urls[0]);
			 ContentValues values = new ContentValues();
			 values.put(MessageProvider.MESSAGE_DIREC, MessageProvider.INCOMING);
			 values.put("username", fromJID.split("@")[0]);
			 values.put(MessageProvider.MESSAGE_CONTENT, urls[0].split("//")[1].split("/")[6]+"#");
			 Log.v("图片名",urls[0].split("//")[1].split("/")[6]+"#");
			 values.put(MessageProvider.MESSAGE_TYPE, MessageProvider.MESSAGE_PIC);
			 values.put(MessageProvider.MESSAGE_DATE, ts);
			 contentResolver.insert(MessageProvider.CONTENT_URI,values);
			 mainService.newMessage(fromJID, "");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
           return false;
       }
	
       
       @Override
       //覆写的方法，当耗时的操作执行完之后执行，这里就是把获得的Bitmap更新到ImageView上
       protected void onPostExecute(Boolean result) {
           // TODO Auto-generated method stub
           super.onPostExecute(result);     
       }

	//就一个获取图片的方法
       private void loadImageFromNetwork(String imageUrl) throws IOException {
           URL url = new URL(imageUrl);
           HttpURLConnection con = (HttpURLConnection)url.openConnection();
           con.setDoInput(true);
           con.setConnectTimeout(5000);
           con.connect();
           InputStream inputStream = con.getInputStream();
           try {
			SaveBitmap.saveImageRecieve(inputStream, imageUrl.split("//")[1].split("/")[6]+"#");
           } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
           
       }
   }
