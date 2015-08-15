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
		//��д�ķ���������������������Ķ���execute()��ʱ�����
       protected Boolean doInBackground(String... urls) {
           Log.v("����asynctask�ɹ���",urls[0]);
		try {
			 this.loadImageFromNetwork(urls[0]);
			 ContentValues values = new ContentValues();
			 values.put(MessageProvider.MESSAGE_DIREC, MessageProvider.INCOMING);
			 values.put("username", fromJID.split("@")[0]);
			 values.put(MessageProvider.MESSAGE_CONTENT, urls[0].split("//")[1].split("/")[6]+"#");
			 Log.v("ͼƬ��",urls[0].split("//")[1].split("/")[6]+"#");
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
       //��д�ķ���������ʱ�Ĳ���ִ����֮��ִ�У�������ǰѻ�õ�Bitmap���µ�ImageView��
       protected void onPostExecute(Boolean result) {
           // TODO Auto-generated method stub
           super.onPostExecute(result);     
       }

	//��һ����ȡͼƬ�ķ���
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
