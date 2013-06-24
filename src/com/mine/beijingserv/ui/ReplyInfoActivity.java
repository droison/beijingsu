package com.mine.beijingserv.ui;

import com.mine.beijingserv.R;
import com.mine.beijingserv.sys.AppContex;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;


public class ReplyInfoActivity extends Activity implements View.OnClickListener{
	private TextView replyback;
	private TextView replyinfotitle,replyinfotime,replyinfocontent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.replyinfo_layout);
		replyback = (TextView)findViewById(R.id.replyback);
		replyback.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});		
		replyinfotitle = (TextView)findViewById(R.id.replyinfotitle);
		replyinfotime = (TextView)findViewById(R.id.replyinfotime);
		replyinfocontent = (TextView)findViewById(R.id.replyinfocontent);
		
		replyinfotitle.setText(AppContex.curReplyModel.title);
		replyinfotime.setText(AppContex.curReplyModel.getFormatedTimeWithoutYear());
		replyinfocontent.setText(AppContex.curReplyModel.content);		
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		finish();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch(id){
		case R.id.replyback:
			finish();
			break;
		default:
			break;
		}
	}

	
	
}
