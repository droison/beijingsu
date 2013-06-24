package com.mine.beijingserv.model;

import java.io.File;
import java.io.FileInputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mine.beijingserv.R;
import com.mine.beijingserv.sys.DBUtil;
import com.mine.beijingserv.sys.SDcardUtil;
import com.mine.beijingserv.sys.SysUtils;

public class MessageInfo {
	public static final int READ_STATE_UNREAD = 0;
	public static final int READ_STATE_READED = 1;
	public static final int MESSAGE_TYPE_ERGENT = 1;
	public static final int MESSAGE_TYPE_NORMAL = 0;
	
	public String title = "";
	public String content = "";
	public int year = 0;
	public int month = 0;
	public int day = 0;
	public int hour = 0;
	public int min = 0;
	public int readState = 0;
	public int localsqlid = -1;
	public int serversqlid = -1;
	public int fcatid = -1;
	public int scatid = -1;
	public int type = 0;
	public int alertlevel = -1;
	public int alerttype = -1;
	public int saygood = -1;
	///
	public boolean isChoosed = false;
	
	
	private Bitmap icon = null;
	/**
	 * 鑾峰彇瀵瑰簲鐨勫浘鏍�	 * @return
	 */
	public Bitmap getIcon()
	{
		if(icon == null)
		{
			File file = new File(new SDcardUtil().iconDir, fcatid+".jpg");
			System.out.println("try to get icon "+file.getAbsolutePath());
			if(file.exists())
			{
				try {
					icon = BitmapFactory.decodeFile(file.getAbsolutePath());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
		return icon;
	}
	
	/////////
	private String formatedTime = null;
	/**
	 * 鑾峰彇鏍煎紡鍖栨椂闂�	 * @return
	 */
	public String getFormatedTime()
	{
		if(formatedTime == null)
		{
			String formathour = hour<10?"0"+hour:hour+"";
			String formatminute = min<10?"0"+min:min+"";
			
			
			formatedTime = new StringBuilder().append(year).append("-")
					.append(month).append("-")
					.append(day).append(" ")
					.append(formathour).append(":")
					.append(formatminute).toString();
		}
		return formatedTime;
	}
	
	/**
	 * 鑾峰彇鏍煎紡鍖栨椂闂�	 * @return
	 */
	public String getFormatedTimeWithoutYear()
	{
		if(formatedTime == null)
		{
			String formathour = hour<10?"0"+hour:hour+"";
			String formatminute = min<10?"0"+min:min+"";
			
			
			formatedTime = new StringBuilder()
					.append(month).append("月")
					.append(day).append("日")
					.append(formathour).append(":")
					.append(formatminute).toString();
		}
		return formatedTime;
	}
	
	
	
	/**
	 * 鎵撳嵃娑堟伅
	 */
	public void printMe()
	{
		SysUtils.log(new StringBuilder().append("title=").append(title)
				.append(",content=").append(content)
				.append(",year=").append(year)
				.append(",month=").append(month)
				.append(",day=").append(day)
				.append(",readstate=").append(readState)
				.append(",localsqlid=").append(localsqlid)
				.append(",serversqlid=").append(serversqlid)
				.append(",fcatid=").append(fcatid)
				.append(",scatid=").append(scatid).toString());
	}
}
