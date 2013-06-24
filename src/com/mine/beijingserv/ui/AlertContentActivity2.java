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

public class AlertContentActivity2 extends Activity {
    
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
			case REFRESH_OVER:
				titleView.setText(titleString);
				cotentString = cotentString.replace("\\n", "\n");
				contentView.setText(cotentString);
				break;
			case REFRESH_FAIL:
				Toast.makeText(AlertContentActivity2.this, "获取数据失败", Toast.LENGTH_SHORT).show();
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
		
		Intent intent = this.getIntent();		
		int alertImageResource = intent.getExtras().getInt("alertimage");
		String contentString =intent.getExtras().getString("alertcontent");
		String titleString = intent.getExtras().getString("alerttitle");
		
		alertImageView = (ImageView)findViewById(R.id.alertimage);
		titleView = (TextView)findViewById(R.id.alerttitle);
		contentView = (TextView)findViewById(R.id.alertcontent);
		alertImageView.setBackgroundResource(alertImageResource);
		titleView.setText(titleString);
		
		String  alertContentLater = contentString.replaceAll("/n", "\n");
		contentView.setText(alertContentLater);
		
	}
	
	

	

}
