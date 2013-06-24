package com.mine.beijingserv.sys;

import java.util.Vector;

import android.app.Activity;

import com.mine.beijingserv.model.FirstCatalogue;
import com.mine.beijingserv.model.MessageInfo;
import com.mine.beijingserv.model.ReplyModel;
import com.mine.beijingserv.model.SecondCatalogue;


public class AppContex 
{
	public static String MQTT_ADDRESS = "tcp://210.73.66.39:1883";
	public static final String GET_ALL_CATS_API = "http://210.73.66.40/getallcats?";
	public static final String GET_ALL_MESSAGES_API = "http://210.73.66.40/getallmessages?";
	public static final String CHECK_NEW_APP_API = "http://210.73.66.40/checknewapp?";
	public static final String SEND_FEED_BACK_API = "http://210.73.66.40/feedback?";
	public static final String SEND_SUBSCRIBE_API = "http://210.73.66.40/subscribe?";
	public static final String UPDATE_MSGSTATE_API = "http://210.73.66.40/msgstate?";
	public static final String SEND_DEVICE_INFO_API = "http://210.73.66.40/devicetinfo?";
	public static final String SEND_COMMENT_API = "http://210.73.66.40/commentmsg?";
	public static final String SEND_ALERTCONTET_API = "http://210.73.66.40/getalertcontent?";
	public static final String SEND_SERCH_API = "http://210.73.66.40/searchfortype?";	
	public static final String SEND_SAYGOOD_API = "http://210.73.66.40/saygoodtomsg?";
	public static final String GET_SAYGOOD_API = "http://210.73.66.40/saygoodnum?";	
	public static final String SEND_READSTATE = "http://210.73.66.40/msgstate?";
	public static final String SEND_SUGGESTION = "http://210.73.66.40/suggestion?";
	public static final String GET_ALLREPLYS = "http://210.73.66.40/getallreply?";
	//debug
//	public static String MQTT_ADDRESS = "tcp://fuwu.mobroad.com:1883";
//	public static final String GET_ALL_CATS_API = "http://fuwu.mobroad.com/getallcats?";
//	public static final String GET_ALL_MESSAGES_API = "http://fuwu.mobroad.com/getallmessages?";
//	public static final String CHECK_NEW_APP_API = "http://fuwu.mobroad.com/checknewapp?";
//	public static final String SEND_FEED_BACK_API = "http://fuwu.mobroad.com/feedback?";
//	public static final String SEND_SUBSCRIBE_API = "http://fuwu.mobroad.com/subscribe?";
//	public static final String UPDATE_MSGSTATE_API = "http://fuwu.mobroad.com/msgstate?";
//	public static final String SEND_DEVICE_INFO_API = "http://fuwu.mobroad.com/devicetinfo?";
//	public static final String SEND_HUDONG_CONTENT_API = "http://fuwu.mobroad.com/comwithimg?deviceid=shfihsfws";
//	public static final String SEND_COMMENT_API = "http://fuwu.mobroad.com/commentmsg?";
//	public static final String SEND_ALERTCONTET_API = "http://fuwu.mobroad.com/getalertcontent?";
//    public static final String SEND_SERCH_API = "http://fuwu.mobroad.com/searchfortype?";
//    public static final String SET_FREETIME_API = "http://fuwu.mobroad.com/nobother?";
//    public static final String SEND_SAYGOOD_API = "http://fuwu.mobroad.com/saygoodtomsg?";
//    public static final String GET_SAYGOOD_API = "http://fuwu.mobroad.com/saygoodnum?";
//	public static final String SEND_READSTATE = "http://fuwu.mobroad.com/msgstate?";
//	public static final String SEND_SUGGESTION = "http://fuwu.mobroad.com/suggestion?";
//	public static final String GET_ALLREPLYS = "http://fuwu.mobroad.com/getallreply?";

	
	public static Vector<FirstCatalogue> catalogues = null;///所有类别得集合
	public static MessageInfo curMessageInfo = null;
	public static ReplyModel curReplyModel = null;
	public static FirstCatalogue curFirstCatalogue = null;
	public static final double APP_VER = 1.9;//版本
	public static final boolean DEBUG = true;

	
	public static Vector<Activity> activities = new Vector<Activity>();
	public static Vector<MessageInfo> searchedInfos = new Vector<MessageInfo>();
	public static Vector<MessageInfo> showMessageInfos = new Vector<MessageInfo>();
	
	public static boolean APPCATION_ON = false;
	public static Vector<SecondCatalogue> tempInfos = new Vector<SecondCatalogue>();
	public static final String seTitle = "系统公告";
}
