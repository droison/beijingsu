package com.mine.beijingserv.model;

import java.io.File;
import java.util.Vector;

import com.mine.beijingserv.sys.SDcardUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class FirstCatalogue 
{
	public static Bitmap defaultIconBitmap = null;
	
	public String title = "";
	public int fcatid = 0;
	public Vector<SecondCatalogue> secondCatalogues = new Vector<SecondCatalogue>();
	public boolean isChoosed = true;////是否需要被过滤掉
	public String iconUrl = null;
	public Bitmap icon = null;
	public int dingyuecount = 0;
	public int allmsgcount = 0;
	public int totalcount = 0;
	public int todaymsgcount = 0;
	public int unreadmsgcount = 0;
	
	
	public int fcatallmsgnum = 0;
	public int fcatcurmonthmsg = 0;
	public int fcatsubnum  = 0;
	
	
	

	/**
	 * 获取对应的图标
	 * @return
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
	
	public void printme()
	{
		System.out.println("FirstCatalogue id="+fcatid+"title = "+title);
	}
}
