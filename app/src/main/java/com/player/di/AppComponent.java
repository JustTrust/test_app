package com.player.di;

import com.player.foreground.services.ConnectivityChangeSyncService;
import com.player.movedetector.MoveDetector;
import com.player.receiver.PlayerInfoChangeReceiver;
import com.player.ui.activity.LoginActivity;
import com.player.ui.activity.PlayerActivity;

import javax.inject.Singleton;

import dagger.Component;


@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {
    void inject(LoginActivity mainActivity);
    void inject(PlayerActivity listActivity);
    void inject(MoveDetector moveDetector);
    void inject(PlayerInfoChangeReceiver playerInfoReceiver);
    void inject(ConnectivityChangeSyncService connectivityChangeSyncService);
}
