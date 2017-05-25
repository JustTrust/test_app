package com.player;

import com.player.model.UserConnectionStatus;
import com.player.parseModel.ConnectionStatus;
import com.player.parseModel.DeviceSettings;

/**
 * Created on 3/8/16.
 */
public class DataSingleton {

    public ConnectionStatus mConnectionStatus;
    public DeviceSettings mDeviceSettings;

    public UserConnectionStatus userConnectionStatus;

    public DataSingleton(){

    }

    public static DataSingleton getInstance(){
        return new DataSingleton();
    }

}
