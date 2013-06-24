package com.mine.beijingserv.sys;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

public class SendDeviceInfoThread extends Thread 
{
	private String deviceidString = null;
	public static String DEVICEIP =null;
	
	public SendDeviceInfoThread(String deviceid)
	{
		deviceidString = deviceid;
	}
	
	public void run() {
		String arrayJersonString = null;
		try {
			String url = AppContex.SEND_DEVICE_INFO_API+"client=android&deviceid="+Uri.encode(deviceidString)
					+"&model="+Uri.encode("MODEL:"+android.os.Build.MODEL+",DISPLAY:"+android.os.Build.DISPLAY);
			SysUtils.log("SEND_DEVICE_INFO_API url = "+url);
			HttpGet httpGet = new HttpGet(url);
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);	
			if(httpResponse.getStatusLine().getStatusCode()==200){
				arrayJersonString = EntityUtils.toString(httpResponse.getEntity());
			}
			
			System.out.println("JersonArray  "+arrayJersonString);
			JSONArray jsonArray = new JSONArray(arrayJersonString);
	
			for(int i=0;i<jsonArray.length();i++){
				JSONObject sjonObject = jsonArray.getJSONObject(i);
				DEVICEIP = sjonObject.getString("ip");
				Log.d("IP:   ", DEVICEIP);
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	};
}
