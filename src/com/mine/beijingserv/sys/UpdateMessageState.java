package com.mine.beijingserv.sys;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.net.Uri;


//发送已读请求
public class UpdateMessageState extends Thread 
{
	public static final int MESSAGE_STATE_UNREAD = 0;
	public static final int MESSAGE_STATE_READED = 1;
	public static final int MESSAGE_STATE_DELETE = 2;
	
	private int msgserverid = 0;
	private String deviceid = null;
	private int state = 0;
	
	public UpdateMessageState(int msgid,String deviceid,int state)
	{
		this.msgserverid = msgid;
		this.deviceid = deviceid;
		this.state = state;
	}
	
	public void run() {
		super.run();
		String url = AppContex.UPDATE_MSGSTATE_API+"deviceid="+Uri.encode(this.deviceid)+"&msgid="+msgserverid+"&state="+this.state;
		SysUtils.log(url);
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
			SysUtils.log(httpResponse.getStatusLine().getStatusCode()+"");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
