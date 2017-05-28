package com.admin;

import android.app.Application;
import android.widget.Toast;

import com.admin.di.AppComponent;
import com.admin.di.AppModule;
import com.admin.di.DaggerAppComponent;
import com.admin.receiver.MovementReceiver;
import com.admin.util.Utils;
import com.google.firebase.FirebaseApp;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @desc Application class for AdminApp
 */
public class AdminApplication extends Application {

    private boolean connectionFlag = true;
    private Subscription mCheckConnectionStatus;
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
        mCheckConnectionStatus = checkConnectionStatus();
        FirebaseApp.initializeApp(this);
        new MovementReceiver(this);
    }

    private Subscription checkConnectionStatus() {
        return Observable.interval(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    if (Utils.isOnline(this)) {
                        if (!connectionFlag) {
                            Toast.makeText(this, "Internet connection is restored", Toast.LENGTH_LONG).show();
                            connectionFlag = true;
                        }
                    } else {
                        if (connectionFlag) {
                            Toast.makeText(this, "You are not connected to the internet", Toast.LENGTH_LONG).show();
                            connectionFlag = false;
                        }
                    }
                });
    }

}
