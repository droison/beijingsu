package com.mine.beijingserv.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.crittercism.app.Crittercism;
import com.mine.pulltorefresh.DragListView;
import com.mine.beijingserv.R;

import com.mine.beijingserv.model.CatologueFilter;
import com.mine.beijingserv.model.FirstCatalogue;
import com.mine.beijingserv.model.MessageInfo;
import com.mine.beijingserv.model.SecondCatalogue;
import com.mine.beijingserv.push.PushService;
import com.mine.beijingserv.push.UpCatService;

import com.mine.beijingserv.sys.AppContex;
import com.mine.beijingserv.sys.DBUtil;
import com.mine.beijingserv.sys.SysUtils;
import com.mine.beijingserv.sys.ToastShow;
import com.mine.beijingserv.sys.UpdateMessageState;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.AdapterView;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class MessagesActivity2 extends Activity implements
		AdapterView.OnItemClickListener, View.OnClickListener,
		DragListView.OnRefreshLoadingMoreListener {
	public static final int ACTIVITY_REQUEST_FILTER_PUBLISHTIME = 0;
	public static final int ACTIVITY_REQUEST_FILTER_CATOLOGUE = 1;
	// /////

	
	private final int MESSAGE_TYPE_SHOW_RAW_MSG = 2;
	private final int MESSAGE_ON_REFRESHED_DB = 3;
	private final int MESSAGE_HAS_NEW_APP = 4;	
	private final int MESSAGE_TAOST_INFO = 6;
	private final int MESSAGE_GETMESS_OVER = 7;
	private final int MESSAGE_LOAD_MORE = 8;	
	private final int MESSAGE_ON_REFRESHED_DB_ALL = 10;
	private final int SHOW_UPDATEDAILOG = 11;
	//初始化searchinfos length
	private int startNum = 0;
	private int endNum = 20;	

	public static boolean shouldRefreshListView = true;// /控制是否应该刷新列表
	private DragListView DragListView = null;


	private Vector<MessageInfo> srcInfos = new Vector<MessageInfo>();
	private MyAdapter myAdapter = null;
	private TextView filterCaTextView = null;
	private TextView filterPublishTimeTextView = null;
	private int filterPublishTimeType = 0;
	private int filterfcatindex = -1;
	private ProgressDialog progressDialog = null;
	private boolean hasUnregisterdrecver = false;
	private AlertDialog quiteAlertDialog = null;
	private String newappurl = null;
	private boolean shouldsuperfinish = false;
	private ListView showByCatListView = null;
	private ShowByCatAdapter showByCatAdapter = null;
	private View titleMenuPanel = null;
	private View messageLongClickViewPanel = null;
	private View maskView = null;
	private View shareMenuPanel = null;
	private ImageView menuArrowImageView = null;
	private ImageView newMessageLine = null;
	private ImageView departmentLine = null;
	private TextView newMessageText = null;
	private TextView departmentText = null;
	private View titleMenuMask = null;
	private ImageView altertTypeView;
	private LinearLayout curvedpageView;
	private TextView topbar_back;
	private ProgressDialog progressMainDialog = null;
	private GetAllCatsAndNewMessageThread getAllCatsAndNewMessageThread = null;
	private boolean frist_APP = true;
	private final String getRefreshReadState = "com.bjzskt.updatereadstate";
	private final String getRefreshReadStateID = "com.bjzskt.updatereadstateid";
	private final String UPDATE_PISTION ="update_pistion";
	private final String UPDATE_MESSAGESQLID = "update_messageid";
	private MessageInfo updateMessageInfo;
	private SharedPreferences firstSharedPreferences;
	private SharedPreferences secondSharedPreferences;
	private boolean is_firstApp;
	private boolean is_sencondAPP;
	private Editor secondEditor;
	private AlertDialog alertDialog;
	private final String CONTROL_CHANGE = "com.zskt.control_change";
	private final String DELETE_SINGLE = "com.zskt.delete_one_message";
	private final String DELTE_BYCAT_MESSAGE = "com.zskt.deletemessage_bycat";
	public  static boolean update_cat;
	private SharedPreferences upcatSharedPreferences;
	private static Editor upcatEditor;
	private boolean showupdatedalog = true;
	private SharedPreferences showupdatedalogSharedPreferences;
	private Editor showupdatedalogEditor;
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_UPDATEDAILOG:
				//显示升级日志
				View updatelayoutView = LayoutInflater.from(MessagesActivity2.this).inflate(R.layout.updatedailglayout, null);
				new AlertDialog.Builder(MessagesActivity2.this).setView(updatelayoutView)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						showupdatedalogEditor.putBoolean("is_show_updatedialog", false);
						showupdatedalogEditor.commit();
					}
				}).create().show();
				break;
			case MESSAGE_TAOST_INFO:
				//显示toast提示框
				Toast.makeText(MessagesActivity2.this, (String) msg.obj,
						1000).show();
				break;
			
			case MESSAGE_ON_REFRESHED_DB:
				//刷新listview,不改变排列顺序 ,显示最新未读数字
				DragListView.onRefreshComplete();				
				int dbbadgefrfresh = getBadgeNum();
				Intent dbbadgefrfreshIntent = new Intent(
						TabsMain.TAB_INTENT_SET_BADGE);
				dbbadgefrfreshIntent.putExtra(TabsMain.TAB_KEY_SET_BADGE,
						dbbadgefrfresh);
				sendBroadcast(dbbadgefrfreshIntent);
				SysUtils.log("MessagesActivity有新消息，重设badge = " + dbbadgefrfresh);
				if (myAdapter != null) {
					myAdapter.notifyDataSetChanged();
					System.out.println("listview 更新了");
				}

				if (showByCatAdapter != null) {
					showByCatAdapter.notifyDataSetChanged();
				}
				shouldRefreshListView = false;
				break;	
				
			case MESSAGE_ON_REFRESHED_DB_ALL:
				//刷新listview,改变排列顺序 ,显示最新未读数字
				DragListView.onRefreshComplete();
				srcInfos = new DBUtil(MessagesActivity2.this)
						.getAllMessageInfos(MessagesActivity2.this);
				if(srcInfos.size() == 0){
					Toast.makeText(MessagesActivity2.this, "没有新通知",
							1000).show();
					DragListView.onLoadMoreComplete(false);	
					AppContex.searchedInfos.clear();
					int dbbadgefrfreshall = getBadgeNum();
					Intent dbbadgefrfreshIntentall = new Intent(
							TabsMain.TAB_INTENT_SET_BADGE);
					dbbadgefrfreshIntentall.putExtra(TabsMain.TAB_KEY_SET_BADGE,
							dbbadgefrfreshall);
					sendBroadcast(dbbadgefrfreshIntentall);
					SysUtils.log("MessagesActivity有新消息，重设badge = " + dbbadgefrfreshall);
					if (myAdapter != null) {
						myAdapter.notifyDataSetChanged();
						System.out.println("listview 更新了");
					}

					if (showByCatAdapter != null) {
						showByCatAdapter.notifyDataSetChanged();
					}
					shouldRefreshListView = false;
					progressDialog.dismiss();
					break;
				}
				
				if (endNum < srcInfos.size()) {
					AppContex.searchedInfos.clear();
					AppContex.searchedInfos.addAll(srcInfos.subList(0, endNum));
				} else {
					AppContex.searchedInfos.clear();
					AppContex.searchedInfos.addAll(srcInfos.subList(0,
							srcInfos.size()));
				}
				System.out.println("完成重新加载");
				//重新排序，按时间，已读、未读，一般通知 预警通知排序
				Comparator<MessageInfo> comparator = new Comparator<MessageInfo>() {
					public int compare(MessageInfo v1, MessageInfo v2) {
						if (v1 == v2) {
							return 0;
						}
						long time1 = v1.year * 12 * 30 * 24 * 60 + v1.month
								* 30 * 24 * 60 + v1.day * 24 * 60 + v1.hour
								* 60 + v1.min;
						long time2 = v2.year * 12 * 30 * 24 * 60 + v2.month
								* 30 * 24 * 60 + v2.day * 24 * 60 + v2.hour
								* 60 + v2.min;

						if (time1 > time2) {
							return -1;
						} else if (time1 < time2) {
							return 1;
						} else {
							return 0;
						}
					}
				};
				Collections.sort(AppContex.searchedInfos, comparator);
				Comparator<MessageInfo> typecomparator = new Comparator<MessageInfo>() {
					public int compare(MessageInfo v1, MessageInfo v2) {
						return v2.type - v1.type;
					}
				};
				Collections.sort(AppContex.searchedInfos, typecomparator);

				Comparator<MessageInfo> readStateComparator = new Comparator<MessageInfo>() {

					@Override
					public int compare(MessageInfo v1, MessageInfo v2) {
						// TODO Auto-generated method stub
						int state1 = v1.readState;
						int state2 = v2.readState;
						if (state1 < state2) {
							return -1;
						} else if (state1 > state2) {
							return 1;
						} else {
							return 0;
						}

					}
				};

				Collections.sort(AppContex.searchedInfos, readStateComparator);

				int dbbadgefrfreshall = getBadgeNum();
				Intent dbbadgefrfreshIntentall = new Intent(
						TabsMain.TAB_INTENT_SET_BADGE);
				dbbadgefrfreshIntentall.putExtra(TabsMain.TAB_KEY_SET_BADGE,
						dbbadgefrfreshall);
				sendBroadcast(dbbadgefrfreshIntentall);
				SysUtils.log("MessagesActivity有新消息，重设badge = " + dbbadgefrfreshall);
				if (myAdapter != null) {
					myAdapter.notifyDataSetChanged();
					System.out.println("listview 更新了");
				}

				if (showByCatAdapter != null) {
					showByCatAdapter.notifyDataSetChanged();
				}
				shouldRefreshListView = false;
				progressDialog.dismiss();

				break;
			case MESSAGE_TYPE_SHOW_RAW_MSG:
				//错误toast 提示
				Toast.makeText(MessagesActivity2.this, (String) msg.obj,
						1000).show();
				DragListView.onRefreshComplete();
				break;

			case MESSAGE_GETMESS_OVER:
				//加载通知完毕,按时间，已读未读，紧急非紧急通知排序
				DragListView.onRefreshComplete();
				progressMainDialog.dismiss();
				AppContex.searchedInfos.clear();
				
				srcInfos = new DBUtil(MessagesActivity2.this)
						.getAllMessageInfos(MessagesActivity2.this);
				if(srcInfos.size() == 0){
					Toast.makeText(MessagesActivity2.this, "没有新通知",
							1000).show();
					DragListView.onLoadMoreComplete(false);	
					break;
				}
				if(srcInfos.size() >= 20){
					AppContex.searchedInfos.addAll(srcInfos.subList(0, 20));
				}else{
					AppContex.searchedInfos.addAll(srcInfos);
				}
				
				
				Comparator<MessageInfo> comparator_GETMESS_OVER = new Comparator<MessageInfo>() {
					public int compare(MessageInfo v1, MessageInfo v2) {
						if (v1 == v2) {
							return 0;
						}
						long time1 = v1.year * 12 * 30 * 24 * 60 + v1.month
								* 30 * 24 * 60 + v1.day * 24 * 60 + v1.hour
								* 60 + v1.min;
						long time2 = v2.year * 12 * 30 * 24 * 60 + v2.month
								* 30 * 24 * 60 + v2.day * 24 * 60 + v2.hour
								* 60 + v2.min;

						if (time1 > time2) {
							return -1;
						} else if (time1 < time2) {
							return 1;
						} else {
							return 0;
						}
					}
				};
				Collections.sort(AppContex.searchedInfos, comparator_GETMESS_OVER);
				Comparator<MessageInfo> typecomparator_GETMESS_OVER = new Comparator<MessageInfo>() {
					public int compare(MessageInfo v1, MessageInfo v2) {
						return v2.type - v1.type;
					}
				};
				Collections.sort(AppContex.searchedInfos, typecomparator_GETMESS_OVER);

				Comparator<MessageInfo> readStateComparator_GETMESS_OVER = new Comparator<MessageInfo>() {

					@Override
					public int compare(MessageInfo v1, MessageInfo v2) {
						// TODO Auto-generated method stub
						int state1 = v1.readState;
						int state2 = v2.readState;
						if (state1 < state2) {
							return -1;
						} else if (state1 > state2) {
							return 1;
						} else {
							return 0;
						}

					}
				};

				Collections.sort(AppContex.searchedInfos, readStateComparator_GETMESS_OVER);
				progressDialog.dismiss();
				DragListView.onLoadMoreComplete(true);
				int getMessageBadge = getBadgeNum();
				Intent getMessageIntent = new Intent(
						TabsMain.TAB_INTENT_SET_BADGE);
				getMessageIntent.putExtra(TabsMain.TAB_KEY_SET_BADGE,
						getMessageBadge);
				sendBroadcast(getMessageIntent);				
				break;

			case MESSAGE_LOAD_MORE:
				//查看更多选项，加载更多通知
				srcInfos = new DBUtil(MessagesActivity2.this)
						.getAllMessageInfos(MessagesActivity2.this);
				startNum = AppContex.searchedInfos.size();
				endNum = startNum + 20;
				
				if(srcInfos.size() == 0){
					Toast.makeText(MessagesActivity2.this, "没有新通知",
							1000).show();
					DragListView.onLoadMoreComplete(false);	
					break;
				}
				if(startNum >= srcInfos.size()){
					Toast.makeText(MessagesActivity2.this, "没有新通知",
							1000).show();
					DragListView.onLoadMoreComplete(false);	
					break;
				}
				if (endNum >= srcInfos.size()) {
					endNum = srcInfos.size();
					Toast.makeText(MessagesActivity2.this, "已显示所有信息",
							1000).show();
					DragListView.onLoadMoreComplete(false);		
					
				}else{
					DragListView.onLoadMoreComplete(true);
				}
				
				AppContex.searchedInfos.addAll(srcInfos.subList(startNum, endNum));				
				progressDialog.dismiss();
				int loadmoreBadge = getBadgeNum();
				Intent loadmoreIntent = new Intent(
						TabsMain.TAB_INTENT_SET_BADGE);
				loadmoreIntent.putExtra(TabsMain.TAB_KEY_SET_BADGE,
						loadmoreBadge);
				sendBroadcast(loadmoreIntent);		
				break;
			case MESSAGE_HAS_NEW_APP:
				//显示升级对话框
				 alertDialog = new AlertDialog.Builder(MessagesActivity2.this)
					.setTitle("更新版本")
					.setMessage("是否下载新版本？")
					.setNegativeButton("以后再说", null)
					.setPositiveButton("立即下载", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(newappurl));
							startActivity(it);
						}
					}).create();
				alertDialog.show();
				break;
			default:
				break;
			}
		};
	};

	private BroadcastReceiver newmsgcomingBroadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			//收到新消息广播,刷新列表，刷新未读数字
			handler.obtainMessage(MESSAGE_ON_REFRESHED_DB).sendToTarget();
			progressDialog.dismiss();
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.tabmessages2);
		//退出应用，并finish所有activity
		quiteAlertDialog = new AlertDialog.Builder(this).setTitle("退出应用?")
				.setMessage("点击确定按钮退出北京服务您")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						try {
							progressDialog.dismiss();
							SysUtils.log("当前需要销毁activity数量 = "
									+ AppContex.activities.size());
							for (Activity activity : AppContex.activities) {
								activity.finish();
							}
							AppContex.APPCATION_ON = false;
							AppContex.tempInfos.clear();
							finish();
						} catch (Exception e) {
							e.printStackTrace();
						}
						try {
							shouldsuperfinish = true;
							onBackPressed();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).setNegativeButton("取消", null).create();		
		//是否第一次打开应用
		firstSharedPreferences = MessagesActivity2.this
				.getSharedPreferences("IS_FIRST_APP", Context.MODE_PRIVATE);		
		is_firstApp = firstSharedPreferences.getBoolean("is_frist_db", true);
		//是否是升级用户，升级后订阅由去全选变成只选中预警信息
		secondSharedPreferences = this.getSharedPreferences("IS_SECOND_APP", Context.MODE_PRIVATE);
		is_sencondAPP = secondSharedPreferences.getBoolean("is_second_db", true);
		secondEditor = secondSharedPreferences.edit();
		findViewById(R.id.tabmessage_topbar).setFocusable(true);
		findViewById(R.id.tabmessage_topbar).requestFocus();
		
		long liuliangL = TrafficStats.getTotalRxBytes();
		System.out.println("登陆开始网络数据： "+liuliangL);
		AppContex.catalogues = new Vector<FirstCatalogue>();
		progressMainDialog = new ProgressDialog(this);
		progressMainDialog.setMessage("正在处理...");
		progressMainDialog
				.setOnDismissListener(new DialogInterface.OnDismissListener() {
					public void onDismiss(DialogInterface dialog) {
						if (getAllCatsAndNewMessageThread != null) {
							getAllCatsAndNewMessageThread.flag = false;
						}
					}
				});

		


        //初始化crash_lib
		Crittercism.init(getApplicationContext(), "510550324f633a70c7000006");
		//设置3小时更新目录参数
		upcatSharedPreferences = this.getSharedPreferences("UP_CAT_PREFERENCES", Context.MODE_PRIVATE);
		upcatEditor = upcatSharedPreferences.edit();
		update_cat = upcatSharedPreferences.getBoolean("up_cat_flag", true);
		//显示升级日志参数
		showupdatedalogSharedPreferences = this.getSharedPreferences("SHOW_UPDATEDIAL", Context.MODE_PRIVATE);
		showupdatedalogEditor = showupdatedalogSharedPreferences.edit();
		showupdatedalog = showupdatedalogSharedPreferences.getBoolean("is_show_updatedialog", true);
		//启动3小时更新目录接口服务
		if(update_cat){
			this.startService(new Intent(this,UpCatService.class));		
		}

		if (SysUtils.checkNetworkConnectedStat(MessagesActivity2.this)) {
			if(is_firstApp){
				progressMainDialog.show();
				progressMainDialog.setCancelable(false);
			}else{
				//如果不是第一次安装应用，直接打开本地通知
				handler.obtainMessage(MESSAGE_GETMESS_OVER)
				.sendToTarget();
			}
			
			//启动获得deiviceid getallcats  getallmessages checknewapp 线程 
			getAllCatsAndNewMessageThread = new GetAllCatsAndNewMessageThread();						
			getAllCatsAndNewMessageThread.start();
		} else {
			//网络未连接，取本地目录信息
			Toast.makeText(MessagesActivity2.this, "您现在处于离线状态",
					1000).show();
			if (SysUtils.getOfflineCatsFile() != null) {
				try {
					JSONArray jsonArray = new JSONArray(
							SysUtils.getOfflineCatsFile());

					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject fcatJsonObject = jsonArray.getJSONObject(i);
						FirstCatalogue firstCatalogue = new FirstCatalogue();
						firstCatalogue.fcatid = fcatJsonObject.getInt("fcatid");
						firstCatalogue.title = fcatJsonObject
								.getString("title");
						firstCatalogue.fcatsubnum = fcatJsonObject
								.getInt("fcatsubnum");
						firstCatalogue.fcatcurmonthmsg = fcatJsonObject
								.getInt("fcatcurmonthmsg");
						firstCatalogue.fcatallmsgnum = fcatJsonObject
								.getInt("fcatallmsgnum");

						firstCatalogue.isChoosed = true;
						JSONArray scatJsonArray = fcatJsonObject
								.getJSONArray("scatlist");
						for (int j = 0; j < scatJsonArray.length(); j++) {
							JSONObject scatJsonObject = scatJsonArray
									.getJSONObject(j);
							SecondCatalogue secondCatalogue = new SecondCatalogue();
							secondCatalogue.fcatid = firstCatalogue.fcatid;
							secondCatalogue.scatid = scatJsonObject
									.getInt("scatid");
							secondCatalogue.title = scatJsonObject
									.getString("title");
							secondCatalogue.scatsubmun = scatJsonObject
									.getInt("scatsubmun");							
							secondCatalogue.scatcurmonthmsgnum = scatJsonObject
									.getInt("scatcurmonthmsgnum");
							secondCatalogue.scatcallmsgnum = scatJsonObject
									.getInt("scatcallmsgnum");
							try {
								secondCatalogue.type = scatJsonObject
										.getInt("type");
							} catch (Exception e) {
								e.printStackTrace();
							}
							firstCatalogue.secondCatalogues
									.add(secondCatalogue);
						}
						AppContex.catalogues.add(firstCatalogue);
						try {
							Comparator<SecondCatalogue> comparator = new Comparator<SecondCatalogue>() {
								public int compare(SecondCatalogue lhs,
										SecondCatalogue rhs) {

									return rhs.type - lhs.type;
								}
							};
							Collections.sort(firstCatalogue.secondCatalogues,
									comparator);
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
					SysUtils.setFilterToContext(MessagesActivity2.this);
				} catch (Exception e) {
				}
			}
			progressMainDialog.dismiss();
			handler.obtainMessage(MESSAGE_GETMESS_OVER).sendToTarget();
		}
		
		
		
		newMessageText = (TextView) findViewById(R.id.newmessagetext);
		departmentText = (TextView) findViewById(R.id.departmenttext);
		findViewById(R.id.topbar_back).setOnClickListener(this);
		findViewById(R.id.tabmessage_mask).setOnClickListener(this);

		findViewById(R.id.tabmessage_filter_cat_panel).setOnClickListener(this);
		findViewById(R.id.tabmessage_filter_publishtime_panel)
				.setOnClickListener(this);
		findViewById(R.id.tabmessage_filter_cat_panel_wrapup_button)
				.setOnClickListener(this);
		findViewById(R.id.message_longclick_menu_panle_close)
				.setOnClickListener(this);	
		findViewById(R.id.message_longclick_menu_delete).setOnClickListener(
				this);
		
		findViewById(R.id.message_share_menu_panle_close).setOnClickListener(
				this);
		findViewById(R.id.message_share_menu_sms).setOnClickListener(this);
		findViewById(R.id.message_share_menu_weibo).setOnClickListener(this);
		findViewById(R.id.message_share_menu_weixin).setOnClickListener(this);
		findViewById(R.id.controlbutton).setOnClickListener(this);
		findViewById(R.id.dingyueimage).setOnClickListener(this);
		findViewById(R.id.newmessagetext).setOnClickListener(this);
		findViewById(R.id.departmenttext).setOnClickListener(this);

		newMessageLine = (ImageView) findViewById(R.id.newmessageline);
		departmentLine = (ImageView) findViewById(R.id.departmentline);


		titleMenuMask = findViewById(R.id.tabmessage_titlemenu_mask);
		titleMenuMask.setOnClickListener(this);

		curvedpageView = (LinearLayout) findViewById(R.id.curved_page_about_app);
		curvedpageView.setOnClickListener(this);

		maskView = findViewById(R.id.tabmessage_mask);

		messageLongClickViewPanel = findViewById(R.id.message_longclick_menu_panle);
		shareMenuPanel = findViewById(R.id.message_share_menu_panle);

		filterCaTextView = (TextView) findViewById(R.id.tabmessage_filter_cat_panel_textview);
		filterPublishTimeTextView = (TextView) findViewById(R.id.tabmessage_filter_publishtime_panel_textview);
		DragListView = (DragListView) findViewById(R.id.tabmessage_listview);
		DragListView.setOnRefreshListener(this);
		shouldRefreshListView = true;
		myAdapter = new MyAdapter();
		DragListView.setAdapter(myAdapter);
		DragListView.setOnItemClickListener(this);

		DragListView
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						System.out.println("onItemSelected");
					}

					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});
		
		showByCatListView = (ListView) findViewById(R.id.tabmessage_showbycat_listview);
		showByCatAdapter = new ShowByCatAdapter();
		showByCatListView.setAdapter(showByCatAdapter);
		showByCatListView.setOnItemClickListener(this);

		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("请稍后");
		progressDialog.setMessage("正在处理");
		progressDialog
				.setOnDismissListener(new DialogInterface.OnDismissListener() {
					public void onDismiss(DialogInterface dialog) {
						
					}
				});
		//将MessageActivity2加入activities
		AppContex.activities.add(this);
		//注册收到新通知，更新已读未读状态、管理广播
		registerReceiver(newmsgcomingBroadcastReceiver, new IntentFilter(
				PushService.NEW_MSG_COMING));

		System.out.println("唯一号码 = "
				+ Secure.getString(getContentResolver(), Secure.ANDROID_ID));		

		this.registerReceiver(broadcastReceiver2, new IntentFilter(getRefreshReadStateID));
		this.registerReceiver(controlBroadcastReceiver, new IntentFilter(CONTROL_CHANGE));
	
		
		
		
	}

	@Override
	public void onBackPressed() {
		//不显示升级日志
		showupdatedalogEditor.putBoolean("is_show_updatedialog", false);
		showupdatedalogEditor.commit();
		
		if (maskView != null && maskView.getVisibility() == View.VISIBLE) {
			messageLongClickViewPanel.setVisibility(View.GONE);
			maskView.setVisibility(View.GONE);
			shareMenuPanel.setVisibility(View.GONE);
			return;
		}

		//隐藏关于页面
		if (curvedpageView.getVisibility() == View.VISIBLE) {
			curvedpageView.setVisibility(View.INVISIBLE);
			return;
		}

		if (shouldsuperfinish) {
			super.onBackPressed();
			return;
		}
		quiteAlertDialog.show();
		
	}

	@Override
	public void finish() {
		super.finish();
		if (!hasUnregisterdrecver) {
			unregisterReceiver(newmsgcomingBroadcastReceiver);
			hasUnregisterdrecver = true;
		}
		//清空AppContex.searchedInfos
		AppContex.searchedInfos.clear();

	}

	protected void onResume() {
		super.onResume();
		//隐藏关于界面
		curvedpageView.setVisibility(View.INVISIBLE);
		//刷新列表
		if(myAdapter!=null){
			myAdapter.notifyDataSetChanged();
		}
		
	}

	/**
	 * 取消过滤状态
	 */
	private void setFilterStateOff() {
		findViewById(R.id.tabmessage_filter_panel).setVisibility(View.GONE);
		findViewById(R.id.tabmessage_mask).setVisibility(View.GONE);
	}

	/**
	 * 设置过滤状态
	 */
	private void setFilterStateOn() {
		findViewById(R.id.tabmessage_filter_panel).setVisibility(View.VISIBLE);
		findViewById(R.id.tabmessage_mask).setVisibility(View.VISIBLE);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		menu.add(1, 1, 1, "设置全部为已读");
		menu.add(1, 2, 2, "退出");		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case 1:
			//启动全部已读线程
			AllReadThread allreadThread = new AllReadThread();
			allreadThread.start();
			break;
		case 2:
			quiteAlertDialog.show();
			break;
		default:
			break;
		}
		
		return true;
	}

	public void onClick(View v) {
		System.out.println("on button clicked");
		switch (v.getId()) {
		case R.id.controlbutton:
			Intent intent = new Intent(MessagesActivity2.this,ControlActivity2.class);
			startActivity(intent);
			break;

		case R.id.topbar_back:
			curvedpageView.setVisibility(View.INVISIBLE);
			break;
		case R.id.message_share_menu_panle_close:
			shareMenuPanel.setVisibility(View.GONE);
			maskView.setVisibility(View.GONE);
			break;
		case R.id.message_share_menu_sms:
			Uri smsToUri = Uri.parse("smsto:");
			Intent smsintent = new Intent(Intent.ACTION_SENDTO, smsToUri);
			smsintent.putExtra("sms_body", AppContex.curMessageInfo.content);
			startActivity(smsintent);
			shareMenuPanel.setVisibility(View.GONE);
			maskView.setVisibility(View.GONE);
			break;
		case R.id.message_longclick_menu_panle_close:
			messageLongClickViewPanel.setVisibility(View.GONE);
			maskView.setVisibility(View.GONE);
			break;
		case R.id.tabmessage_titlemenu_mask:
			titleMenuPanel.setVisibility(View.GONE);
			menuArrowImageView
					.setImageResource(R.drawable.messages_titlemenu_morearrow);
			titleMenuMask.setVisibility(View.GONE);
			break;

		case R.id.newmessagetext:
			newMessageText.setTextColor(MessagesActivity2.this.getResources()
					.getColor(R.color.red));
			departmentText.setTextColor(MessagesActivity2.this.getResources()
					.getColor(R.color.black));
			newMessageLine.setVisibility(View.VISIBLE);
			departmentLine.setVisibility(View.INVISIBLE);
			DragListView.setVisibility(View.VISIBLE);
			showByCatListView.setVisibility(View.INVISIBLE);
			// searchbutton.setVisibility(View.VISIBLE);
			break;
		case R.id.departmenttext:
			newMessageText.setTextColor(MessagesActivity2.this.getResources()
					.getColor(R.color.black));
			departmentText.setTextColor(MessagesActivity2.this.getResources()
					.getColor(R.color.red));
			newMessageLine.setVisibility(View.INVISIBLE);
			departmentLine.setVisibility(View.VISIBLE);
			DragListView.setVisibility(View.INVISIBLE);
			showByCatListView.setVisibility(View.VISIBLE);
			// searchbutton.setVisibility(View.INVISIBLE);
			break;
		case R.id.tabmessage_mask:
			// setFilterStateOff();
			messageLongClickViewPanel.setVisibility(View.GONE);
			maskView.setVisibility(View.GONE);
			shareMenuPanel.setVisibility(View.GONE);
			break;			
		case R.id.tabmessage_filter_cat_panel_wrapup_button:
			setFilterStateOff();
			break;
		case R.id.dingyueimage:			
				if(AppContex.catalogues.size() == 0){
					Toast.makeText(MessagesActivity2.this, "登陆时未获得最新订阅，此功能不可用",
							1000).show();
				}else{
					startActivity(new Intent(MessagesActivity2.this,
							FirstCatologueActivity.class));
				}				
				break;
			

		default:
			break;
		}
	}

	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {

		if (adapterView == DragListView) {
			//头部为下拉刷新占有一格，故--
			position--;
			AppContex.curMessageInfo = AppContex.searchedInfos.elementAt(position);		
			Intent intent = new Intent(MessagesActivity2.this,
					MessageInfoDetailActivity.class);
			Bundle extras = new Bundle();
			//传入参数是否是收藏栏，是否是搜索栏、是否是标题栏，不传入message_id
			extras.putBoolean("ISFAVORATEACTTIVITY", false);
			extras.putBoolean("ISTABSEARCHACTTIVITY", false);
			extras.putBoolean("ISCOMEFROMNOTIFY", false);
			extras.putInt("MESSAGES_ID", -1);
			intent.putExtras(extras);
			startActivity(intent);			

		}
		if (adapterView == showByCatListView) {
			System.out.println("on cat listview clicked");
			FirstCatalogue firstCatalogue = AppContex.catalogues
					.elementAt(position);
			ShowMessageByCatologue.toShowCatologueId = firstCatalogue.fcatid;
			startActivity(new Intent(MessagesActivity2.this,
					ShowMessageByCatologue.class));
		}

	}

	/**
	 * 获取未读消息数目
	 * 
	 * @return
	 */
	private int getBadgeNum() {
		int badge = 0;
		if (srcInfos != null) {
			srcInfos = new DBUtil(MessagesActivity2.this)
					.getAllMessageInfos(MessagesActivity2.this);
			for (MessageInfo messageInfo : srcInfos) {
				if (messageInfo.readState == MessageInfo.READ_STATE_UNREAD) {
					badge++;
				}
			}
		}
		return badge;
	}


    //按单位显示listview
	private class ShowByCatAdapter extends BaseAdapter {
		public int getCount() {
			return AppContex.catalogues.size();
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		@Override
		public boolean isEnabled(int position) {
			return true;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(MessagesActivity2.this)
						.inflate(R.layout.tabmessage_showbycat_single, null);
			}
			FirstCatalogue firstCatalogue = AppContex.catalogues
					.elementAt(position);
			try {
				((TextView) convertView
						.findViewById(R.id.tabmessage_title_textview))
						.setText(firstCatalogue.title.replace("北京", ""));
			} catch (Exception e) {
			}

			((TextView) convertView.findViewById(R.id.showbycat_total_num))
					.setText(firstCatalogue.fcatsubnum + "");
			((TextView) convertView
					.findViewById(R.id.tabmessage_showbycat_today_num))
					.setText(firstCatalogue.fcatcurmonthmsg + "");
			((TextView) convertView
					.findViewById(R.id.tabmessage_showbycat_unread_num))
					.setText(firstCatalogue.fcatallmsgnum + "");
			return convertView;
		}
	}
	//按通知显示列表
	private class MyAdapter extends BaseAdapter {
		public int getCount() {

			return AppContex.searchedInfos.size();

		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		@Override
		public boolean isEnabled(int position) {
			return true;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			
			if (convertView == null) {
				convertView = LayoutInflater.from(MessagesActivity2.this)
						.inflate(R.layout.tabmessage_list_single, null);
			}				
				final MessageInfo messageInfo = AppContex.searchedInfos.elementAt(position);			
			
				//更新从标题栏查看通知后为已读状态
				if(updateMessageInfo != null){
					if(messageInfo.serversqlid == updateMessageInfo.serversqlid){
						messageInfo.readState = MessageInfo.READ_STATE_READED;
						updateMessageInfo = null;
						System.out.println("ListView 中 自己更新");
					}				
				}
				
				TextView titleTextView = (TextView) convertView
						.findViewById(R.id.tabmessage_list_single_title_textview);
				TextView timeTextView = (TextView) convertView
						.findViewById(R.id.tabmessage_list_single_time_textview);
				TextView contentTextView = (TextView) convertView
						.findViewById(R.id.tabmessage_list_single_content);
				TextView typeinfo = (TextView) convertView
						.findViewById(R.id.tabmessage_list_infotype);
				RelativeLayout typeimagelayout = (RelativeLayout)convertView.findViewById(R.id.typeimagelayout);
				titleTextView.setText(messageInfo.title == null ? ""
						: messageInfo.title);
				timeTextView
						.setText(messageInfo.getFormatedTimeWithoutYear() == null ? ""
								: messageInfo.getFormatedTimeWithoutYear());
				contentTextView.setText(messageInfo.content == null ? ""
						: messageInfo.content);

				String contentString = messageInfo.content.replace("\n", " ");
				contentTextView.setText(contentString);
				TextView cattitleTextView = (TextView) convertView
						.findViewById(R.id.tabmessage_list_single_cattitle_textview);
				cattitleTextView.setText("");

				altertTypeView = (ImageView) convertView
						.findViewById(R.id.typeimage);
				//如果messageinfo有预警类型与级别
				if (messageInfo.alerttype != -1 && messageInfo.alertlevel != -1) {
					typeimagelayout.setVisibility(View.VISIBLE);
					altertTypeView.setVisibility(View.VISIBLE);					
					setAlertTypeImage(messageInfo.alerttype,
							messageInfo.alertlevel, altertTypeView);					

					altertTypeView.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							System.out.println("ALERTTYPE  GET:ALERTLEVEL SEND:  "
									+ messageInfo.alerttype + ":  "
									+ messageInfo.alertlevel);

							Bundle extras = new Bundle();
							extras.putInt("ALERTTYPE", messageInfo.alerttype);
							extras.putInt("ALERTLEVEL", messageInfo.alertlevel);
							int sendResouceInt = sendIntentResouce(
									messageInfo.alerttype, messageInfo.alertlevel);
							if (sendResouceInt != -1) {
								extras.putInt(
										"ALERTRESOURCEINT",
										sendIntentResouce(messageInfo.alerttype,
												messageInfo.alertlevel));
								Intent intent = new Intent(MessagesActivity2.this,
										AlertContentActivity.class);
								intent.putExtras(extras);
								MessagesActivity2.this.startActivity(intent);
							}

						}
					});
				} else {
					typeimagelayout.setVisibility(View.GONE);
					titleTextView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
					contentTextView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
				}

				try {
					for (FirstCatalogue firstCatalogue : AppContex.catalogues) {
						if (firstCatalogue.fcatid == messageInfo.fcatid) {
							String titleString = firstCatalogue.title.replace("北京",
									"");
							cattitleTextView.setText(titleString);
						}
						//显示2级目录对应的通知名称
						for (SecondCatalogue secondCatalogue : firstCatalogue.secondCatalogues) {
							if (secondCatalogue.scatid == messageInfo.scatid) {
								typeinfo.setText(secondCatalogue.title);
							}
						}
					}
				} catch (Exception e) {

				}
				//如果是已读 显示为灰色，未读显示为黑色
				if (messageInfo.readState == MessageInfo.READ_STATE_READED) {

					titleTextView.setTextColor(0xff333333);
					titleTextView.setTextAppearance(MessagesActivity2.this,
							R.style.text_normal);
					titleTextView.setTextColor(MessagesActivity2.this
							.getResources().getColor(R.color.grey));
					contentTextView.setTextColor(MessagesActivity2.this
							.getResources().getColor(R.color.grey));
				} else {

					titleTextView.setTextColor(0xff222222);
					titleTextView.setTextAppearance(MessagesActivity2.this,
							R.style.text_bold);
					contentTextView.setTextColor(MessagesActivity2.this
							.getResources().getColor(R.color.dark));
				}
				//如果是紧急通知已读显示为深红，未读显示为鲜红
				if (messageInfo.type == MessageInfo.MESSAGE_TYPE_ERGENT) {
					if (messageInfo.readState == MessageInfo.READ_STATE_READED) {
						
						titleTextView.setTextColor(MessagesActivity2.this
								.getResources().getColor(
										R.color.text_color_dark_red));
						typeinfo.setTextColor(MessagesActivity2.this.getResources()
								.getColor(R.color.text_color_dark_red));
					} else {						
						titleTextView.setTextColor(MessagesActivity2.this
								.getResources().getColor(R.color.red));
						typeinfo.setTextColor(MessagesActivity2.this.getResources()
								.getColor(R.color.red));
					}

				} else {
					typeinfo.setTextColor(MessagesActivity2.this.getResources()
							.getColor(R.color.bule));
				}
			return convertView;
		}

	}
	
	//下拉刷新时，获取交互服务器上最新通知，不过本地不存在则显示
	private class RefreshMessagesAllThread extends Thread {
		public boolean flag = false;

		public void run() {
			super.run();
			System.out.println("RefreshMessagesThread更新消息");
			flag = true;
			try {
				String url = AppContex.GET_ALL_MESSAGES_API
						+ "client=android&deviceid="
						+ Uri.encode(SysUtils
								.getDeviceID(MessagesActivity2.this));
				SysUtils.log(url);
				HttpGet httpGet = new HttpGet(url);
				HttpResponse httpResponse = new DefaultHttpClient()
						.execute(httpGet);
				if (!flag) {
					return;
				}
				
				
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					try {
						String result = EntityUtils.toString(httpResponse
								.getEntity());
						SysUtils.log("新消息反馈 = " + result);
						JSONArray jsonArray = new JSONArray(result);
						DBUtil dbUtil = new DBUtil(MessagesActivity2.this);
						for (int i = 0; i < jsonArray.length(); i++) {
							MessageInfo messageInfo = new MessageInfo();
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							messageInfo.content = jsonObject
									.getString("content");
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

							if (jsonObject.has("alertlevel")
									&& !jsonObject.isNull("alertlevel")) {
								messageInfo.alertlevel = jsonObject
										.getInt("alertlevel");
							} else {
								messageInfo.alertlevel = -1;
							}

							if (jsonObject.has("alerttype")
									&& !jsonObject.isNull("alerttype")) {
								messageInfo.alerttype = jsonObject
										.getInt("alerttype");
							} else {
								messageInfo.alerttype = -1;
							}

							if (jsonObject.has("saygood")
									&& !jsonObject.isNull("saygood")) {
								messageInfo.saygood = jsonObject
										.getInt("saygood");
							} else {
								messageInfo.saygood = -1;
							}
							//在免打扰时间内
							if (messageInfo.type == MessageInfo.MESSAGE_TYPE_NORMAL) {
								if (checkIfInFreetime()) {
									SysUtils.log("在免打扰时间内");
									continue;
								}

							}
							//不再大约范围
							if(!getFilterResult(messageInfo.fcatid, messageInfo.scatid))
							{
								System.out.println("消息被过滤掉");
								return;
							}
							//如果不存在数据库中，则插入
							if (!dbUtil.isMessageAlreadyExist(
									messageInfo.serversqlid,
									MessagesActivity2.this)) {
								dbUtil.insertNewMessageInfo(messageInfo);							
							}
							//如果存在，则更新已读状态
							if (dbUtil.isMessageAlreadyExist(
									messageInfo.serversqlid,
									MessagesActivity2.this)) {
								dbUtil.checkReadState(messageInfo,
										MessagesActivity2.this);
							}

							
						}

					} catch (Exception e) {
					}
					//全部刷新，重新排列
					handler.obtainMessage(MESSAGE_ON_REFRESHED_DB_ALL).sendToTarget();
					progressDialog.dismiss();

				} else {
					progressDialog.dismiss();
					handler.obtainMessage(MESSAGE_TYPE_SHOW_RAW_MSG, "网络未连接  请您检查网络设置")
							.sendToTarget();
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
				progressDialog.dismiss();
				handler.obtainMessage(MESSAGE_TYPE_SHOW_RAW_MSG, "网络未连接  请您检查网络设置")
						.sendToTarget();
				return;
			}
		}
	}
    ///显示预警图片
	public void setAlertTypeImage(int messagesAlertType,
			int messagesAlertLevel, ImageView alertImage) {
		// TODO Auto-generated method stub
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds=true; 
		int sampleSize = 8;
		options.inJustDecodeBounds = false;
		options.inSampleSize = sampleSize;	    		
		
		try {

			if (messagesAlertType == 0 && messagesAlertLevel == 2) {
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert0_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 0 && messagesAlertLevel == 3) {				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert0_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 1 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert1_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 1 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert1_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 10 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert10_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 10 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert10_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 10 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert10_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 11 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert11_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 11 && messagesAlertLevel == 1) {
				alertImage.setBackgroundResource(R.drawable.alert11_1);
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert11_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 11 && messagesAlertLevel == 2) {				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert11_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 11 && messagesAlertLevel == 3) {				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert11_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 12 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert12_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 12 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert12_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 12 && messagesAlertLevel == 2) {				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert12_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 12 && messagesAlertLevel == 3) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert12_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 13 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert13_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 13 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert13_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 13 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert13_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 13 && messagesAlertLevel == 3) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert13_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 14 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert14_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 14 && messagesAlertLevel == 1) {				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert14_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 14 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert14_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 14 && messagesAlertLevel == 3) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(),R.drawable.alert14_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}
			
			if (messagesAlertType == 15 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert15_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 15 && messagesAlertLevel == 1) {				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert15_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 15 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert15_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 15 && messagesAlertLevel == 3) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(),R.drawable.alert15_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 2 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert2_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 2 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert2_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 2 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert2_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}
			
			if (messagesAlertType == 3 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert3_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 3 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert3_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 3 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert3_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 4 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert4_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 4 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert4_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 4 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert4_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 5 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert5_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 5 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert5_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 5 && messagesAlertLevel == 3) {
				alertImage.setBackgroundResource(R.drawable.alert5_3);
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert5_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 6 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert6_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 6 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert6_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 6 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert6_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 7 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(),R.drawable.alert7_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 7 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(),R.drawable.alert7_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 7 && messagesAlertLevel == 2) {				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert7_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 8 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert8_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 8 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert8_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 9 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert9_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 9 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(),R.drawable.alert9_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 9 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert9_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 9 && messagesAlertLevel == 3) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(MessagesActivity2.this.getResources(), R.drawable.alert9_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}
			alertImage.setVisibility(View.VISIBLE);

		} catch (Exception e) {
			e.printStackTrace();
			alertImage.setVisibility(View.GONE);
		}

	}
	
	//获得预警图片int_id
	public int sendIntentResouce(int messagesAlertType, int messagesAlertLevel) {
		// TODO Auto-generated method stub

		if (messagesAlertType == 0 && messagesAlertLevel == 2) {
			return R.drawable.alert0_2;
		}

		if (messagesAlertType == 0 && messagesAlertLevel == 3) {
			return R.drawable.alert0_3;
		}

		if (messagesAlertType == 1 && messagesAlertLevel == 1) {
			return R.drawable.alert1_1;
		}

		if (messagesAlertType == 1 && messagesAlertLevel == 2) {
			return R.drawable.alert1_2;
		}

		if (messagesAlertType == 10 && messagesAlertLevel == 0) {
			return R.drawable.alert10_0;
		}

		if (messagesAlertType == 10 && messagesAlertLevel == 1) {
			return R.drawable.alert10_1;
		}

		if (messagesAlertType == 10 && messagesAlertLevel == 2) {
			return R.drawable.alert10_2;
		}

		if (messagesAlertType == 11 && messagesAlertLevel == 0) {
			return R.drawable.alert11_0;
		}

		if (messagesAlertType == 11 && messagesAlertLevel == 1) {
			return R.drawable.alert11_1;
		}

		if (messagesAlertType == 11 && messagesAlertLevel == 2) {
			return R.drawable.alert11_2;
		}

		if (messagesAlertType == 11 && messagesAlertLevel == 3) {
			return R.drawable.alert11_3;
		}

		if (messagesAlertType == 12 && messagesAlertLevel == 0) {
			return R.drawable.alert12_0;
		}

		if (messagesAlertType == 12 && messagesAlertLevel == 1) {
			return R.drawable.alert12_1;
		}

		if (messagesAlertType == 12 && messagesAlertLevel == 2) {
			return R.drawable.alert12_2;
		}

		if (messagesAlertType == 12 && messagesAlertLevel == 3) {
			return R.drawable.alert12_3;
		}

		if (messagesAlertType == 13 && messagesAlertLevel == 0) {
			return R.drawable.alert13_0;
		}

		if (messagesAlertType == 13 && messagesAlertLevel == 1) {
			return R.drawable.alert13_1;
		}

		if (messagesAlertType == 13 && messagesAlertLevel == 2) {
			return R.drawable.alert13_2;
		}

		if (messagesAlertType == 13 && messagesAlertLevel == 3) {
			return R.drawable.alert13_3;
		}

		if (messagesAlertType == 14 && messagesAlertLevel == 0) {
			return R.drawable.alert14_0;
		}

		if (messagesAlertType == 14 && messagesAlertLevel == 1) {
			return R.drawable.alert14_1;
		}

		if (messagesAlertType == 14 && messagesAlertLevel == 2) {
			return R.drawable.alert14_2;
		}

		if (messagesAlertType == 14 && messagesAlertLevel == 3) {
			return R.drawable.alert14_3;
		}
		if (messagesAlertType == 15 && messagesAlertLevel == 0) {
			return R.drawable.alert15_0;
		}

		if (messagesAlertType == 15 && messagesAlertLevel == 1) {
			return R.drawable.alert15_1;
		}

		if (messagesAlertType == 15 && messagesAlertLevel == 2) {
			return R.drawable.alert15_2;
		}

		if (messagesAlertType == 15 && messagesAlertLevel == 3) {
			return R.drawable.alert15_3;
		}
		if (messagesAlertType == 2 && messagesAlertLevel == 0) {
			return R.drawable.alert2_0;
		}

		if (messagesAlertType == 2 && messagesAlertLevel == 1) {
			return R.drawable.alert2_1;
		}

		if (messagesAlertType == 2 && messagesAlertLevel == 2) {
			return R.drawable.alert2_2;
		}
		if (messagesAlertType == 3 && messagesAlertLevel == 0) {
			return R.drawable.alert3_1;
		}
		if (messagesAlertType == 3 && messagesAlertLevel == 1) {
			return R.drawable.alert3_0;
		}

		if (messagesAlertType == 3 && messagesAlertLevel == 2) {
			return R.drawable.alert3_2;
		}

		if (messagesAlertType == 4 && messagesAlertLevel == 0) {
			return R.drawable.alert4_0;
		}

		if (messagesAlertType == 4 && messagesAlertLevel == 1) {
			return R.drawable.alert4_1;
		}

		if (messagesAlertType == 4 && messagesAlertLevel == 2) {
			return R.drawable.alert4_2;
		}

		if (messagesAlertType == 5 && messagesAlertLevel == 1) {
			return R.drawable.alert5_1;
		}

		if (messagesAlertType == 5 && messagesAlertLevel == 2) {
			return R.drawable.alert5_2;
		}

		if (messagesAlertType == 5 && messagesAlertLevel == 3) {
			return R.drawable.alert5_3;
		}

		if (messagesAlertType == 6 && messagesAlertLevel == 0) {
			return R.drawable.alert6_0;
		}

		if (messagesAlertType == 6 && messagesAlertLevel == 1) {
			return R.drawable.alert6_1;
		}

		if (messagesAlertType == 6 && messagesAlertLevel == 2) {
			return R.drawable.alert6_2;
		}

		if (messagesAlertType == 7 && messagesAlertLevel == 0) {
			return R.drawable.alert7_0;
		}

		if (messagesAlertType == 7 && messagesAlertLevel == 1) {
			return R.drawable.alert7_1;
		}

		if (messagesAlertType == 7 && messagesAlertLevel == 2) {
			return R.drawable.alert7_2;
		}

		if (messagesAlertType == 8 && messagesAlertLevel == 0) {
			return R.drawable.alert8_0;
		}

		if (messagesAlertType == 8 && messagesAlertLevel == 1) {
			return R.drawable.alert8_1;
		}

		if (messagesAlertType == 9 && messagesAlertLevel == 0) {
			return R.drawable.alert9_0;
		}

		if (messagesAlertType == 9 && messagesAlertLevel == 1) {
			return R.drawable.alert9_1;
		}

		if (messagesAlertType == 9 && messagesAlertLevel == 2) {
			return R.drawable.alert9_2;
		}

		if (messagesAlertType == 9 && messagesAlertLevel == 3) {
			return R.drawable.alert9_3;
		}
		return -1;
	}

	//是否在免打扰时间内
	private boolean checkIfInFreetime() {
		SharedPreferences sharedPreferences = getSharedPreferences("pref",
				Context.MODE_PRIVATE);
		int fromhour = sharedPreferences.getInt(
				SetFreeTimeActivity.KEY_FREETIME_FROM_HOUR, 22);
		int fromminute = sharedPreferences.getInt(
				SetFreeTimeActivity.KEY_FREETIME_FROM_MINUTE, 0);
		int tohour = sharedPreferences.getInt(
				SetFreeTimeActivity.KEY_FREETIME_TO_HOUR, 8);
		int tominute = sharedPreferences.getInt(
				SetFreeTimeActivity.KEY_FREETIME_TO_MINUTE, 0);
		Calendar calendar = Calendar.getInstance();
		int nowhour = calendar.get(Calendar.HOUR_OF_DAY);
		int nowminute = calendar.get(Calendar.MINUTE);
		int tominuteinall = tohour * 60 + tominute;
		int fromminuteinall = fromhour * 60 + fromminute;
		int nowminuteinall = nowhour * 60 + nowminute;

		if (tominuteinall >= fromminuteinall) {
			if (nowminuteinall >= fromminuteinall
					&& nowminuteinall <= tominuteinall) {
				return true;
			}
		} else {
			if (nowminuteinall >= fromminuteinall
					|| nowminuteinall <= tominuteinall) {
				return true;
			}
		}

		return false;
	}

	private class GetAllCatsAndNewMessageThread extends Thread {
		public boolean flag = false;

		private String allCatsString = null;
		
		Editor firsteditor = firstSharedPreferences.edit();
		public void run() {
			super.run();
			flag = true;
			
			SharedPreferences sharedPreferences = getSharedPreferences("pref",
					Context.MODE_PRIVATE);
			// ///////////////////////////////
			long begintime = 0;
			long endtime = 0;
			
			//如果第一次进入应用，取得DIVICEID API 
			if(is_firstApp){
				try {
					
					// //////////获取mqtt的地址
					String url = AppContex.SEND_DEVICE_INFO_API
							+ "client=android&deviceid="
							+ Uri.encode(SysUtils
									.getDeviceID(MessagesActivity2.this))
							+ "&model="
							+ Uri.encode("MODEL:" + android.os.Build.MODEL
									+ ",DISPLAY:" + android.os.Build.DISPLAY);

					System.out.println("DEVICE_INFO_API:  " + url);
					SharedPreferences sharedPreferences2 = MessagesActivity2.this
							.getSharedPreferences("MQTTADRESS",
									Context.MODE_PRIVATE);
					Editor editor = sharedPreferences2.edit();
					begintime = System.currentTimeMillis();
					
					HttpGet httpGet = new HttpGet(url);
					HttpResponse httpResponse = new DefaultHttpClient()
							.execute(httpGet);
					
					endtime = System.currentTimeMillis();
					System.out.println("发送设备号耗时 = "+(endtime-begintime));
					
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						String jsonString = EntityUtils.toString(httpResponse
								.getEntity());
						JSONObject jsonObject = new JSONObject(jsonString);
						String ipString = jsonObject.getString("ip");
						System.out.println("ipString:  "+ipString);
						AppContex.MQTT_ADDRESS = String.format("tcp://%s:1883",
								ipString);
						System.out.println("AppContex.MQTT_ADDRESS = "
								+ AppContex.MQTT_ADDRESS);
						editor.putString("MQTT_ADDRESS",
								String.format("tcp://%s:1883", ipString));
						
						editor.commit();

					}else{
						System.out.println("NETWORK IS NOT CONNECTIONG");
					}
				} catch (Exception e) {
					e.printStackTrace();
					progressMainDialog.dismiss();
					handler.obtainMessage(MESSAGE_TYPE_SHOW_RAW_MSG, "网络未连接  请您检查网络设置")
							.sendToTarget();
					return;
				}
			}	
			///三个小时取目录接口
			update_cat = upcatSharedPreferences.getBoolean("up_cat_flag", true);
			System.out.println("当前update_cat:  "+String.valueOf(update_cat));
			if(update_cat){
				System.out.println("GET ALLCAPAPI:-----");
				try {
					// /////					
					String url = AppContex.GET_ALL_CATS_API
							+ "deviceid="
							+ Uri.encode(SysUtils
									.getDeviceID(MessagesActivity2.this));
					SysUtils.log(url);
					begintime = System.currentTimeMillis();
					HttpGet httpGet = new HttpGet(url);
					HttpResponse httpResponse = new DefaultHttpClient()
							.execute(httpGet);
					endtime = System.currentTimeMillis();
					System.out.println("目录耗时 = "+(endtime-begintime));
					if (!flag) {
						return;
					}
					SysUtils.log("httpResponse.getStatusLine().getStatusCode() = "
							+ httpResponse.getStatusLine().getStatusCode());
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						allCatsString = EntityUtils.toString(httpResponse
								.getEntity());
						SysUtils.saveOfflineCatsFile(allCatsString);
						SysUtils.log("allCatsString" + allCatsString);
						Editor editor2 = sharedPreferences.edit();
						editor2.putString("key_pref_cats", allCatsString);
						editor2.commit();
						// ///初始化所有cat
						JSONArray jsonArray = new JSONArray(allCatsString);
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject fcatJsonObject = jsonArray.getJSONObject(i);
							FirstCatalogue firstCatalogue = new FirstCatalogue();
							firstCatalogue.fcatid = fcatJsonObject.getInt("fcatid");
							firstCatalogue.title = fcatJsonObject
									.getString("title");
							firstCatalogue.fcatsubnum = fcatJsonObject
									.getInt("fcatsubnum");
							firstCatalogue.fcatcurmonthmsg = fcatJsonObject
									.getInt("fcatcurmonthmsg");
							firstCatalogue.fcatallmsgnum = fcatJsonObject
									.getInt("fcatallmsgnum");

							firstCatalogue.isChoosed = true;
							JSONArray scatJsonArray = fcatJsonObject
									.getJSONArray("scatlist");
							for (int j = 0; j < scatJsonArray.length(); j++) {
								JSONObject scatJsonObject = scatJsonArray
										.getJSONObject(j);
								SecondCatalogue secondCatalogue = new SecondCatalogue();
								secondCatalogue.fcatid = firstCatalogue.fcatid;
								secondCatalogue.scatid = scatJsonObject
										.getInt("scatid");
								secondCatalogue.title = scatJsonObject
										.getString("title");
								secondCatalogue.scatsubmun = scatJsonObject
										.getInt("scatsubmun");							
								secondCatalogue.scatcurmonthmsgnum = scatJsonObject
										.getInt("scatcurmonthmsgnum");
								secondCatalogue.scatcallmsgnum = scatJsonObject
										.getInt("scatcallmsgnum");
								try {
									secondCatalogue.type = scatJsonObject
											.getInt("type");
								} catch (Exception e) {
									e.printStackTrace();
								}
								firstCatalogue.secondCatalogues
										.add(secondCatalogue);
							}
							//二级目录排序，紧急信息提前
							AppContex.catalogues.add(firstCatalogue);
							try {
								Comparator<SecondCatalogue> comparator = new Comparator<SecondCatalogue>() {
									public int compare(SecondCatalogue lhs,
											SecondCatalogue rhs) {

										return rhs.type - lhs.type;
									}
								};
								Collections.sort(firstCatalogue.secondCatalogues,
										comparator);
							} catch (Exception e) {
								e.printStackTrace();
							}

						}

						if (is_firstApp) {
							//将预警类型、与系统公告订阅设置为true
							SysUtils.setFirstFilterToContext(MessagesActivity2.this);
							//将预警类型、与系统公告订阅存入本地
							SysUtils.saveFilterToPref(MessagesActivity2.this);	
							//新安装的用户直接不显示is_sencondAPP
							secondEditor.putBoolean("is_second_db", false);
							secondEditor.commit();
						}else if(is_sencondAPP){
							//将预警类型、与系统公告订阅设置为true
							SysUtils.setFirstFilterToContext(MessagesActivity2.this);
							//将预警类型、与系统公告订阅存入本地
							SysUtils.saveFilterToPref(MessagesActivity2.this);	
							secondEditor.putBoolean("is_second_db", false);
							secondEditor.commit();
							handler.obtainMessage(MESSAGE_ON_REFRESHED_DB).sendToTarget();
						}else {
							//取得订阅情况
							SysUtils.setFilterToContext(MessagesActivity2.this);
							handler.obtainMessage(MESSAGE_ON_REFRESHED_DB).sendToTarget();
						}
						
						long liuliangL2 = TrafficStats.getTotalRxBytes();
						System.out.println("获取目录后网络数据： "+liuliangL2);
						
					} else {
						handler.obtainMessage(MESSAGE_TYPE_SHOW_RAW_MSG, "网络未连接  请您检查网络设置")
								.sendToTarget();
						progressMainDialog.dismiss();
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
					progressMainDialog.dismiss();
					handler.obtainMessage(MESSAGE_TYPE_SHOW_RAW_MSG, "网络未连接  请您检查网络设置")
							.sendToTarget();

					return;
				}
			}else{
				//3个小时内，取本地目录数据
				System.out.println("走本地目录");				
				if (SysUtils.getOfflineCatsFile() != null) {
					try {
						JSONArray jsonArray = new JSONArray(
								SysUtils.getOfflineCatsFile());

						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject fcatJsonObject = jsonArray.getJSONObject(i);
							FirstCatalogue firstCatalogue = new FirstCatalogue();
							firstCatalogue.fcatid = fcatJsonObject.getInt("fcatid");
							firstCatalogue.title = fcatJsonObject
									.getString("title");
							firstCatalogue.fcatsubnum = fcatJsonObject
									.getInt("fcatsubnum");
							firstCatalogue.fcatcurmonthmsg = fcatJsonObject
									.getInt("fcatcurmonthmsg");
							firstCatalogue.fcatallmsgnum = fcatJsonObject
									.getInt("fcatallmsgnum");

							firstCatalogue.isChoosed = true;
							JSONArray scatJsonArray = fcatJsonObject
									.getJSONArray("scatlist");
							for (int j = 0; j < scatJsonArray.length(); j++) {
								JSONObject scatJsonObject = scatJsonArray
										.getJSONObject(j);
								SecondCatalogue secondCatalogue = new SecondCatalogue();
								secondCatalogue.fcatid = firstCatalogue.fcatid;
								secondCatalogue.scatid = scatJsonObject
										.getInt("scatid");
								secondCatalogue.title = scatJsonObject
										.getString("title");
								secondCatalogue.scatsubmun = scatJsonObject
										.getInt("scatsubmun");							
								secondCatalogue.scatcurmonthmsgnum = scatJsonObject
										.getInt("scatcurmonthmsgnum");
								secondCatalogue.scatcallmsgnum = scatJsonObject
										.getInt("scatcallmsgnum");
								try {
									secondCatalogue.type = scatJsonObject
											.getInt("type");
								} catch (Exception e) {
									e.printStackTrace();
								}
								firstCatalogue.secondCatalogues
										.add(secondCatalogue);
							}
							AppContex.catalogues.add(firstCatalogue);
							try {
								Comparator<SecondCatalogue> comparator = new Comparator<SecondCatalogue>() {
									public int compare(SecondCatalogue lhs,
											SecondCatalogue rhs) {

										return rhs.type - lhs.type;
									}
								};
								Collections.sort(firstCatalogue.secondCatalogues,
										comparator);
							} catch (Exception e) {
								e.printStackTrace();
							}

						}
						SysUtils.setFilterToContext(MessagesActivity2.this);
					} catch (Exception e) {
					}
				}
				progressMainDialog.dismiss();
				handler.obtainMessage(MESSAGE_GETMESS_OVER).sendToTarget();
			}
		
			//取10条最新通知
			if(is_firstApp){
				try {
					String url = AppContex.GET_ALL_MESSAGES_API
							+ "client=android&deviceid="
							+ Uri.encode(SysUtils
									.getDeviceID(MessagesActivity2.this));
					SysUtils.log("MESSAGEURL:" + url);
					begintime = System.currentTimeMillis();
					HttpGet httpGet = new HttpGet(url);
					HttpResponse httpResponse = new DefaultHttpClient()
							.execute(httpGet);
					endtime = System.currentTimeMillis();
					System.out.println("获取40条耗时 = "+(endtime-begintime));
					if (!flag) {
						return;
					}
					SysUtils.log("httpResponse.getStatusLine().getStatusCode()="
							+ httpResponse.getStatusLine().getStatusCode());
					if (httpResponse.getStatusLine().getStatusCode() == 200) {

						try {
							String result = EntityUtils.toString(httpResponse
									.getEntity());
							SysUtils.log("MESSAGERESULT:  " + result);
							JSONArray jsonArray = new JSONArray(result);
							DBUtil dbUtil = new DBUtil(MessagesActivity2.this);
							for (int i = 0; i < jsonArray.length(); i++) {
								MessageInfo messageInfo = new MessageInfo();
								JSONObject jsonObject = jsonArray.getJSONObject(i);
								messageInfo.content = jsonObject
										.getString("content");
								messageInfo.day = jsonObject.getInt("day");
								messageInfo.fcatid = jsonObject.getInt("fcatid");
								messageInfo.hour = jsonObject.getInt("hour");
								messageInfo.min = jsonObject.getInt("min");
								messageInfo.month = jsonObject.getInt("month");
								messageInfo.readState = MessageInfo.READ_STATE_UNREAD;
								messageInfo.scatid = jsonObject.getInt("scatid");
								messageInfo.serversqlid = jsonObject.getInt("id");
								messageInfo.title = jsonObject.getString("title");
								System.out.println("MessageTitle:   "
										+ messageInfo.alerttype);
								messageInfo.year = jsonObject.getInt("year");
								messageInfo.type = jsonObject.getInt("type");

								if (jsonObject.has("alertlevel")
										&& !jsonObject.isNull("alertlevel")) {
									messageInfo.alertlevel = jsonObject
											.getInt("alertlevel");
								} else {
									messageInfo.alertlevel = -1;
								}

								if (jsonObject.has("alerttype")
										&& !jsonObject.isNull("alerttype")) {
									messageInfo.alerttype = jsonObject
											.getInt("alerttype");
								} else {
									messageInfo.alerttype = -1;
								}

								if (jsonObject.has("saygood")
										&& !jsonObject.isNull("saygood")) {
									messageInfo.saygood = jsonObject
											.getInt("saygood");
								} else {
									messageInfo.saygood = -1;
								}							

								if (!dbUtil.isMessageAlreadyExist(
										messageInfo.serversqlid,
										MessagesActivity2.this)) {
									dbUtil.insertNewMessageInfo(messageInfo);
									System.out.println("插入成功");
								}

							}
							firsteditor.putBoolean("is_frist_db", false);
							firsteditor.commit();
							progressMainDialog.dismiss();
							
							handler.obtainMessage(MESSAGE_GETMESS_OVER)
									.sendToTarget();
						} catch (Exception e) {
						}

					} else {
						progressMainDialog.dismiss();
						handler.obtainMessage(MESSAGE_TYPE_SHOW_RAW_MSG, "网络未连接  请您检查网络设置")
								.sendToTarget();
					}
				} catch (Exception e) {
					e.printStackTrace();
					progressMainDialog.dismiss();
					handler.obtainMessage(MESSAGE_TYPE_SHOW_RAW_MSG, "网络未连接  请您检查网络设置")
							.sendToTarget();
				}
			}
			long liuliangL = TrafficStats.getTotalRxBytes();
			System.out.println("主线程网络数据： "+liuliangL);
			//每次都检查网上是否有新版本
			new CheckNewAppThread().start();
			System.out.println("start checkappthread");		
			//3个小时内，不取目录接口
			update_cat = false;
			upcatEditor.putBoolean("up_cat_flag", false);
			upcatEditor.commit();
			System.out.println("已存入本地目录flag：  "  + String.valueOf(update_cat));
			//第一次线束升级日志
//			if(showupdatedalog){
//				handler.obtainMessage(SHOW_UPDATEDAILOG).sendToTarget();
//				System.out.println("显示升级日志");
//			}
			//启动mqtt服务
			startService(new Intent(MessagesActivity2.this,
					PushService.class));
		}
	}

	/**
	 * 下拉刷新
	 */
	public void onRefresh() {
		
		RefreshMessagesAllThread refreshMessagesThreadall = new RefreshMessagesAllThread();
		refreshMessagesThreadall.start();
	}
	/*
	 * (non-Javadoc)
	 * @see com.mine.pulltorefresh.DragListView.OnRefreshLoadingMoreListener#onLoadMore()
	 * 查看更多
	 */
	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		handler.obtainMessage(MESSAGE_LOAD_MORE).sendToTarget();
	}
	
	
	class AllReadThread extends Thread{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			DBUtil readDBUtil = new  DBUtil(MessagesActivity2.this);
			srcInfos = readDBUtil.getAllMessageInfos(MessagesActivity2.this);
			for(MessageInfo messageInfo:srcInfos){
				messageInfo.readState = MessageInfo.READ_STATE_READED;
				readDBUtil.updateMessageInfo(messageInfo);
				NotificationManager notificationManager = (NotificationManager) MessagesActivity2.this.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.cancel(messageInfo.serversqlid);
			}
			
			handler.obtainMessage(MESSAGE_ON_REFRESHED_DB_ALL).sendToTarget();
		}
		
	}
	

		
		
		 private BroadcastReceiver broadcastReceiver2 = new BroadcastReceiver(){

				@Override
				public void onReceive(Context arg0, Intent intent) {
					// TODO Auto-generated method stub
					String action = intent.getAction();
					//获得更新已读通知ID
					int serversqlid = intent.getExtras().getInt(UPDATE_MESSAGESQLID);					
					MessageInfo currentMessageInfo = new DBUtil(MessagesActivity2.this).getMessageAlready(serversqlid, MessagesActivity2.this);
					updateMessageInfo = currentMessageInfo;
					if(action.equals(getRefreshReadStateID)){
						//先删除之前发送的消息，延迟1秒刷新listview ，不删除之前的消息通知，会引起数据不匹配，造成闪退
						handler.removeMessages(MESSAGE_ON_REFRESHED_DB);
						handler.sendEmptyMessageDelayed(MESSAGE_ON_REFRESHED_DB, 1000);				
					
					}
				}
				
			};
			
			
			private class CheckNewAppThread extends Thread
			{
				public boolean flag = false;
				
				public void run() {
					super.run();
					flag = true;
					try {
						String url = AppContex.CHECK_NEW_APP_API+"deviceid="+Uri.encode(SysUtils.getDeviceID(MessagesActivity2.this))+"&client=android&appver="+AppContex.APP_VER;
						SysUtils.log(url);
		    			HttpGet httpGet = new HttpGet(url);
						HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
						if(!flag)
						{
							return;
						}
						System.out.println("httpResponse.getStatusLine().getStatusCode() = "+httpResponse.getStatusLine().getStatusCode());
						if(httpResponse.getStatusLine().getStatusCode() == 200)
						{
							progressDialog.dismiss();
							String result = EntityUtils.toString(httpResponse.getEntity());
							SysUtils.log("check更新result = "+result);
							try {
								JSONObject jsonObject = new JSONObject(result);
								int appnew = jsonObject.getInt("appnew");
								if(appnew == 1)
								{
									newappurl = jsonObject.getString("url");
									if(newappurl == null || !newappurl.startsWith("http"))
									{
										System.out.println("当前已经是最新版本");
										return;
									}
									handler.obtainMessage(MESSAGE_HAS_NEW_APP).sendToTarget();
								}else {
									
									System.out.println("当前已经是最新版本");
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							
						}else {
							progressDialog.dismiss();
							System.out.println("update unsuccess");
						}
					} catch (Exception e) {
						e.printStackTrace();
						progressDialog.dismiss();
						System.out.println("update unsuccess");
					}
				}
			}
			
		public  BroadcastReceiver controlBroadcastReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if(action.equals(CONTROL_CHANGE)){					
					handler.removeMessages(MESSAGE_ON_REFRESHED_DB_ALL);					
					handler.sendEmptyMessageDelayed(MESSAGE_ON_REFRESHED_DB_ALL, 1000);
					System.out.println("已接收管理广播");
				}
			}
			
		};
		
		public  BroadcastReceiver deleteBroadcastReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if(action.equals(DELETE_SINGLE)){
					Message message = new Message();
					message.what = MESSAGE_ON_REFRESHED_DB;
					handler.handleMessage(message);					
					System.out.println("已接收删除广播");
				}
			}
			
		};
		
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
				
			} catch (Exception e) {
			}
			
			System.out.println("filterresult:  "+filterresult);
			return filterresult;
		}		
		
		 static public class UpCatBroadCastReceiver extends BroadcastReceiver {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
					MessagesActivity2.update_cat = true;
					upcatEditor.putBoolean("up_cat_flag", true);	
					upcatEditor.commit();
					System.out.println("broadcast_update_cat :  "+String.valueOf(MessagesActivity2.update_cat));

			}

		}

}
