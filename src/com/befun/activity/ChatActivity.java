package com.befun.activity;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONObject;
import com.befun.db.MessageProvider;
import com.befun.entity.ChatMsgViewAdapter;
import com.befun.entity.Friend;
import com.befun.http.AcountRelated;
import com.befun.service.IConnectionStatusCallback;
import com.befun.service.MainService;
import com.befun.test.R;
import com.befun.util.PreferenceConstants;
import com.befun.util.PreferenceUtils;
import com.befun.util.SaveBitmap;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends Activity implements IConnectionStatusCallback {
	public LinearLayout yy_lay,plus_lay;
	public boolean yy_bool = true,plus_bool = true, is_on;
	public ListView chatListView;
	public TextView chatName;
	public EditText edit;
	public static ChatActivity instance = null;
	public ChatMsgViewAdapter chatMsgViewAdapter;
	private MainService mainService;
	private String LAG = "ChatActivity";
	public Friend friend;
	private ChatMsgViewAdapter adapter;
	private Timer timer;
	private DeleteTask timetask;
	private int messageCount = 0, newinminut = 0;
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 0x123){
			if(is_on){			
			Cursor cursor = ChatActivity.this.getContentResolver().query
				(MessageProvider.CONTENT_URI, null
				, "username = ?",new String[]{friend.befunId}, null);
			if(cursor.getCount() > 0){
				cursor.moveToFirst();
				long date = cursor.getLong(4);
				ChatActivity.this.getContentResolver().delete(MessageProvider.CONTENT_URI,
				MessageProvider.MESSAGE_DATE+"="+date,null);
				try {
					Thread.sleep(110);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				messageCount--;
				if(cursor.getInt(3) == MessageProvider.MESSAGE_PIC){
					SaveBitmap.deleteImage(cursor.getString(5));
				}					
				cursor.close();
			}
			}
			super.handleMessage(msg);
		} 
		}
    };
	private ContentObserver mContactObserver = new ContactObserver();
	ServiceConnection mServiceConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mainService = ((MainService.XXBinder) service).getService();
			mainService.registerConnectionStatusCallback(ChatActivity.this);
			if (!mainService.isAuthenticated()) {
				String usr = PreferenceUtils.getPrefString(ChatActivity.this,
						PreferenceConstants.USERNAME, "");
				String password = PreferenceUtils.getPrefString(
						ChatActivity.this, PreferenceConstants.PASSWORD, "");
				mainService.login(usr, password);
				}else {			
			}
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mainService.unRegisterConnectionStatusCallback();
			mainService = null;
		}
	};
	private void unbindXMPPService() {
		try {
			unbindService(mServiceConnection);
		} catch (IllegalArgumentException e) {
			Log.e(LAG,"Service wasn't bound!");
		}
	}

	private void bindXMPPService() {
		Intent mServiceIntent = new Intent(this, MainService.class);
		bindService(mServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		Log.d(LAG,"bind service");
	}
	@Override
	protected void onResume() {
		super.onResume();
		is_on = true;
		getContentResolver().registerContentObserver(
    			MessageProvider.CONTENT_URI, true, mContactObserver);		
		updateAdapter();
		bindXMPPService();
		Cursor cursor = getContentResolver().query(MessageProvider.CONTENT_URI, null
				, "username = ?"
				,new String[]{friend.befunId}, null);
		messageCount = cursor.getCount();
		newinminut = messageCount;
		while(newinminut > 0){
			mHandler.sendEmptyMessageDelayed(0x123, 10000);
			newinminut--;
		}
		cursor.close();
		if(timer == null)
			timer = new Timer();				
		if(timetask == null)
			timetask = new DeleteTask();
		timer.schedule(timetask, 50, 800);
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		is_on = false;
		getContentResolver().unregisterContentObserver(mContactObserver);
		mHandler.removeCallbacksAndMessages(null);
		if(adapter.cursor != null && !adapter.cursor.isClosed())
		adapter.cursor.close();
		unbindXMPPService();
		if(timer != null){
			timer.cancel();
			timer = null;
		}
		if(timetask != null){
			timetask.cancel();
			timetask = null;
		}				
	}
	
	@Override
	protected void onDestroy() {
		instance = null;		
		super.onDestroy();
	}

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chating);
		instance = this;
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		Intent intent = getIntent();
		friend = (Friend) intent.getExtras().get("friend");
		getContentResolver().registerContentObserver(
    			MessageProvider.CONTENT_URI, true, mContactObserver);		
		Log.v("ChatActivity",friend.befunId);
        setChatWindowAdapter();            
		yy_lay = (LinearLayout)findViewById(R.id.yy_layout);
		plus_lay = (LinearLayout)findViewById(R.id.plus_layout);
		chatListView = (ListView) findViewById(R.id.chatlistview);
		chatName = (TextView) findViewById(R.id.chatName);
		edit = (EditText)findViewById(R.id.et_sendmessage);
		chatName.setText(friend.nickName);
		edit.setOnKeyListener(new OnKeyListener() {		
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER){
					String chatContent = edit.getText().toString();
					if(chatContent.length() > 0){
					mainService.sendMessage(friend.befunId+"@"+PreferenceConstants.xmppHost,chatContent
							,MessageProvider.MESSAGE_TXT);
					edit.setText("");}
					return true;
					}
				return false;
			}
		});
		edit.setOnTouchListener(new View.OnTouchListener()
		  {		   
		   @Override
		   public boolean onTouch(View v, MotionEvent event)
		   {
			   yy_lay.setVisibility(View.GONE);           
			   plus_lay.setVisibility(View.GONE);
		    return false;
		   }
	});
		chatListView.setOnTouchListener(new View.OnTouchListener()
		  {		   
			   @Override
			   public boolean onTouch(View v, MotionEvent event)
			   {
				   ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow            
					(ChatActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				   yy_lay.setVisibility(View.GONE);           
				   plus_lay.setVisibility(View.GONE);				
			    return false;
			   }
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)){
			if(yy_lay.getVisibility() == View.VISIBLE || plus_lay.getVisibility() == View.VISIBLE ||
					((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(edit.getWindowToken(),0)){
				((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow            
				(ChatActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			   yy_lay.setVisibility(View.GONE);           
			   plus_lay.setVisibility(View.GONE);	
			}
			else ChatActivity.this.finish();
			return false; 
			}else { 
			return super.onKeyDown(keyCode, event); 
			} 
	}
	public void chat_back(View v){
		this.finish();
	}
	public void yy_ready(View v){
		if(yy_bool){
			((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow            
			(ChatActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);                                               
			yy_lay.setVisibility(View.VISIBLE);           
			plus_lay.setVisibility(View.GONE);
			plus_bool = true;
		}
		else{
			((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow            
			(ChatActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);           
			yy_lay.setVisibility(View.GONE);
		}
		yy_bool = !yy_bool;
	}
	public void moremore_ready(View v){
		if(plus_bool){
			((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow            
			(ChatActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);                                                
			plus_lay.setVisibility(View.VISIBLE);           
			yy_lay.setVisibility(View.GONE);
			yy_bool = true;
		}
		else{
			((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow            
			(ChatActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);          
			plus_lay.setVisibility(View.GONE);
		}
		plus_bool = !plus_bool;
	}
	public void registerConnectionStatusCallback(IConnectionStatusCallback cb) {
	}

	public void unRegisterConnectionStatusCallback() {
	}
	@Override
	public void connectionStatusChanged(int connectedState, String reason) {
		if(connectedState == 9 || connectedState == 0)
		Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
	}
	private class ContactObserver extends ContentObserver {
		public ContactObserver() {
			super(new Handler());
		}
		
		@Override
		public boolean deliverSelfNotifications() {
			// TODO Auto-generated method stub
			return true;
		}

		public void onChange(boolean selfChange) {
			updateContactStatus();}
	}
	
	private void updateContactStatus(){		
		updateAdapter();
	}
	private void setChatWindowAdapter() {
		new AsyncQueryHandler(getContentResolver()){			
			@Override
			protected void onQueryComplete(int token, Object cookie,
					Cursor cursor0) {
				adapter = new ChatMsgViewAdapter(ChatActivity.this,cursor0
						,friend.gender);				
				chatListView.setAdapter(adapter);
				chatListView.setSelection(chatListView.getCount() - 1);
			}
		}.startQuery(0,null,MessageProvider.CONTENT_URI,null,"username = ?"
				,new String[]{friend.befunId},null);
	}
	private void updateAdapter() {
		new AsyncQueryHandler(getContentResolver()) {			
			@Override
			protected void onQueryComplete(int token, Object cookie,
					Cursor cursor) {
				adapter.changeCursor(cursor);		
				chatListView.setSelection(chatListView.getCount() - 1);
			}
		}.startQuery(0,null,MessageProvider.CONTENT_URI,null,"username = ?"
				,new String[]{friend.befunId},null);
	}
	public void pick_pic(View v){
		plus_lay.setVisibility(View.GONE);
		Intent intent = new Intent();  
        intent.setType("image/*");  
        intent.setAction(Intent.ACTION_GET_CONTENT);  
        startActivityForResult(intent,2); 
	}
	public void take_pic(View v){
		plus_lay.setVisibility(View.GONE);
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);  
        startActivityForResult(intent, 1);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v("requestCode", requestCode+"");
			if(data != null){
				Uri uri = data.getData();
				if(uri !=null){
					Log.v("uri",uri.toString());
					try {
						Bitmap image = MediaStore.Images.Media.getBitmap
								(this.getContentResolver(), uri);
						if (image != null) {  
							send_pick(image);
                        } 
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}   
				}
				else{
					Bundle extras = data.getExtras();  
                    if (extras != null) {                         
                        Bitmap image = extras.getParcelable("data");  
                        if (image != null) {  
                        	send_pick(image);
                        }  
                    }  
				}
			}
			else{
				Log.v("异常","data异常");
			}	
	}
	
	private void send_pick(final Bitmap bitmap){
		new Thread(){
			@Override
			public void run() {	
				ByteArrayOutputStream out = null;
				try {
					out = new ByteArrayOutputStream();  
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);  
		        int options = 100; 
		        Log.v("图片大小",out.toByteArray().length+"");
		        while ( (double)out.toByteArray().length / 1024*(double)options/100>120) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩         
		            //这里压缩options%，把压缩后的数据存放到baos中  
		            options -= 1;//每次都减少10  
		        }
		        out.reset();//重置baos即清空baos  
		        bitmap.compress(Bitmap.CompressFormat.JPEG, options, out);
		        bitmap.recycle();
	            byte[] imgBytes = out.toByteArray();  
	            String bitmapString = Base64.encodeToString(imgBytes, Base64.DEFAULT);
	            Log.v("Base64String",bitmapString);
				String picXML = new AcountRelated().deleteUser(bitmapString,false);//上传图片			
					JSONObject jsonObject = new JSONObject(picXML);
					String code = jsonObject.getString("code");						
					Log.v("code",code);					
					if(code.equals("1"))
						failtoSend();
					if(code.equals("0")){
						String url = jsonObject.getJSONObject("result").getString("url");
						String width = jsonObject.getJSONObject("result").getString("width");
						String height = jsonObject.getJSONObject("result").getString("height");
						Log.v("url",url);						
						SaveBitmap.saveImage(out, url.split("/")[6]);
						ContentValues values = new ContentValues();
						values.put(MessageProvider.MESSAGE_DIREC, MessageProvider.OUTGOING);
						values.put("username", friend.befunId);
						values.put(MessageProvider.MESSAGE_CONTENT, url.split("/")[6]);
						values.put(MessageProvider.MESSAGE_TYPE, MessageProvider.MESSAGE_PIC);
						values.put(MessageProvider.MESSAGE_DATE, System.currentTimeMillis());
						ChatActivity.this.getContentResolver().insert(MessageProvider.CONTENT_URI,values);
						mainService.sendMessage(friend.befunId+"@"+PreferenceConstants.xmppHost
							, imgeContent(url, width, height), MessageProvider.MESSAGE_PIC);}
							out.flush();
							out.close();
						} catch (Exception e) {
							failtoSend();
							// TODO Auto-generated catch block
							e.printStackTrace();
						}finally {  
				            try {  
				                out.flush();  
				                out.close();  
				            } catch (IOException e) {  
				                // TODO Auto-generated catch block  
				                e.printStackTrace();  
				            }
				        }  
	}
		}.start();
	}
	private void failtoSend(){
		mHandler.post(new Runnable() {		
			@Override
			public void run() {
				Toast.makeText(ChatActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
			}
		});
	}
	private String imgeContent(String url,String width,String height){
		String content = "<image>" + "<url>" + "xxxxx"+url + "</url>" + "<width>"+width+"</width>"
				+"<height>"+height+"</height>"+"</image>";
		return content;		
	}
	class DeleteTask extends TimerTask{
		@Override
		public void run() {
			//Log.v("DeleteTask",+messageCount+"");
			Cursor cursor = getContentResolver().query(MessageProvider.CONTENT_URI, null
					, "username = ?",new String[]{friend.befunId}, null);
			newinminut = cursor.getCount() - messageCount;
			messageCount = cursor.getCount();
			while(newinminut > 0){
				Log.v("发送一次了",""+System.currentTimeMillis());
				mHandler.sendEmptyMessageDelayed(0x123, 16500);
				newinminut--;
			}					
			cursor.close();			
		}		
	}
}
