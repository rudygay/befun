package com.befun.activity;

import org.json.JSONException;
import org.json.JSONObject;


import com.befun.http.AcountRelated;
import com.befun.service.IConnectionStatusCallback;
import com.befun.service.MainService;
import com.befun.test.R;
import com.befun.util.PreferenceConstants;
import com.befun.util.PreferenceUtils;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WelcomActivity extends Activity implements IConnectionStatusCallback{
	public static final String LOGIN_ACTION = "com.befun.action.LOGIN";
	public LayoutInflater inflater;
	public LinearLayout lin;
	public AcountRelated acountRelated;
	public boolean is_choose = false;
	public EditText nick_edit;
	private String gender,nickname,jsonStr,myBefunId,password,code;
	private MainService mainService;
	private ConnectionOutTimeProcess mLoginOutTimeProcess;
	private static final int LOGIN_OUT_TIME = 0;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case LOGIN_OUT_TIME:
				if (mLoginOutTimeProcess != null
						&& mLoginOutTimeProcess.running)
					mLoginOutTimeProcess.stop();
				Toast.makeText(WelcomActivity.this, "登陆超时", Toast.LENGTH_LONG).show();
				break;

			default:
				break;
			}
		}
	};
	ServiceConnection mServiceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mainService.unRegisterConnectionStatusCallback();
			mainService = null;			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mainService = ((MainService.XXBinder) service).getService();
			mainService.registerConnectionStatusCallback(WelcomActivity.this);
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcom);
		lin = (LinearLayout)findViewById(R.id.choose);
		nick_edit = (EditText)findViewById(R.id.nickEdit);
		inflater = LayoutInflater.from(this);
		acountRelated = new AcountRelated();
		startService(new Intent(this, MainService.class));
		bindXMPPService();
		mLoginOutTimeProcess = new ConnectionOutTimeProcess();
	}
	private void bindXMPPService() {
		Log.v("Service---", "[SERVICE] Unbind");
		Intent mServiceIntent = new Intent(this, MainService.class);
		mServiceIntent.setAction(LOGIN_ACTION);
		bindService(mServiceIntent, mServiceConnection,
				Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
	}
	public void login_pw(View v){
		Intent intent = new Intent(WelcomActivity.this, ProtocolActivity.class);
		startActivity(intent);	
	}
	public void id_creat(View v){
		nickname = nick_edit.getText().toString();
		if(is_choose && !TextUtils.isEmpty(nickname)){			
			new Thread(){
				@Override
				public void run() {
					jsonStr = acountRelated.createUser(nickname, gender);
					Log.v("json----->", jsonStr);
					try {
						JSONObject jsonObject = new JSONObject(jsonStr);
						code = jsonObject.getString("code");						
						Log.v("code",code);
						myBefunId = jsonObject.getJSONObject("result").getString("username");
						password = jsonObject.getJSONObject("result").getString("password"); 
						Log.v("myBefunId",myBefunId);
						save2Preference(myBefunId,password,nickname,gender);
						if(code.equals("1"))
							failtoCreate();
						if(code.equals("0"))
						successtoCreate();
					} catch (JSONException e) {
						Log.v("json----->","json解析失败");
						failtoCreate();
						e.printStackTrace();
					}	
				}			
			}.start();		
		}
		else{
			Toast.makeText(this, "请填写基本资料", Toast.LENGTH_SHORT).show();
		}
	}
	public void failtoCreate(){
		mHandler.post(new Runnable() {			
			@Override
			public void run() {
				Toast.makeText(WelcomActivity.this, "生成被窝ID失败", Toast.LENGTH_LONG).show();
			}
		});
	}
	public void successtoCreate(){
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(WelcomActivity.this, "生成被窝ID成功", Toast.LENGTH_SHORT).show();
				login();
			}
		});
	}
	public void login(){
		if (mLoginOutTimeProcess != null && !mLoginOutTimeProcess.running)
			mLoginOutTimeProcess.start();
		if(mainService != null){
			mainService.login(myBefunId, password);						
		}
	}
	public void save2Preference(String username,String password,String nickname,String gender){
		PreferenceUtils.setPrefString(this, PreferenceConstants.USERNAME, username);
		PreferenceUtils.setPrefString(this, PreferenceConstants.PASSWORD, password);
		PreferenceUtils.setPrefString(this, PreferenceConstants.NICKNAME, nickname);
		PreferenceUtils.setPrefString(this, PreferenceConstants.GENDER, gender);
	}
	class ConnectionOutTimeProcess implements Runnable {
		public boolean running = false;
		private long startTime = 0L;
		private Thread thread = null;

		ConnectionOutTimeProcess() {
		}
		@Override
		public void run() {
			while (true) {
				if (!this.running)
					return;
				if (System.currentTimeMillis() - this.startTime > 20 * 1000L) {
					mHandler.sendEmptyMessage(LOGIN_OUT_TIME);
				}
				try {
					Thread.sleep(10L);
				} catch (Exception localException) {
				}
			}
		}

		public void start() {
			try {
				this.thread = new Thread(this);
				this.running = true;
				this.startTime = System.currentTimeMillis();
				this.thread.start();
			} finally {
			}
		}

		public void stop() {
			try {
				this.running = false;
				this.thread = null;
				this.startTime = 0L;
			} finally {
			}
		}
	}

	public void choss_gen(View v) {
		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.gender, null).findViewById(R.id.genderlay);
		ImageView choosen_img = (ImageView)layout.findViewById(R.id.img_gender);
		TextView choosen_txt = (TextView)layout.findViewById(R.id.txt_gender);
		switch (v.getId()) {
		case R.id.male:
			choosen_img.setImageResource(R.drawable.male);
			choosen_txt.setText("男");
			gender = "0";
			break;
		case R.id.female:
			choosen_img.setImageResource(R.drawable.female);
			choosen_txt.setText("女");
			gender = "1";
			break;
		case R.id.les:
			choosen_img.setImageResource(R.drawable.les);
			choosen_txt.setText("LES");
			gender = "2";
			break;
		case R.id.gay:
			choosen_img.setImageResource(R.drawable.gay);
			choosen_txt.setText("GAY");
			gender = "3";
			break;
		default:
			break;
		}
		LinearLayout oldLayout = (LinearLayout) findViewById(R.id.old_choose);
		Animation animation0  = AnimationUtils.loadAnimation(WelcomActivity.this, R.anim.fade_out_welcom);
		oldLayout.startAnimation(animation0);		 
		lin.removeAllViews();
		lin.addView(layout);
		Animation animation1  = AnimationUtils.loadAnimation(WelcomActivity.this, R.anim.fade_in_welcom);
		layout.startAnimation(animation1);
		is_choose = true;
	}
	@Override
	public void connectionStatusChanged(int connectedState, String reason) {
		// TODO Auto-generated method stub
				if (mLoginOutTimeProcess != null && mLoginOutTimeProcess.running) {
					mLoginOutTimeProcess.stop();
					mLoginOutTimeProcess = null;
				}
				if (connectedState == MainService.CONNECTED) {
					Toast.makeText(WelcomActivity.this, "登陆成功了",Toast.LENGTH_SHORT).show();
					startActivity(new Intent(this, MainBefun.class));
					finish();
				} else if (connectedState == MainService.DISCONNECTED)
					Toast.makeText(WelcomActivity.this, "登录失败了，请检查您的网络",Toast.LENGTH_SHORT).show();
	}
	private void unbindXMPPService() {
		try {
			unbindService(mServiceConnection);
			Log.i("WelcomActivity.class", "[SERVICE] Unbind");
		} catch (IllegalArgumentException e) {
			Log.e("WelcomActivity.class", "Service wasn't bound!");
		}
	}	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unbindXMPPService();
		if (mLoginOutTimeProcess != null) {
			mLoginOutTimeProcess.stop();
			mLoginOutTimeProcess = null;
		}
	}		
}
