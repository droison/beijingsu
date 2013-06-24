package com.mine.beijingserv.sys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.provider.Settings.Secure;
import android.util.Log;

import com.mine.beijingserv.model.CatologueFilter;
import com.mine.beijingserv.model.FirstCatalogue;
import com.mine.beijingserv.model.MessageInfo;
import com.mine.beijingserv.model.SecondCatalogue;
import com.mine.beijingserv.ui.FirstCatologueActivity;

public class SysUtils 
{
	private static final String deletedMessageIDFileName = "delmsg.db";
	
	private static final String offlinecatscachefilename = "offlinecats";
	

	
	/**
	 * 保存目录到本地
	 * @param catsjsonString
	 */
	public static void saveOfflineCatsFile(String catsjsonString)
	{
		try {
			File file = new File(new SDcardUtil().rootDir, offlinecatscachefilename);
			if(file.exists())
			{
				file.delete();
			}
			file.createNewFile();
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(catsjsonString.getBytes("utf-8"));
			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取本地缓存目录
	 * @return
	 */
	public static String getOfflineCatsFile()
	{
		try {
			File file = new File(new SDcardUtil().rootDir, offlinecatscachefilename);
			if(file.exists())
			{
				FileInputStream fileInputStream = new FileInputStream(file);
				byte[] buffer = new byte[(int)file.length()];
				fileInputStream.read(buffer);
				fileInputStream.close();
				return new String(buffer, "utf-8");
			}else{
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 判断当前是否有网络连接
	 * @return
	 */
	public static boolean checkNetworkConnectedStat(Context context)
	{
		try {
			ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

	        //mobile 3G Data Network
	        State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
	        //wifi
	        State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
	        if(mobile == State.CONNECTED || mobile == State.CONNECTING)
	        {
	        	return true;
	        }
	            
	        if(wifi == State.CONNECTED || wifi == State.CONNECTING)
	        {
	        	return true;
	        }
		} catch (Exception e) {
		}
		
            
		
		return false;
	}
	
	
	/**
	 * 检查是否已经存在于记录
	 * @param msgid
	 * @return
	 */
	public static boolean checkIfMessageIdExist(int msgid)
	{
		File file = new File(new SDcardUtil().rootDir, deletedMessageIDFileName);
		JSONArray jsonArray = null;
		try {
			if(!file.exists())
			{
				file.createNewFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			byte buffer[] = new byte[(int)file.length()];
			fileInputStream.read(buffer);
			fileInputStream.close();
			String string = new String(buffer);
			jsonArray = new JSONArray(string);
			SysUtils.log("已保存的所有id信息 = "+jsonArray.toString());
			for(int i=0;i<jsonArray.length();i++)
			{
				if(jsonArray.getInt(i) == msgid)
				{
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			if(jsonArray == null)
			{
				jsonArray = new JSONArray();
			}
			jsonArray.put(msgid);
			String string2 = jsonArray.toString();
			System.out.println("write string2:  "+string2);
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(string2.getBytes());
			fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return false;
	}
	
	/**
	 * 获取设备唯一号
	 * @param context
	 * @return
	 */
	public static String getDeviceID(Context context)
	{
		SharedPreferences sharedPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
		String deviceid = null;
		deviceid = sharedPreferences.getString("key_pref_deviceid", null);
		if(deviceid == null)
		{
			deviceid = System.currentTimeMillis()+"";
			Editor editor = sharedPreferences.edit();
			editor.putString("key_pref_deviceid", deviceid);
			editor.commit();
		}
		return deviceid;
	}
	
	/**
	 * 获取格式化的时间设置
	 * @param hour
	 * @param min
	 * @return
	 */
	public static String formatFreeTime(int hh,int mm)
	{
		String string = "";
		if(hh >9)
		{
			
			string += hh+":";
		}
		if(hh<=9 &&hh>0)
		{
			string +="0"+ hh+":";
		}
		if(hh==0)
		{
			string += "00:";
		}
		if(mm<10)
		{
			string+="0"+mm;
		}
		else {
			string+=mm;
		}
		return string;
	}
	
	
	//设置第一次保存状态
	public static void setFirstFilterToContext(Context context)
	{
		try {
			SharedPreferences sharedPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
			String jsonString =  sharedPreferences.getString("key_pref_filter", "[]");
			JSONArray jsonArray = new JSONArray(jsonString);
			SysUtils.log(" 第一次保存的过滤情况 = "+jsonString);
			for(FirstCatalogue firstCatalogue:AppContex.catalogues)
			{
				for(SecondCatalogue secondCatalogue:firstCatalogue.secondCatalogues)
				{
					if(secondCatalogue.type == SecondCatalogue.SECOND_CAT_TYPE_URGENT){
						secondCatalogue.isChoosed = true;
					}
					
					if(secondCatalogue.title.equals("系统公告")){
						secondCatalogue.isChoosed = true;
					}
					
				}
			}			
		} catch (Exception e) {
		}
	}
	/**
	 * 应用当前过滤状态
	 */
	public static void setFilterToContext(Context context)
	{
		try {
			SharedPreferences sharedPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
			String jsonString =  sharedPreferences.getString("key_pref_filter", "[]");
			JSONArray jsonArray = new JSONArray(jsonString);
			SysUtils.log("保存的过滤情况 = "+jsonString);
			
			for(FirstCatalogue firstCatalogue:AppContex.catalogues)
			{
				for(SecondCatalogue secondCatalogue:firstCatalogue.secondCatalogues)
				{
					if(secondCatalogue.type == SecondCatalogue.SECOND_CAT_TYPE_URGENT)
					{
						secondCatalogue.isChoosed = true;////紧急通知全部选中
					}
					
					if(secondCatalogue.title.equals("系统公告")){
						secondCatalogue.isChoosed = true;
					}
				}
			}
////////////////////
			for(int i=0;i<jsonArray.length();i++)
			{
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				int fcatid = jsonObject.getInt(CatologueFilter.CATOLOGUE_FILTER_KEY_FCATID);
				int scatid = jsonObject.getInt(CatologueFilter.CATOLOGUE_FILTER_KEY_SCATID);
				for(FirstCatalogue firstCatalogue:AppContex.catalogues)
				{
					for(SecondCatalogue secondCatalogue:firstCatalogue.secondCatalogues)
					{
						if(fcatid == firstCatalogue.fcatid && scatid==secondCatalogue.scatid)
						{
							secondCatalogue.isChoosed = true;									
						}
					}
				}
				
			}
			
			
		} catch (Exception e) {
		}
	}
	

	/**
	 * 保存当前过滤状态
	 */
	public static void saveFilterToPref(Context context)
	{
		long liuliangL = TrafficStats.getTotalRxBytes();
		System.out.println("发送订阅目录前网络数据： "+liuliangL);
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
	
	
	
	/**
	 * 获取info距离现在得天数
	 * @param messageInfo
	 * @return
	 */
	public static int getMessageInfoDurationInDay(MessageInfo messageInfo)
	{
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, messageInfo.year);
			calendar.set(Calendar.MONTH, messageInfo.month-1);
			calendar.set(Calendar.DAY_OF_MONTH, messageInfo.day);
			long days = calendar.getTimeInMillis()/(1000*60*60*24);
			long curdays = System.currentTimeMillis()/(1000*60*60*24);
			return (int)(curdays - days);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * 打印日至
	 * @param info
	 */
	public static void log(String info)
	{
		if(info == null)
		{
			return;
		}
		if(AppContex.DEBUG)
		{
			System.out.println(info);
		}
	}
	
	/**
	 * 判断一个字符串是否为空
	 * @param string
	 * @return
	 */
	public static boolean isStringEmpty(String string)
	{
		if(string == null)
		{
			return true;
		}
		if(string.length() <1)
		{
			return true;
		}
		if(string.trim().length()<1)
		{
			return true;
		}
		return false;
	}
	
	@SuppressLint("SimpleDateFormat")
	public static String getCurrentTime() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd   hh:mm:ss");
		String date = sDateFormat.format(new java.util.Date());
		
		return date;
	}
}
