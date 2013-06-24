package com.mine.beijingserv.ui;

import java.io.IOException;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.JetPlayer;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.mine.beijingserv.R;
import com.mine.beijingserv.model.FirstCatalogue;
import com.mine.beijingserv.model.MessageInfo;
import com.mine.beijingserv.sys.AppContex;
import com.mine.beijingserv.sys.DBUtil;
import com.mine.beijingserv.sys.FavorateDBUtil;
import com.mine.beijingserv.sys.FavorateDBUtil;
import com.mine.beijingserv.sys.SysUtils;
import com.mine.beijingserv.sys.ToastShow;
import com.mine.beijingserv.sys.UpdateMessageState;
public class MessageInfoDetailActivity extends Activity implements
		View.OnClickListener{
	private View sharepanelView = null;
	private TextView titleTextView = null;
	private TextView contentTextView = null;
	
	private TextView zanTextView;
	private GestureDetector gestureDetector = null;
	private ScaleGestureDetector scaleGestureDetector = null;

 	private int messageindex = 0;
	private View maskView = null;
	private ImageView zanImage;
	private FavorateDBUtil favorateDBUtil = null;
	private int Text_SETTING;

	private boolean isFavorate = false;
	private boolean isSearchTab = false;
	private final int MESSAGE_SHOW_RAW_MSG = 0;	
	private final int UPDATE_SUCCED = 1;
	private final int UPDATE_UNSUCCED = 2;	
	private ImageView messageinfo_index;
	private SharedPreferences isFirstAppSharedPreferences;
	private boolean isFirstApp = true;
	private NotificationManager notificationManager;
	private TextView time_covert_department_text;
	private LinearLayout bottom_layout;
	private SharedPreferences singleMessageSharedPreferences;
	private boolean issingleMessage = true;
	private Editor issingleeditor;
	private boolean iscomefromnotify = false;
	private final String UPDATE_MESSAGESQLID = "update_messageid";
	private final String getRefreshReadStateID = "com.bjzskt.updatereadstateid";
	private final String COME_FROM_MESSAGESINFO = "com.zhkt.comefrommessageinfo";
	private final String DELETE_SINGLE = "com.zskt.delete_one_message";
	private TextView deletetextveiw;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MESSAGE_SHOW_RAW_MSG:
				Toast.makeText(MessageInfoDetailActivity.this,
						(String) msg.obj, Toast.LENGTH_SHORT).show();
				break;
			case UPDATE_SUCCED:	
				if(AppContex.curMessageInfo.saygood == 0){
					zanTextView.setVisibility(View.INVISIBLE);
					break;
				}				
				zanTextView.setText(String
						.valueOf(AppContex.curMessageInfo.saygood));
				if (AppContex.curMessageInfo.saygood > 99) {
					zanTextView.setText("99+");
				}
				zanTextView.setVisibility(View.VISIBLE);
				break;				
		
			case UPDATE_UNSUCCED:
				Toast.makeText(MessageInfoDetailActivity.this, "您已经赞过了",
						Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		};
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.messageinfodetail);
		Intent intent = this.getIntent();
		int message_id = intent.getExtras().getInt("MESSAGES_ID");
		//从标题栏传入message_id ，通过id获得当前messageinfo
		if(message_id != -1){
			AppContex.curMessageInfo = new DBUtil(this).getMessageAlready(message_id, this);			
		}
		System.out.println("MESSAFGINFO:  "
				+ AppContex.curMessageInfo.serversqlid);
		//清楚标题栏
		notificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		deletetextveiw = (TextView)findViewById(R.id.deletetextveiw);
		messageinfo_index = (ImageView) findViewById(R.id.messageinfo_index);
		messageinfo_index.setOnClickListener(this);
		isFirstAppSharedPreferences = this.getSharedPreferences("IS_FRSIT_APP",
				Context.MODE_PRIVATE);
		isFirstApp = isFirstAppSharedPreferences.getBoolean("FRSIT_APP", true);
		//第一次进入通知详情，显示引导图片
		if (isFirstApp) {
			messageinfo_index.setVisibility(View.VISIBLE);
		}
		isFavorate = intent.getExtras()
				.getBoolean("ISFAVORATEACTTIVITY", false);
		
		isSearchTab = intent.getExtras().getBoolean("ISTABSEARCHACTTIVITY");
		iscomefromnotify = intent.getExtras().getBoolean("ISCOMEFROMNOTIFY",false);		
		bottom_layout = (LinearLayout)findViewById(R.id.bottom_layout);
		//隐藏评论、赞等图标
		if(isSearchTab){
			bottom_layout.setVisibility(View.GONE);
			deletetextveiw.setVisibility(View.GONE);
		}
		
		singleMessageSharedPreferences = this.getSharedPreferences("IS_SINGLEMESSAGE", this.MODE_PRIVATE);
		issingleeditor = singleMessageSharedPreferences.edit();
		issingleeditor.putBoolean("SINGLEMESSAGE_BOOLEAN", issingleMessage);
		issingleeditor.putBoolean("COME_FROM_NOTIFY", iscomefromnotify);
		issingleeditor.commit();		
		//取得赞数目
		new Thread(new Runnable() {
			public void run() {
				doGetSayGoodNumFormer();				
			}
		}).start();
		
		
		//删除消息按钮
		deletetextveiw.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(MessageInfoDetailActivity.this).setMessage(R.string.deletemessage)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						new DBUtil(MessageInfoDetailActivity.this).deleteMessageInfo(AppContex.curMessageInfo.localsqlid);
						Intent intent = new Intent(DELETE_SINGLE);
						AppContex.searchedInfos.remove(AppContex.curMessageInfo);
						MessageInfoDetailActivity.this.sendBroadcast(intent);
						finish();
					}
				}).setNegativeButton("取消", null).create().show();
				
				
			}
		});
		System.out.println("SEND READSTATE API");
		favorateDBUtil = new FavorateDBUtil(this);
		findViewById(R.id.topbar_back).setOnClickListener(this);

		findViewById(R.id.messageinfodetail_share_button).setOnClickListener(
				this);
		findViewById(R.id.zan_button).setOnClickListener(this);
		findViewById(R.id.messageinfodetail_comment_button).setOnClickListener(
				this);
		sharepanelView = findViewById(R.id.message_share_menu_panle);
		findViewById(R.id.message_share_menu_panle_close).setOnClickListener(
				this);
		findViewById(R.id.message_share_menu_sms).setOnClickListener(this);
		findViewById(R.id.message_share_menu_weibo).setOnClickListener(this);
		findViewById(R.id.message_share_menu_weixin).setOnClickListener(this);
		findViewById(R.id.shoucang_button).setOnClickListener(this);
		SharedPreferences sharedPreferences = this.getSharedPreferences(
				"TextSize_Setting", Context.MODE_PRIVATE);
		//获得字体大小
		Text_SETTING = sharedPreferences.getInt("Size_Setting", 18);
		System.out.println("TEXT_SETTING:  " + Text_SETTING);
		zanTextView = (TextView) findViewById(R.id.zannumtext);
		//显示赞数目
		if (AppContex.curMessageInfo.saygood > 0) {
			zanTextView.setVisibility(View.VISIBLE);
			if (AppContex.curMessageInfo.saygood > 99) {
				zanTextView.setText("99+");
			}else{
				zanTextView.setText(String
						.valueOf(AppContex.curMessageInfo.saygood));
			}	
		}else{
			zanTextView.setVisibility(View.INVISIBLE);
		}

		maskView = findViewById(R.id.tabmessage_mask);
		maskView.setOnClickListener(this);
		zanImage = (ImageView) findViewById(R.id.zan_image);
		titleTextView = (TextView) findViewById(R.id.messageinfodetail_title);
		titleTextView.setText(AppContex.curMessageInfo.title);
		time_covert_department_text = (TextView) findViewById(R.id.time_covert_layout);
		contentTextView = (TextView) findViewById(R.id.messageinfodetail_content);
		String timeString = AppContex.curMessageInfo.getFormatedTime();		
		contentTextView.setText(AppContex.curMessageInfo.content);		
		String fcattitleString = "";
		//取得消息标题
		for (FirstCatalogue firstCatalogue : AppContex.catalogues) {
			if (firstCatalogue.fcatid == AppContex.curMessageInfo.fcatid) {
				fcattitleString = firstCatalogue.title;
				break;
			}
		}
		//取得时间 委办局
		String convetString = timeString + "  来源：" + fcattitleString;
		time_covert_department_text.setText(convetString);
		//设置字体大小
		if (Text_SETTING != 18) {
			int middleValue = Text_SETTING - 18;
			titleTextView
					.setTextSize(titleTextView.getTextSize() + middleValue);
			contentTextView.setTextSize(Text_SETTING);
			time_covert_department_text.setTextSize(time_covert_department_text
					.getTextSize() + middleValue);

		}
		//新建滑动监听
		if (!isFavorate) {
			gestureDetector = new GestureDetector(this,
					new FlipGestureListener());
		}else{
			deletetextveiw.setVisibility(View.GONE);
		}
		//新建放大缩小监听
		scaleGestureDetector = new ScaleGestureDetector(this,
				new ScaleGestureDetector.OnScaleGestureListener() {
					private float titletextviewsize = 0;
					private float timetextviewsize = 0;
					private float contenttextviewsize = 0;
					private float fcattextviewsize = 0;
					private float convertTextSize = 0;
					private float time_covert_department_textsize = 0;

					public void onScaleEnd(ScaleGestureDetector detector) {
						System.out.println("onScaleEnd");
					}

					public boolean onScaleBegin(ScaleGestureDetector detector) {
						System.out.println("onScaleBegin");
						titletextviewsize = titleTextView.getTextSize();
						time_covert_department_textsize = time_covert_department_text
								.getTextSize();
						contenttextviewsize = contentTextView.getTextSize();

						return true;
					}

					public boolean onScale(ScaleGestureDetector detector) {
						System.out.println("onScale");
						System.out.println("detector.getScaleFactor() = "
								+ detector.getScaleFactor());

						float scale = detector.getScaleFactor();

						titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
								titletextviewsize * scale);
						time_covert_department_text.setTextSize(
								TypedValue.COMPLEX_UNIT_PX,
								time_covert_department_textsize * scale);
						contentTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
								contenttextviewsize * scale);

						return false;
					}
				});
		
		if(!isSearchTab){
			//更新为已读状态
			if (AppContex.curMessageInfo.readState == MessageInfo.READ_STATE_UNREAD) {
				AppContex.curMessageInfo.readState = MessageInfo.READ_STATE_READED;
				new Thread(new Runnable() {			
					@Override
					public void run() {
						// TODO Auto-generated method stub
						sendReadState(AppContex.curMessageInfo);
					}
					
				}).start();
				new DBUtil(this).updateMessageInfo(AppContex.curMessageInfo);
				System.out.println("sendreadstatetoservice  ");
				Intent updateIntent = new Intent(getRefreshReadStateID);
				Bundle extras = new Bundle();
				extras.putInt(UPDATE_MESSAGESQLID, AppContex.curMessageInfo.serversqlid);				
				updateIntent.putExtras(extras);		
				this.sendBroadcast(updateIntent);
			}				
		}
		
		if(isSearchTab){
			Intent comefromMessageInfoIntent = new Intent(COME_FROM_MESSAGESINFO);
			this.sendBroadcast(comefromMessageInfoIntent);
			System.out.println("SEND COMEFROM MESSAGEINFO BROADCAST");
		}
		//获得消息角标
		for (int i = 0; i < AppContex.searchedInfos.size(); i++) {
			MessageInfo messageInfo = AppContex.searchedInfos.elementAt(i);
			if (messageInfo.localsqlid == AppContex.curMessageInfo.localsqlid) {
				messageindex = i;
				System.out.println("SEARCH_INFO_INDEX:   "+messageindex);
				break;
			}
		}
		//删除消息栏通知
		notificationManager.cancel(AppContex.curMessageInfo.serversqlid);

	}

	private void reloadMessageInfo() {
		issingleMessage = true;
		issingleeditor.putBoolean("SINGLEMESSAGE_BOOLEAN", issingleMessage);
		issingleeditor.commit();
		titleTextView.setText(AppContex.curMessageInfo.title);
		String timeString = AppContex.curMessageInfo.getFormatedTime();
		
		if (AppContex.curMessageInfo.saygood > 0) {
			zanTextView.setVisibility(View.VISIBLE);
			if (AppContex.curMessageInfo.saygood > 99) {
				zanTextView.setText("99+");
			}else{
				zanTextView.setText(String
						.valueOf(AppContex.curMessageInfo.saygood));
			}	
		}else{
			zanTextView.setVisibility(View.INVISIBLE);
		}
		
		
		
		zanImage.setBackgroundResource(R.drawable.msginfodetail_saygood_off);

		String messageContent = AppContex.curMessageInfo.content;
		
		contentTextView.setText(messageContent);	

		String fcattitleString = "";
		for (FirstCatalogue firstCatalogue : AppContex.catalogues) {
			if (firstCatalogue.fcatid == AppContex.curMessageInfo.fcatid) {
				fcattitleString = firstCatalogue.title;
				break;
			}
		}

		String convertString = timeString + "   来源：" + fcattitleString;
		time_covert_department_text.setText(convertString);
		if (AppContex.curMessageInfo.readState == MessageInfo.READ_STATE_UNREAD) {
			AppContex.curMessageInfo.readState = MessageInfo.READ_STATE_READED;			
			new Thread(new Runnable() {
				public void run() {
					doGetSayGoodNumFormer();				
				}
			}).start();				
			new DBUtil(this).updateMessageInfo(AppContex.curMessageInfo);
			Intent updateIntent = new Intent(getRefreshReadStateID);
			updateIntent.putExtra(UPDATE_MESSAGESQLID, AppContex.curMessageInfo.serversqlid);
			this.sendBroadcast(updateIntent);
			System.out.println("发送更新消息");
		}
		
		
		
		deletetextveiw = (TextView)findViewById(R.id.deletetextveiw);
		deletetextveiw.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(MessageInfoDetailActivity.this).setMessage(R.string.deletemessage)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						new DBUtil(MessageInfoDetailActivity.this).deleteMessageInfo(AppContex.curMessageInfo.localsqlid);
						Intent intent = new Intent(DELETE_SINGLE);
						AppContex.searchedInfos.remove(AppContex.curMessageInfo);
						MessageInfoDetailActivity.this.sendBroadcast(intent);
						finish();
					}
				}).setNegativeButton("取消", null).create().show();
			}
		});
	

		notificationManager.cancel(AppContex.curMessageInfo.serversqlid);	

	}



	@Override
	public void onBackPressed() {
		if (maskView.getVisibility() == View.VISIBLE) {
			sharepanelView.setVisibility(View.GONE);
			maskView.setVisibility(View.GONE);
			return;
		}
		super.onBackPressed();		
	}


	public boolean dispatchTouchEvent(MotionEvent ev) {
		scaleGestureDetector.onTouchEvent(ev);
		if (!isFavorate) {
			gestureDetector.onTouchEvent(ev);
		}

		return super.dispatchTouchEvent(ev);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		//隐藏引导图片
		case R.id.messageinfo_index:
			messageinfo_index.setVisibility(View.GONE);
			Editor editor = isFirstAppSharedPreferences.edit();
			editor.putBoolean("FRSIT_APP", false);
			editor.commit();
			break;
		case R.id.zan_button:
			zanImage.setBackgroundResource(R.drawable.msginfodetail_saygood_on);
			//发送赞请求
			if(SysUtils.checkNetworkConnectedStat(MessageInfoDetailActivity.this)){
				RefreshMessagesThread refreshMessagesThread = new RefreshMessagesThread();
				refreshMessagesThread.start();
			}else{
				ToastShow.toastshow(MessageInfoDetailActivity.this, "网络未连接");
			}
			break;
		case R.id.tabmessage_mask:
			sharepanelView.setVisibility(View.GONE);
			maskView.setVisibility(View.GONE);
			break;

		case R.id.topbar_back:			
			finish();
			break;
		case R.id.messageinfodetail_share_button:
			//分享消息
			Intent Shareintent = new Intent(Intent.ACTION_SEND);
			Shareintent.setType("text/plain");
			Shareintent.putExtra(Intent.EXTRA_SUBJECT, "分享");
			Shareintent.putExtra(Intent.EXTRA_TEXT,
					AppContex.curMessageInfo.content);
			startActivity(Intent.createChooser(Shareintent, getTitle()));
			break;
		case R.id.messageinfodetail_comment_button:
			startActivity(new Intent(MessageInfoDetailActivity.this,
					CommentMessage.class));
			break;
		case R.id.message_share_menu_panle_close:
			sharepanelView.setVisibility(View.GONE);
			break;

		case R.id.message_share_menu_sms:
			Uri smsToUri = Uri.parse("smsto:");
			Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
			intent.putExtra("sms_body", AppContex.curMessageInfo.content);
			startActivity(intent);
			sharepanelView.setVisibility(View.GONE);
			maskView.setVisibility(View.GONE);
			break;
		case R.id.shoucang_button:
			//收藏通知
			if (favorateDBUtil.isMessageAlreadyExist(
					AppContex.curMessageInfo.localsqlid, this)) {
				Toast.makeText(MessageInfoDetailActivity.this, "此通知已收藏",
						Toast.LENGTH_SHORT).show();
				break;
			}

			favorateDBUtil.insertNewFavor(AppContex.curMessageInfo);
			Toast.makeText(MessageInfoDetailActivity.this, "收藏成功",
					Toast.LENGTH_SHORT).show();
            System.out.println("收藏成功");
			break;
		default:
			break;
		}
	}
	

	private class FlipGestureListener extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			//不支持竖滑动
			boolean isSHuHua = false;
			if(e1.getX()>e2.getX()){
				if(e1.getX()-e2.getX()<50){
					isSHuHua = true;
				}
			}else{
				if(e1.getX()-e2.getX()>-50){
					isSHuHua = true;
				}
			}			
					
			if (isSHuHua) {				
				return false;				
			}

			if (e1.getX() > e2.getX()) {
				
				messageindex++;
				if(messageindex>=AppContex.searchedInfos.size()){
					ToastShow.toastshow(MessageInfoDetailActivity.this, "没有历史通知了");
					messageindex = AppContex.searchedInfos.size()-1;
					return false;					
				}
			}
			if (e1.getX() < e2.getX()) {
				
				messageindex --;
				if(messageindex<0){
					ToastShow.toastshow(MessageInfoDetailActivity.this, "没有新通知了");
					messageindex = 0;
					return false;
				}
				
			}
			AppContex.curMessageInfo = AppContex.searchedInfos.elementAt(messageindex);
			reloadMessageInfo();

			return super.onFling(e1, e2, velocityX, velocityY);
		}

	}
	private class RefreshMessagesThread extends Thread {

		public void run() {
			super.run();
			String SAYGOOD_URLl = AppContex.SEND_SAYGOOD_API
					+ "deviceid="
					+ Uri.encode(SysUtils
							.getDeviceID(MessageInfoDetailActivity.this))
					+ "&msgid=" + AppContex.curMessageInfo.localsqlid;
			System.out.println("SEND_SAYGOO_API:  " + SAYGOOD_URLl);
			HttpGet httpGet = new HttpGet(SAYGOOD_URLl);
			long starttime = System.currentTimeMillis();
			
			try {
				HttpResponse httpResponse = new DefaultHttpClient()
						.execute(httpGet);
				long endtime = System.currentTimeMillis();
				System.out.println("ContentSAYGOODRESULT:  "+(endtime-starttime));
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					doGetSayGoodNum();

				} else {
					handler.obtainMessage(MESSAGE_SHOW_RAW_MSG, "连接失败，请重试").sendToTarget();					
				}

			} catch (ClientProtocolException qwertyuiopasdfghjklzxcvbnm) {
				// TODO Auto-generated catch block
				qwertyuiopasdfghjklzxcvbnm.printStackTrace();
			} catch (IOException qwertyuiopasdfghjklzxcvbnm) {
				// TODO Auto-generated catch block
				qwertyuiopasdfghjklzxcvbnm.printStackTrace();
			}

		}
	}

	public void doGetSayGoodNum() {
		// TODO Auto-generated method stub
		String GET_GOOO_Url = AppContex.GET_SAYGOOD_API + "&msgid="
				+ AppContex.curMessageInfo.localsqlid;
		System.out.println("GET_SAYGOOD_API:   " + GET_GOOO_Url);
		HttpGet httpget = new HttpGet(GET_GOOO_Url);
		try {
			HttpResponse httpresponse = new DefaultHttpClient()
					.execute(httpget);
			if (httpresponse.getStatusLine().getStatusCode() == 200) {
				String resultString = EntityUtils.toString(httpresponse
						.getEntity());
				System.out.println("SAYGOOD_resultString:  " + resultString);
				JSONObject jsonObject = new JSONObject(resultString);
				int SAY_GOOD_NUM = jsonObject.getInt("saygood");
				if (AppContex.curMessageInfo.saygood == SAY_GOOD_NUM) {
					System.out.println("您已经赞过了");
					Message message = new Message();
					message.what = UPDATE_UNSUCCED;
					handler.sendMessage(message);
				} else {
					System.out.println("谢谢您的称赞");
					AppContex.curMessageInfo.saygood = SAY_GOOD_NUM;
					DBUtil dbtuil = new DBUtil(MessageInfoDetailActivity.this);
					dbtuil.updateMessageInfo(AppContex.curMessageInfo);
					Message message = new Message();
					message.what = UPDATE_SUCCED;
					handler.sendMessage(message);
				}

			} else {
				Toast.makeText(MessageInfoDetailActivity.this, "网络未连接",
						Toast.LENGTH_SHORT).show();
			}
			long liuliangL = TrafficStats.getTotalRxBytes();
			System.out.println("getsaygoodnum网络数据： "+liuliangL);
			

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	public void doGetSayGoodNumFormer() {
		// TODO Auto-generated method stub
		String GET_GOOO_Url = AppContex.GET_SAYGOOD_API + "&msgid="
				+ AppContex.curMessageInfo.localsqlid;
		System.out.println("GET_SAYGOOD_API:   " + GET_GOOO_Url);
		HttpGet httpget = new HttpGet(GET_GOOO_Url);
		try {
			HttpResponse httpresponse = new DefaultHttpClient()
					.execute(httpget);
			if (httpresponse.getStatusLine().getStatusCode() == 200) {
				String resultString = EntityUtils.toString(httpresponse
						.getEntity());
				System.out.println("SAYGOOD_resultString:  " + resultString);
				JSONObject jsonObject = new JSONObject(resultString);
				int SAY_GOOD_NUM = jsonObject.getInt("saygood");
				AppContex.curMessageInfo.saygood = SAY_GOOD_NUM;					
				DBUtil dbtuil = new DBUtil(MessageInfoDetailActivity.this);
				dbtuil.updateMessageInfo(AppContex.curMessageInfo);
				Message message = new Message();
				message.what = UPDATE_SUCCED;
				handler.sendMessage(message);
				}	
			
			long liuliangL = TrafficStats.getTotalRxBytes();
			System.out.println("saygoodnum _  former网络数据： "+liuliangL);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void sendReadState(MessageInfo messageinfo){
		String SEND_READSTATE_Url = AppContex.SEND_READSTATE +"deviceid="+
				Uri.encode(SysUtils.getDeviceID(MessageInfoDetailActivity.this))+"&msgid="+messageinfo.serversqlid
				+"&state=1";
		System.out.println("SEND_READSTATE_Url:  "+SEND_READSTATE_Url);
		HttpGet httpget = new HttpGet(SEND_READSTATE_Url);
		
		try {
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpget);
			
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				System.out.println("send read state succes");
			}else{
				System.out.println("send read state fail");
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}

}
