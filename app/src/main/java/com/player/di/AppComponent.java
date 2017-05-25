package com.player.di;

import com.player.ui.activity.LoginActivity;
import com.player.ui.activity.PlayerActivity;

import javax.inject.Singleton;

import dagger.Component;


@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {
    void inject(LoginActivity mainActivity);
    void inject(PlayerActivity listActivity);
}
