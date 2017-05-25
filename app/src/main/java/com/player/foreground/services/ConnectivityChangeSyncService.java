package com.player.foreground.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.player.AppConstant;
import com.player.DataSingleton;
import com.player.foreground.events.ConnectivityChangedEvent;
import com.player.parseModel.DeviceSettings;

import org.greenrobot.eventbus.EventBus;

public class ConnectivityChangeSyncService extends IntentService {

    private static final String NAME = ConnectivityChangeSyncService.class.getName();

    private static final String EXTRA_IS_NETWORK_CONNECTED = "EXTRA_IS_NETWORK_CONNECTED";

    public ConnectivityChangeSyncService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null || !extras.containsKey(EXTRA_IS_NETWORK_CONNECTED)) {
            throw new IllegalArgumentException("Required call params are not specified.");
        }
        boolean isNetworkConnected = extras.getBoolean(EXTRA_IS_NETWORK_CONNECTED);
        EventBus.getDefault().post(new ConnectivityChangedEvent(isNetworkConnected));


        if (isNetworkConnected && DataSingleton.getInstance().mDeviceSettings!=null) {
            ParseQuery<DeviceSettings> deviceSettingsParseQuery = DeviceSettings.getQuery().whereEqualTo(AppConstant.FIELD_DEVICE_Id, ParseUser.getCurrentUser().getObjectId());
            deviceSettingsParseQuery.getFirstInBackground(new GetCallback<DeviceSettings>() {
                @Override
                public void done(DeviceSettings object, ParseException e) {
                    DeviceSettings settings = null;
                    if (object != null && e == null) {
                        object.setEndTime(DataSingleton.getInstance().mDeviceSettings.getEndTime());
                        object.setSongInterval(DataSingleton.getInstance().mDeviceSettings.getSongInterval());
                        object.setPauseInterval(DataSingleton.getInstance().mDeviceSettings.getPauseInterval());
                        object.setStartTime(DataSingleton.getInstance().mDeviceSettings.getStartTime());
                        object.saveEventually();
                    } else {
                        settings = DataSingleton.getInstance().mDeviceSettings;
                        settings.saveEventually();
                    }
                    DataSingleton.getInstance().mDeviceSettings = null;
                }
            });
        }
    }

    public static Intent getIntent(Context caller, boolean isConnected) {
        Intent intent = new Intent(caller, ConnectivityChangeSyncService.class);
        intent.putExtra(EXTRA_IS_NETWORK_CONNECTED, isConnected);
        return intent;
    }

}
