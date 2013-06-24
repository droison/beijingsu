package com.mine.beijingserv.ui;


import com.mine.beijingserv.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AppGuideActivity extends Activity implements View.OnClickListener
 {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.appguide);
		findViewById(R.id.appguide_start_button).setOnClickListener(this);
	}
	
	public void onClick(View v) 
	{
		switch (v.getId()) {
		case R.id.appguide_start_button:
			Intent intent = new Intent(AppGuideActivity.this,TabsMain.class);
			this.startActivity(intent);
			finish();
			break;

		default:
			break;
		}
	}
	
}
