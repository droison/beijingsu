package com.mine.beijingserv.sys;

import java.io.File;


import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class SDcardUtil 
{
	//////文件夹名称
	private final static String ROOT_DIR_NAME = "beijingservyou";
	private final static String ICONS_DIR_NAME  = "icons";

	//////文件夹
	public  File rootDir = null;
	public  File iconDir = null;
	///////////
	
	/**
	 * 初始化系统目录
	 */
	public SDcardUtil()
	{
		try
		{
			rootDir = new File(Environment.getExternalStorageDirectory(),ROOT_DIR_NAME);
			if(!rootDir.exists())
			{
				rootDir.mkdir();
			}
			iconDir = new File(rootDir,ICONS_DIR_NAME);
			if(!iconDir.exists())
			{
				iconDir.mkdir();
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 返回sd卡的剩余空间，以Mb为单位
	 * @return
	 */
	public static int getSDCardAvableSpaceMb()
	{
		try {
			StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
			long blocSize = statFs.getBlockSize();
			long availaBlocks = statFs.getAvailableBlocks();
			long freeSizeMb = availaBlocks * blocSize/1024/1024;
			return (int)freeSizeMb;
		} catch (Exception e) {
		}
		return 0;
	}
	

	
	
	
}
