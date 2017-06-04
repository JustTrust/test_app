package com.player.di;

import com.player.foreground.services.BackgroundVideoRecordingService;
import com.player.foreground.services.ConnectivityChangeSyncService;
import com.player.movedetector.MoveDetector;
import com.player.receiver.PlayerInfoChangeReceiver;
import com.player.ui.activity.LoginActivity;
import com.player.ui.activity.NewPlayerActivity;
import com.player.ui.activity.PlayerActivity;
import com.player.ui.activity.SettingActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Anton
 * mail to a.belichenko@gmail.com
 */

@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {
    void inject(LoginActivity mainActivity);
    void inject(PlayerActivity listActivity);
    void inject(NewPlayerActivity newPlayerActivity);
    void inject(SettingActivity settingActivity);
    void inject(MoveDetector moveDetector);
    void inject(PlayerInfoChangeReceiver playerInfoReceiver);
    void inject(ConnectivityChangeSyncService connectivityChangeSyncService);
    void inject(BackgroundVideoRecordingService backgroundVideoRecordingService);
}
