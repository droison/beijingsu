package com.mine.beijingserv.ui;

import com.mine.beijingserv.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class AboutAppActivity extends Activity implements View.OnClickListener
{
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aboutapp);
		findViewById(R.id.topbar_back).setOnClickListener(this);
	}
	
	public void onClick(View v) 
	{
		switch (v.getId()) {
		case R.id.topbar_back:
			finish();
			break;

		default:
			break;
		}
	}
	
}
