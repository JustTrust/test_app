/**
 * 
 */
package com.player.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.player.AppConstant;
import com.player.ui.activity.PlayerActivity;

/**
 * @desc Broadcast Receiver for set song start timer
 */
public class SongStartTimeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent mainIntent = new Intent(context, PlayerActivity.class);
        mainIntent.putExtra(AppConstant.INTENT_CATEGORY, AppConstant.INTENT_START);
        mainIntent.putExtra(AppConstant.FIELD_MESSAGE_DATA, intent.getSerializableExtra(AppConstant.FIELD_MESSAGE_DATA));
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mainIntent);
    }
}
