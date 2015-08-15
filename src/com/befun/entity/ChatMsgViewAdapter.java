
package com.befun.entity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.befun.db.MessageProvider;
import com.befun.test.R;
import com.befun.util.PreferenceConstants;
import com.befun.util.PreferenceUtils;
import com.befun.util.SaveBitmap;

public class ChatMsgViewAdapter extends BaseAdapter {
	
    private static final String TAG = ChatMsgViewAdapter.class.getSimpleName();
    public Cursor cursor;
    private Context ctx;
    private String friendGender;
    private LayoutInflater mInflater;
    public ChatMsgViewAdapter(Context context,Cursor cursor,String friendGender) {
        ctx = context;
        this.cursor = cursor;
        mInflater = LayoutInflater.from(context);
        this.friendGender = friendGender;
    }

    public int getCount() {
        return cursor.getCount();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        
        return position;
    }
	
    public View getView(int position, View convertView, ViewGroup parent) {
    	if(cursor!=null && !cursor.isClosed()){
    	cursor.moveToPosition(position); 
    	String gender = PreferenceUtils.getPrefString
        		(ctx, PreferenceConstants.GENDER,"0");
    	if(cursor.getInt(2) == MessageProvider.INCOMING && cursor.getInt(3)==MessageProvider.MESSAGE_TXT){
	    	convertView = mInflater.inflate(R.layout.chatting_item_msg_text_left, null);
	    	TextView time = (TextView) convertView.findViewById(R.id.tv_sendtime);
	    	TextView content = (TextView) convertView.findViewById(R.id.tv_chatcontent);
	    	com.befun.view.RoundedImageView head = 
	    			(com.befun.view.RoundedImageView)convertView.findViewById(R.id.iv_userhead);
	    	switch (friendGender) {
			case "0":
				head.setImageResource(R.drawable.mess_male);
				break;
			case "1":
				head.setImageResource(R.drawable.mess_female);
				break;
			case "2":
				head.setImageResource(R.drawable.mess_les);
				break;
			case "3":
				head.setImageResource(R.drawable.mess_gay);
				break;
			default:
				break;
			}
	    	time.setText(timeHandle(cursor.getLong(4)));
	    	content.setText(cursor.getString(5));
	    }
	    if(cursor.getInt(2) == MessageProvider.OUTGOING && cursor.getInt(3)==MessageProvider.MESSAGE_TXT){
	    	convertView = mInflater.inflate(R.layout.chatting_item_msg_text_right, null);
	    	TextView time = (TextView) convertView.findViewById(R.id.tv_sendtime);
	    	TextView content = (TextView) convertView.findViewById(R.id.tv_chatcontent);
	    	com.befun.view.RoundedImageView head = 
	    			(com.befun.view.RoundedImageView)convertView.findViewById(R.id.iv_userhead);
	    	switch (gender) {
			case "0":
				head.setImageResource(R.drawable.mess_male);
				break;
			case "1":
				head.setImageResource(R.drawable.mess_female);
				break;
			case "2":
				head.setImageResource(R.drawable.mess_les);
				break;
			case "3":
				head.setImageResource(R.drawable.mess_gay);
				break;
			default:
				break;
			}    	
	    	time.setText(timeHandle(cursor.getLong(4)));
	    	content.setText(cursor.getString(5));
	    }
	    if(cursor.getInt(2) == MessageProvider.OUTGOING && cursor.getInt(3)==MessageProvider.MESSAGE_PIC){
	    	convertView = mInflater.inflate(R.layout.chating_item_msg_pic_right, null);
	    	TextView time = (TextView) convertView.findViewById(R.id.tv_sendtime);
	    	ImageView content = (ImageView) convertView.findViewById(R.id.tv_chatcontent);
	    	time.setText(timeHandle(cursor.getLong(4)));
	    	com.befun.view.RoundedImageView head = 
	    			(com.befun.view.RoundedImageView)convertView.findViewById(R.id.iv_userhead);
	    	switch (gender) {
			case "0":
				head.setImageResource(R.drawable.mess_male);
				break;
			case "1":
				head.setImageResource(R.drawable.mess_female);
				break;
			case "2":
				head.setImageResource(R.drawable.mess_les);
				break;
			case "3":
				head.setImageResource(R.drawable.mess_gay);
				break;
			default:
				break;
			}
	    	Bitmap bm = SaveBitmap.getImageFromSDCardlist(cursor.getString(5));
	    	content.setImageBitmap(bm);	    	
	    }
	    if(cursor.getInt(2) == MessageProvider.INCOMING && cursor.getInt(3)==MessageProvider.MESSAGE_PIC){
	    	convertView = mInflater.inflate(R.layout.chating_item_msg_pic_left, null);
	    	TextView time = (TextView) convertView.findViewById(R.id.tv_sendtime);
	    	ImageView content = (ImageView) convertView.findViewById(R.id.tv_chatcontent);
	    	time.setText(timeHandle(cursor.getLong(4)));
	    	com.befun.view.RoundedImageView head = 
	    			(com.befun.view.RoundedImageView)convertView.findViewById(R.id.iv_userhead);
	    	switch (friendGender) {
			case "0":
				head.setImageResource(R.drawable.mess_male);
				break;
			case "1":
				head.setImageResource(R.drawable.mess_female);
				break;
			case "2":
				head.setImageResource(R.drawable.mess_les);
				break;
			case "3":
				head.setImageResource(R.drawable.mess_gay);
				break;
			default:
				break;
			}
	    	Bitmap bm = SaveBitmap.getImageFromSDCardlist(cursor.getString(5));
	    	content.setImageBitmap(bm);	    	
	    }
	    return convertView;}
    	return null;
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

	public void changeCursor(Cursor cursor){
		if(this.cursor != null && !this.cursor.isClosed()){
			this.cursor.close();
		}
		this.cursor = cursor;
		notifyDataSetChanged();
	}

}
