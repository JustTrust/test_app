package com.admin;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.widget.Toast;

import com.admin.parsemodel.ConnectionStatus;
import com.admin.parsemodel.DeviceSettings;
import com.admin.util.Utils;
import com.google.firebase.FirebaseApp;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @desc Application class for AdminApp
 */
public class AdminApplication extends Application {

    private boolean connectionFlag = true;
    public static Context mContext;
    private Subscription mCheckConnectionStatus;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this.getApplicationContext();
        ParseObject.registerSubclass(ConnectionStatus.class);
        ParseObject.registerSubclass(DeviceSettings.class);
        Parse.initialize(this, AppConstant.PARSE_APP_ID, AppConstant.PARSE_CLIENT_KEY);
        ParseUser.enableAutomaticUser();
        ParseInstallation currentInstall = ParseInstallation.getCurrentInstallation();
        currentInstall.saveInBackground();
        mCheckConnectionStatus = checkConnectionStatus();
        FirebaseApp.initializeApp(this);
    }

    public static Context getContext() {
        return mContext;
    }

    public static void showMessage(String message, int nType) {
        Toast.makeText(mContext, message, nType).show();
    }

    private Subscription checkConnectionStatus() {
        return Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    if (Utils.isOnline(mContext)) {
                        if (!connectionFlag) {
                            Toast.makeText(mContext, "Internet connection is restored", Toast.LENGTH_LONG).show();
                            connectionFlag = true;
                        }
                    } else {
                        if (connectionFlag) {
                            Toast.makeText(mContext, "You are not connected to the internet", Toast.LENGTH_LONG).show();
                            connectionFlag = false;
                        }

                    }
                });
    }

}
