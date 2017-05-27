package com.admin.di;

import com.admin.ui.MainListActivity;

import javax.inject.Singleton;

import dagger.Component;


@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {
    void inject(MainListActivity mainActivity);
}
