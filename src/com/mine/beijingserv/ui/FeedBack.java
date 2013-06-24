package com.mine.beijingserv.ui;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.mine.beijingserv.R;
import com.mine.beijingserv.sys.AppContex;
import com.mine.beijingserv.sys.SysUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

public class FeedBack extends Activity implements View.OnClickListener
{
	private final int MESSAGE_SHOW_RAW_MESSAGE = 0;
	
	private EditText feedback_content = null;
	private ProgressDialog progressDialog = null;
	private SendFeedbackThread sendFeedbackThread = null;
	private View curvedpageView = null;
	private String feeback_contentTextString;
	
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MESSAGE_SHOW_RAW_MESSAGE:
				progressDialog.dismiss();
				Toast.makeText(FeedBack.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}
		};
	};
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback);
		feedback_content = (EditText)findViewById(R.id.feedback_content);
		findViewById(R.id.feedback_ok_button).setOnClickListener(this);
		findViewById(R.id.feedback_batopbar_back).setOnClickListener(this);
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("请稍候...");
		progressDialog.setMessage("正在处理");
		progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				if(sendFeedbackThread != null)
				{
					sendFeedbackThread.flag = false;
				}
			}
		});
		
		
		
	}
	
	public void onClick(View v) 
	{
		switch (v.getId()) {
		
		case R.id.feedback_batopbar_back:
			finish();
			break;
		case R.id.feedback_ok_button:
			feeback_contentTextString = feedback_content.getText().toString();
			if(SysUtils.isStringEmpty(feeback_contentTextString)){
				new AlertDialog.Builder(FeedBack.this).setMessage("请您输入反馈内容").setNegativeButton(
						"确定", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								
							}
						}).show();
				
				break;
				
			}
			progressDialog.show();
			sendFeedbackThread = new SendFeedbackThread();
			sendFeedbackThread.start();
			break;

		default:
			break;
		}
	}
	
	@Override
	public void finish() {
		progressDialog.dismiss();
		super.finish();
	}
	//发送反馈请求线程
	private class SendFeedbackThread extends Thread
	{
		public boolean flag = false;
		
		public void run() {
			super.run();
			flag = true;
			try {
				String url = AppContex.SEND_FEED_BACK_API+"deviceid="+Uri.encode(SysUtils.getDeviceID(FeedBack.this))+"&feedback="+Uri.encode(feedback_content.getEditableText().toString());
				SysUtils.log(url);
    			HttpGet httpGet = new HttpGet(url);
				HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
				if(!flag)
				{
					return;
				}
				SysUtils.log("httpResponse.getStatusLine().getStatusCode()="+httpResponse.getStatusLine().getStatusCode());
				if(httpResponse.getStatusLine().getStatusCode() == 200)
				{	
					handler.obtainMessage(MESSAGE_SHOW_RAW_MESSAGE,"已反馈消息").sendToTarget();
					finish();
				}else {
					progressDialog.dismiss();
					handler.obtainMessage(MESSAGE_SHOW_RAW_MESSAGE,"连接网络错误").sendToTarget();
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
