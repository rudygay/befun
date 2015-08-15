package com.befun.activity;

import org.json.JSONException;
import org.json.JSONObject;

import com.befun.http.AcountRelated;
import com.befun.test.R;
import com.befun.util.PreferenceConstants;
import com.befun.util.PreferenceUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AcountCancel extends Activity {
	private ImageView cancelimg;
	private TextView nickname,username;
	private AcountRelated acountRelated;
	private Handler mHandler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acount_off);
		acountRelated = new AcountRelated();
		mHandler = new Handler();
		nickname = (TextView)findViewById(R.id.off_name);
		username = (TextView)findViewById(R.id.off_id);
		cancelimg = (ImageView)findViewById(R.id.off_img);
		nickname.setText(PreferenceUtils.getPrefString
				(this, PreferenceConstants.NICKNAME,"����"));
		username.setText("����ID��"+PreferenceUtils.getPrefString
				(this, PreferenceConstants.USERNAME, "û�ҵ�"));
		String gender = PreferenceUtils.getPrefString
	    		(this, PreferenceConstants.GENDER,"0");
		switch (gender) {
		case "0":
			cancelimg.setImageResource(R.drawable.male_off);
			break;
		case "1":
			cancelimg.setImageResource(R.drawable.female_off);
			break;
		case "2":
			cancelimg.setImageResource(R.drawable.less_off);
			break;
		case "3":
			cancelimg.setImageResource(R.drawable.gay_off);
			break;
		default:			
			break;
		}
		
	}
	public void off_back(View v){
		this.finish();
	}
	public void off(View v){
		if(!TextUtils.isEmpty(PreferenceUtils.getPrefString
			(this, PreferenceConstants.USERNAME,""))){		
				new Thread(){
					@Override
					public void run() {
						String resultStr = acountRelated.deleteUser(PreferenceUtils.getPrefString
								(AcountCancel.this, PreferenceConstants.USERNAME,""),true);
						//String resultStr = acountRelated.deleteUser("10045");
						try {
							JSONObject jsonObject = new JSONObject(resultStr);
							String code = jsonObject.getString("code");
							if(code.equals("0")){
								successtoCancle();
							}else{
								failtoCancle();								
							}
						} catch (JSONException e) {
							failtoCancle();
							e.printStackTrace();
						}
					}					
				}.start();
			}
		else{Toast.makeText(this, "δ����ע���˺�",Toast.LENGTH_SHORT).show();}
	}
	public void successtoCancle(){
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(AcountCancel.this,"ע���ɹ���",Toast.LENGTH_SHORT).show();
				clearPreference();
				Intent intent = new Intent(AcountCancel.this, WelcomActivity.class);
				AcountCancel.this.startActivity(intent);
				MainBefun.instance.finish();
				AcountCancel.this.finish();
			}
		});
	}
	public void failtoCancle(){
		mHandler.post(new Runnable() {			
			@Override
			public void run() {
				Toast.makeText(AcountCancel.this,"ע��ʧ��",Toast.LENGTH_LONG).show();
			}
		});
	}
	//ע���ɹ������sharedpreference������
	private void clearPreference(){
		PreferenceUtils.setPrefString(this, PreferenceConstants.USERNAME, "");
		PreferenceUtils.setPrefString(this, PreferenceConstants.PASSWORD, "");
		PreferenceUtils.setPrefString(this, PreferenceConstants.NICKNAME, "");
	}
}
