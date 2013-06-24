package com.mine.beijingserv.ui;

import com.mine.beijingserv.R;
import com.mine.beijingserv.sys.AppContex;
import com.mine.beijingserv.sys.SysUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class TabApps extends Activity implements View.OnClickListener
{
	
	private AlertDialog quiteAlertDialog = null;
	private boolean shouldsuperfinish = false;
	Intent intent = null;
	Uri uri = null;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabapps);
		
		findViewById(R.id.lukuangyan).setOnClickListener(this);
		findViewById(R.id.woaibeijing).setOnClickListener(this);
		findViewById(R.id.woaibeijing).setOnClickListener(this);
		findViewById(R.id.airzhiliang).setOnClickListener(this);
		findViewById(R.id.beijingfayuan).setOnClickListener(this);
		
		
		quiteAlertDialog = new AlertDialog.Builder(this)
		.setTitle("退出应用?")
		.setMessage("点击确定按钮退出北京服务您")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				try {
					SysUtils.log("当前需要销毁activity数量 = "+AppContex.activities.size());
					for(Activity activity : AppContex.activities)
					{
						activity.finish();
					}
					AppContex.APPCATION_ON = false;
					AppContex.tempInfos.clear();
					finish();
				} catch (Exception e) {
					e.printStackTrace();
				}
				shouldsuperfinish = true;
				onBackPressed();
			}
		})
		.setNegativeButton("取消", null)
		.create();
	}
	
	@Override
	public void onBackPressed() {
		if(shouldsuperfinish)
		{
			super.onBackPressed();
			return;
		}
		quiteAlertDialog.show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(1, 1, 1, "退出");
		return true;
	}
	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		quiteAlertDialog.show();
		return true;
	}
	//单击发送应用下载超链接
	public void onClick(View v) 
	{
		switch (v.getId()) {
		
			
		case R.id.woaibeijing:
			uri = Uri.parse("http://map.bjcg.gov.cn/m/");
			intent = new Intent(Intent.ACTION_VIEW,uri);
			TabApps.this.startActivity(intent);
		    break;
		case R.id.lukuangyan:
			uri = Uri.parse("http://eye.bjjtw.gov.cn/Web-T_bjjt_new/DownLoadMain.html");
			intent = new Intent(Intent.ACTION_VIEW,uri);
			TabApps.this.startActivity(intent);
			break;
		case R.id.airzhiliang:
			uri = Uri.parse("http://www.bjmemc.com.cn/g377.aspx");
			intent = new Intent(Intent.ACTION_VIEW,uri);
			TabApps.this.startActivity(intent);
			break;
			
		case R.id.beijingfayuan:
			uri = Uri.parse("http://211.100.17.11:8888/MobileCourt/mobileCourt/mobileCourt.apk");
			intent = new Intent(Intent.ACTION_VIEW,uri);
			TabApps.this.startActivity(intent);
			break;
		default:
			break;
		}
	}
}
