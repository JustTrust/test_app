package com.player;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.player.di.AppComponent;
import com.player.di.AppModule;
import com.player.di.DaggerAppComponent;
import com.player.parseModel.ConnectionStatus;
import com.player.parseModel.DeviceSettings;


public class PlayerApplication extends Application {

    public static Context mContext;
    private static AppComponent appComponent;

    public static AppComponent getAppComponent() {
        return appComponent;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
        FirebaseApp.initializeApp(this);
    }

    public static Context getContext() {
        return mContext;
    }

    public static void showToast(String message, int toastType) {
        Toast.makeText(mContext, message, toastType).show();
    }
}