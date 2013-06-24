package com.mine.beijingserv.push;

import java.util.Calendar;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class UpCatService extends Service {
	int currentHour;
	public final static  String UP_CAT_BROADCAST = "com.zskt.upcat";
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		System.out.println("UpCatService oncreate");
	}
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		new MyThread().start();
		System.out.println("UpCatService onstart");
	}
	
	//3个小时候重新获得目录请求
	public class MyThread extends Thread{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			try {
				MyThread.sleep(3*60*60*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Intent itIntent = new Intent(UP_CAT_BROADCAST);
			UpCatService.this.sendBroadcast(itIntent);
			
		}
		
	}

}
