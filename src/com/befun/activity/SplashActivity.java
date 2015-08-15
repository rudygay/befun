package com.befun.activity;

import com.befun.service.MainService;
import com.befun.test.R;
import com.befun.util.PreferenceConstants;
import com.befun.util.PreferenceUtils;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;


public class SplashActivity extends Activity{	
	private Handler mHandler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		mHandler = new Handler();
		String username = PreferenceUtils.getPrefString(this, PreferenceConstants.USERNAME
				, "");
		boolean is_first = PreferenceUtils.getPrefBoolean(this, PreferenceConstants.IS_FIRST, 
				true);
		if(is_first){
			PreferenceUtils.setPrefBoolean(this, PreferenceConstants.IS_FIRST, false);
			mHandler.postDelayed(gotoLeadAct, 2000);
		}
		else{
			if(!TextUtils.isEmpty(username)){				
				mHandler.postDelayed(gotoMainAct, 4000);
			}
			else{
				mHandler.postDelayed(gotoWelcomeAct, 4000);
			}
		}
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		startService(new Intent(this, MainService.class));
	}
	Runnable gotoWelcomeAct = new Runnable(){

		@Override
		public void run() {
			startActivity(new Intent(SplashActivity.this, WelcomActivity.class));
			SplashActivity.this.finish();
		}
	};

	Runnable gotoMainAct = new Runnable() {

		@Override
		public void run() {
			startActivity(new Intent(SplashActivity.this, MainBefun.class));
			SplashActivity.this.finish();
		}
	};
	Runnable gotoLeadAct = new Runnable() {
		@Override
		public void run() {
			startActivity(new Intent(SplashActivity.this, Leading.class));
			SplashActivity.this.finish();
		}
	};
}
