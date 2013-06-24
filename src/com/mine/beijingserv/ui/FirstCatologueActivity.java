package com.mine.beijingserv.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mine.beijingserv.R;
import com.mine.beijingserv.model.CatologueFilter;
import com.mine.beijingserv.model.FirstCatalogue;
import com.mine.beijingserv.model.MessageInfo;
import com.mine.beijingserv.model.SecondCatalogue;
import com.mine.beijingserv.push.PushService;
import com.mine.beijingserv.sys.AppContex;
import com.mine.beijingserv.sys.SDcardUtil;
import com.mine.beijingserv.sys.SubmitSubscribeThread;
import com.mine.beijingserv.sys.SysUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;


@SuppressLint("ResourceAsColor")
public class FirstCatologueActivity extends Activity implements View.OnClickListener,OnCheckedChangeListener
{
	
	private final String SP_NAME = "DINGYUESHOW_INFO";
	private View curvedpageView;
	private TextView allDingyueText;
	private TextView hotDingyueText;
	private TextView myDingyueText;
	private ImageView allDingyueLine;
	private ImageView hotDingyueLine;
	private ImageView myDingyueLine;
	private ExpandableListView alldingyueListView;
	private ListView hotdingyueListview;
	private ListView mydingyueListView;
	private Vector<SecondCatalogue> hotSecondCatalogues = new Vector<SecondCatalogue>();
	private Vector<SecondCatalogue> top10SecondCatalogues = new Vector<SecondCatalogue>();
	private Vector<SecondCatalogue> mydingyueCatalogues = new Vector<SecondCatalogue>();
	private AllCatAdapter alladapter;	
	private HotAdapter hotAdapter;
	private MyDingYueAdapter myDingYueAdapter;	
	private SharedPreferences fristCatologuePreferences ;
	private boolean is_fristcatologue = true;
	private Editor editor;
	private String resubcribe = "com.zzku.reresubcribe";
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabcatologues);
		//是否是一次进入订阅
		fristCatologuePreferences = this.getSharedPreferences("fristcatologuepreferences", Context.MODE_PRIVATE);
		is_fristcatologue = fristCatologuePreferences.getBoolean("isfrist_fristcatologue", true);
		editor = fristCatologuePreferences.edit();

		View alertDingYuelayout = LayoutInflater.from(this).inflate(R.layout.alertdingyuelayout, null);
		final CheckBox cancelAlertChecked = (CheckBox)alertDingYuelayout.findViewById(R.id.cancelalert);
		//显示订阅对话框
		if(is_fristcatologue){
			new AlertDialog.Builder(this).setView(alertDingYuelayout).setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					finish();
				}
			})
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					boolean iscancelAlert = cancelAlertChecked.isChecked();
					if(iscancelAlert){
						editor.putBoolean("isfrist_fristcatologue", false);
						editor.commit();
					}
				}
			}).create().show();
		}
		allDingyueText = (TextView)findViewById(R.id.alldingyye);
		hotDingyueText = (TextView)findViewById(R.id.hotdingyye);
		myDingyueText = (TextView)findViewById(R.id.mydingyye);
		allDingyueLine = (ImageView)findViewById(R.id.alldingyyeline);
		hotDingyueLine = (ImageView)findViewById(R.id.hotdingyyeline);
		myDingyueLine = (ImageView)findViewById(R.id.mydingyyeline);
		allDingyueText.setOnClickListener(this);
		myDingyueText.setOnClickListener(this);
		hotDingyueText.setOnClickListener(this);
		
		alldingyueListView = (ExpandableListView)findViewById(R.id.tabcatologue_listview);
		alladapter = new AllCatAdapter();
		alldingyueListView.setAdapter(alladapter);
		hotdingyueListview = (ListView)findViewById(R.id.hotcatologue_listview);
		mydingyueListView = (ListView)findViewById(R.id.mycatologue_listview);		
		hotAdapter = new HotAdapter();
		hotdingyueListview.setAdapter(hotAdapter);
		myDingYueAdapter = new MyDingYueAdapter();
		mydingyueListView.setAdapter(myDingYueAdapter);
		findViewById(R.id.topbar_back).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		
		refreshArrays();		
		
	}
	
	
	//获得3个listview 排序
	private void refreshArrays()
	{
		/////热门
		hotSecondCatalogues.clear();
		top10SecondCatalogues.clear();
		for(FirstCatalogue firstCatalogue : AppContex.catalogues)
		{
			for(SecondCatalogue secondCatalogue : firstCatalogue.secondCatalogues)
			{
				hotSecondCatalogues.add(secondCatalogue);
				
			}
		}
		///最热排序
		Comparator<SecondCatalogue> comparator = new Comparator<SecondCatalogue>() {
			public int compare(SecondCatalogue lhs, SecondCatalogue rhs) {
				return -lhs.scatsubmun + rhs.scatsubmun;
			}
		};
		Collections.sort(hotSecondCatalogues,comparator);
		
		if(hotSecondCatalogues.size()>10){
			top10SecondCatalogues.addAll(hotSecondCatalogues.subList(0, 10));
		}else{
			top10SecondCatalogues.addAll(hotSecondCatalogues);
		}
		
		//////我的订阅
		if(!AppContex.APPCATION_ON){
			mydingyueCatalogues.clear();
			for(FirstCatalogue firstCatalogue : AppContex.catalogues)
			{
				for(SecondCatalogue secondCatalogue : firstCatalogue.secondCatalogues)
				{				
					if(secondCatalogue.isChoosed)
					{
						mydingyueCatalogues.add(secondCatalogue);
						System.out.println("mydingyueCatalogues:  "+mydingyueCatalogues.size());
					}
				}
			}
			
			AppContex.APPCATION_ON = true;
			AppContex.tempInfos.clear();
			AppContex.tempInfos.addAll(mydingyueCatalogues);
			System.out.println("AppContex.tempInfos:  "+AppContex.tempInfos.size());
		}else{
			mydingyueCatalogues.clear();
			mydingyueCatalogues.addAll(AppContex.tempInfos)  ;
			for(FirstCatalogue firstCatalogue : AppContex.catalogues)
			{
				for(SecondCatalogue secondCatalogue : firstCatalogue.secondCatalogues)
				{				
					if(secondCatalogue.isChoosed)
					{
						if(mydingyueCatalogues.contains(secondCatalogue)){							
						}else{
							mydingyueCatalogues.add(secondCatalogue);
							System.out.println("THE NEW OBJECT IS INTO");
						}
					}
				}
			}
			
			System.out.println("走临列表");
		}
		
		
		alladapter.notifyDataSetChanged();
		hotAdapter.notifyDataSetChanged();
		myDingYueAdapter.notifyDataSetChanged();		
		
	}
	
	



	@SuppressLint("ResourceAsColor")
	public void onClick(View v) {
		System.out.println("onclick");
		switch (v.getId()) {		
			
		case  R.id.alldingyye:
			allDingyueText.setTextColor(FirstCatologueActivity.this.getResources().getColor(R.color.red));
			hotDingyueText.setTextColor(FirstCatologueActivity.this.getResources().getColor(R.color.dark));
			myDingyueText.setTextColor(FirstCatologueActivity.this.getResources().getColor(R.color.dark));		
			allDingyueLine.setVisibility(View.VISIBLE);
			hotDingyueLine.setVisibility(View.INVISIBLE);
			myDingyueLine.setVisibility(View.INVISIBLE);
			alldingyueListView.setVisibility(View.VISIBLE);
			hotdingyueListview.setVisibility(View.INVISIBLE);
			mydingyueListView.setVisibility(View.INVISIBLE);
			break;
		case  R.id.hotdingyye:
			allDingyueText.setTextColor(FirstCatologueActivity.this.getResources().getColor(R.color.dark));
			hotDingyueText.setTextColor(FirstCatologueActivity.this.getResources().getColor(R.color.red));
			myDingyueText.setTextColor(FirstCatologueActivity.this.getResources().getColor(R.color.dark));
			allDingyueLine.setVisibility(View.INVISIBLE);
			hotDingyueLine.setVisibility(View.VISIBLE);
			myDingyueLine.setVisibility(View.INVISIBLE);
			alldingyueListView.setVisibility(View.INVISIBLE);
			hotdingyueListview.setVisibility(View.VISIBLE);
			mydingyueListView.setVisibility(View.INVISIBLE);
			break;
			
		case  R.id.mydingyye:
			allDingyueText.setTextColor(FirstCatologueActivity.this.getResources().getColor(R.color.dark));
			hotDingyueText.setTextColor(FirstCatologueActivity.this.getResources().getColor(R.color.dark));
			myDingyueText.setTextColor(FirstCatologueActivity.this.getResources().getColor(R.color.red));
			allDingyueLine.setVisibility(View.INVISIBLE);
			hotDingyueLine.setVisibility(View.INVISIBLE);
			myDingyueLine.setVisibility(View.VISIBLE);			
			alldingyueListView.setVisibility(View.INVISIBLE);
			hotdingyueListview.setVisibility(View.INVISIBLE);
			mydingyueListView.setVisibility(View.VISIBLE);
			break;

		default:
			break;
		}
	}
	
	@Override
	public void finish() {
		super.finish();		
		saveFilterToPref(FirstCatologueActivity.this);		
	}
	

	class AllCatAdapter extends BaseExpandableListAdapter {

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return null;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public View getChildView(final int groupPosition, final int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			if(convertView == null)
			{
				convertView = LayoutInflater.from(FirstCatologueActivity.this).inflate(R.layout.tabcatologue_list_single2, null);
			}
			AppContex.curFirstCatalogue = AppContex.catalogues.elementAt(groupPosition);
			final SecondCatalogue secondCatalogue = AppContex.curFirstCatalogue.secondCatalogues.elementAt(childPosition);
			TextView secondTitleText = ((TextView) convertView
					.findViewById(R.id.tabmessage_list_single_title_textviewlist2));
			secondTitleText.setText(secondCatalogue.title);
			((TextView)convertView.findViewById(R.id.tabmessage_showbycat_ding_numlist2)).setText(secondCatalogue.scatsubmun+"");
			((TextView)convertView.findViewById(R.id.tabmessage_showbycat_curmonth_numlist2)).setText(secondCatalogue.scatcurmonthmsgnum+"");
			((TextView)convertView.findViewById(R.id.tabmessage_showbycat_total_numlist2)).setText(secondCatalogue.scatcallmsgnum+"");
			final CheckBox checkSingleButton = (CheckBox)convertView.findViewById(R.id.checkdingyuebutton2);
			checkSingleButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(secondCatalogue.type == SecondCatalogue.SECOND_CAT_TYPE_URGENT)
					{
						Toast.makeText(FirstCatologueActivity.this, "预警消息不能取消", 1000).show();
						checkSingleButton.setChecked(true);
					}
					
					if(secondCatalogue.title.equals("系统公告"))
					{
						Toast.makeText(FirstCatologueActivity.this, "系统公告不能取消", 1000).show();
						checkSingleButton.setChecked(true);
					}
				}
			});
			checkSingleButton.setOnCheckedChangeListener(null);
			checkSingleButton.setChecked(secondCatalogue.isChoosed);
			CheckIndex checkIndex = new CheckIndex();
			checkIndex.listtype = 0;
			checkIndex.fcatindex = groupPosition;
			checkIndex.scatindex = childPosition;
			checkIndex.isFirstCat = false;
			checkSingleButton.setTag(checkIndex);
			checkSingleButton.setOnCheckedChangeListener(FirstCatologueActivity.this);			
			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			AppContex.curFirstCatalogue = AppContex.catalogues.elementAt(groupPosition);
			return AppContex.curFirstCatalogue.secondCatalogues.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return null;
		}

		@Override
		public int getGroupCount() {
			return AppContex.catalogues.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return 0;
		}

		@Override
		public View getGroupView(final int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			if(convertView == null)
				{
					convertView = LayoutInflater.from(FirstCatologueActivity.this).inflate(R.layout.tabcatologue_list_single, null);
				}
				FirstCatalogue firstCatalogue = AppContex.catalogues.elementAt(groupPosition);			
				((TextView)convertView.findViewById(R.id.tabmessage_list_single_title_textviewlist)).setText(firstCatalogue.title);
				((TextView)convertView.findViewById(R.id.tabmessage_showbycat_ding_numlist)).setText(firstCatalogue.fcatsubnum+"");

				TextView  fcatcurmonthmsgView = ((TextView)convertView.findViewById(R.id.tabmessage_showbycat_curmonth_numlist));
				fcatcurmonthmsgView.setText(String.valueOf(firstCatalogue.fcatcurmonthmsg));
				if(firstCatalogue.fcatcurmonthmsg>9999){
					String catcurmonthmsg = String.valueOf(firstCatalogue.fcatcurmonthmsg);
					String latercatcurmonthmsg = catcurmonthmsg.substring(0, catcurmonthmsg.length()-4);
					fcatcurmonthmsgView.setText(latercatcurmonthmsg+"万");
				}
				

				TextView  fcatallmsgnumView = ((TextView)convertView.findViewById(R.id.tabmessage_showbycat_total_numlist));
				fcatallmsgnumView.setText(String.valueOf(firstCatalogue.fcatallmsgnum));
				if(firstCatalogue.fcatallmsgnum>9999){
					String catcurmonthmsg = String.valueOf(firstCatalogue.fcatallmsgnum);
					String latercatcurmonthmsg = catcurmonthmsg.substring(0, catcurmonthmsg.length()-4);
					fcatcurmonthmsgView.setText(latercatcurmonthmsg+"万");
				}
				RelativeLayout tab_list_backgroundLayout = (RelativeLayout)convertView.findViewById(R.id.tab_list_background);
				 tab_list_backgroundLayout.setBackgroundResource(R.color.white);
				CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.checkdingyuebutton);				
				CheckIndex checkIndex = new CheckIndex();
				checkIndex.listtype = 0;
				checkIndex.isFirstCat = true;
				checkIndex.fcatindex = groupPosition;
				checkBox.setTag(checkIndex);
				checkBox.setOnCheckedChangeListener(null);
				boolean isAllChoosed = true;
				for(SecondCatalogue secondCatalogue : firstCatalogue.secondCatalogues)
				{
					if(!secondCatalogue.isChoosed)
					{
						isAllChoosed = false;
						break;
					}
				}
				////
				checkBox.setChecked(isAllChoosed);
				checkBox.setOnCheckedChangeListener(FirstCatologueActivity.this);
				return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}


	class HotAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return top10SecondCatalogues.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null)
			{
				convertView = LayoutInflater.from(FirstCatologueActivity.this).inflate(R.layout.mydingyuelayout, null);
			}
			final SecondCatalogue secondCatalogue = top10SecondCatalogues.elementAt(position);
			FirstCatalogue firstCatalogue = null;
			for(FirstCatalogue firstCatalogue2 : AppContex.catalogues)
			{
				if(firstCatalogue2.fcatid == secondCatalogue.fcatid)
				{
					firstCatalogue = firstCatalogue2;
					break;
				}
			}
			((TextView)convertView.findViewById(R.id.mydingyue_title_textview)).setText(secondCatalogue.title);
			if(firstCatalogue.title == null || firstCatalogue.title.trim().length()==0){
				Toast.makeText(FirstCatologueActivity.this, "网络信号不好，请稍后重试", 1000).show();
				finish();
			}
			((TextView)convertView.findViewById(R.id.mydingyue_department_textview)).setText(firstCatalogue.title);
			((TextView)convertView.findViewById(R.id.mydingyue_totalnum_text)).setText(secondCatalogue.scatsubmun+"");			
			 
			final CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.mydingyue_checkbox);
			checkBox.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(secondCatalogue.type == SecondCatalogue.SECOND_CAT_TYPE_URGENT)
					{
						Toast.makeText(FirstCatologueActivity.this, "预警消息不能取消", 1000).show();
						checkBox.setChecked(true);
					}
					
					if(secondCatalogue.title.equals(AppContex.seTitle))
					{
						Toast.makeText(FirstCatologueActivity.this, "系统公告不能取消", 1000).show();
						checkBox.setChecked(true);
					}
				}
			});
			checkBox.setOnCheckedChangeListener(null);
			checkBox.setChecked(secondCatalogue.isChoosed);
			CheckIndex checkIndex = new CheckIndex();
			checkIndex.listtype = 1;
			checkIndex.scatindex = position;
			checkBox.setTag(checkIndex);
			checkBox.setOnCheckedChangeListener(FirstCatologueActivity.this);			
			return convertView;
			
		}
		
	}
	
	
	class MyDingYueAdapter extends BaseAdapter{

		@Override
		public int getCount() {			
			return mydingyueCatalogues.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null)
			{
				convertView = LayoutInflater.from(FirstCatologueActivity.this).inflate(R.layout.mydingyuelayout, null);
			}
			final SecondCatalogue secondCatalogue = mydingyueCatalogues.elementAt(position);
			FirstCatalogue firstCatalogue = null;
			for(FirstCatalogue firstCatalogue2 : AppContex.catalogues)
			{
				if(firstCatalogue2.fcatid == secondCatalogue.fcatid)
				{
					firstCatalogue = firstCatalogue2;
					break;
				}
			}
			
			((TextView)convertView.findViewById(R.id.mydingyue_title_textview)).setText(secondCatalogue.title);
			((TextView)convertView.findViewById(R.id.mydingyue_department_textview)).setText(firstCatalogue.title);
			((TextView)convertView.findViewById(R.id.mydingyue_totalnum_text)).setText(secondCatalogue.scatsubmun+"");			
			
			final CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.mydingyue_checkbox);
			checkBox.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(secondCatalogue.type == SecondCatalogue.SECOND_CAT_TYPE_URGENT)
					{
						Toast.makeText(FirstCatologueActivity.this, "预警消息不能取消", 1000).show();
						checkBox.setChecked(true);
					}
					
					if(secondCatalogue.title.equals(AppContex.seTitle))
					{
						Toast.makeText(FirstCatologueActivity.this, "系统公告不能取消", 1000).show();
						checkBox.setChecked(true);
					}
				}
			});
			checkBox.setOnCheckedChangeListener(null);
			checkBox.setChecked(secondCatalogue.isChoosed);
			CheckIndex checkIndex = new CheckIndex();
			checkIndex.listtype = 2;
			checkIndex.scatindex = position;
			checkBox.setTag(checkIndex);
			checkBox.setOnCheckedChangeListener(FirstCatologueActivity.this);			
			return convertView;
		}
		
	}
	
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
	{
		
	
		CheckIndex checkIndex =  (CheckIndex)buttonView.getTag();
		
		if(checkIndex.listtype == 0)///所有
		{
			if (!checkIndex.isFirstCat) {
				FirstCatalogue firstCatalogue = AppContex.catalogues
						.elementAt(checkIndex.fcatindex);
				firstCatalogue.secondCatalogues.elementAt(checkIndex.scatindex).isChoosed = isChecked;
				if(firstCatalogue.secondCatalogues.elementAt(checkIndex.scatindex).type == SecondCatalogue.SECOND_CAT_TYPE_NORMAL
						&& !firstCatalogue.secondCatalogues.elementAt(checkIndex.scatindex).title.equals(AppContex.seTitle)){
					if (isChecked) {
						firstCatalogue.secondCatalogues.elementAt(checkIndex.scatindex).scatsubmun++;
					} else {
						firstCatalogue.secondCatalogues.elementAt(checkIndex.scatindex).scatsubmun--;
					}
				}
				

				boolean isallchecked = false;
				boolean isallcancel = true;
				if(isChecked){
					int count = 0;
					for (SecondCatalogue secondCatalogue : firstCatalogue.secondCatalogues){
						if(secondCatalogue.type == SecondCatalogue.SECOND_CAT_TYPE_NORMAL && !secondCatalogue.title.equals(AppContex.seTitle)){
							if(secondCatalogue.isChoosed  ){
								count++;
							}	
						}			
											
					}
					
					for(SecondCatalogue secondCatalogue : firstCatalogue.secondCatalogues){
						if(secondCatalogue.type == SecondCatalogue.SECOND_CAT_TYPE_URGENT){
							isallchecked = false;
							break;
						}
						
						if(secondCatalogue.title.equals(AppContex.seTitle)){
							isallchecked = false;
							break;
						}
						
						if(count == 1){
							isallchecked = true;
						}
					}
					
					
					
					if(isallchecked){
						firstCatalogue.fcatsubnum++;
					}
				}else{
					for (SecondCatalogue secondCatalogue : firstCatalogue.secondCatalogues){
						if(secondCatalogue.type == SecondCatalogue.SECOND_CAT_TYPE_URGENT){
							isallcancel = false;
							break;
						}
						if(secondCatalogue.title.equals(AppContex.seTitle)){
							isallcancel = false;
							break;
						}
						if(secondCatalogue.isChoosed){
							isallcancel = false;
							break;
						}
					}
					
					if(isallcancel){
						firstCatalogue.fcatsubnum--;
					}
				}
				
				
			} else {
				FirstCatalogue firstCatalogue = AppContex.catalogues
						.elementAt(checkIndex.fcatindex);
				if (isChecked) {

					boolean allChecked = true;
					for(SecondCatalogue secondCatalogue : firstCatalogue.secondCatalogues){
						if (secondCatalogue.type == SecondCatalogue.SECOND_CAT_TYPE_URGENT) {
							allChecked = false;
						}
						if (secondCatalogue.title.equals(AppContex.seTitle)) {
							allChecked = false;
						}
						if(secondCatalogue.isChoosed){
							allChecked = false;
						}
					}
					for (SecondCatalogue secondCatalogue : firstCatalogue.secondCatalogues) {
						if (secondCatalogue.type != SecondCatalogue.SECOND_CAT_TYPE_URGENT && !secondCatalogue.title.equals(AppContex.seTitle) &&
								secondCatalogue.isChoosed == false) {
							secondCatalogue.scatsubmun++;
						}
						
						secondCatalogue.isChoosed = true;

						
					}


					if (allChecked) {
						firstCatalogue.fcatsubnum++;
					}

				} else {

					boolean allCancel = true;

					for (SecondCatalogue secondCatalogue : firstCatalogue.secondCatalogues) {

						if (secondCatalogue.type != SecondCatalogue.SECOND_CAT_TYPE_URGENT && 
								!secondCatalogue.title.equals(AppContex.seTitle)) {
							secondCatalogue.isChoosed = false;
							secondCatalogue.scatsubmun--;
						}

					}

					for (SecondCatalogue secondCatalogue : firstCatalogue.secondCatalogues) {
						if (secondCatalogue.type == SecondCatalogue.SECOND_CAT_TYPE_URGENT) {
							allCancel = false;
							break;
						}
						
						
						if (secondCatalogue.title.equals(AppContex.seTitle)) {
							allCancel = false;
							break;
						}
						if(secondCatalogue.isChoosed){
							allCancel = false;
							break;
						}
					}

					if (allCancel) {
						firstCatalogue.fcatsubnum--;
					}

				}
			}
		}else if(checkIndex.listtype == 1)
		{
			SecondCatalogue secondCatalogue = top10SecondCatalogues.elementAt(checkIndex.scatindex);
			secondCatalogue.isChoosed = isChecked;
			if(secondCatalogue.type == SecondCatalogue.SECOND_CAT_TYPE_NORMAL && !secondCatalogue.title.equals(AppContex.seTitle)){
				if(isChecked){
					secondCatalogue.scatsubmun++;
				}else{
					secondCatalogue.scatsubmun--;
				}
			}


		}else {
			
			SecondCatalogue secondCatalogue = mydingyueCatalogues.elementAt(checkIndex.scatindex);
			secondCatalogue.isChoosed = isChecked;
			if(secondCatalogue.type == SecondCatalogue.SECOND_CAT_TYPE_NORMAL && !secondCatalogue.title.equals(AppContex.seTitle)){
				if(isChecked){
					secondCatalogue.scatsubmun++;
				}else{
					secondCatalogue.scatsubmun--;
				}
			}
		}
		refreshArrays();
	}
	
	
	
	private class CheckIndex
	{
		public int fcatindex = 0;
		public int scatindex = 0;
		public boolean isFirstCat = false;
		public int listtype = 0;////0表示所有1表示热门2表示我的订阅
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		finish();
	}
	
	//保存订阅信息 预警信息与系统公告默认为选择
	public  void saveFilterToPref(Context context)
	{		
		JSONArray jsonArray = new JSONArray();
		for(FirstCatalogue firstCatalogue:AppContex.catalogues)
		{
			for(SecondCatalogue secondCatalogue : firstCatalogue.secondCatalogues)
			{
				if(secondCatalogue.type == SecondCatalogue.SECOND_CAT_TYPE_URGENT){
					secondCatalogue.isChoosed = true;
				}
				
				if(secondCatalogue.title.equals("系统公告")){
					secondCatalogue.isChoosed = true;
				}
				
				if(secondCatalogue.isChoosed)
				{
					System.out.println("TITLE:  "+secondCatalogue.title);
					try {
						JSONObject jsonObject = new JSONObject();
						jsonObject.put(CatologueFilter.CATOLOGUE_FILTER_KEY_FCATID, firstCatalogue.fcatid);
						jsonObject.put(CatologueFilter.CATOLOGUE_FILTER_KEY_SCATID, secondCatalogue.scatid);
						jsonArray.put(jsonObject);
					} catch (Exception e) {
					}
					
				}
			}
		}
		System.out.println("saved filter = "+jsonArray.toString());
		SharedPreferences sharedPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
		Editor editor =  sharedPreferences.edit();
		editor.putString("key_pref_filter", jsonArray.toString());
		editor.commit();
		new SubmitSubscribeThread(jsonArray.toString(), SysUtils.getDeviceID(context)).start();
		
	}
	
	//发送订阅请求，发送重新订阅广播
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

			Intent intent = new Intent(resubcribe);
			FirstCatologueActivity.this.sendBroadcast(intent);
		}
	}
	
}
