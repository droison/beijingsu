package com.mine.beijingserv.push;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttSimpleCallback;
import com.mine.beijingserv.R;
import com.mine.beijingserv.model.CatologueFilter;
import com.mine.beijingserv.model.FirstCatalogue;
import com.mine.beijingserv.model.MessageInfo;
import com.mine.beijingserv.model.SecondCatalogue;
import com.mine.beijingserv.sys.AppContex;
import com.mine.beijingserv.sys.DBUtil;
import com.mine.beijingserv.sys.SysUtils;
import com.mine.beijingserv.sys.ToastShow;
import com.mine.beijingserv.sys.UpdateMessageState;

import com.mine.beijingserv.ui.MessageInfoDetailActivity;
import com.mine.beijingserv.ui.TabsMain;
import com.mine.beijingserv.ui.SetFreeTimeActivity;
import com.mine.beijingserv.ui.TabsMain;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

public class PushService extends Service implements MqttSimpleCallback
{
	private static String SUBSCRIB_TITLE = "MSGNOTIFY";
	public static final String NEW_MSG_COMING = "com.mine.NEW_MSG_COMING";
	public static final String MY_APP_NAME = "com.mine.beijingserv";
	
	private static final int MESSAGE_URGENT_ARRIVED = 0;
	private static final int MESSAGE_DIALOG_DISMISS = 1;
	private static int message_index = 0;
	private Vector<Integer>  messageindex;
	private Vector<MessageInfo> mymessageInfo;
	
	private MqttClient mqttClient = null;
	private short keep_alive = 30;
	private MessageInfo currentMessageInfo = null;
	private long lastAudioPlayTime = 0;
	private AlertDialog alertDialog;
	private AlertDialog currentalertDialog;
	private Vector<AlertDialog>  alertinfo;
	private String resubcribe = "com.zzku.reresubcribe";
	
	int fromhour = 0;
	int tohour = 0;
	int frommin = 0;
	int tomin = 0;
	public static final String KEY_FREETIME_FROM_HOUR = "fromhour";
	public static final String KEY_FREETIME_FROM_MINUTE = "frommin";
	public static final String KEY_FREETIME_TO_HOUR = "tohour";
	
	private SharedPreferences volumnSharedPreferences;

	

	private Handler handler = new Handler(){
		@Override
		public void handleMessage(final Message msg) 
		{
			super.handleMessage(msg);
			if(msg.what == MESSAGE_URGENT_ARRIVED)				
			{
				alertDialog = new AlertDialog.Builder(getApplicationContext())
				.setTitle("紧急通知")
				.setMessage((String)msg.obj)
				.setPositiveButton("查看通知", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if(SysUtils.checkNetworkConnectedStat(PushService.this)){
							//查询应用是否打开
							boolean APP_IS_ALIVE = false;
							ActivityManager activityManager = (ActivityManager) PushService.this.getSystemService(Context.ACTIVITY_SERVICE);
							List<RunningTaskInfo> infoList = activityManager.getRunningTasks(100);
							for(RunningTaskInfo runninginfo:infoList){
								if(runninginfo.topActivity.getPackageName().equals(MY_APP_NAME) && runninginfo.baseActivity.getPackageName()
										.equals(MY_APP_NAME)){
									System.out.println("APPISALIVE");
									APP_IS_ALIVE = true;								
									break;
									
								}else{
									System.out.println("APPISSHUT");
									APP_IS_ALIVE = false;								
									
								}
							}
							//如果打开进入通知详情
							if(APP_IS_ALIVE){
								AppContex.curMessageInfo =currentMessageInfo;
								
								Intent intent = new Intent(Intent.ACTION_MAIN);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.addCategory(Intent.CATEGORY_LAUNCHER);
								intent.setClass(PushService.this, MessageInfoDetailActivity.class);
								Bundle extras = new Bundle();
								extras.putBoolean("ISFAVORATEACTTIVITY", false);
								extras.putBoolean("ISTABSEARCHACTTIVITY", false);
								extras.putBoolean("ISCOMEFROMNOTIFY",false);
								extras.putInt("MESSAGES_ID", -1);
								intent.putExtras(extras);
								PushService.this.startActivity(intent);
								if(AppContex.curMessageInfo.readState == MessageInfo.READ_STATE_UNREAD)
								{
									new UpdateMessageState(AppContex.curMessageInfo.serversqlid, SysUtils.getDeviceID(PushService.this), UpdateMessageState.MESSAGE_STATE_READED).start();
								}
								AppContex.curMessageInfo.readState = MessageInfo.READ_STATE_READED;
								new DBUtil(PushService.this).updateMessageInfo(AppContex.curMessageInfo);
							}else{
								//打开应用起始页
								Intent openintent = new Intent(Intent.ACTION_MAIN);
								openintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								openintent.addCategory(Intent.CATEGORY_LAUNCHER);
								openintent.setClass(PushService.this, TabsMain.class); 
								startActivity(openintent);
							}
						}else{
							Toast.makeText(PushService.this, "网络未连接", Toast.LENGTH_SHORT).show();
						}				
						
					}
				})
				.create();
				alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT); 
				alertDialog.show();
				
				//发出预警音乐
				try {
					if(System.currentTimeMillis() - lastAudioPlayTime > 5000)
					{
						lastAudioPlayTime = System.currentTimeMillis();
						MediaPlayer mediaPlayer = MediaPlayer.create(PushService.this, R.raw.alert);
				    	mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
							public void onCompletion(MediaPlayer mp) {
								try {
									mp.stop();
									mp.release();
								} catch (Exception e) {
								}
							}
						});
						mediaPlayer.start();
				    	
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				currentalertDialog = alertDialog;
				alertinfo.add(alertDialog);				
				handler.obtainMessage(MESSAGE_DIALOG_DISMISS).sendToTarget();
			}
			//替换之前的alertdialog
			if(msg.what == MESSAGE_DIALOG_DISMISS){
				if(alertinfo.size() >1){
					AlertDialog formerDialog = alertinfo.elementAt(alertinfo.indexOf(currentalertDialog)-1);
					formerDialog.dismiss();
					alertinfo.remove(formerDialog);
				}
			}
			
			
		}
	};
	
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		//获得声音设置
		volumnSharedPreferences = this.getSharedPreferences("VOLUMN_SETTING", Context.MODE_PRIVATE);
		
		AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(getApplicationContext(), PushService.class);
        PendingIntent   pendingIntent = PendingIntent.getService(this, 987654321, intent,0);
        alertinfo = new Vector<AlertDialog>();
        messageindex = new Vector<Integer>();
    	mymessageInfo = new Vector<MessageInfo>();
        try {
            alarmManager.cancel(pendingIntent);
        } catch (Exception e) {
        	
        }
        int timeForAlarm=60000;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+timeForAlarm, timeForAlarm,pendingIntent);
        this.registerReceiver(resubcribeBroadcast, new IntentFilter(resubcribe));
	}
	
	public void onStart(Intent intent, int startId) {
		SysUtils.log("onStart");
		if(mqttClient !=null)
		{
			System.out.println("onstart mqttClient !=null");
			return;
		}
		new Thread(new Runnable() {
			public void run() {
				if(SysUtils.checkNetworkConnectedStat(PushService.this)){
					try {
						
						if(mqttClient == null){
							SharedPreferences shePreferences = PushService.this.getSharedPreferences("MQTTADRESS", Context.MODE_PRIVATE);
							AppContex.MQTT_ADDRESS = shePreferences.getString("MQTT_ADDRESS", "tcp://210.73.66.39:1883");
							mqttClient = new MqttClient(AppContex.MQTT_ADDRESS);
							mqttClient.registerSimpleHandler(PushService.this);										
						}else{
							mqttClient.disconnect();
						}
					} catch (Exception e) {

					}

						boolean isConnected = false;
						while(!isConnected)
						{
						
							try {
							
							   //设置mqtt心跳时间，id
								mqttClient.connect(SysUtils.getDeviceID(getApplicationContext()), false, keep_alive);								
								String [] scatidStrings = getScatIdArray();
								int qos[] = new int[scatidStrings.length];
								for (int l = 0; l < qos.length; l++) {
									qos[l]=1;
								}
								//设置mqtt订阅
								mqttClient.subscribe(scatidStrings, qos);
								mqttClient.setRetry(60);
								isConnected = true;
								break;
							} catch (Exception e) {
								e.printStackTrace();
							}
							try {
								Thread.sleep(10000);
							} catch (Exception e) {
							}
						}
						SysUtils.log("已连接上mqtt");
					}				
			}
		}).start();		
	}
	
	public void connectionLost() throws Exception 
	{
		SysUtils.log("连接断掉");
		long liuliangL1 = TrafficStats.getTotalRxBytes();
		System.out.println("断网重连前网络数据： "+liuliangL1);
		new Thread(new Runnable() {
			
			
			public void run() {
				boolean isConnected = false;
				while(!isConnected)
				{
					SysUtils.log("尝试重连...");
					try {
						String deviceidString = SysUtils.getDeviceID(getApplicationContext());
						System.out.println("deviceid string ="+deviceidString);
						//设置mqtt心跳，连接id、订阅
						mqttClient.connect(deviceidString, false, keep_alive);
						String [] scatidStrings = getScatIdArray();
						int qos[] = new int[scatidStrings.length];
						if(qos.length != 0){
							for (int l = 0; l < qos.length; l++) {
								qos[l]=1;
							}
						}
						
						mqttClient.subscribe(scatidStrings, qos);
						mqttClient.setRetry(60);
						isConnected = true;
						break;
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep(10000);
					} catch (Exception e) {
					}
					
				}
				SysUtils.log("重连上mqtt");				
			}
		}).start();
	}
	//判断是否在免打扰时间
	private boolean checkIfInFreetime()
	{
		SharedPreferences sharedPreferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
		int fromhour = sharedPreferences.getInt(SetFreeTimeActivity.KEY_FREETIME_FROM_HOUR, 22);
		int fromminute = sharedPreferences.getInt(SetFreeTimeActivity.KEY_FREETIME_FROM_MINUTE, 0);
		int tohour = sharedPreferences.getInt(SetFreeTimeActivity.KEY_FREETIME_TO_HOUR, 8);
		int tominute = sharedPreferences.getInt(SetFreeTimeActivity.KEY_FREETIME_TO_MINUTE, 0);
		Calendar calendar = Calendar.getInstance();
		int nowhour =  calendar.get(Calendar.HOUR_OF_DAY);
		int nowminute = calendar.get(Calendar.MINUTE);
		int tominuteinall = tohour*60 +tominute;
		int fromminuteinall = fromhour*60 +fromminute;
		int nowminuteinall = nowhour*60+nowminute;
		

	
		if(tominuteinall >= fromminuteinall)
		{
			if(nowminuteinall >= fromminuteinall && nowminuteinall <= tominuteinall)
			{
				return true;
			}
		}else {
			if(nowminuteinall >= fromminuteinall || nowminuteinall <= tominuteinall)
			{
				return true;
			}
		}
		
		return false;
	}
	//判断是否在订阅范围内
	private boolean getFilterResult(int fcatid , int scatid)
	{
		SharedPreferences sharedPreferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
		String filterString = sharedPreferences.getString("key_pref_filter", null);
		boolean filterresult = true;
		try {
			JSONArray jsonArray = new JSONArray(filterString);
			System.out.println("保存的过滤消息为 = "+filterString);
			if(jsonArray.length() >0)
			{
				filterresult = false;
				for(int i=0;i<jsonArray.length();i++)
				{
					JSONObject object = jsonArray.getJSONObject(i);
					int filterfcatid = object.getInt(CatologueFilter.CATOLOGUE_FILTER_KEY_FCATID);
					int filterscatid = object.getInt(CatologueFilter.CATOLOGUE_FILTER_KEY_SCATID);
					if(filterfcatid == fcatid && filterscatid == scatid)
					{
						filterresult = true;
						break;
					}
				}
			}
			
		} catch (Exception e) {}
		
		System.out.println("filterresult:  "+filterresult);
		return filterresult;
	}
	//获得推送消息
	public void publishArrived(String arg0, byte[] notifydata, int arg2, boolean arg3)
			throws Exception 
	{
		System.out.println("publishArrived");
		message_index ++;
		try {
			String notifyString = new String(notifydata, "utf-8");
			SysUtils.log("notifydata消息内容: "+notifyString);
			JSONObject jsonObject = new JSONObject(notifyString);
			MessageInfo messageInfo = new MessageInfo();
			messageInfo.content = jsonObject.getString("content");
			messageInfo.day = jsonObject.getInt("day");
			messageInfo.fcatid = jsonObject.getInt("fcatid");
			messageInfo.hour = jsonObject.getInt("hour");
			messageInfo.min = jsonObject.getInt("min");
			messageInfo.month = jsonObject.getInt("month");
			messageInfo.readState = MessageInfo.READ_STATE_UNREAD;
			messageInfo.scatid = jsonObject.getInt("scatid");
			messageInfo.serversqlid = jsonObject.getInt("id");
			messageInfo.title = jsonObject.getString("title");
			messageInfo.year = jsonObject.getInt("year");
			messageInfo.type = jsonObject.getInt("type");
			currentMessageInfo = messageInfo;
			
			messageindex.add(messageInfo.serversqlid);
			mymessageInfo.add(messageInfo);
			
			if(jsonObject.has("alertlevel") && !jsonObject.isNull("alertlevel")){
				messageInfo.alertlevel = jsonObject.getInt("alertlevel");
			}else{
				messageInfo.alertlevel = -1;
			}
			
			if(jsonObject.has("alerttype") && !jsonObject.isNull("alerttype")){
				messageInfo.alerttype = jsonObject.getInt("alerttype"); 
			}else{
				messageInfo.alerttype = -1;
			}
			
			if(jsonObject.has("saygood") && !jsonObject.isNull("saygood")){
				messageInfo.saygood = jsonObject.getInt("saygood"); 
			}else{
				messageInfo.saygood = -1;
			}
			
			
			
			Calendar calendar = Calendar.getInstance();
			
			int curmonth =  calendar.get(Calendar.MONTH)+1;
			int curdayofmonth =  calendar.get(Calendar.DAY_OF_MONTH);
			
			int curhour = calendar.get(Calendar.HOUR_OF_DAY);
			int curmin = calendar.get(Calendar.MINUTE);		
			
			long liuliangL = TrafficStats.getTotalRxBytes();
			System.out.println("pusharrived网络数据： "+liuliangL);
			
			System.out.println("TODAY TIME:  "+curhour+":"+curmin);
			if(curmonth>messageInfo.month || curdayofmonth > messageInfo.day)
			{				
				SysUtils.log("过滤掉今天以前的消息   "+"发送今天以前通知的title:   "+messageInfo.title);				
				return;
			}
			
			if(!getFilterResult(messageInfo.fcatid, messageInfo.scatid))
			{
				System.out.println("消息被过滤掉");
				return;
			}
			//判断消息是否已删除过
			if(SysUtils.checkIfMessageIdExist(messageInfo.serversqlid))
			{
				System.out.println("消息已经存在.file"+"   消息已经存在的通知的title:   "+messageInfo.title);				
				return;
			}
			
			if(messageInfo.type == MessageInfo.MESSAGE_TYPE_NORMAL){
				if(checkIfInFreetime())
				{
					SysUtils.log("在免打扰时间内");
					return;
				}
				
			}
			DBUtil dbUtil = new DBUtil(this);
			if(dbUtil.isMessageAlreadyExist(messageInfo.serversqlid, this))
			{
				System.out.println("消息已经存在.db");
				return;
			}else {
				dbUtil.insertNewMessageInfo(messageInfo);					
				AppContex.searchedInfos.insertElementAt(messageInfo, 0);
				System.out.println("已插入最新SearchInfo 数据");
			}
			//如果是紧急消息，弹出对话框
			if(messageInfo.type == MessageInfo.MESSAGE_TYPE_ERGENT)
			{
				Message message = new Message();
				message.what = MESSAGE_URGENT_ARRIVED;
				
				message.obj = messageInfo.title;
				handler.sendMessage(message);
				sendBroadcast(new Intent(NEW_MSG_COMING));
			}else {
				if(checkIfInFreetime())
				{
					SysUtils.log("在免打扰时间内");
					return;
				}
				//一般消息，在标题栏显示
				sendBroadcast(new Intent(NEW_MSG_COMING));
				NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);               
				Notification n = new Notification(R.drawable.messageicondefault, messageInfo.title, System.currentTimeMillis());  
				setVolumnMode(n);				
				n.flags = Notification.FLAG_AUTO_CANCEL;				
				boolean APP_IS_ALIVE = false;
				ActivityManager activityManager = (ActivityManager) PushService.this.getSystemService(Context.ACTIVITY_SERVICE);
				List<RunningTaskInfo> infoList = activityManager.getRunningTasks(100);
				for(RunningTaskInfo runninginfo:infoList){
					if(runninginfo.topActivity.getPackageName().equals(MY_APP_NAME) && runninginfo.baseActivity.getPackageName()
							.equals(MY_APP_NAME)){
						System.out.println("APPISALIVE");
						APP_IS_ALIVE = true;								
						break;
						
					}else{
						System.out.println("APPISSHUT");
						APP_IS_ALIVE = false;								
						
					}
				}
				
				Intent openintent = new Intent(Intent.ACTION_MAIN);
				openintent.setData(Uri.parse("custom://"+System.currentTimeMillis()));
				openintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				openintent.addCategory(Intent.CATEGORY_LAUNCHER);	
				//如果应用已启动，进入通知详情
				if(APP_IS_ALIVE){
					openintent.setClass(PushService.this, MessageInfoDetailActivity.class);
					Bundle extras = new Bundle();
					extras.putBoolean("ISFAVORATEACTTIVITY", false);
					extras.putBoolean("ISTABSEARCHACTTIVITY", false);
					extras.putBoolean("ISCOMEFROMNOTIFY",true);
					extras.putInt("MESSAGES_ID", messageInfo.serversqlid);
					openintent.putExtras(extras);					
				}else{
					openintent.setClass(PushService.this, TabsMain.class);
				}
							       
				//PendingIntent
				PendingIntent contentIntent = PendingIntent.getActivity(
						getApplicationContext(), 
				        R.string.app_name, 
				        openintent, 
				        PendingIntent.FLAG_ONE_SHOT);
				n.setLatestEventInfo(
						getApplicationContext(),
				        messageInfo.title, 
				        "", 
				        contentIntent);  
				nm.notify(messageInfo.serversqlid, n);
				
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
	}
	//设置铃音状态
	private void setVolumnMode(Notification notification) {
		// TODO Auto-generated method stub
		boolean isslice_mode = volumnSharedPreferences.getBoolean("is_slice_mode", false);
		boolean isslice_vibrate_mode = volumnSharedPreferences.getBoolean("is_slice_vibrate_mode", false);		
		boolean isno_vibrate_mode = volumnSharedPreferences.getBoolean("is_no_vibrate_mode", true);
		boolean ishave_vibrate_mode = volumnSharedPreferences.getBoolean("is_have_vibrate_mode", false);
		
		
		System.out.println("静音模式GET： "+isslice_mode);
		System.out.println("静音振动模式GET：： "+isslice_vibrate_mode);
		System.out.println("铃音模式GET： "+isno_vibrate_mode);
		System.out.println("铃音振动模式GET：： "+ishave_vibrate_mode);
	
		if(isslice_mode){
			notification.sound = null;
			notification.vibrate = null;
			System.out.println("显示静音模式： ");
			return;
		}
		
		if(isslice_vibrate_mode){
			notification.sound = null;
			long[] vibrate = new long[] { 1000, 1000, 1000 };
			notification.vibrate = vibrate;
			System.out.println("显示静音振动模式： ");
			return;
		}
		
		if(isno_vibrate_mode){
			
			AudioManager audioManager = (AudioManager) PushService.this.getSystemService(Context.AUDIO_SERVICE);
			if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE){
				notification.defaults = Notification.DEFAULT_SOUND;	
				long[] vibrate = new long[] { 1000, 1000, 1000};
				notification.vibrate = vibrate;
				System.out.println("走振动");
				return;
			}else{
				notification.defaults = Notification.DEFAULT_SOUND;	
				System.out.println("走普通");
			}
			
			return;
		}
		
		if(ishave_vibrate_mode){
			notification.defaults = Notification.DEFAULT_SOUND;	
			long[] vibrate = new long[] { 1000, 1000, 1000};
			notification.vibrate = vibrate;
			System.out.println("显示铃音振动模式： ");
			return;
		}
		
		notification.defaults = Notification.DEFAULT_ALL;
		System.out.println("显示默认模式： ");
	}
	
	//获得订阅数组
	private String[] getScatIdArray() {
		ArrayList<String> list = new ArrayList<String>();
		SharedPreferences sharedPreferences = PushService.this
				.getSharedPreferences("pref", Context.MODE_PRIVATE);
		String jsonString = sharedPreferences
				.getString("key_pref_filter", "[]");
		try {
			JSONArray jsonArray = new JSONArray(jsonString);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				int scatid = jsonObject
						.getInt(CatologueFilter.CATOLOGUE_FILTER_KEY_SCATID);
				list.add(SUBSCRIB_TITLE+"/"+scatid);
			}
			list.add(SUBSCRIB_TITLE+"/"+"urgent");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}	
		System.out.println("SID LIST:  "+list.toString());
		return list.toArray(new String[list.size()]);

	}
	
	//订阅更改后重新订阅
	private BroadcastReceiver resubcribeBroadcast = new BroadcastReceiver(){

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			if(SysUtils.checkNetworkConnectedStat(PushService.this)){
				dosubscribe();
			}
		}
		
	};

	private void dosubscribe(){
		new Thread(new Runnable() {
			public void run() {
				if(SysUtils.checkNetworkConnectedStat(PushService.this)){							
					boolean isConnected = false;
					while(!isConnected)
					{
					
						try {
						
						
							mqttClient.connect(SysUtils.getDeviceID(getApplicationContext()), false, keep_alive);								
							String [] scatidStrings = getScatIdArray();
							int qos[] = new int[scatidStrings.length];
							for (int l = 0; l < qos.length; l++) {
								qos[l]=1;
							}
							mqttClient.subscribe(scatidStrings, qos);
							mqttClient.setRetry(60);
							isConnected = true;
							break;
						} catch (Exception e) {
							e.printStackTrace();
						}
						try {
							Thread.sleep(10000);
						} catch (Exception e) {
						}
					}
						
						SysUtils.log("已重新订阅");
					}				
			}
		}).start();	
	}
	
	
}
