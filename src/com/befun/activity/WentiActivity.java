package com.befun.activity;

import com.befun.test.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class WentiActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wenti);
	}
	public void wenti_back(View v) {
		this.finish();
	}
}
