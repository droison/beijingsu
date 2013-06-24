package com.mine.beijingserv.model;

public class SecondCatalogue 
{
	public static final int SECOND_CAT_TYPE_NORMAL = 0;
	public static final int SECOND_CAT_TYPE_URGENT = 1;
	
	public int fcatid = 0;
	public int scatid = 0;
	public String title = "";
	public int type = 0;
	public int scatsubmun = 0;
	public int scatcurmonthmsgnum = 0;
	public int scatcallmsgnum = 0;
	
	public boolean isChoosed = false;////是否需要被过滤掉
	
	public void printme()
	{
		System.out.println("SecondCatalogue id="+scatid+"title = "+title);
	}
}
