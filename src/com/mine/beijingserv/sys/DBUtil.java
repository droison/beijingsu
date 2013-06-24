package com.mine.beijingserv.sys;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.json.JSONArray;

import com.mine.beijingserv.model.MessageInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Thumbnails;
import android.sax.Element;
import android.util.Log;

public class DBUtil 
{
	final public static String DB_NAME = "beijingservudb";
	final public static String MESSAGE_TABLE_NAME = "messages";
	/////////////////////////////////////////////每个消息的信息
	final public static String MESSAGE_TITLE = "title";//varchar
	final public static String MESSAGE_CONTENT = "content";//varchar
	final public static String MESSAGE_YEAR = "year";//int
	final public static String MESSAGE_MONTH = "month";//int
	final public static String MESSAGE_HOUR = "hour";//int
	final public static String MESSAGE_MINUTE = "minute";//int
	final public static String MESSAGE_DAY = "day";//int
	final public static String MESSAGE_READ_STATE = "read";//int
	final public static String MESSAGE_LOCAL_SQL_ID = "localid";//int
	final public static String MESSAGE_SERVER_SQL_ID = "serverid";
	final public static String MESSAGE_FCAT_ID = "fcatid";//int
	final public static String MESSAGE_SCAT_ID = "scatid";//int
	final public static String MESSAGE_TYPE = "type";//int
	final public static String MESSAGE_ALERTTYPE = "alerttype";//int
	final public static String MESSAGE_ALERTLEVEL = "alertlevel";//int
	final public static String MESSAGE_SAYGOOD = "saygood";//int
	
	private MySQLHelper mySQLHelper = null;
	
	public DBUtil(Context context)
	{
		mySQLHelper = new MySQLHelper(context);
	}
	
	/**
	 * 获取所有消息，按照时间、消息类型、已读状态排序
	 * @param context
	 * @return
	 */
	public Vector<MessageInfo> getAllMessageInfos(Context context)
	{
		Vector<MessageInfo> v = new Vector<MessageInfo>();
		SQLiteDatabase db = null;
    	Cursor cursor = null;
		try
		{
			db = mySQLHelper.getWritableDatabase();
			cursor = db.query(DBUtil.MESSAGE_TABLE_NAME, 
	    			null,null, null, null, null,DBUtil.MESSAGE_LOCAL_SQL_ID);
	    	if(cursor.moveToFirst())
	    	{
	    		while(!cursor.isAfterLast())
		    	{
		    		MessageInfo messageInfo = new MessageInfo();
		    		messageInfo.content = cursor.getString(cursor.getColumnIndex(MESSAGE_CONTENT));
		    		messageInfo.day = cursor.getInt(cursor.getColumnIndex(MESSAGE_DAY));
		    		messageInfo.fcatid = cursor.getInt(cursor.getColumnIndex(MESSAGE_FCAT_ID));
		    		messageInfo.month = cursor.getInt(cursor.getColumnIndex(MESSAGE_MONTH));
		    		messageInfo.readState = cursor.getInt(cursor.getColumnIndex(MESSAGE_READ_STATE));
		    		messageInfo.scatid = cursor.getInt(cursor.getColumnIndex(MESSAGE_SCAT_ID));
		    		messageInfo.localsqlid = cursor.getInt(cursor.getColumnIndex(MESSAGE_LOCAL_SQL_ID));
		    		messageInfo.serversqlid = cursor.getInt(cursor.getColumnIndex(MESSAGE_SERVER_SQL_ID));
		    		messageInfo.title = cursor.getString(cursor.getColumnIndex(MESSAGE_TITLE));
		    		messageInfo.year = cursor.getInt(cursor.getColumnIndex(MESSAGE_YEAR));
		    		messageInfo.hour = cursor.getInt(cursor.getColumnIndex(MESSAGE_HOUR));
		    		messageInfo.min = cursor.getInt(cursor.getColumnIndex(MESSAGE_MINUTE));
		    		messageInfo.type = cursor.getInt(cursor.getColumnIndex(MESSAGE_TYPE));
		    		messageInfo.alerttype = cursor.getInt(cursor.getColumnIndex(MESSAGE_ALERTTYPE));
		    		messageInfo.alertlevel = cursor.getInt(cursor.getColumnIndex(MESSAGE_ALERTLEVEL));
		    		messageInfo.saygood = cursor.getInt(cursor.getColumnIndex(MESSAGE_SAYGOOD));
		    		v.add(messageInfo);
		    		cursor.moveToNext();
		    	}
	    	}
	    	
	    	cursor.close();
	    	
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			try {
				cursor.close();
			} catch (Exception e2) {
			}
			try {
		    	db.close();
			} catch (Exception e2) {
			}
			return v;
		}
		try {
			db.close();
		} catch (Exception e) {
		}
		Comparator<MessageInfo> comparator = new Comparator<MessageInfo>()
		{
			public int compare(MessageInfo v1, MessageInfo v2) 
			{
				if(v1 == v2)
				{
					return 0;
				}
				long time1 = v1.year*12*30*24*60+v1.month*30*24*60+v1.day*24*60+v1.hour*60+v1.min;
				long time2 = v2.year*12*30*24*60+v2.month*30*24*60+v2.day*24*60+v2.hour*60+v2.min;
						
				if(time1 > time2)
				{
					return -1;
				}else if (time1 < time2) {
					return 1;
				}
				else {
					return 0;
				}
			}
		};		
		Collections.sort(v, comparator);
		
		Comparator<MessageInfo> typecomparator = new Comparator<MessageInfo>() {
			public int compare(MessageInfo v1, MessageInfo v2) {
				return v2.type-v1.type;
			}
		};
		Collections.sort(v,typecomparator);
		
		Comparator<MessageInfo> readStateComparator = new Comparator<MessageInfo>() {

			@Override
			public int compare(MessageInfo v1, MessageInfo v2) {
				// TODO Auto-generated method stub
				int state1 = v1.readState;
				int state2 = v2.readState;
				if(state1<state2){
					return -1;
				}else if(state1>state2){
					return 1;
				}else{
					return 0;
				}
				
				
			}
		};
		
		Collections.sort(v, readStateComparator);
		
		return v;
	}
	//获得部门所有消息，按照时间、类型排序
	public Vector<MessageInfo> getAllMessageInfos2(Context context)
	{
		Vector<MessageInfo> v = new Vector<MessageInfo>();
		SQLiteDatabase db = null;
    	Cursor cursor = null;
		try
		{
			db = mySQLHelper.getWritableDatabase();
			cursor = db.query(DBUtil.MESSAGE_TABLE_NAME, 
	    			null,null, null, null, null,DBUtil.MESSAGE_LOCAL_SQL_ID);
	    	if(cursor.moveToFirst())
	    	{
	    		while(!cursor.isAfterLast())
		    	{
		    		MessageInfo messageInfo = new MessageInfo();
		    		messageInfo.content = cursor.getString(cursor.getColumnIndex(MESSAGE_CONTENT));
		    		messageInfo.day = cursor.getInt(cursor.getColumnIndex(MESSAGE_DAY));
		    		messageInfo.fcatid = cursor.getInt(cursor.getColumnIndex(MESSAGE_FCAT_ID));
		    		messageInfo.month = cursor.getInt(cursor.getColumnIndex(MESSAGE_MONTH));
		    		messageInfo.readState = cursor.getInt(cursor.getColumnIndex(MESSAGE_READ_STATE));
		    		messageInfo.scatid = cursor.getInt(cursor.getColumnIndex(MESSAGE_SCAT_ID));
		    		messageInfo.localsqlid = cursor.getInt(cursor.getColumnIndex(MESSAGE_LOCAL_SQL_ID));
		    		messageInfo.serversqlid = cursor.getInt(cursor.getColumnIndex(MESSAGE_SERVER_SQL_ID));
		    		messageInfo.title = cursor.getString(cursor.getColumnIndex(MESSAGE_TITLE));
		    		messageInfo.year = cursor.getInt(cursor.getColumnIndex(MESSAGE_YEAR));
		    		messageInfo.hour = cursor.getInt(cursor.getColumnIndex(MESSAGE_HOUR));
		    		messageInfo.min = cursor.getInt(cursor.getColumnIndex(MESSAGE_MINUTE));
		    		messageInfo.type = cursor.getInt(cursor.getColumnIndex(MESSAGE_TYPE));
		    		messageInfo.alerttype = cursor.getInt(cursor.getColumnIndex(MESSAGE_ALERTTYPE));
		    		messageInfo.alertlevel = cursor.getInt(cursor.getColumnIndex(MESSAGE_ALERTLEVEL));
		    		messageInfo.saygood = cursor.getInt(cursor.getColumnIndex(MESSAGE_SAYGOOD));
		    		v.add(messageInfo);
		    		cursor.moveToNext();
		    	}
	    	}
	    	
	    	cursor.close();
	    	
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			try {
				cursor.close();
			} catch (Exception e2) {
			}
			try {
		    	db.close();
			} catch (Exception e2) {
			}
			return v;
		}
		try {
			db.close();
		} catch (Exception e) {
		}
		Comparator<MessageInfo> comparator = new Comparator<MessageInfo>()
		{
			public int compare(MessageInfo v1, MessageInfo v2) 
			{
				if(v1 == v2)
				{
					return 0;
				}
				long time1 = v1.year*12*30*24*60+v1.month*30*24*60+v1.day*24*60+v1.hour*60+v1.min;
				long time2 = v2.year*12*30*24*60+v2.month*30*24*60+v2.day*24*60+v2.hour*60+v2.min;
						
				if(time1 > time2)
				{
					return -1;
				}else if (time1 < time2) {
					return 1;
				}
				else {
					return 0;
				}
			}
		};		
		Collections.sort(v, comparator);
		Comparator<MessageInfo> typecomparator = new Comparator<MessageInfo>() {
			public int compare(MessageInfo v1, MessageInfo v2) {
				return v2.type-v1.type;
			}
		};
		Collections.sort(v,typecomparator);		
		return v;
	}
	
	
	
	
	
	/**
	 * 判断一个消息是否已经存在
	 * @param messageInfo
	 * @param context
	 * @return
	 */
	public boolean isMessageAlreadyExist(int serversqlid,Context context)
	{
		Vector<MessageInfo> messageInfos = getAllMessageInfos(context);
		for(MessageInfo item : messageInfos)
		{
			if(item.serversqlid == serversqlid)
			{
				return true;
			}
		}
		return false;
	}
	
	//通过id获得消息
	public MessageInfo getMessageAlready(int serversqlid,Context context)
	{
		Vector<MessageInfo> messageInfos = getAllMessageInfos(context);
		for(MessageInfo item : messageInfos)
		{
			if(item.serversqlid == serversqlid)
			{
				return item;
			}
		}
		return null;
	}
	
	
	
	/**
	 * 更新一个已有的model
	 * @param shareItem
	 * @return
	 */
	public boolean updateMessageInfo(MessageInfo messageInfo)
	{
		if(messageInfo == null)
		{
			return false;
		}
		if(messageInfo.localsqlid <0)
		{
			return false;
		}
		SQLiteDatabase db = null;		
		try
		{
			db = mySQLHelper.getWritableDatabase();
			ContentValues contentValues = new ContentValues();
			contentValues.put(DBUtil.MESSAGE_CONTENT, messageInfo.content);
			contentValues.put(DBUtil.MESSAGE_DAY, messageInfo.day);
			contentValues.put(DBUtil.MESSAGE_FCAT_ID, messageInfo.fcatid);
			contentValues.put(DBUtil.MESSAGE_LOCAL_SQL_ID, messageInfo.localsqlid);
			contentValues.put(DBUtil.MESSAGE_MONTH, messageInfo.month);
			contentValues.put(DBUtil.MESSAGE_READ_STATE, messageInfo.readState);
			contentValues.put(DBUtil.MESSAGE_SCAT_ID, messageInfo.scatid);
			contentValues.put(DBUtil.MESSAGE_SERVER_SQL_ID, messageInfo.serversqlid);
			contentValues.put(DBUtil.MESSAGE_TITLE, messageInfo.title);
			contentValues.put(DBUtil.MESSAGE_YEAR, messageInfo.year);
			contentValues.put(DBUtil.MESSAGE_HOUR, messageInfo.hour);
			contentValues.put(DBUtil.MESSAGE_MINUTE, messageInfo.min);
			contentValues.put(DBUtil.MESSAGE_TYPE, messageInfo.type);
			contentValues.put(DBUtil.MESSAGE_ALERTTYPE, messageInfo.alerttype);
			contentValues.put(DBUtil.MESSAGE_ALERTLEVEL, messageInfo.alertlevel);
			contentValues.put(DBUtil.MESSAGE_SAYGOOD, messageInfo.saygood);
	    	db.update(DBUtil.MESSAGE_TABLE_NAME, contentValues, DBUtil.MESSAGE_LOCAL_SQL_ID+"=?", new String[]{messageInfo.localsqlid+""});
		
	    	System.out.println("UPDATE COMPLETED");
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			try {
				db.close();
			} catch (Exception e2) {
			}
			return false;
		}
		try {
			db.close();
		} catch (Exception e) {
		}
		return true;
	}
	//获得消息已读状态
	public void checkReadState(MessageInfo messageInfo,Context context){

		Vector<MessageInfo> messageInfos = getAllMessageInfos(context);
		for(MessageInfo item : messageInfos)
		{
			if(item.serversqlid == messageInfo.serversqlid)
			{
				messageInfo.readState = item.readState;
				System.out.println("已匹配readstate");
			}
		}
	}
	
	/**
	 * 插入一条新的消息
	 * @param messageInfo
	 * @return
	 */
	public int insertNewMessageInfo(MessageInfo messageInfo)
	{
		if(messageInfo == null)
		{
			return -1;
		}
		int id = -1;
		SQLiteDatabase db = null;
		try 
		{
			db = mySQLHelper.getWritableDatabase();
			ContentValues contentValues = new ContentValues();
			contentValues.put(DBUtil.MESSAGE_CONTENT, messageInfo.content);
			contentValues.put(DBUtil.MESSAGE_DAY, messageInfo.day);
			contentValues.put(DBUtil.MESSAGE_FCAT_ID, messageInfo.fcatid);
			contentValues.put(DBUtil.MESSAGE_MONTH, messageInfo.month);
			contentValues.put(DBUtil.MESSAGE_READ_STATE, messageInfo.readState);
			contentValues.put(DBUtil.MESSAGE_SCAT_ID, messageInfo.scatid);
			contentValues.put(DBUtil.MESSAGE_SERVER_SQL_ID, messageInfo.serversqlid);
			contentValues.put(DBUtil.MESSAGE_TITLE, messageInfo.title);
			contentValues.put(DBUtil.MESSAGE_YEAR, messageInfo.year);
			contentValues.put(DBUtil.MESSAGE_HOUR, messageInfo.hour);
			contentValues.put(DBUtil.MESSAGE_MINUTE, messageInfo.min);
			contentValues.put(DBUtil.MESSAGE_TYPE, messageInfo.type);
			contentValues.put(DBUtil.MESSAGE_ALERTTYPE, messageInfo.alerttype);
			contentValues.put(DBUtil.MESSAGE_ALERTLEVEL, messageInfo.alertlevel);	
			contentValues.put(DBUtil.MESSAGE_SAYGOOD, messageInfo.saygood);	
			messageInfo.localsqlid = (int)db.insert(DBUtil.MESSAGE_TABLE_NAME, DBUtil.MESSAGE_LOCAL_SQL_ID, contentValues);
			System.out.println("INSERT_MESSAGE: COMPLETED");
		}
		catch (Exception e) 
		{
			e.printStackTrace();		
			try {
				db.close();
			} catch (Exception e2) {
			}
			return -1;
		}
		try {
			db.close();
		} catch (Exception e2) {
		}
		return id;
	}
	
	
	
	/**
	 * 删除一条纪录或全部纪录
	 * @param id
	 * @return
	 */
	public boolean deleteMessageInfo(int id)
	{
		SQLiteDatabase db = null;
		if(id <0)
		{
			return false;
		}
		try 
		{
			db = mySQLHelper.getWritableDatabase();
			db.delete(DBUtil.MESSAGE_TABLE_NAME, DBUtil.MESSAGE_LOCAL_SQL_ID+"=?", new String[]{id+""});
	    	
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			try {
				db.close();
			} catch (Exception e2) {
			}
			return false;
		}
		try {
			db.close();
		} catch (Exception e) {
		}
		return true;
	}
	
	
	
	class MySQLHelper extends SQLiteOpenHelper 
	{
		public final static int TABLE_VERSION = 5;
		public MySQLHelper(Context context) 
		{
			super(context, DBUtil.DB_NAME, null, TABLE_VERSION);
		}
		
		
		
		public void onCreate(SQLiteDatabase db) 
		{
			////创建shareitem表
			String sql = "create table if not exists "
				+ DBUtil.MESSAGE_TABLE_NAME+ " (" 
				+ DBUtil.MESSAGE_LOCAL_SQL_ID + " integer primary key,"
				+ DBUtil.MESSAGE_CONTENT + " varchar,"
				+ DBUtil.MESSAGE_DAY+ " integer,"
				+ DBUtil.MESSAGE_HOUR+ " integer,"
				+ DBUtil.MESSAGE_MINUTE+ " integer,"
				+ DBUtil.MESSAGE_FCAT_ID+ " integer," 
				+ DBUtil.MESSAGE_MONTH+ " integer," 
				+ DBUtil.MESSAGE_READ_STATE + " integer," 
				+ DBUtil.MESSAGE_SCAT_ID + " integer,"
				+ DBUtil.MESSAGE_SERVER_SQL_ID + " integer,"
				+ DBUtil.MESSAGE_TYPE + " integer,"
				+ DBUtil.MESSAGE_TITLE+ " varchar," 
				+ DBUtil.MESSAGE_ALERTTYPE+ " integer,"
				+ DBUtil.MESSAGE_ALERTLEVEL+ " integer,"
				+ DBUtil.MESSAGE_SAYGOOD + " integer,"
				+ DBUtil.MESSAGE_YEAR + " integer);";
			db.execSQL(sql);
			System.out.println("TABLE ONCREATE");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{
			String sqlString1 = "drop table "+DBUtil.MESSAGE_TABLE_NAME;
			System.out.println("SQLITE1:  "+sqlString1);		
			db.execSQL(sqlString1);			
			onCreate(db);
		}



		
	}
}


