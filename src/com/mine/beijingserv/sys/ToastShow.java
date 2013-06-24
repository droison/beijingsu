package com.mine.beijingserv.sys;

import android.content.Context;
import android.widget.Toast;

public class ToastShow {
	public static void toastshow(Context context,String str){
		Toast.makeText(context, str, 1000).show();
	}
	
}
