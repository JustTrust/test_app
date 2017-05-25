package com.player;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.player.parseModel.ConnectionStatus;
import com.player.parseModel.DeviceSettings;


public class PlayerApplication extends Application {
	public static Context mContext;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mContext = this.getApplicationContext();
		ParseObject.registerSubclass(ConnectionStatus.class);
		ParseObject.registerSubclass(DeviceSettings.class);
		Parse.initialize(this, AppConstant.PARSE_APP_ID, AppConstant.PARSE_CLIENT_KEY);
	}

	public static Context getContext() {
		return mContext;
	}
	public static void showToast(String message, int toastType){
		Toast.makeText(mContext, message, toastType).show();
	}
}