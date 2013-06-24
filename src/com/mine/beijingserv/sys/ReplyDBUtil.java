package com.mine.beijingserv.sys;


import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import com.mine.beijingserv.model.ReplyModel;
import com.mine.beijingserv.model.MessageInfo;
import com.mine.beijingserv.model.ReplyModel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import android.util.Log;

public class ReplyDBUtil 
{
	final public static String DB_NAME = "myreplay.db";
	final public static String TABLE_NAME = "myreplytable";

	final public static String TITLE = "title";	
	final public static String CONTENT = "content";
	final public static String YEAR = "year";
	final public static String MONTH = "month";
	final public static String DAY = "day";
	final public static String HOUR = "hour";
	final public static String MIN = "min";
	final public static String _ID = "sqlid";
	
	
	private MySQLHelper mySQLHelper = null;
	
	public ReplyDBUtil(Context context)
	{
		mySQLHelper = new MySQLHelper(context);
	}
	
	/**
	 * 获取所有回复
	 * @param context
	 * @return
	 */
	public Vector<ReplyModel> getAllSavedReply()
	{
		Vector<ReplyModel> vector = new Vector<ReplyModel>();
		SQLiteDatabase db = null;
    	Cursor cursor = null;
		try
		{
			db = mySQLHelper.getWritableDatabase();
			cursor = db.query(ReplyDBUtil.TABLE_NAME, 
	    			null,null, null, null, null,ReplyDBUtil._ID);
	    	if(cursor.moveToFirst())
	    	{
	    		while(!cursor.isAfterLast())
		    	{
		    		ReplyModel replyModel = new ReplyModel();
		    		replyModel.title = cursor.getString(cursor.getColumnIndex(TITLE));
		    		replyModel.content = cursor.getString(cursor.getColumnIndex(CONTENT));
		    		replyModel.year = cursor.getInt(cursor.getColumnIndex(YEAR));
		    		replyModel.month = cursor.getInt(cursor.getColumnIndex(MONTH));
		    		replyModel.hour = cursor.getInt(cursor.getColumnIndex(HOUR));
		    		replyModel.min = cursor.getInt(cursor.getColumnIndex(MIN));
		    		replyModel._id = cursor.getInt(cursor.getColumnIndex(_ID));
		    		replyModel.day = cursor.getInt(cursor.getColumnIndex(DAY));
		    		vector.add(replyModel);
		    		cursor.moveToNext();
		    	}
	    	}	    	
	    	cursor.close();
	    	Comparator<ReplyModel> comparator = new Comparator<ReplyModel>(){

				@Override
				public int compare(ReplyModel v1, ReplyModel v2) {
					// TODO Auto-generated method stub
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
	    	
	    	Collections.sort(vector, comparator);
	    	
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
			return vector;
		}
		try {
			db.close();
		} catch (Exception e) {
		}
		return vector;
	}
	
	
	/*
	 * 判读回复是否已存在
	 */
	public boolean isReplyAlreadyExist(int serversqlid)
	{
		Vector<ReplyModel> replyModels = getAllSavedReply();
		for(ReplyModel item : replyModels)
		{
			if(item._id == serversqlid)
			{
				return true;
			}
		}
		return false;
	}
	/**
	 * 插入一条新的回复
	 * @param messageInfo
	 * @return
	 */
	public int insertNewReply(ReplyModel replyModel)
	{
		int id = -1;
		SQLiteDatabase db = null;
		try 
		{
			db = mySQLHelper.getWritableDatabase();
			ContentValues contentValues = new ContentValues();
			contentValues.put(_ID, replyModel._id);
			contentValues.put(YEAR, replyModel.year);
			contentValues.put(MONTH, replyModel.month);
			contentValues.put(DAY, replyModel.day);
			System.out.println("INSERT DAY:  "+replyModel.day);
			contentValues.put(HOUR, replyModel.hour);
			contentValues.put(MIN, replyModel.min);
			contentValues.put(TITLE, replyModel.title);
			contentValues.put(CONTENT, replyModel.content);
			
			id = (int)db.insert(ReplyDBUtil.TABLE_NAME, ReplyDBUtil._ID, contentValues);
			System.out.println("insert new replymodel");
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
	public boolean deleteReplay(int id)
	{
		SQLiteDatabase db = null;
		if(id <0)
		{
			return false;
		}
		try 
		{
			db = mySQLHelper.getWritableDatabase();
			db.delete(TABLE_NAME, ReplyDBUtil._ID+"=?", new String[]{id+""});
			Log.d("DELETE:   ", "DONE");
	    	
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
		public MySQLHelper(Context context) 
		{
			super(context, ReplyDBUtil.DB_NAME, null, 1);
		}
		public void onCreate(SQLiteDatabase db) 
		{
			
			String sql = "create table if not exists "
				+ TABLE_NAME+ " (" 
				+ _ID + " integer primary key,"
				+ YEAR + " integer,"
				+ MONTH +" integer,"
				+ DAY + " integer,"
				+ HOUR + " integer,"
				+ MIN + " integer,"
				+ TITLE + " varchar,"
				+ CONTENT + " varchar);";
			
			System.out.println(sql);
			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{			
			onCreate(db);
		}
	}
}


