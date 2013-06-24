package com.mine.beijingserv.ui;

import com.mine.beijingserv.R;
import com.mine.beijingserv.R.layout;
import com.mine.beijingserv.sys.AppContex;
import com.mine.beijingserv.sys.SysUtils;

import android.app.Activity;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;

public class TabsMain extends TabActivity implements OnTabChangeListener
{
	public static final int TAB_MESSAGE_INDEX = 0;
	public static final int TAB_INTERACTIVE_INDEX = 1;
	public static final int TAB_APPS_INDEX = 2;
	public static final int TAB_ID_SEARCHACTIVE = 3;
	public static final int TAB_SETTINGS_INDEX = 4;

	
	private final String TAB_ID_MESSAGE = "message";
	private final String TAB_ID_DEMAND = "searchactive";
	private final String TAB_ID_INTERACTIVE = "interactive";
	private final String TAB_ID_APPS = "apps";
	private final String TAB_ID_SETTINGS = "settings";
	
	public static final String TAB_KEY_SET_BADGE = "setbadge";
	public static final String TAB_INTENT_SET_BADGE = "com.mine.tab.setbadge";
	
	public static final String TAB_INTENT_SET_TAB_TO_MESSAGE_LIST = "com.mine.tab.settabtolist";
	
	private final int TAB_TEXT_COLOR_ON = 0xffffffff;
	private final int TAB_TEXT_COLOR_OFF = 0xff999999;
	
	private TabHost tabHost = null;
	private BadgeView badgeView = null;
	private boolean hasReceverUnRegistered = false;
	//未读通知数字广播
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			int badge = intent.getIntExtra(TAB_KEY_SET_BADGE, 0);
			SysUtils.log("set badge = "+badge);
			if(badge <=0)
			{
				badgeView.setVisibility(View.GONE);
			}else {
				
				badgeView.setVisibility(View.VISIBLE);
				if(badge>99){
					badgeView.setTextSize(8);
					badgeView.setText("99+");
				}
				badgeView.setText(""+badge);
			}
			badgeView.invalidate();
		}
	};
	//将通知也作为首页
	private BroadcastReceiver broadcastReceiver2 = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			tabHost.setCurrentTab(0);
		}
	};
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabsmain);
		tabHost = getTabHost();
		//添加各种页面到tabHost
		View tab1 = View.inflate(this, R.layout.tabs_tab, null);
		((ImageView)tab1.findViewById(R.id.tabs_tab_img)).setImageResource(R.drawable.tab_message_on);
		((TextView)tab1.findViewById(R.id.tabs_tab_text)).setText("通知");
		((TextView)tab1.findViewById(R.id.tabs_tab_text)).setTextColor(TAB_TEXT_COLOR_ON);
		badgeView = (BadgeView)tab1.findViewById(R.id.tabs_badge);
		badgeView.setVisibility(View.GONE);
		tab1.findViewById(R.id.tabs_tab_on).setVisibility(View.VISIBLE);
		
        TabHost.TabSpec messageTabSpec = tabHost.newTabSpec(TAB_ID_MESSAGE)
        		.setIndicator(tab1)
        		.setContent(new Intent(this,MessagesActivity2.class));
        tabHost.addTab(messageTabSpec);
        

        
        View tab2 = View.inflate(this, R.layout.tabs_tab, null);
		((ImageView)tab2.findViewById(R.id.tabs_tab_img)).setImageResource(R.drawable.tab_interactive_off);
		((TextView)tab2.findViewById(R.id.tabs_tab_text)).setText("互动");
		((TextView)tab2.findViewById(R.id.tabs_tab_text)).setTextColor(TAB_TEXT_COLOR_OFF);
		tab2.findViewById(R.id.tabs_tab_on).setVisibility(View.INVISIBLE);
		tab2.findViewById(R.id.tabs_badge).setVisibility(View.INVISIBLE);
        TabHost.TabSpec catTabSpec = tabHost.newTabSpec(TAB_ID_INTERACTIVE)
        		.setIndicator(tab2)
        		.setContent(new Intent(this,TabInteractive.class));
        tabHost.addTab(catTabSpec);
        
        View tab3 = View.inflate(this, R.layout.tabs_tab, null);
		((ImageView)tab3.findViewById(R.id.tabs_tab_img)).setImageResource(R.drawable.tab_catologue_off);
		((TextView)tab3.findViewById(R.id.tabs_tab_text)).setText("应用");
		((TextView)tab3.findViewById(R.id.tabs_tab_text)).setTextColor(TAB_TEXT_COLOR_OFF);
		tab3.findViewById(R.id.tabs_tab_on).setVisibility(View.INVISIBLE);
		tab3.findViewById(R.id.tabs_badge).setVisibility(View.INVISIBLE);
        TabHost.TabSpec appsTabSpec = tabHost.newTabSpec(TAB_ID_APPS)
        		.setIndicator(tab3)
        		.setContent(new Intent(this,TabApps.class));
        tabHost.addTab(appsTabSpec);
        
        View tab4 = View.inflate(this, R.layout.tabs_tab, null);
		((ImageView)tab4.findViewById(R.id.tabs_tab_img)).setImageResource(R.drawable.search_icon_off);
		((TextView)tab4.findViewById(R.id.tabs_tab_text)).setText("查询");
		((TextView)tab4.findViewById(R.id.tabs_tab_text)).setTextColor(TAB_TEXT_COLOR_OFF);
		tab4.findViewById(R.id.tabs_tab_on).setVisibility(View.INVISIBLE);
		tab4.findViewById(R.id.tabs_badge).setVisibility(View.INVISIBLE);
        TabHost.TabSpec serchTabSpec = tabHost.newTabSpec(TAB_ID_DEMAND)
        		.setIndicator(tab4)
        		.setContent(new Intent(this,TabSercherActive2.class));
        tabHost.addTab(serchTabSpec);
        
        View tab5 = View.inflate(this, R.layout.tabs_tab, null);
		((ImageView)tab5.findViewById(R.id.tabs_tab_img)).setImageResource(R.drawable.tab_settings_off);
		((TextView)tab5.findViewById(R.id.tabs_tab_text)).setText("设置");
		((TextView)tab5.findViewById(R.id.tabs_tab_text)).setTextColor(TAB_TEXT_COLOR_OFF);
		tab5.findViewById(R.id.tabs_tab_on).setVisibility(View.INVISIBLE);
		tab5.findViewById(R.id.tabs_badge).setVisibility(View.INVISIBLE);
        TabHost.TabSpec settingsTabSpec = tabHost.newTabSpec(TAB_ID_SETTINGS)
        		.setIndicator(tab5)
        		.setContent(new Intent(this,SettingsActivity.class));
        tabHost.addTab(settingsTabSpec);
        
        tabHost.setCurrentTab(0);
        tabHost.setOnTabChangedListener(this); 
        
      
        registerReceiver(broadcastReceiver, new IntentFilter(TAB_INTENT_SET_BADGE));
        registerReceiver(broadcastReceiver2, new IntentFilter(TAB_INTENT_SET_TAB_TO_MESSAGE_LIST));
        
        AppContex.activities.add(this);
	}
	
	@Override
	public void finish() {
		super.finish();
		if(!hasReceverUnRegistered)
		{
			System.out.println("#################");
			unregisterReceiver(broadcastReceiver);
			unregisterReceiver(broadcastReceiver2);
			hasReceverUnRegistered = true;
		}
		
	}
	//单击图标视图变化
	public void onTabChanged(String tabId) {
		setAllTabOff();
		if(TAB_ID_MESSAGE.equals(tabId))
		{
			View tabView =  getTabHost().getTabWidget().getChildTabViewAt(TAB_MESSAGE_INDEX);
			((ImageView)tabView.findViewById(R.id.tabs_tab_img)).setImageResource(R.drawable.tab_message_on);
			((TextView)tabView.findViewById(R.id.tabs_tab_text)).setTextColor(TAB_TEXT_COLOR_ON);
			tabView.findViewById(R.id.tabs_tab_on).setVisibility(View.VISIBLE);
		}else if (TAB_ID_INTERACTIVE.equals(tabId)) {
			View tabView =  getTabHost().getTabWidget().getChildTabViewAt(TAB_INTERACTIVE_INDEX);
			((ImageView)tabView.findViewById(R.id.tabs_tab_img)).setImageResource(R.drawable.tab_interactive_on);
			((TextView)tabView.findViewById(R.id.tabs_tab_text)).setTextColor(TAB_TEXT_COLOR_ON);
			tabView.findViewById(R.id.tabs_tab_on).setVisibility(View.VISIBLE);
		}else if (TAB_ID_APPS.equals(tabId)) {
			View tabView =  getTabHost().getTabWidget().getChildTabViewAt(TAB_APPS_INDEX);
			((ImageView)tabView.findViewById(R.id.tabs_tab_img)).setImageResource(R.drawable.tab_catologue_on);
			((TextView)tabView.findViewById(R.id.tabs_tab_text)).setTextColor(TAB_TEXT_COLOR_ON);
			tabView.findViewById(R.id.tabs_tab_on).setVisibility(View.VISIBLE);
		}else if(TAB_ID_DEMAND.equals(tabId)){
			View tabView =  getTabHost().getTabWidget().getChildTabViewAt(TAB_ID_SEARCHACTIVE);
			((ImageView)tabView.findViewById(R.id.tabs_tab_img)).setImageResource(R.drawable.search_icon_on);
			((TextView)tabView.findViewById(R.id.tabs_tab_text)).setTextColor(TAB_TEXT_COLOR_ON);
			tabView.findViewById(R.id.tabs_tab_on).setVisibility(View.VISIBLE);
		}else if (TAB_ID_SETTINGS.equals(tabId)) {
			View tabView =  getTabHost().getTabWidget().getChildTabViewAt(TAB_SETTINGS_INDEX);
			((ImageView)tabView.findViewById(R.id.tabs_tab_img)).setImageResource(R.drawable.tab_settings_on);
			((TextView)tabView.findViewById(R.id.tabs_tab_text)).setTextColor(TAB_TEXT_COLOR_ON);
			tabView.findViewById(R.id.tabs_tab_on).setVisibility(View.VISIBLE);
		}
	}
	//图标关闭时状态
	private void setAllTabOff()
	{
		View tabView1 =  getTabHost().getTabWidget().getChildTabViewAt(TAB_MESSAGE_INDEX);
		((ImageView)tabView1.findViewById(R.id.tabs_tab_img)).setImageResource(R.drawable.tab_message_off);
		((TextView)tabView1.findViewById(R.id.tabs_tab_text)).setTextColor(TAB_TEXT_COLOR_OFF);
		tabView1.findViewById(R.id.tabs_tab_on).setVisibility(View.INVISIBLE);
		
		View tabView2 =  getTabHost().getTabWidget().getChildTabViewAt(TAB_INTERACTIVE_INDEX);
		((ImageView)tabView2.findViewById(R.id.tabs_tab_img)).setImageResource(R.drawable.tab_interactive_off);
		((TextView)tabView2.findViewById(R.id.tabs_tab_text)).setTextColor(TAB_TEXT_COLOR_OFF);
		tabView2.findViewById(R.id.tabs_tab_on).setVisibility(View.INVISIBLE);
		
		View tabView3 =  getTabHost().getTabWidget().getChildTabViewAt(TAB_APPS_INDEX);
		((ImageView)tabView3.findViewById(R.id.tabs_tab_img)).setImageResource(R.drawable.tab_catologue_off);
		((TextView)tabView3.findViewById(R.id.tabs_tab_text)).setTextColor(TAB_TEXT_COLOR_OFF);
		tabView3.findViewById(R.id.tabs_tab_on).setVisibility(View.INVISIBLE);
		
		View tabView4 =  getTabHost().getTabWidget().getChildTabViewAt(TAB_ID_SEARCHACTIVE);
		((ImageView)tabView4.findViewById(R.id.tabs_tab_img)).setImageResource(R.drawable.search_icon_off);
		((TextView)tabView4.findViewById(R.id.tabs_tab_text)).setTextColor(TAB_TEXT_COLOR_OFF);
		tabView4.findViewById(R.id.tabs_tab_on).setVisibility(View.INVISIBLE);
		
		View tabView5 =  getTabHost().getTabWidget().getChildTabViewAt(TAB_SETTINGS_INDEX);
		((ImageView)tabView5.findViewById(R.id.tabs_tab_img)).setImageResource(R.drawable.tab_settings_off);
		((TextView)tabView5.findViewById(R.id.tabs_tab_text)).setTextColor(TAB_TEXT_COLOR_OFF);
		tabView5.findViewById(R.id.tabs_tab_on).setVisibility(View.INVISIBLE);
	}
}
