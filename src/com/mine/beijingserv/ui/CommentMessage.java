package com.mine.beijingserv.ui;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.mine.beijingserv.R;
import com.mine.beijingserv.sys.AppContex;
import com.mine.beijingserv.sys.SysUtils;


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

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class CommentMessage extends Activity implements View.OnClickListener
{
	private static final int ACTIVITY_REQUEST_PICK_LOCAL_PHOTO = 0;
	private static final int ACTIVITY_REQUEST_TAKE_NEW_PHOTO = 1;
	
	private AlertDialog chooseMediaTypeAlertDialog = null;///选择对话框
	private File imagefile = null;
	private EditText commentedit;
	private String contentString = null;
	private String filepathNameString =null;
	private ProgressDialog pregressDialog = null;
	private final int SUCEED_SEND = 1;
	private final int UNSUCEED_SEND = 2;
	private AlertDialog alertDialog;
	private ImageView photoimage;
	
	
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			switch(msg.what){
			//发送成功
			case SUCEED_SEND:
				pregressDialog.dismiss();
				 alertDialog = 	new AlertDialog.Builder(CommentMessage.this).setMessage("发送成功").setNegativeButton("确定",
						new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				}).show();
				
				 commentedit.setText("");
				 photoimage.setBackgroundColor(CommentMessage.this.getResources().getColor(R.color.commentbackground));
				break;
				//发送失败
			case UNSUCEED_SEND:
				pregressDialog.dismiss();
				 alertDialog = 	new AlertDialog.Builder(CommentMessage.this).setMessage("发送失败").setNegativeButton("确定",
						new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				}).show();
				commentedit.setText("");
				break;
				default :
					break;
			}
		}

		
		
		
	};
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.commentmessage);
		pregressDialog = new ProgressDialog(CommentMessage.this);
		pregressDialog.setTitle("正在提交 ");
		pregressDialog.setMessage("请稍后");

		commentedit = (EditText)findViewById(R.id.comment_edittext);
		photoimage = (ImageView)findViewById(R.id.photoimage);
		findViewById(R.id.topbar_back).setOnClickListener(this);
		findViewById(R.id.comment_ok_button).setOnClickListener(this);	
		findViewById(R.id.uploadpic).setOnClickListener(this);
		
		chooseMediaTypeAlertDialog = new AlertDialog.Builder(this)
		.setTitle("选择多媒体")
		.setItems(new String[]{"选取本地图片","拍摄新照片"}, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(which == 0)
				{
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(intent, ACTIVITY_REQUEST_PICK_LOCAL_PHOTO);
				}
				if(which == 1)
				{
					imagefile = new File(Environment.getExternalStorageDirectory(), "temp"+System.currentTimeMillis()+".jpg");
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imagefile));
					startActivityForResult(intent, ACTIVITY_REQUEST_TAKE_NEW_PHOTO);
				}
			}
		})
		.setNegativeButton("取消", null)
		.create();
	}
	
	public void onClick(View v) 
	{
		switch (v.getId()) {
	
		case R.id.topbar_back:
			finish();
			break;
		case R.id.comment_ok_button:
			contentString = commentedit.getText().toString();
			if(SysUtils.isStringEmpty(contentString)){
				new AlertDialog.Builder(CommentMessage.this).setMessage("内容不能为空").setNegativeButton("确定", 
						new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								dialog.cancel();
							}
						}).show();
				break;
			}			
			
			if(SysUtils.checkNetworkConnectedStat(CommentMessage.this)){
				pregressDialog.show();
				new SubmitPhotoThread(filepathNameString,contentString).start();	
				
			}else{
				Toast.makeText(CommentMessage.this, "网络未连接", Toast.LENGTH_SHORT).show();
			}
					
			break;
			
		case R.id.uploadpic:
			chooseMediaTypeAlertDialog.show();
			break;
		default:
			break;
		}
	}
	
	
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			if(resultCode != RESULT_OK)
			{
				return;
			}
			
			
			switch (requestCode) 
			{
				case ACTIVITY_REQUEST_PICK_LOCAL_PHOTO:
					try {
						Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
		        		cursor.moveToFirst();
		        		String filepath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
		        		cursor.close();
		        		File imageFile = new File(filepath);
		        		System.out.println(imageFile.length());
		        		filepathNameString = filepath;
		        		BitmapFactory.Options options = new BitmapFactory.Options();
		        		options.inJustDecodeBounds=true; 
		        		int sampleSize = 8;
		        		options.inJustDecodeBounds = false;
		        		options.inSampleSize = sampleSize;		        		
		        		Bitmap bitmap = BitmapFactory.decodeFile(filepathNameString,options);		        		
		        		photoimage.setBackgroundDrawable(new BitmapDrawable(bitmap));
		        		/////!!!!!注意获取评论内容
					} catch (Exception e) {
					}
					break;
				case ACTIVITY_REQUEST_TAKE_NEW_PHOTO:
					try {
						System.out.println(imagefile.length());
						filepathNameString = imagefile.getAbsolutePath();
						BitmapFactory.Options options = new BitmapFactory.Options();
		        		options.inJustDecodeBounds=true; 
		        		int sampleSize = 8;
		        		options.inJustDecodeBounds = false;
		        		options.inSampleSize = sampleSize;		        		
		        		Bitmap bitmap = BitmapFactory.decodeFile(filepathNameString,options);
		        		//photoimage.setImageBitmap(bitmap);
		        		photoimage.setBackgroundDrawable(new BitmapDrawable(bitmap));
					} catch (Exception e) {
					}
					break;
				default:
					break;
			}
			
		}
	 
	 //发送提交请求
	private class SubmitPhotoThread extends Thread {
		private String fileString = null;
		private String commentString = null;

		public SubmitPhotoThread(String filepath, String comment) {
			fileString = filepath;
			commentString = comment;
		}

		public void run() {
			super.run();
			String URL = AppContex.SEND_COMMENT_API + "comment="
					+ Uri.encode(commentString) + "&deviceid="
					+ Uri.encode(SysUtils.getDeviceID(CommentMessage.this))
					+ "&msgid=" + AppContex.curMessageInfo.serversqlid;
			System.out.println("HTTPPOST:    " + URL);
			HttpPost httpPost = new HttpPost(URL);
			if(fileString != null&& fileString.trim().length()>0){
				MultipartEntity multipartEntity = new MultipartEntity();
				try {
					multipartEntity.addPart("image", new FileBody(new File(
							fileString), "image/*"));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				httpPost.setEntity(multipartEntity);
			}			

			HttpResponse httpResponse = null;
			try {
				httpResponse = new DefaultHttpClient().execute(httpPost);
				System.out.println("getStatusCode = "
						+ httpResponse.getStatusLine().getStatusCode());
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Message msg = new Message();
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				msg.what = SUCEED_SEND;
				handler.sendMessage(msg);

			} else {
				msg.what = UNSUCEED_SEND;
				handler.sendMessage(msg);
			}

		}

	}
}

