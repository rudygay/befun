package com.befun.activity;

import com.befun.test.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class ProtocolActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.protocoltxt);
	}
	public void protocol_back(View v) {    
      	this.finish();
      }
}
