package com.mine.beijingserv.ui;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.mine.beijingserv.R;
import com.mine.beijingserv.model.ReplyModel;
import com.mine.beijingserv.sys.AppContex;
import com.mine.beijingserv.sys.ReplyDBUtil;
import com.mine.beijingserv.sys.SysUtils;
import com.mine.beijingserv.sys.ToastShow;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TabInteractive extends Activity implements View.OnClickListener,AdapterView.OnItemClickListener
{
	
	private AlertDialog quiteAlertDialog = null;
	private boolean shouldsuperfinish = false;	
	private EditText commentedit;
	private String contentString = null;	
	private ProgressDialog pregressDialog = null;
	private final int LOAD_RELAYS_OVER = 0;
	private final int SUCEED_SEND = 1;
	private final int UNSUCEED_SEND = 2;
	private final int EDITTEXT_CHANGE = 3;	
	private TextView suggstiontext,aplaytextview;
	private RelativeLayout suggstionlayout,aplaylayout,suggstionfinallayout,tabinteractivelayout;
	private ImageView suggstionline,aplayline;
	private ListView repaylistview;
	private ImageView  imageline;
	private ReplayAdapter myAdapter;
	private Vector<ReplyModel> replyVector;
	private ProgressDialog progressDialog;
	private String contentLaterString;
	private TextView  xiexietextview,intructtextview,jieshaotextview;
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			switch(msg.what){
			//刷新回复列表
			case LOAD_RELAYS_OVER:
				replyVector.clear();
				replyVector = new ReplyDBUtil(TabInteractive.this).getAllSavedReply();
				if(myAdapter != null){
					myAdapter.notifyDataSetChanged();
					System.out.println("THE ADPTER IS REFRESH   ");
				}
				break;
			case SUCEED_SEND:				
				Toast.makeText(TabInteractive.this, "发送成功", 1000).show();
				commentedit.setText("");
				break;
				
			case UNSUCEED_SEND:				
				Toast.makeText(TabInteractive.this, "发送失败", 1000).show();
				break;
			case EDITTEXT_CHANGE:
				commentedit.setText(contentLaterString);
				break;
			default :
				break;
			}
		}

		
		
		
	};
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabinteractive);
		pregressDialog = new ProgressDialog(TabInteractive.this);
		pregressDialog.setTitle("正在提交 ");
		pregressDialog.setMessage("请稍后");
		replyVector = new Vector<ReplyModel> ();
		replyVector = new ReplyDBUtil(this).getAllSavedReply();
		
		xiexietextview = (TextView)findViewById(R.id.xiexietextview);
		intructtextview = (TextView)findViewById(R.id.intructtextview);
		jieshaotextview = (TextView)findViewById(R.id.jieshaotextview);		
		commentedit = (EditText)findViewById(R.id.commentedit);
		//添加最大字数监听
		commentedit.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
				if(commentedit.length()>140){
					ToastShow.toastshow(TabInteractive.this, "为保证您的建议及时传给我们，请不要超过最多字数限制");
					String contentFrom = commentedit.getEditableText().toString();
					contentLaterString = contentFrom.substring(0, 140);
					handler.obtainMessage(EDITTEXT_CHANGE).sendToTarget();					
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		findViewById(R.id.addphotobutton).setOnClickListener(this);
		quiteAlertDialog = new AlertDialog.Builder(this)
		.setTitle("退出应用?")
		.setMessage("点击确定按钮退出北京服务您")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				try {
					SysUtils.log("当前需要销毁activity数量 = "+AppContex.activities.size());
					for(Activity activity : AppContex.activities)
					{
						activity.finish();
					}
					AppContex.APPCATION_ON = false;
					AppContex.tempInfos.clear();
					finish();
				} catch (Exception e) {
					e.printStackTrace();
				}
				shouldsuperfinish = true;
				onBackPressed();
			}
		})
		.setNegativeButton("取消", null)
		.create();		
		suggstionlayout = (RelativeLayout)findViewById(R.id.suggstionlayout);
		suggstionlayout.setOnClickListener(this);
		aplaylayout = (RelativeLayout)findViewById(R.id.aplaylayout);
		aplaylayout.setOnClickListener(this);		
		suggstiontext = (TextView)findViewById(R.id.suggstiontext);
		aplaytextview = (TextView)findViewById(R.id.aplaytextview);		
		suggstionline = (ImageView)findViewById(R.id.suggstionline);
		aplayline = (ImageView)findViewById(R.id.aplayline);
		repaylistview = (ListView)findViewById(R.id.repaylistview);
		imageline = (ImageView)findViewById(R.id.imageline);
		myAdapter = new ReplayAdapter();
		repaylistview.setAdapter(myAdapter);
		repaylistview.setOnItemClickListener(this);
		suggstionfinallayout = (RelativeLayout)findViewById(R.id.suggstionfinallayout);
		tabinteractivelayout = (RelativeLayout)findViewById(R.id.tabinteractivelayout);
		
	}
	
	@Override
	public void onBackPressed() {
		if(shouldsuperfinish)
		{
			super.onBackPressed();
			return;
		}
		quiteAlertDialog.show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(1, 1, 1, "退出");
		return true;
	}
	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		quiteAlertDialog.show();
		return true;
	}
	
	
	public void onClick(View v) 
	{
		switch (v.getId()) {
		//添加建议
		case R.id.addphotobutton:
			contentString = commentedit.getText().toString();
			if(SysUtils.isStringEmpty(contentString)){
				new AlertDialog.Builder(TabInteractive.this).setMessage("内容不能为空").setNegativeButton("确定", 
						new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								dialog.cancel();
							}
						}).show();
				break;
			}			
			pregressDialog.show();
			if(SysUtils.checkNetworkConnectedStat(TabInteractive.this)){
				new SendFeedbackThread().start();	
			}else{
				ToastShow.toastshow(TabInteractive.this, "网络未连接,请您检查网络设置");
			}
					
			break;
			//查看回复
		case R.id.aplaylayout:
			suggstiontext.setTextColor(TabInteractive.this.getResources().getColor(R.color.black));
			suggstionline.setVisibility(View.INVISIBLE);
			aplaytextview.setTextColor(TabInteractive.this.getResources().getColor(R.color.red));
			aplayline.setVisibility(View.VISIBLE);
			repaylistview.setVisibility(View.VISIBLE);
			imageline.setVisibility(View.VISIBLE);
			suggstionfinallayout.setVisibility(View.INVISIBLE);
			tabinteractivelayout.setBackgroundColor(TabInteractive.this.getResources().getColor(R.color.white));
			if(SysUtils.checkNetworkConnectedStat(TabInteractive.this)){
				new GetNewReplysThread().start();
				progressDialog = new ProgressDialog(this);
				progressDialog.setMessage("正在加载....");
				progressDialog.show();
				progressDialog.setCancelable(true);
			}else{
				handler.obtainMessage(LOAD_RELAYS_OVER).sendToTarget();
			}
			break;
			//显示建议页面
		case R.id.suggstionlayout:
			suggstiontext.setTextColor(TabInteractive.this.getResources().getColor(R.color.red));
			suggstionline.setVisibility(View.VISIBLE);
			aplaytextview.setTextColor(TabInteractive.this.getResources().getColor(R.color.black));
			aplayline.setVisibility(View.INVISIBLE);
			repaylistview.setVisibility(View.INVISIBLE);
			imageline.setVisibility(View.INVISIBLE);
			suggstionfinallayout.setVisibility(View.VISIBLE);
			tabinteractivelayout.setBackgroundColor(TabInteractive.this.getResources().getColor(R.color.commentbackground));
			if(progressDialog!=null){
				progressDialog.dismiss();
			}
			
			break;
		
		default:
			break;
		}
	}
	//发送建议线程
	private class SendFeedbackThread extends Thread
	{
		public boolean flag = false;
		
		public void run() {
			super.run();
			flag = true;
			try {
				String url = AppContex.SEND_SUGGESTION+"deviceid="+Uri.encode(SysUtils.getDeviceID(TabInteractive.this))+"&suggest="+contentString;
				SysUtils.log(url);
    			HttpGet httpGet = new HttpGet(url);
				HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
				if(!flag)
				{
					return;
				}
				SysUtils.log("httpResponse.getStatusLine().getStatusCode()="+httpResponse.getStatusLine().getStatusCode());
				if(httpResponse.getStatusLine().getStatusCode() == 200)
				{	
					pregressDialog.dismiss();
					Message msg = new Message();
					msg.what = SUCEED_SEND;
					handler.sendMessage(msg);
				
				}else {
					pregressDialog.dismiss();
					Message msg = new Message();
					msg.what = UNSUCEED_SEND;
					handler.sendMessage(msg);
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		commentedit.setText("");
	}
	//回复listview adapter 
	private class ReplayAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			
			return replyVector.size();
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
		public View getView(int postion, View convertView, ViewGroup arg2) {
			// TODO Auto-generated method stub
			if(convertView == null){
				convertView = LayoutInflater.from(TabInteractive.this).inflate(R.layout.replay_list_layout, null);
			}
			
			ReplyModel replyModel = replyVector.elementAt(postion);
			((TextView)convertView.findViewById(R.id.replytitle)).setText(replyModel.title);
			((TextView)convertView.findViewById(R.id.replytime)).setText(replyModel.getFormatedTimeWithoutYear());
			
			return convertView;
		}
		
	}
	//进入回复详情页面
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int postion, long arg3) {
		// TODO Auto-generated method stub
		AppContex.curReplyModel = replyVector.elementAt(postion);
		Intent intent = new Intent(TabInteractive.this,ReplyInfoActivity.class);
		startActivity(intent);
	}
	//发送获取回复请求
	private class GetNewReplysThread extends Thread{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			String urlString = AppContex.GET_ALLREPLYS+"deviceid="+Uri.encode(SysUtils.getDeviceID(TabInteractive.this));
			System.out.println("getallreplysAPI:   :"+urlString);
			HttpGet httpGet = new HttpGet(urlString);
			try {
				HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
				if(httpResponse.getStatusLine().getStatusCode() == 200 ){
					
					String resultString = EntityUtils.toString(httpResponse.getEntity());
					System.out.println("resultString:   "+resultString);
					try {
						JSONArray jsonarray = new JSONArray(resultString);
						for(int i=0;i<jsonarray.length();i++){
							ReplyModel replyModel = new ReplyModel();
							ReplyDBUtil replyDBUtil = new ReplyDBUtil(TabInteractive.this);
							JSONObject jsonObject = jsonarray.getJSONObject(i);
							replyModel._id = jsonObject.getInt("id");
							replyModel.year = jsonObject.getInt("year");
							replyModel.month = jsonObject.getInt("month");
							replyModel.day = jsonObject.getInt("day");
							System.out.println("REPLY  DAY:  "+replyModel.day);
							replyModel.hour = jsonObject.getInt("hour");
							replyModel.min = jsonObject.getInt("min");
							replyModel.title = jsonObject.getString("title");
							replyModel.content = jsonObject.getString("content");
							
							if(replyDBUtil.isReplyAlreadyExist(replyModel._id)){
								System.out.println("the reply is already in db");
							}else{
								replyDBUtil.insertNewReply(replyModel);
							}
						}
						progressDialog.dismiss();
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						progressDialog.dismiss();
					}
					
				}else {
					System.out.println("conneting error");
					progressDialog.dismiss();
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				progressDialog.dismiss();
			}
			
			handler.obtainMessage(LOAD_RELAYS_OVER).sendToTarget();
		}
		
	}	    	

}
