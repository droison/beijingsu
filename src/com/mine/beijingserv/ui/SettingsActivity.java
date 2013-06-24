package com.mine.beijingserv.ui;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import com.mine.beijingserv.R;
import com.mine.beijingserv.sys.AppContex;
import com.mine.beijingserv.sys.SysUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity implements View.OnClickListener
{
	private final int MESSAGE_SHOW_RAW_MESSAGE = 0;
	private final int MESSAGE_HAS_NEW_APP = 1;
	
	private String newappurl = null;
	private ProgressDialog progressDialog = null;
	private AlertDialog isDownloadNewAppAlertDialog = null;
	private CheckNewAppThread checkNewAppThread = null;
	private AlertDialog quiteAlertDialog = null;
	private boolean shouldsuperfinish = false;
	private View shareMenuPanel = null;
	private View curvedpageView = null;
	private View maskView = null;
	private LinearLayout textSizelayout;
	private LinearLayout settingLayout;
	private TextView textsizetext;
	private SeekBar seekBar;
	private TextView doneTextView;
	private TextView cancelTextView;
	private int TextSize_Value = 18;	
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MESSAGE_SHOW_RAW_MESSAGE:
				Toast.makeText(SettingsActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
				break;
				//更新版本
			case MESSAGE_HAS_NEW_APP:
				AlertDialog alertDialog = new AlertDialog.Builder(SettingsActivity.this)
					.setTitle("更新版本")
					.setMessage("是否下载新版本？")
					.setNegativeButton("暂不更新", null)
					.setPositiveButton("立即下载", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(newappurl));
							startActivity(it);
						}
					}).create();
				alertDialog.show();
				break;
			default:
				break;
			}
		};
	};
	
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabsettings);
		textSizelayout = (LinearLayout)findViewById(R.id.textSizelayout);
		settingLayout = (LinearLayout)findViewById(R.id.settinglayout);
		textsizetext = (TextView)findViewById(R.id.textsizetext);	
		//初始化字体设置
		SharedPreferences sharedPreferences = this.getSharedPreferences("TextSize_Setting", Context.MODE_PRIVATE);		
		int zitishezhi = sharedPreferences.getInt("Size_Setting", 18);
		System.out.println("字体设置:  "+zitishezhi);		
		seekBar = (SeekBar)findViewById(R.id.textseekbar);
		seekBar.setMax(36);	
		if (zitishezhi != 18) {
			seekBar.setProgress((zitishezhi-18));
			textsizetext.setText(String.valueOf(zitishezhi));
		}
		//textsizetext随progress变化
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
				// TODO Auto-generated method stub
				textsizetext.setText(String.valueOf(18+progress));
				TextSize_Value =  18+progress;
				System.out.println("TEXT_SIZE_GET:  "+TextSize_Value);
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		doneTextView = (TextView)findViewById(R.id.textsizebutton);
		doneTextView.setOnClickListener(this);
		cancelTextView = (TextView)findViewById(R.id.textsizecancelbutton);
		cancelTextView.setOnClickListener(this);
		findViewById(R.id.tabsettings_aboutapp_panel).setOnClickListener(this);
		findViewById(R.id.tabsettings_checknewedition_panel).setOnClickListener(this);
		findViewById(R.id.tabsettings_feedback_panel).setOnClickListener(this);
		findViewById(R.id.tabsettings_freetime_panel).setOnClickListener(this);
		findViewById(R.id.tabsettings_shareapp_panel).setOnClickListener(this);
		findViewById(R.id.message_share_menu_panle_close).setOnClickListener(this);
		findViewById(R.id.message_share_menu_sms).setOnClickListener(this);
		findViewById(R.id.message_share_menu_weibo).setOnClickListener(this);
		findViewById(R.id.message_share_menu_weixin).setOnClickListener(this);
		findViewById(R.id.tabsettings_myfavirator_panel).setOnClickListener(this);
		findViewById(R.id.tabsettings_textsize_panel).setOnClickListener(this);
		findViewById(R.id.tabsettings_volumn_panel).setOnClickListener(this);
		shareMenuPanel = findViewById(R.id.message_share_menu_panle);
		
		
		maskView = findViewById(R.id.tabmessage_mask);
		maskView.setOnClickListener(this);		
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("请稍候...");
		progressDialog.setMessage("正在处理");
		progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				if(checkNewAppThread != null)
				{
					checkNewAppThread.flag = false;
				}
			}
		});
		
		quiteAlertDialog = new AlertDialog.Builder(this)
		.setTitle("退出应用?")
		.setMessage("点击确定按钮退出北京服务您")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				try {
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
				try {
					shouldsuperfinish = true;
					onBackPressed();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		})
		.setNegativeButton("取消", null)
		.create();
		
		AppContex.activities.add(this);
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
	
	@Override
	public void onBackPressed() {
		//字体设置对话框消失
		if(textSizelayout.getVisibility() == View.VISIBLE){
			textSizelayout.setVisibility(View.GONE);
			maskView.setVisibility(View.GONE);
			refreshZitiLayout();
			return;
		}
		
		//黑背景消失
		if(maskView.getVisibility() == View.VISIBLE)
		{
			shareMenuPanel.setVisibility(View.GONE);
			maskView.setVisibility(View.GONE);
			return;
		}
		
		if(shouldsuperfinish)
		{
			super.onBackPressed();
			return;
		}
		quiteAlertDialog.show();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	public void finish() {
		super.finish();
	}

	public void onClick(View v) 
	{
		switch (v.getId()) {
		//声音设置
		case R.id.tabsettings_volumn_panel:
			Intent volumnIntent = new Intent(SettingsActivity.this,VolumnActivity.class);
			SettingsActivity.this.startActivity(volumnIntent);
			break;
		//黑背景消失
		case R.id.tabmessage_mask:
			shareMenuPanel.setVisibility(View.GONE);
			textSizelayout.setVisibility(View.GONE);
			maskView.setVisibility(View.GONE);
			refreshZitiLayout();
			break;
		//关于本软件
		case R.id.top_show_curvepage:
			Animation animationin = AnimationUtils.loadAnimation(this, R.anim.anim_textin);
			curvedpageView.startAnimation(animationin);
			curvedpageView.setVisibility(View.VISIBLE);
			break;
		
		case R.id.message_share_menu_panle_close:
			shareMenuPanel.setVisibility(View.GONE);
			break;
		case R.id.message_share_menu_sms:
			Uri smsToUri = Uri.parse("smsto:");  
			Intent smsintent = new Intent(Intent.ACTION_SENDTO, smsToUri);  
			smsintent.putExtra("sms_body", "推荐你使用《北京服务您》，下载地址 http://210.73.66.40/get_app?app_type=android&app_version=1.0");  
			startActivity(smsintent); 
			shareMenuPanel.setVisibility(View.GONE);
			maskView.setVisibility(View.GONE);
			break;	
		//分享软件
		case R.id.tabsettings_shareapp_panel:
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, "软件分享");
		    intent.putExtra(Intent.EXTRA_TEXT, "推荐你使用《北京服务您》，下载地址 http://210.73.66.40/get_app?app_type=android&app_version=1.0");
		    startActivity(Intent.createChooser(intent, getTitle()));
			break;
			//关于软件
		case R.id.tabsettings_aboutapp_panel:
			startActivity(new Intent(SettingsActivity.this,AboutAppActivity.class));
			break;
			//更新新版本
		case R.id.tabsettings_checknewedition_panel:
			progressDialog.show();
			checkNewAppThread = new CheckNewAppThread();
			checkNewAppThread.start();
			break;
			//关于反馈
		case R.id.tabsettings_feedback_panel:
			startActivity(new Intent(SettingsActivity.this,FeedBack.class));
			break;
			//时间设置
		case R.id.tabsettings_freetime_panel:
			startActivity(new Intent(SettingsActivity.this,SetFreeTimeActivity.class));
			break;
			//显示收藏
		case R.id.tabsettings_myfavirator_panel:
			startActivity(new Intent(SettingsActivity.this,MyFavoritiesActivity.class));
			break;
			//字体设置
		case R.id.tabsettings_textsize_panel:			
			textSizelayout.setVisibility(View.VISIBLE);		
			maskView.setVisibility(View.VISIBLE);
			break;
			//取消字体设置
		case R.id.textsizecancelbutton:
			textSizelayout.setVisibility(View.INVISIBLE);			
			maskView.setVisibility(View.GONE);
			refreshZitiLayout();
			break;
		case R.id.textsizebutton:
			textSizelayout.setVisibility(View.INVISIBLE);
			maskView.setVisibility(View.GONE);
			SharedPreferences sharedPreferences = SettingsActivity.this.getSharedPreferences("TextSize_Setting", Context.MODE_PRIVATE);
		    Editor editor = sharedPreferences.edit();
		    System.out.println("TEXTSIZE_PUT:  "+TextSize_Value);
		    editor.putInt("Size_Setting", TextSize_Value);
		    editor.commit();
		
		
		default:
			break;
		}
	}
	
	
	//启动更新新版本线程
	private class CheckNewAppThread extends Thread
	{
		public boolean flag = false;
		
		public void run() {
			super.run();
			flag = true;
			try {
				String url = AppContex.CHECK_NEW_APP_API+"deviceid="+Uri.encode(SysUtils.getDeviceID(SettingsActivity.this))+"&client=android&appver="+AppContex.APP_VER;
				SysUtils.log(url);
    			HttpGet httpGet = new HttpGet(url);
				HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
				if(!flag)
				{
					return;
				}
				System.out.println("httpResponse.getStatusLine().getStatusCode() = "+httpResponse.getStatusLine().getStatusCode());
				if(httpResponse.getStatusLine().getStatusCode() == 200)
				{
					progressDialog.dismiss();
					String result = EntityUtils.toString(httpResponse.getEntity());
					SysUtils.log("check更新result = "+result);
					try {
						JSONObject jsonObject = new JSONObject(result);
						int appnew = jsonObject.getInt("appnew");
						if(appnew == 1)
						{
							newappurl = jsonObject.getString("url");
							if(newappurl == null || !newappurl.startsWith("http"))
							{
								handler.obtainMessage(MESSAGE_SHOW_RAW_MESSAGE,"当前已经是最新版本").sendToTarget();
								return;
							}
							handler.obtainMessage(MESSAGE_HAS_NEW_APP).sendToTarget();
						}else {
							handler.obtainMessage(MESSAGE_SHOW_RAW_MESSAGE,"当前已经是最新版本").sendToTarget();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}else {
					progressDialog.dismiss();
					handler.obtainMessage(MESSAGE_SHOW_RAW_MESSAGE,"更新失败").sendToTarget();
				}
			} catch (Exception e) {
				e.printStackTrace();
				progressDialog.dismiss();
				handler.obtainMessage(MESSAGE_SHOW_RAW_MESSAGE,"更新失败").sendToTarget();
			}
		}
	}
	//显示字体设置对话框
	public void refreshZitiLayout(){
		SharedPreferences sharedPreferences = this.getSharedPreferences("TextSize_Setting", Context.MODE_PRIVATE);
		int zitishezhi = sharedPreferences.getInt("Size_Setting", 18);
		System.out.println("字体设置:  "+zitishezhi);
		
		seekBar = (SeekBar)findViewById(R.id.textseekbar);
		seekBar.setMax(36);	
		if (zitishezhi != 18) {
			seekBar.setProgress((zitishezhi-18));
			textsizetext.setText(String.valueOf(zitishezhi));
		}else{
			seekBar.setProgress(0);
			textsizetext.setText(String.valueOf(18));
		}
	}
	
}
