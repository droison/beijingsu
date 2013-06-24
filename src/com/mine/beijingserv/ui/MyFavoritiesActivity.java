package com.mine.beijingserv.ui;

import java.util.Vector;

import com.mine.beijingserv.R;
import com.mine.beijingserv.model.FavourModel;
import com.mine.beijingserv.model.FirstCatalogue;
import com.mine.beijingserv.model.MessageInfo;
import com.mine.beijingserv.model.SecondCatalogue;
import com.mine.beijingserv.sys.AppContex;
import com.mine.beijingserv.sys.DBUtil;
import com.mine.beijingserv.sys.FavorateDBUtil;
import com.mine.beijingserv.sys.SysUtils;
import com.mine.beijingserv.sys.UpdateMessageState;
import com.mine.beijingserv.ui.FirstCatologueActivity.AllCatAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyFavoritiesActivity extends Activity implements
		View.OnClickListener,AdapterView.OnItemClickListener{
	private ListView listView;
	private MyFavoritiesAdapter adapter;
	private Vector<MessageInfo> messageInfos = new Vector<MessageInfo>();
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.favoratieslayout);
		
		
		
		listView = (ListView) findViewById(R.id.favoritieslist);
		adapter = new MyFavoritiesAdapter();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		findViewById(R.id.topbar_back_favorities).setOnClickListener(this);
		
		refreshArrays();
		
	}
	//取得收藏的消息
	private void refreshArrays()
	{
		messageInfos.clear();
		Vector<MessageInfo> infos = new DBUtil(this).getAllMessageInfos(this);
		Vector<FavourModel> favourModels = new FavorateDBUtil(this).getAllSavedFavors(this);
		for(FavourModel favourModel : favourModels)
		{
			for(MessageInfo messageInfo : infos)
			{
				if(messageInfo.localsqlid == favourModel.messageinfoid)
				{
					messageInfos.add(messageInfo);
				}
			}
		}
		adapter.notifyDataSetChanged();
		
	}
	
	public void onClick(final View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.topbar_back_favorities:
			finish();
			break;
			
		case R.id.cancelbutton2:
			AlertDialog cancelALertDialog = new AlertDialog.Builder(this).setTitle("取消收藏").setMessage("是否取消收藏")
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					int position = (Integer)v.getTag();
					MessageInfo messageInfo = messageInfos.elementAt(position);
					new FavorateDBUtil(MyFavoritiesActivity.this).deleteFavor(messageInfo.localsqlid);
					refreshArrays();
				}
			}).setNegativeButton("取消", null).create();
			
			cancelALertDialog.show();
			break;
		default:
			break;
		}

	}

	public class MyFavoritiesAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return messageInfos.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if(convertView == null)
			{
				convertView = LayoutInflater.from(MyFavoritiesActivity.this).inflate(R.layout.tabmessage_list_single2, null);
			}
			MessageInfo messageInfo = messageInfos.elementAt(position);
			TextView titleTextView = (TextView)convertView.findViewById(R.id.tabmessage_list_single_title_textview2);
			TextView timeTextView = (TextView)convertView.findViewById(R.id.tabmessage_list_single_time_textview2);
			TextView contentTextView = (TextView)convertView.findViewById(R.id.tabmessage_list_single_content2);
			contentTextView.setTextColor(MyFavoritiesActivity.this.getResources().getColor(R.color.dark));
			TextView infotypeTextView = (TextView)convertView.findViewById(R.id.tabmessage_list_infotype2);
			infotypeTextView.setText("");
			titleTextView.setText(messageInfo.title==null?"":messageInfo.title);
			timeTextView.setText(messageInfo.getFormatedTimeWithoutYear()==null?"":messageInfo.getFormatedTimeWithoutYear());
			contentTextView.setText(messageInfo.content==null?"":messageInfo.content);
			TextView cancelbuttonTextView = (TextView)convertView.findViewById(R.id.cancelbutton2);
			cancelbuttonTextView.setVisibility(View.VISIBLE);
			cancelbuttonTextView.setTag(position);
			cancelbuttonTextView.setOnClickListener(MyFavoritiesActivity.this);
			SysUtils.log(messageInfo.title);
			SysUtils.log("readState = "+messageInfo.readState);
			TextView cattitleTextView = (TextView)convertView.findViewById(R.id.tabmessage_list_single_cattitle_textview2);
			cattitleTextView.setText("");
			
			if(messageInfo.readState == MessageInfo.READ_STATE_READED)
			{
		
				titleTextView.setTextColor(0xff333333);
				titleTextView.setTextAppearance(MyFavoritiesActivity.this, R.style.text_normal);
			}else {
			
				titleTextView.setTextColor(0xff222222);
				titleTextView.setTextAppearance(MyFavoritiesActivity.this, R.style.text_bold);
			}
			
			
			try {
				for(FirstCatalogue firstCatalogue:AppContex.catalogues)
				{
					if(firstCatalogue.fcatid == messageInfo.fcatid)
					{
						String titleString = firstCatalogue.title.replace("北京", "");
						cattitleTextView.setText(titleString);
					}
					
					for(SecondCatalogue secondCatalogue:firstCatalogue.secondCatalogues){
						if(secondCatalogue.scatid == messageInfo.scatid){
							infotypeTextView.setText(secondCatalogue.title);
						}
					}
				}
			} catch (Exception e) {
			}
			
			return convertView;
		}

	}
	//进入通知详情，通过putboolean 传入ISFAVORATEACTTIVITY
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int postion, long arg3) {
		// TODO Auto-generated method stub
		AppContex.curMessageInfo = messageInfos.elementAt(postion);
		Intent intent = new Intent(MyFavoritiesActivity.this,MessageInfoDetailActivity.class);
		Bundle extras = new Bundle();
		extras.putBoolean("ISFAVORATEACTTIVITY", true);
		extras.putBoolean("ISTABSEARCHACTTIVITY", false);
		extras.putBoolean("ISCOMEFROMNOTIFY",false);
		extras.putInt("MESSAGES_ID", -1);
		intent.putExtras(extras);
		startActivity(intent);		
	}
}
