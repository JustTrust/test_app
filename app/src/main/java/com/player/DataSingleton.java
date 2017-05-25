package com.player;

import com.player.parseModel.ConnectionStatus;
import com.player.parseModel.DeviceSettings;

/**
 * Created on 3/8/16.
 */
public class DataSingleton {

    private static DataSingleton mInstance = null;
    public ConnectionStatus mConnectionStatus;
    public DeviceSettings mDeviceSettings;

    private DataSingleton(){

    }

    public static DataSingleton getInstance(){
        if (mInstance == null){
            mInstance = new DataSingleton();
        }
        return  mInstance;
    }

}
