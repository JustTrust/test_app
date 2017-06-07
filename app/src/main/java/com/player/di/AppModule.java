package com.player.di;


import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.player.util.AudioAppManager;
import com.player.util.DataManager;
import com.player.util.FirebaseDataManager;
import com.player.util.NotificationUtils;
import com.player.util.PlayHelper;

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

    @Provides
    @Singleton
    AudioAppManager provideAudioManager(Context context){
        return new AudioAppManager(context);
    }

    @Provides
    @Singleton
    PlayHelper providePlayHelper(Context context, AudioAppManager audioAppManager){
        return new PlayHelper(context, audioAppManager);
    }
}
