package com.mine.beijingserv.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//开机自动启动服务广播
public class MyBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) 
	{
		context.startService(new Intent(context,PushService.class));
	}

}
