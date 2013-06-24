package com.mine.beijingserv.model;

public class ReplyModel {
	public String title;
	public String content;
	public int year;
	public int month;
	public int day;
	public int hour;
	public int min;
	public int _id;
	
	
/////////
private String formatedTime = null;

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

public String getFormatedTimeWithoutYear()
{
	if(formatedTime == null)
	{
		String formathour = hour<10?"0"+hour:hour+"";
		String formatminute = min<10?"0"+min:min+"";
		
		System.out.println("SHOW REPLY DAY:  "+day);
		formatedTime = new StringBuilder()
				.append(month).append("月")
				.append(day).append("日")
				.append(formathour).append(":")
				.append(formatminute).toString();
	}
	return formatedTime;
}

}
