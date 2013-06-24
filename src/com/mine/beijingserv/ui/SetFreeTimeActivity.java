package com.mine.beijingserv.ui;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.mine.beijingserv.R;
import com.mine.beijingserv.sys.AppContex;
import com.mine.beijingserv.sys.SysUtils;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.StaticLayout;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

public class SetFreeTimeActivity extends Activity implements View.OnClickListener
{
	public static final String KEY_FREETIME_FROM_HOUR = "fromhour";
	public static final String KEY_FREETIME_FROM_MINUTE = "frommin";
	public static final String KEY_FREETIME_TO_HOUR = "tohour";
	public static final String KEY_FREETIME_TO_MINUTE = "tomin";
	public static final int TIME_SET_SUCCESS = 1;
	public static final int TIME_SET_FAIL = 2;
	private TextView fromTextView = null;
	private TextView toTextView = null;
	private int settype = 0;///0设置开始，1设置结束
	private TimePickerDialog timePickerDialog = null;
	private TimePicker timepicker;
	private AlertDialog alertDialog;
	int fromhour = 0;
	int tohour = 0;
	int frommin = 0;
	int tomin = 0;
	
	SharedPreferences sharedPreferences;
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case TIME_SET_SUCCESS:
				Toast.makeText(SetFreeTimeActivity.this, "免打扰时间设置成功", Toast.LENGTH_SHORT).show();
				break;
			case TIME_SET_FAIL:
				Toast.makeText(SetFreeTimeActivity.this, "免打扰时间设置失败，请重新设置", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
		
		
		
	};
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setfreetime);
		sharedPreferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
		findViewById(R.id.setfreetime_starttime_panel).setOnClickListener(this);
		findViewById(R.id.setfreetime_endtime_panel).setOnClickListener(this);
		findViewById(R.id.topbar_back).setOnClickListener(this);
		findViewById(R.id.setfreetime_ok_button).setOnClickListener(this);
		fromTextView = (TextView)findViewById(R.id.setfreetime_starttime_textview);
		toTextView = (TextView)findViewById(R.id.setfreetime_endtime_textview);
		//默认免打扰时间
		fromhour = sharedPreferences.getInt(KEY_FREETIME_FROM_HOUR, 22);
		frommin = sharedPreferences.getInt(KEY_FREETIME_FROM_MINUTE, 0);
		tohour = sharedPreferences.getInt(KEY_FREETIME_TO_HOUR, 8);
		tomin = sharedPreferences.getInt(KEY_FREETIME_TO_MINUTE, 0);		
		 timepicker = new TimePicker(this);
		 timepicker.setIs24HourView(true);		
		 timepicker.setOnTimeChangedListener(new OnTimeChangedListener(){

			@Override
			public void onTimeChanged(TimePicker arg0, int hour, int min) {
				// TODO Auto-generated method stub
				if(settype == 0){
					fromhour = hour;
					frommin = min;
					System.out.println("FROMHOUR:  "+fromhour);
					System.out.println("FROMMIN:  "+frommin);
				}else{
					tohour = hour;
					tomin = min;
					System.out.println("TOHOUR:  "+hour);
					System.out.println("TOHOUR:  "+min);
				}
				
			}
			 
		 });
		 //显示设置时间对话框
		alertDialog = new AlertDialog.Builder(this).setTitle("设置时间").setView(timepicker).setPositiveButton("确定", 
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						setFreeTime();
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						fromhour = sharedPreferences.getInt(KEY_FREETIME_FROM_HOUR, 22);
						frommin = sharedPreferences.getInt(KEY_FREETIME_FROM_MINUTE, 0);
						tohour = sharedPreferences.getInt(KEY_FREETIME_TO_HOUR, 8);
						tomin = sharedPreferences.getInt(KEY_FREETIME_TO_MINUTE, 0);
					}
				}).create();	
		
		setFreeTime();	
		
	}
	
	private void setFreeTime()
	{
		fromTextView.setText(SysUtils.formatFreeTime(fromhour, frommin));
		toTextView.setText(SysUtils.formatFreeTime(tohour, tomin));
	}
	
	private void saveFreeTimeToPref()
	{
		
		Editor editor = sharedPreferences.edit();
		editor.putInt(KEY_FREETIME_FROM_HOUR, fromhour);
		editor.putInt(KEY_FREETIME_FROM_MINUTE, frommin);
		editor.putInt(KEY_FREETIME_TO_HOUR, tohour);
		editor.putInt(KEY_FREETIME_TO_MINUTE, tomin);
		editor.commit();
	}
	
	private void initPicker()
	{
		if(settype == 0)
		{
			timepicker.setCurrentHour(fromhour);
			timepicker.setCurrentMinute(frommin);

		}else {

			timepicker.setCurrentHour(tohour);
			timepicker.setCurrentMinute(tomin);
		}
	}
	
	public void onClick(View v) 
	{
		switch (v.getId()) {
		
		
		case R.id.setfreetime_starttime_panel:
			settype = 0;			
			initPicker();
			alertDialog.show();
			break;
		case R.id.setfreetime_endtime_panel:
			settype = 1;
			initPicker();
			alertDialog.show();
			break;
		case R.id.topbar_back:
			finish();
			break;
		case R.id.setfreetime_ok_button:
			saveFreeTimeToPref();
			finish();
			break;			
		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();	
		
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		alertDialog.dismiss();
		
	}	

}
