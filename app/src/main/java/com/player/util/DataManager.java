package com.player.util;


import com.google.firebase.auth.FirebaseUser;
import com.player.model.PhoneSettings;
import com.player.model.UserConnectionStatus;

/**
 * Created by Anton
 * mail to a.belichenko@gmail.com
 */

public interface DataManager {

     void saveSettings(PhoneSettings phoneSettings);

     void saveStatus(UserConnectionStatus status);

     void saveCoordinateInStatus(final String latitude, final String longitude);

     void sendNotificationToAdmin(double latitude, double longitude);

     void storeUserConnection(UserConnectionStatus userConnectionStatus);

     FirebaseUser getCurrentUser();

     String getDeviceId();

     void logout();

     void storeFileInStorage(String filepath);

     void deleteMessage();
}
