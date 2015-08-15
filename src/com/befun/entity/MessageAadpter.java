package com.befun.entity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.befun.activity.AcountCancel;
import com.befun.activity.ChatActivity;
import com.befun.db.FriendProvider;
import com.befun.db.MessageProvider;
import com.befun.http.AcountRelated;
import com.befun.service.MainService;
import com.befun.test.R;
import com.befun.util.PreferenceConstants;
import com.befun.util.PreferenceUtils;
import com.befun.view.BadgeView;

public class MessageAadpter extends BaseAdapter {
	private Cursor cursor;
	private Context ctx;
	private LayoutInflater mInflater;
	private MainService service;
	
	public MessageAadpter(Cursor cursor,Context ctx,MainService service){
		super();
		this.cursor = cursor;
		this.ctx = ctx;
		mInflater = LayoutInflater.from(ctx);
		this.service = service;
	}
	@Override
	public int getCount() {
		return cursor.getCount();
	}
	@Override
	public Object getItem(int position) {
		return position;
	}
	@Override
	public long getItemId(int position) {
		return position;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		cursor.moveToPosition(getCount() - position - 1);
		ViewHolder mHolder = null;
		if (convertView == null) 
        {
            convertView = mInflater.inflate(R.layout.mess_item, null);
            mHolder = new ViewHolder();
            mHolder.Header = (ImageView)convertView.findViewById(R.id.img_mess_id);
            mHolder.Content = (TextView)convertView.findViewById(R.id.frien_name);
            mHolder.Delete = (Button)convertView.findViewById(R.id.mess_delete_id);
            mHolder.Badge = (BadgeView)convertView.findViewById(R.id.badgeView1);
            mHolder.Position = getCount() - position - 1;
            convertView.setTag(mHolder);
        } else
        {
            mHolder = (ViewHolder)convertView.getTag();
        }
		convertView.setOnLongClickListener(mOnLongClickListener);
		convertView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				ViewHolder holder = (ViewHolder)v.getTag();
				holder.Delete.setVisibility(View.INVISIBLE);
				Intent intent = new Intent(ctx, ChatActivity.class);
				cursor.moveToPosition(holder.Position);
				String nickname = cursor.getString(4);
				String username = cursor.getString(1);
				String gender = cursor.getString(2);	
				Log.v("MessageAdapter",username+"@"+nickname+"@"+gender);
				Friend friend = new Friend(nickname, username, gender);	
				ContentValues values = new ContentValues();
				values.put("is_read", 0);
				ctx.getContentResolver().update(FriendProvider.CONTENT_URI
				, values, "username = ?", new String[]{username});
				intent.putExtra("friend", friend);
				ctx.startActivity(intent);
			}
		});
		switch (cursor.getInt(3)) {
		case 0:
			mHolder.Badge.hide();
			break;
		case 1:
			mHolder.Badge.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
		switch (cursor.getString(2)) {
		case "0":
			mHolder.Header.setImageResource(R.drawable.mess_male);
			break;
		case "1":
			mHolder.Header.setImageResource(R.drawable.mess_female);
			break;
		case "2":
			mHolder.Header.setImageResource(R.drawable.mess_les);
			break;
		case "3":
			mHolder.Header.setImageResource(R.drawable.mess_gay);
			break;
		default:
			mHolder.Header.setImageResource(R.drawable.mess_male);
			break;
		}
		mHolder.Content.setText(cursor.getString(4));
		mHolder.Delete.setOnClickListener(new DeleteClickListener(mHolder));
		return convertView;
	}
	OnLongClickListener mOnLongClickListener = new OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			final ViewHolder holder = (ViewHolder)v.getTag();
			holder.Delete.setVisibility(View.VISIBLE);
			notifyDataSetChanged();
			return false;
		}
	}; 
	class DeleteClickListener implements OnClickListener{
		ViewHolder holder;
		public DeleteClickListener(ViewHolder holder) {
			this.holder = holder;
		}
		@Override
		public void onClick(View v) {
			final View dialayout = mInflater.inflate(R.layout.delete_dialog, null);
			final Button po = (Button)dialayout.findViewById(R.id.delete_po);
			final Button ne = (Button)dialayout.findViewById(R.id.delete_ne);		        		         
			final Dialog alert = new Dialog(ctx,R.style.myDialog);
			alert.setContentView(dialayout);
	        alert.show();
	        ne.setOnClickListener(new OnClickListener() {					
				@Override
				public void onClick(View v) {
					alert.dismiss();
					holder.Delete.setVisibility(View.INVISIBLE);
				}
			});
	        po.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					cursor.moveToPosition(holder.Position);				
					service.sendMessage(cursor.getString(1)+"@"+PreferenceConstants.xmppHost,
							"d", MessageProvider.MESSAGE_DELETE);
					ctx.getContentResolver().delete(FriendProvider.CONTENT_URI, 
							"username=?", new String[]{cursor.getString(1)});
					ctx.getContentResolver().delete(MessageProvider.CONTENT_URI,
							"username=?", new String[]{cursor.getString(1)});
					new Thread(){
						@Override
						public void run() {							
							String log = new AcountRelated().deleteFriend(PreferenceUtils.getPrefString
									(ctx, PreferenceConstants.USERNAME,""), cursor.getString(1));
							Log.v("É¾³ý½á¹û",log);
						}						
					}.start();
					alert.dismiss();
				}
			});
		}		
	} 
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();		
	}
	static class ViewHolder 
    {
        ImageView Header;
        TextView Content; 
        Button Delete; 
        BadgeView Badge;
        int Position;
    }
}
