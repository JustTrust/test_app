package com.admin.util;

import com.admin.model.NotificationMessage;
import com.admin.model.PhoneSettings;

/**
 * Created by Anton
 * mail to a.belichenko@gmail.com
 */

public interface DataManager {

    void saveSettings(PhoneSettings phoneSettings);

    void sendPushNotification(NotificationMessage message, String deviceId);

    void sendVolumePushNotification(int volume, String deviceId);

    void setGpsStatus(String deviceID, Boolean isChecked);

    void clearMessages();
}
