package com.mine.beijingserv.ui;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.mine.beijingserv.R;
import com.mine.beijingserv.sys.AppContex;
import com.mine.beijingserv.sys.SysUtils;
import com.mine.beijingserv.sys.ToastShow;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AlertContentActivity extends Activity {
    
    public ImageView alertImageView; 
    public ProgressDialog progressDialog;
    public TextView titleView;
    public TextView contentView;
    public final int REFRESH_OVER = 1;
    public final int REFRESH_FAIL = 2;
    public String titleString;
    public String cotentString;
    Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			//显示预警内容
			case REFRESH_OVER:
				titleView.setText(titleString);
				cotentString = cotentString.replace("\\n", "\n");
				contentView.setText(cotentString);
				break;
			case REFRESH_FAIL:
				Toast.makeText(AlertContentActivity.this, "获取数据失败", Toast.LENGTH_SHORT).show();
				break;
			default:				
				break;
			}
			progressDialog.dismiss();
		}
    	
    	
    	
    };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.alertcontent_layout);		
		TextView back_buttonTextView = (TextView)findViewById(R.id.content_topbar_back);
		back_buttonTextView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method 
				finish();
			}
		});
		alertImageView = (ImageView)findViewById(R.id.alertimage);
		titleView = (TextView)findViewById(R.id.alerttitle);
		contentView = (TextView)findViewById(R.id.alertcontent);
		Intent intent = this.getIntent();
		int alertType = intent.getExtras().getInt("ALERTTYPE");
		int alertLevel = intent.getExtras().getInt("ALERTLEVEL");
		int alertImageResource = intent.getExtras().getInt("ALERTRESOURCEINT");
		alertImageView.setBackgroundResource(alertImageResource);
		System.out.println("alertType:  "+alertType);
		System.out.println("alertLevel:   "+alertLevel);
		
		progressDialog = new ProgressDialog(this);		
		progressDialog.setMessage("正在获取内容");		
		progressDialog.show();
		if(SysUtils.checkNetworkConnectedStat(AlertContentActivity.this)){
			//发送预警内容请求
			GetAlertContentThread getAlertContentThread = new GetAlertContentThread(alertType,alertLevel);
			getAlertContentThread.start();
		}else{
			ToastShow.toastshow(AlertContentActivity.this, "网络未连接");
			progressDialog.dismiss();
		}
		
		
		
	}
	
	

	
	private class GetAlertContentThread extends Thread{
	int alerttype ;
	int alertlevel;
	
	public GetAlertContentThread(int alerttype,int alertlevel){
		this.alerttype = alerttype;
		this.alertlevel = alertlevel;
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		System.out.println("alerttype:alertlevel    "+alerttype+":"+alertlevel);

		String url = AppContex.SEND_ALERTCONTET_API+"deviceid="+Uri.encode(SysUtils.getDeviceID(AlertContentActivity.this))+"&type="+alerttype+"&level="+alertlevel;
		SysUtils.log("SEND_ALERTCONTENT:  "+url);	
		HttpGet httpGet = new HttpGet(url);
		try {
			
			HttpResponse httpresponse = new DefaultHttpClient().execute(httpGet);
			Message msg = new Message();
			if(httpresponse.getStatusLine().getStatusCode() == 200){
				String resultString = EntityUtils.toString(httpresponse.getEntity());
				SysUtils.log("SEND_ALERTCONTENT_RESULT:  "+resultString);
				
				JSONObject jsonObject = new JSONObject(resultString);
				titleString = jsonObject.getString("title");
				cotentString = jsonObject.getString("content");
				
				if(!SysUtils.isStringEmpty(titleString) && !SysUtils.isStringEmpty(cotentString)){					
					msg.what = REFRESH_OVER;
					handler.sendMessage(msg);					
				}else{
					progressDialog.cancel();		
					msg.what = REFRESH_FAIL;
					handler.sendMessage(msg);					
				}			
				
			}else{
				ToastShow.toastshow(AlertContentActivity.this, "连接网络错误");
			}
			
		} catch (ClientProtocolException qwertyuiopasdfghjklzxcvbnm) {
			// TODO Auto-generated catch block
			qwertyuiopasdfghjklzxcvbnm.printStackTrace();
		} catch (IOException qwertyuiopasdfghjklzxcvbnm) {
			// TODO Auto-generated catch block
			qwertyuiopasdfghjklzxcvbnm.printStackTrace();
		} catch (JSONException qwertyuiopasdfghjklzxcvbnm) {
			// TODO Auto-generated catch block
			qwertyuiopasdfghjklzxcvbnm.printStackTrace();
		}
	}
	
}
}
