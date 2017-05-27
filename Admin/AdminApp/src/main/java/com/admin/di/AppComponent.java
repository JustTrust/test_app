package com.admin.di;

import com.admin.ui.MainListActivity;
import com.admin.ui.PlayerSettingActivity;

import javax.inject.Singleton;

import dagger.Component;


@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {
    void inject(MainListActivity mainActivity);
    void inject(PlayerSettingActivity playerSettingActivity);
}
