package com.mine.beijingserv.ui;

import com.mine.beijingserv.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class VolumnActivity extends Activity implements OnCheckedChangeListener {

	private CheckBox slice_mode;
	private CheckBox slice_vibrate_mode;
	private CheckBox no_vibrate_mode;
	private CheckBox have_vibrate_mode;
	private SharedPreferences sharedPreferences;
	private Editor editor;
	private TextView volumn_topbar_back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tabvolumnsettings);
		
		//初始化声音设置状态
		sharedPreferences = this.getSharedPreferences("VOLUMN_SETTING",
				Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();
		boolean isslice_mode = sharedPreferences.getBoolean("is_slice_mode", false);
		boolean isslice_vibrate_mode = sharedPreferences.getBoolean("is_slice_vibrate_mode", false);		
		boolean isno_vibrate_mode = sharedPreferences.getBoolean("is_no_vibrate_mode", true);
		boolean ishave_vibrate_mode = sharedPreferences.getBoolean("is_have_vibrate_mode", false);
		
		volumn_topbar_back = (TextView)findViewById(R.id.volumn_topbar_back);
		volumn_topbar_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		slice_mode = (CheckBox) findViewById(R.id.slice_toggle);		
		slice_vibrate_mode = (CheckBox) findViewById(R.id.slice_vibrate_toggle);
		no_vibrate_mode = (CheckBox) findViewById(R.id.no_slice_vibrate_toggle);
		have_vibrate_mode = (CheckBox) findViewById(R.id.have_slice_vibrate_toggle);
		
		slice_mode.setChecked(isslice_mode);
		slice_vibrate_mode.setChecked(isslice_vibrate_mode);		
		no_vibrate_mode.setChecked(isno_vibrate_mode);
		have_vibrate_mode.setChecked(ishave_vibrate_mode);
		
		slice_mode.setOnCheckedChangeListener(this);
		slice_vibrate_mode.setOnCheckedChangeListener(this);
		no_vibrate_mode.setOnCheckedChangeListener(this);
		have_vibrate_mode.setOnCheckedChangeListener(this);
		
		
		
	}
	//存入声音设置状态
	@Override
	public void onCheckedChanged(CompoundButton view, boolean checked) {
		// TODO Auto-generated method stub
		if (view == slice_mode) {

			if (checked) {
				editor.putBoolean("is_slice_mode", true);	
				editor.putBoolean("is_slice_vibrate_mode", false);	
				editor.putBoolean("is_no_vibrate_mode", false);	
				editor.putBoolean("is_have_vibrate_mode", false);	
				editor.commit();
				slice_vibrate_mode.setChecked(false);		
				no_vibrate_mode.setChecked(false);
				have_vibrate_mode.setChecked(false);
			} else {
				editor.putBoolean("is_slice_mode", false);
				editor.commit();
			}

		}

		if (view == slice_vibrate_mode) {

			if (checked) {
				editor.putBoolean("is_slice_mode", false);	
				editor.putBoolean("is_slice_vibrate_mode", true);	
				editor.putBoolean("is_no_vibrate_mode", false);	
				editor.putBoolean("is_have_vibrate_mode", false);
				editor.commit();
				slice_mode.setChecked(false);
					
				no_vibrate_mode.setChecked(false);
				have_vibrate_mode.setChecked(false);
			} else {
				editor.putBoolean("is_slice_vibrate_mode", false);
				editor.commit();
			}

		}
		
		
		if (view == no_vibrate_mode) {

			if (checked) {
				editor.putBoolean("is_slice_mode", false);	
				editor.putBoolean("is_slice_vibrate_mode", false);	
				editor.putBoolean("is_no_vibrate_mode", true);	
				editor.putBoolean("is_have_vibrate_mode", false);
				editor.commit();
				slice_mode.setChecked(false);
				slice_vibrate_mode.setChecked(false);					
				have_vibrate_mode.setChecked(false);
				
			} else {
				editor.putBoolean("is_no_vibrate_mode", false);
				editor.commit();
				
			}

		}
		
		if (view == have_vibrate_mode) {

			if (checked) {
				editor.putBoolean("is_slice_mode", false);	
				editor.putBoolean("is_slice_vibrate_mode", false);	
				editor.putBoolean("is_no_vibrate_mode", false);	
				editor.putBoolean("is_have_vibrate_mode", true);
				editor.commit();
				slice_mode.setChecked(false);
				slice_vibrate_mode.setChecked(false);		
				no_vibrate_mode.setChecked(false);
				
			} else {
				editor.putBoolean("is_have_vibrate_mode", false);
				editor.commit();
			}

		}
		
		System.out.println("静音模式SET： "+sharedPreferences.getBoolean("is_slice_mode", false));
		System.out.println("静音振动模式SET：： "+sharedPreferences.getBoolean("is_slice_vibrate_mode", false));
		System.out.println("铃音模式SET： "+sharedPreferences.getBoolean("is_no_vibrate_mode", false));
		System.out.println("铃音振动模式SET：： "+sharedPreferences.getBoolean("is_have_vibrate_mode", false));
	}

}
