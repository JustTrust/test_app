package com.player.di;


import android.content.Context;
import android.support.annotation.NonNull;

import com.player.util.DataManager;
import com.player.util.FirebaseDataManager;
import com.player.util.NotificationUtils;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Anton
 * mail to a.belichenko@gmail.com
 */

@Module
public class AppModule {
    private Context appCont;

    public AppModule(@NonNull Context context){
        appCont = context;
    }

    @Provides
    @Singleton
    Context provideContext(){
        return appCont;
    }

    @Provides
    @Singleton
    DataManager provideDataManager(Context context){
        return new FirebaseDataManager(context);
    }

    @Provides
    @Singleton
    NotificationUtils provideNotificationUtils(Context context){
        return new NotificationUtils(context);
    }
}
