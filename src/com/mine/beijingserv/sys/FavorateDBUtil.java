package com.mine.beijingserv.sys;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.json.JSONArray;

import com.mine.beijingserv.model.FavourModel;
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

public class FavorateDBUtil 
{
	final public static String DB_NAME = "myfavoritiesdb";
	final public static String FAVOR_TABLE_NAME = "myfavoritiestable";
	/////////////////////////////////////////////每个消息的信息
	final public static String FAVOR_LOCAL_SQL_ID = "localid";//int
	final public static String MESSAGE_MESSAGE_INFO_ID = "infoid";//int
	
	private MySQLHelper mySQLHelper = null;
	
	public FavorateDBUtil(Context context)
	{
		mySQLHelper = new MySQLHelper(context);
	}
	
	/**
	 * 获取所有收藏消息
	 * @param context
	 * @return
	 */
	public Vector<FavourModel> getAllSavedFavors(Context context)
	{
		Vector<FavourModel> v = new Vector<FavourModel>();
		SQLiteDatabase db = null;
    	Cursor cursor = null;
		try
		{
			db = mySQLHelper.getWritableDatabase();
			cursor = db.query(FavorateDBUtil.FAVOR_TABLE_NAME, 
	    			null,null, null, null, null,FavorateDBUtil.FAVOR_LOCAL_SQL_ID);
	    	if(cursor.moveToFirst())
	    	{
	    		while(!cursor.isAfterLast())
		    	{
		    		FavourModel favourModel = new FavourModel();
		    		favourModel.messageinfoid = cursor.getInt(cursor.getColumnIndex(MESSAGE_MESSAGE_INFO_ID));
		    		favourModel.id = cursor.getInt(cursor.getColumnIndex(FAVOR_LOCAL_SQL_ID));
		    		v.add(favourModel);
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
		return v;
	}
	
	
	//判读收藏是否存在
	public boolean isMessageAlreadyExist(int serversqlid,Context context)
	{
		Vector<FavourModel> messageInfos = getAllSavedFavors(context);
		for(FavourModel item : messageInfos)
		{
			if(item.messageinfoid == serversqlid)
			{
				return true;
			}
		}
		return false;
	}
	/**
	 * 插入一条新的消息
	 * @param messageInfo
	 * @return
	 */
	public int insertNewFavor(MessageInfo messageInfo)
	{
		int id = -1;
		SQLiteDatabase db = null;
		try 
		{
			db = mySQLHelper.getWritableDatabase();
			ContentValues contentValues = new ContentValues();
			contentValues.put(FavorateDBUtil.MESSAGE_MESSAGE_INFO_ID, messageInfo.localsqlid);
			id = (int)db.insert(FavorateDBUtil.FAVOR_TABLE_NAME, FavorateDBUtil.FAVOR_LOCAL_SQL_ID, contentValues);
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
	public boolean deleteFavor(int id)
	{
		SQLiteDatabase db = null;
		if(id <0)
		{
			return false;
		}
		try 
		{
			db = mySQLHelper.getWritableDatabase();
			db.delete(FavorateDBUtil.FAVOR_TABLE_NAME, FavorateDBUtil.MESSAGE_MESSAGE_INFO_ID+"=?", new String[]{id+""});
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
			super(context, FavorateDBUtil.DB_NAME, null, 1);
		}
		public void onCreate(SQLiteDatabase db) 
		{
			////创建shareitem表
			String sql = "create table if not exists "
				+ FavorateDBUtil.FAVOR_TABLE_NAME+ " (" 
				+ FavorateDBUtil.FAVOR_LOCAL_SQL_ID + " integer primary key,"
				+ FavorateDBUtil.MESSAGE_MESSAGE_INFO_ID + " integer);";
			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{
			db.execSQL("drop table if exists "+FavorateDBUtil.FAVOR_TABLE_NAME);
			onCreate(db);
		}
	}
}


