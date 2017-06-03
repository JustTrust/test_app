package com.player.foreground.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.player.PlayerApplication;
import com.player.foreground.events.ConnectivityChangedEvent;
import com.player.util.DataManager;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

public class ConnectivityChangeSyncService extends IntentService {

    private static final String NAME = ConnectivityChangeSyncService.class.getName();
    private static final String EXTRA_IS_NETWORK_CONNECTED = "EXTRA_IS_NETWORK_CONNECTED";

    @Inject
    DataManager dataManager;

    public ConnectivityChangeSyncService() {
        super(NAME);
    }

    public static Intent getIntent(Context caller, boolean isConnected) {
        Intent intent = new Intent(caller, ConnectivityChangeSyncService.class);
        intent.putExtra(EXTRA_IS_NETWORK_CONNECTED, isConnected);
        return intent;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PlayerApplication.getAppComponent().inject(this);
        Bundle extras = intent.getExtras();
        if (extras == null || !extras.containsKey(EXTRA_IS_NETWORK_CONNECTED)) {
            throw new IllegalArgumentException("Required call params are not specified.");
        }
        boolean isNetworkConnected = extras.getBoolean(EXTRA_IS_NETWORK_CONNECTED);
        EventBus.getDefault().post(new ConnectivityChangedEvent(isNetworkConnected));

    }

}
