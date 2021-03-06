package com.admin.di;

import com.admin.ui.MainListActivity;
import com.admin.ui.PlayerSettingActivity;
import com.admin.ui.dialog.NotificationDialog;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Anton
 * mail to a.belichenko@gmail.com
 */

@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {
    void inject(MainListActivity mainActivity);
    void inject(PlayerSettingActivity playerSettingActivity);
    void inject(NotificationDialog notificationDialog);
}
