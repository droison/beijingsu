package com.mine.beijingserv.ui;

import java.util.Vector;

import com.mine.beijingserv.R;
import com.mine.beijingserv.model.FirstCatalogue;
import com.mine.beijingserv.model.MessageInfo;
import com.mine.beijingserv.model.SecondCatalogue;
import com.mine.beijingserv.sys.AppContex;
import com.mine.beijingserv.sys.DBUtil;
import com.mine.beijingserv.sys.SysUtils;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ShowMessageByCatologue extends Activity implements
		AdapterView.OnItemClickListener, View.OnClickListener {
	public static int toShowCatologueId = 0;

	private ListView listView = null;

	private Vector<MessageInfo> messageInfos = new Vector<MessageInfo>();
	private Vector<MessageInfo> srcInfos = new Vector<MessageInfo>();
	private View curvedpageView = null;
	private TextView topbar_back;
	private TextView search_topbar_back;
	private TextView title_text;
	private TextView department_Searchview;
	private EditText seacherEdit;
	private RelativeLayout serchbar_layout;
	private ImageView searchImage;
	private MyAdapter myAdapter;
	private final String DELETE_BYCAT_MESSAGE = "com.zskt.deletemessage_bycat";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showmsgbycatologue);
		listView = (ListView) findViewById(R.id.showmsgbycat_listview);
		//添加委办局下面的通知
		srcInfos = new DBUtil(this).getAllMessageInfos2(this);
		if (srcInfos != null) {
			for (MessageInfo messageInfo : srcInfos) {
				if (messageInfo.fcatid == toShowCatologueId) {
					messageInfos.add(messageInfo);
				}
			}
		}

		myAdapter = new MyAdapter();
		listView.setAdapter(myAdapter);
		listView.setOnItemClickListener(this);
		serchbar_layout = (RelativeLayout) findViewById(R.id.serchbar_layout);
		seacherEdit = (EditText) findViewById(R.id.serch_bar);
		topbar_back = (TextView) findViewById(R.id.topbar_back);
		title_text = (TextView) findViewById(R.id.top_title);
		search_topbar_back = (TextView) findViewById(R.id.serch_topbar_back);
		topbar_back.setOnClickListener(this);
		search_topbar_back.setOnClickListener(this);
		searchImage = (ImageView) findViewById(R.id.showcatologue_searchbutton);
		searchImage.setOnClickListener(this);
		getMessageTitle();
		department_Searchview = (TextView) findViewById(R.id.department_searchview);
		department_Searchview.setOnClickListener(this);
		this.registerReceiver(deleteMessgeByCatBroadcast, new IntentFilter(DELETE_BYCAT_MESSAGE));
	}
	//显示委办局名称
	private void getMessageTitle() {
		// TODO Auto-generated method stub
		for (FirstCatalogue firstCatalogue : AppContex.catalogues) {
			if (firstCatalogue.fcatid == toShowCatologueId) {
				title_text.setText(firstCatalogue.title);
				break;
			}
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.topbar_back:
			finish();
			break;
		//显示搜索图标
		case R.id.showcatologue_searchbutton:
			searchImage.setVisibility(View.INVISIBLE);
			topbar_back.setVisibility(View.INVISIBLE);
			search_topbar_back.setVisibility(View.VISIBLE);
			title_text.setText("搜索");
			serchbar_layout.setVisibility(View.VISIBLE);
			messageInfos.clear();
			myAdapter.notifyDataSetInvalidated();
			break;
		//搜索返回
		case R.id.serch_topbar_back:
			topbar_back.setVisibility(View.VISIBLE);
			search_topbar_back.setVisibility(View.GONE);
			searchImage.setVisibility(View.VISIBLE);
			getMessageTitle();
			serchbar_layout.setVisibility(View.GONE);
			seacherEdit.setText("");

			if (srcInfos != null) {
				for (MessageInfo messageInfo : srcInfos) {
					if (messageInfo.fcatid == toShowCatologueId) {
						messageInfos.add(messageInfo);
					}
				}
			}

			myAdapter.notifyDataSetChanged();
			break;
			
			//显示搜索结果
		case R.id.department_searchview:
			String searchString = seacherEdit.getText().toString();
			if (SysUtils.isStringEmpty(searchString)) {
				Toast.makeText(ShowMessageByCatologue.this, "请您输入查询内容",
						Toast.LENGTH_SHORT).show();
				break;
			}
			messageInfos.clear();
			if (srcInfos != null) {
				for (MessageInfo messageInfo : srcInfos) {
					if (messageInfo.fcatid == toShowCatologueId
							&& messageInfo.title != null
							&& messageInfo.title.contains(searchString)) {
						messageInfos.add(messageInfo);
					}
				}
			}

			if (messageInfos.size() == 0) {
				Toast.makeText(ShowMessageByCatologue.this, "无相关通知",
						Toast.LENGTH_SHORT).show();
			}
			myAdapter.notifyDataSetChanged();
			break;

		default:
			break;
		}
	}
	//listview 单击进入通知详情
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {
		if (adapterView == listView) {
			AppContex.curMessageInfo = messageInfos.elementAt(position);

			Intent intent = new Intent(ShowMessageByCatologue.this,
					MessageInfoDetailActivity2.class);
			Bundle extras = new Bundle();
			extras.putBoolean("ISFAVORATEACTTIVITY", false);
			extras.putBoolean("ISTABSEARCHACTTIVITY", false);
			extras.putBoolean("ISCOMEFROMNOTIFY",false);
			extras.putInt("MESSAGES_ID", -1);
			extras.putInt("SHOWCATOLOGUEID",toShowCatologueId);
			intent.putExtras(extras);
			startActivity(intent);
		}

	}

	private class MyAdapter extends BaseAdapter {
		public int getCount() {
			return messageInfos.size();
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
				convertView = LayoutInflater.from(ShowMessageByCatologue.this)
						.inflate(R.layout.tabmessage_list_single, null);
			}				
				final MessageInfo messageInfo = messageInfos.elementAt(position);			
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

				ImageView altertTypeView = (ImageView) convertView
						.findViewById(R.id.typeimage);
				if (messageInfo.alerttype != -1 && messageInfo.alertlevel != -1) {

					typeimagelayout.setVisibility(View.VISIBLE);
					altertTypeView.setVisibility(View.VISIBLE);					
					setAlertTypeImage(messageInfo.alerttype,
							messageInfo.alertlevel, altertTypeView);
					altertTypeView.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							// myAdapter.notifyDataSetChanged();

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
								Intent intent = new Intent(ShowMessageByCatologue.this,
										AlertContentActivity.class);
								intent.putExtras(extras);
								ShowMessageByCatologue.this.startActivity(intent);
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

						for (SecondCatalogue secondCatalogue : firstCatalogue.secondCatalogues) {
							if (secondCatalogue.scatid == messageInfo.scatid) {
								typeinfo.setText(secondCatalogue.title);
							}
						}
					}
				} catch (Exception e) {

				}

				

					titleTextView.setTextColor(0xff222222);
					titleTextView.setTextAppearance(ShowMessageByCatologue.this,
							R.style.text_normal);
					contentTextView.setTextColor(ShowMessageByCatologue.this
							.getResources().getColor(R.color.dark));
				
				if (messageInfo.type == MessageInfo.MESSAGE_TYPE_ERGENT) {
					if (messageInfo.readState == MessageInfo.READ_STATE_READED) {
						
						titleTextView.setTextColor(ShowMessageByCatologue.this
								.getResources().getColor(
										R.color.text_color_dark_red));
						typeinfo.setTextColor(ShowMessageByCatologue.this.getResources()
								.getColor(R.color.text_color_dark_red));
					} else {						
						titleTextView.setTextColor(ShowMessageByCatologue.this
								.getResources().getColor(R.color.red));
						typeinfo.setTextColor(ShowMessageByCatologue.this.getResources()
								.getColor(R.color.red));
					}

				} else {
					typeinfo.setTextColor(ShowMessageByCatologue.this.getResources()
							.getColor(R.color.bule));
				}
			return convertView;
		}
	}
	//显示预警图标
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
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert0_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 0 && messagesAlertLevel == 3) {				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert0_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 1 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert1_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 1 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert1_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 10 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert10_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 10 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert10_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 10 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert10_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 11 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert11_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 11 && messagesAlertLevel == 1) {
				alertImage.setBackgroundResource(R.drawable.alert11_1);
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert11_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 11 && messagesAlertLevel == 2) {				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert11_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 11 && messagesAlertLevel == 3) {				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert11_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 12 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert12_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 12 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert12_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 12 && messagesAlertLevel == 2) {				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert12_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 12 && messagesAlertLevel == 3) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert12_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 13 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert13_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 13 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert13_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 13 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert13_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 13 && messagesAlertLevel == 3) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert13_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 14 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert14_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 14 && messagesAlertLevel == 1) {				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert14_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 14 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert14_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 14 && messagesAlertLevel == 3) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(),R.drawable.alert14_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}
			
			if (messagesAlertType == 15 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert15_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 15 && messagesAlertLevel == 1) {				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert15_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 15 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert15_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 15 && messagesAlertLevel == 3) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(),R.drawable.alert15_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 2 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert2_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 2 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert2_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 2 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert2_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}
			
			if (messagesAlertType == 3 && messagesAlertLevel == 0){
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert3_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 3 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert3_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 3 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert3_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 4 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert4_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 4 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert4_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 4 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert4_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 5 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert5_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 5 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert5_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 5 && messagesAlertLevel == 3) {
				alertImage.setBackgroundResource(R.drawable.alert5_3);
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert5_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 6 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert6_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 6 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert6_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 6 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert6_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 7 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(),R.drawable.alert7_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 7 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(),R.drawable.alert7_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 7 && messagesAlertLevel == 2) {				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert7_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 8 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert8_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 8 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert8_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 9 && messagesAlertLevel == 0) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert9_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 9 && messagesAlertLevel == 1) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(),R.drawable.alert9_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 9 && messagesAlertLevel == 2) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert9_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 9 && messagesAlertLevel == 3) {
				
				Bitmap bitmap = BitmapFactory.decodeResource(ShowMessageByCatologue.this.getResources(), R.drawable.alert9_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}
			alertImage.setVisibility(View.VISIBLE);

		} catch (Exception e) {
			e.printStackTrace();
			alertImage.setVisibility(View.GONE);
		}

	}
	//发送预警背景图片id
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
			return R.drawable.alert3_0;
		}
		if (messagesAlertType == 3 && messagesAlertLevel == 1) {
			return R.drawable.alert3_1;
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
	//接收删除广播
	private BroadcastReceiver deleteMessgeByCatBroadcast = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if(action.equals(action)){
				srcInfos.clear();
				messageInfos.clear();
				srcInfos = new DBUtil(ShowMessageByCatologue.this).getAllMessageInfos2(ShowMessageByCatologue.this);
				if (srcInfos != null) {
					for (MessageInfo messageInfo : srcInfos) {
						if (messageInfo.fcatid == toShowCatologueId) {
							messageInfos.add(messageInfo);
						}
					}
				}
				
				if(myAdapter != null){
					myAdapter.notifyDataSetChanged();
				}
				
				System.out.println("接收删除广播");
			}
		}
		
	};
}
