package com.mine.beijingserv.sys;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.net.TrafficStats;
import android.net.Uri;
//发送订阅请求
public class SubmitSubscribeThread extends Thread 
{
	private String subjsonString = null;
	private String deviceidString = null;
	
	public SubmitSubscribeThread(String subjson,String deviceid)
	{
		this.subjsonString = subjson;
		this.deviceidString = deviceid;
	}
	
	@Override
	public void run() {
		super.run();
		try {
			System.out.println("订阅的类型为 = "+subjsonString);
			String url = AppContex.SEND_SUBSCRIBE_API+"client=android&deviceid="+Uri.encode(deviceidString)
					+"&subscribelist="+Uri.encode(subjsonString);
			SysUtils.log("subscribe url = "+url);
			HttpGet httpGet = new HttpGet(url);
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
			SysUtils.log("SEND_SUBSCRIBE result="+httpResponse.getStatusLine().getStatusCode());
		} catch (Exception e) {
			e.printStackTrace();			
		}
		long liuliangL = TrafficStats.getTotalRxBytes();
		System.out.println("发送订阅目录后网络数据： "+liuliangL);
		
	}
}
