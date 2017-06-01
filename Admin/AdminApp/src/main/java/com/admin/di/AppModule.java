package com.admin.di;


import android.content.Context;
import android.support.annotation.NonNull;

import com.admin.util.DataManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

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
        return new DataManager(context);
    }

}
