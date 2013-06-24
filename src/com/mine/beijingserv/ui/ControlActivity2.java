package com.mine.beijingserv.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.mine.beijingserv.R;
import com.mine.beijingserv.model.MessageInfo;
import com.mine.beijingserv.sys.AppContex;
import com.mine.beijingserv.sys.DBUtil;
import com.mine.beijingserv.sys.SysUtils;
import com.mine.beijingserv.sys.ToastShow;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ControlActivity2 extends Activity implements View.OnClickListener{
	
	private TextView backTextview;
	private TextView allselecttextview;
	private ListView messageslist;
	private Vector<MessageInfo> srcInfo;
	private Vector<MessageInfo> deleteInfo;
	private DeleteAdpter myAdpter;
	private boolean isAllSelectFlag = false;
	private final int MESSAGE_ALL_CANCEL = 0;
	private final int MESSAGE_ALL_SELECT = 1;
	private final int MESSAGE_READSTATE = 2;
	private final int DELETE_MESSAGES = 3;
	private final int CANCEL_SINGLE_MESSAGE = 4;
	private final int IS_ALL_CHECKED = 5;
	HashMap<Integer,Boolean> map;
	private final String CONTROL_CHANGE = "com.zskt.control_change";
	private ImageView quanimage,yiduimage,shanchuimage;
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			int what = msg.what;
			switch(what){
			//取消全选
			case MESSAGE_ALL_CANCEL:
				deleteInfo.clear();
				System.out.println("deleteInfo.size():   "+deleteInfo.size());
				if(myAdpter != null){
					for(int i=0;i<srcInfo.size();i++){
						myAdpter.hashMap.put(i, false);
					}
					myAdpter.notifyDataSetChanged();
				}
				break;
				//选择全选
			case MESSAGE_ALL_SELECT:					
				deleteInfo.clear();
				deleteInfo.addAll(srcInfo);
				System.out.println("deleteInfo.size():   "+deleteInfo.size());
				if(myAdpter != null){
					for(int i=0;i<srcInfo.size();i++){
						myAdpter.hashMap.put(i, true);
					}
					myAdpter.notifyDataSetChanged();
				}
				break;
				//设置为已读
			case MESSAGE_READSTATE:
				System.out.println("deleteInfo  " +deleteInfo.size());
				for(final MessageInfo messageInfo:deleteInfo){	
					NotificationManager manager = (NotificationManager) ControlActivity2.this.getSystemService(Context.NOTIFICATION_SERVICE);
					manager.cancel(messageInfo.serversqlid);
					if(messageInfo.readState == MessageInfo.READ_STATE_UNREAD){
						messageInfo.readState = MessageInfo.READ_STATE_READED;
						new DBUtil(ControlActivity2.this).updateMessageInfo(messageInfo);
						System.out.println("updatetitle:  "+messageInfo.title);
						new Thread(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								sendReadState(messageInfo);
							}
							
						}).start();						
					}
				}
				srcInfo.clear();				
				deleteInfo.clear();
				srcInfo = new DBUtil(ControlActivity2.this).getAllMessageInfos2(ControlActivity2.this);
				if(myAdpter != null){
					isAllSelectFlag = false;
					for(int i=0;i<srcInfo.size();i++){
						myAdpter.hashMap.put(i, false);
					}
					myAdpter.notifyDataSetChanged();
					System.out.println("listview  更新了");
				}
				//发送状态更新广播
				Intent setReadstate = new Intent(CONTROL_CHANGE);
				ControlActivity2.this.sendBroadcast(setReadstate);
				break;
				
				//选择删除
			case DELETE_MESSAGES:
				new AlertDialog.Builder(ControlActivity2.this).setMessage("是否确认删除已选中的通知")
				.setNegativeButton("取消", null)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						for (final MessageInfo messageInfo : deleteInfo) {
							for(int i=0;i<srcInfo.size();i++){
								MessageInfo currentInfo = srcInfo.elementAt(i);					
								if(deleteInfo.contains(currentInfo)){
									System.out.println("delete dbmessage: "+currentInfo.serversqlid+"  "+currentInfo.title);
									new DBUtil(ControlActivity2.this).deleteMessageInfo(currentInfo.localsqlid);						
								}					
							}
							srcInfo.clear();				
							deleteInfo.clear();
							srcInfo = new DBUtil(ControlActivity2.this).getAllMessageInfos2(ControlActivity2.this);
							if(myAdpter != null){
								for(int i=0;i<srcInfo.size();i++){
									myAdpter.hashMap.put(i, false);
								}
								isAllSelectFlag = false;
								myAdpter.notifyDataSetChanged();
							}
						}
						srcInfo.clear();
						deleteInfo.clear();
						srcInfo = new DBUtil(ControlActivity2.this)
								.getAllMessageInfos2(ControlActivity2.this);
						if (myAdpter != null) {
							isAllSelectFlag = false;
							for (int i = 0; i < srcInfo.size(); i++) {
								myAdpter.hashMap.put(i, false);
							}
							myAdpter.notifyDataSetChanged();
							System.out.println("listview  更新了");
						}
						//发送状态更新广播
						Intent setReadstate = new Intent(CONTROL_CHANGE);
						ControlActivity2.this.sendBroadcast(setReadstate);
					}
				}).create().show();				
				break;
				//选择一个
			case CANCEL_SINGLE_MESSAGE:
				isAllSelectFlag = false;
				allselecttextview.setText("全选");
				quanimage.setBackgroundResource(R.drawable.tubiao1);
				break;
				//全选
			case IS_ALL_CHECKED:
				if(deleteInfo.size() == srcInfo.size()){
					isAllSelectFlag = true;
					handler.obtainMessage(MESSAGE_ALL_SELECT).sendToTarget();				
					quanimage.setBackgroundResource(R.drawable.tubiao1checked);
					yiduimage.setBackgroundResource(R.drawable.tubiao2);
					shanchuimage.setBackgroundResource(R.drawable.tubiao3);
					allselecttextview.setText("取消全选");
				}
				break;
			default:
				break;
			}
			
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.controllayout);		
		srcInfo = new DBUtil(this).getAllMessageInfos2(this);
		map = new HashMap<Integer,Boolean>();
		//需要用hashmap 来判断checkbox是否选上
		for(int i=0;i<srcInfo.size();i++){
			map.put(i, false);
		};	
		deleteInfo = new Vector<MessageInfo>();		
		backTextview = ((TextView)findViewById(R.id.controlbar_back));
		backTextview.setOnClickListener(this);
		messageslist = (ListView)findViewById(R.id.messageslist);
		myAdpter = new DeleteAdpter(map);
		messageslist.setAdapter(myAdpter);
		allselecttextview = (TextView)findViewById(R.id.selecttextview);
		findViewById(R.id.allselectlayout).setOnClickListener(this);
		findViewById(R.id.readstatelayout).setOnClickListener(this);
		findViewById(R.id.deletelayout).setOnClickListener(this);		
		quanimage = (ImageView)findViewById(R.id.quanimage);
		yiduimage = (ImageView)findViewById(R.id.yiduimage);
		shanchuimage = (ImageView)findViewById(R.id.shanchuimage);
		
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch(id){
		case R.id.controlbar_back:			
			finish();
			break;
		case R.id.allselectlayout:
			if(isAllSelectFlag == false){
				isAllSelectFlag = true;
				handler.obtainMessage(MESSAGE_ALL_SELECT).sendToTarget();				
				quanimage.setBackgroundResource(R.drawable.tubiao1checked);
				yiduimage.setBackgroundResource(R.drawable.tubiao2);
				shanchuimage.setBackgroundResource(R.drawable.tubiao3);
				allselecttextview.setText("取消全选");
			}else{
				isAllSelectFlag = false;
				allselecttextview.setText("全选");
				handler.obtainMessage(MESSAGE_ALL_CANCEL).sendToTarget();
				quanimage.setBackgroundResource(R.drawable.tubiao1);
				yiduimage.setBackgroundResource(R.drawable.tubiao2);
				shanchuimage.setBackgroundResource(R.drawable.tubiao3);				
			}
			
			break;	
			
		case R.id.readstatelayout:				
			quanimage.setBackgroundResource(R.drawable.tubiao1);
			yiduimage.setBackgroundResource(R.drawable.tubiao2checked);
			shanchuimage.setBackgroundResource(R.drawable.tubiao3);
			if(deleteInfo.size()==0){
				ToastShow.toastshow(ControlActivity2.this, "请您至少选择一条通知");
				break;
			}else{
				handler.obtainMessage(MESSAGE_READSTATE).sendToTarget();
			}
			break;
		case R.id.deletelayout:			
			quanimage.setBackgroundResource(R.drawable.tubiao1);
			yiduimage.setBackgroundResource(R.drawable.tubiao2);
			shanchuimage.setBackgroundResource(R.drawable.tubiao3checked);
			if(deleteInfo.size()==0){
				ToastShow.toastshow(ControlActivity2.this, "请您至少选择一条通知");
				break;
			}else{
				handler.obtainMessage(DELETE_MESSAGES).sendToTarget();
			}
			break;
		default :
			break;
		}
	}

	
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		deleteInfo.clear();
		srcInfo.clear();
	}



	private class DeleteAdpter extends BaseAdapter{
		private HashMap<Integer,Boolean> hashMap;
		
		public DeleteAdpter(HashMap<Integer,Boolean> map){
			hashMap = map;
		}
		@Override
		public int getCount() {			
			return srcInfo.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int postion, View convertview, ViewGroup arg2) {
			// TODO Auto-generated method stub
	
			convertview = null;
			convertview = LayoutInflater.from(ControlActivity2.this).inflate(R.layout.controlmessagelist, null);
			try{
			final MessageInfo messageInfo = srcInfo.elementAt(postion);			
			TextView deleteTitle = (TextView)convertview.findViewById(R.id.deletesrcinfo_title_textview);
			TextView deleteContent = (TextView)convertview.findViewById(R.id.deletesrcinfo_content);
			deleteTitle.setText(messageInfo.title);
			deleteContent.setText(messageInfo.content);
			CheckBox deleteCheck = (CheckBox)convertview.findViewById(R.id.mydelete_checkbox);
			
			if (messageInfo.readState == MessageInfo.READ_STATE_READED) {
				deleteTitle.setTextColor(0xff333333);
				deleteTitle.setTextAppearance(ControlActivity2.this,
						R.style.text_normal);
				deleteTitle.setTextColor(ControlActivity2.this
						.getResources().getColor(R.color.grey));
				deleteContent.setTextColor(ControlActivity2.this
						.getResources().getColor(R.color.grey));
				
			} else {
				deleteTitle.setTextColor(0xff222222);
				deleteTitle.setTextAppearance(ControlActivity2.this,
						R.style.text_bold);
				deleteContent.setTextColor(ControlActivity2.this
						.getResources().getColor(R.color.dark));
			}	

			if(hashMap.get(postion)){
				deleteCheck.setChecked(true);
			}else{
				deleteCheck.setChecked(false);
			}
			
			
			deleteCheck.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					// TODO Auto-generated method stub
					if(isChecked){
						hashMap.put(postion, true);
						deleteInfo.add(messageInfo);
						handler.obtainMessage(IS_ALL_CHECKED).sendToTarget();
						System.out.println("put "+postion+"  "+true);
						System.out.println("deleteInfo.size  " +deleteInfo.size());
					}else{						
						hashMap.put(postion, false);
						handler.obtainMessage(CANCEL_SINGLE_MESSAGE).sendToTarget();
						deleteInfo.remove(messageInfo);						
						System.out.println("put "+postion+"  "+false);
						System.out.println("deleteInfo.size  " +deleteInfo.size());
					}
				}
				
			});
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}		
			return convertview;
		}
		
	}
	//发送已读请求
	public void sendReadState(MessageInfo messageinfo){
		String SEND_READSTATE_Url = AppContex.SEND_READSTATE +"deviceid="+
				Uri.encode(SysUtils.getDeviceID(ControlActivity2.this))+"&msgid="+messageinfo.serversqlid
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
