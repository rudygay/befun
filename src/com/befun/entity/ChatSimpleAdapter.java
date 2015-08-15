package com.befun.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.befun.db.MessageProvider;
import com.befun.test.R;
import com.befun.util.SaveBitmap;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ChatSimpleAdapter extends SimpleCursorAdapter{	
	private static final String TAG = ChatMsgViewAdapter.class.getSimpleName();
	 public Cursor cursor;
	 private Context ctx;   
	 private LayoutInflater mInflater;
	 
	 @SuppressWarnings("deprecation")
	public ChatSimpleAdapter(Context context, int layout, Cursor c) {
			super(context, layout, c, null, null);
			ctx = context;
			mInflater = LayoutInflater.from(ctx);
		}
	 public View getView(int position, View convertView, ViewGroup parent){
	    	cursor.moveToPosition(position);
	    	ViewHolder viewHolder = null;	
		    if(cursor.getInt(2) == MessageProvider.INCOMING && cursor.getInt(3)==MessageProvider.MESSAGE_TXT){
		    	convertView = mInflater.inflate(R.layout.chatting_item_msg_text_left, null);
		    	TextView time = (TextView) convertView.findViewById(R.id.tv_sendtime);
		    	TextView content = (TextView) convertView.findViewById(R.id.tv_chatcontent);
		    	com.befun.view.RoundedImageView head = 
		    			(com.befun.view.RoundedImageView)convertView.findViewById(R.id.iv_userhead);
		    	time.setText(timeHandle(cursor.getLong(4)));
		    	content.setText(cursor.getString(5));
		    }
		    if(cursor.getInt(2) == MessageProvider.OUTGOING && cursor.getInt(3)==MessageProvider.MESSAGE_TXT){
		    	convertView = mInflater.inflate(R.layout.chatting_item_msg_text_right, null);
		    	TextView time = (TextView) convertView.findViewById(R.id.tv_sendtime);
		    	TextView content = (TextView) convertView.findViewById(R.id.tv_chatcontent);
		    	com.befun.view.RoundedImageView head = 
		    			(com.befun.view.RoundedImageView)convertView.findViewById(R.id.iv_userhead);
		    	
		    	time.setText(timeHandle(cursor.getLong(4)));
		    	content.setText(cursor.getString(5));
		    }
		    if(cursor.getInt(2) == MessageProvider.OUTGOING && cursor.getInt(3)==MessageProvider.MESSAGE_PIC){
		    	convertView = mInflater.inflate(R.layout.chating_item_msg_pic_right, null);
		    	TextView time = (TextView) convertView.findViewById(R.id.tv_sendtime);
		    	ImageView content = (ImageView) convertView.findViewById(R.id.tv_chatcontent);
		    	time.setText(timeHandle(cursor.getLong(4)));
		    	Bitmap bm = SaveBitmap.getImageFromSDCardlist(cursor.getString(5));
		    	content.setImageBitmap(bm);	    	
		    }
		    if(cursor.getInt(2) == MessageProvider.INCOMING && cursor.getInt(3)==MessageProvider.MESSAGE_PIC){
		    	convertView = mInflater.inflate(R.layout.chating_item_msg_pic_left, null);
		    	TextView time = (TextView) convertView.findViewById(R.id.tv_sendtime);
		    	ImageView content = (ImageView) convertView.findViewById(R.id.tv_chatcontent);
		    	time.setText(timeHandle(cursor.getLong(4)));
		    	Bitmap bm = SaveBitmap.getImageFromSDCardlist(cursor.getString(5));
		    	content.setImageBitmap(bm);	    	
		    }
		    return convertView;
	    }
	    @Override
		public int getViewTypeCount() {
			// TODO Auto-generated method stub
			return 2;
		}
		static class ViewHolder { 
	        public TextView tvSendTime;
	        public TextView tvContent;
	        public boolean isComMsg = true;
	    }
		private String timeHandle(long time){
	    	Date date = new Date(time);
	    	SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
	    	String dateString = formatter.format(date);  
	    	String dateString1 = "00:00";
	    	String times[] = dateString.split(":");
	    	int hour = Integer.parseInt(times[0]);
	    	if(hour < 12){
	        	if(hour < 5){
	        		dateString1 = "Áè³¿" + dateString;}
	        	else{
	        		dateString1 = "ÔçÉÏ" + dateString;}
	        	}
	        else{
	        	 hour = hour - 12;
	        	 if(hour < 7){
	        		 dateString1 = "ÏÂÎç" + hour +":"+ times[1];
	        		 }
	        	 else{
	        		 dateString1 = "ÍíÉÏ" + hour +":"+ times[1];
	        	 }
	        }
			return dateString1;    	
	    }
}
