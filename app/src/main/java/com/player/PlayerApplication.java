package com.player;

import android.app.Application;

import com.google.firebase.FirebaseApp;

import com.player.di.AppComponent;
import com.player.di.AppModule;
import com.player.di.DaggerAppComponent;
import com.player.receiver.PlayerInfoChangeReceiver;


public class PlayerApplication extends Application {

    private static AppComponent appComponent;

    public static AppComponent getAppComponent() {
        return appComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
        FirebaseApp.initializeApp(this);
        new PlayerInfoChangeReceiver();
    }
}