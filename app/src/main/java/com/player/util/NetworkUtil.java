package com.player.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {

	/**
	 * Check Network Aviablity
	 * 
	 * @param context
	 *            Context Object Of Activity
	 * @return
	 */
	public static boolean isOnline(Context context) {

		/*
		 * final ConnectivityManager connMgr = (ConnectivityManager)
		 * context.getSystemService(Context.CONNECTIVITY_SERVICE);
		 * 
		 * final android.net.NetworkInfo wifi =
		 * connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		 * 
		 * final android.net.NetworkInfo mobile =
		 * connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		 * 
		 * if( wifi.isAvailable() ){ //Toast.makeText(context, "Wifi" ,
		 * Toast.LENGTH_LONG).show(); return true; } else if(
		 * mobile.isAvailable() ){ //Toast.makeText(contexst, "Mobile 3G " ,
		 * Toast.LENGTH_LONG).show(); return true; } else { //
		 * Toast.makeText(context, "No Network " , Toast.LENGTH_LONG).show();
		 * return false; }
		 */
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		} else {
			return false;
		}

	}

}
