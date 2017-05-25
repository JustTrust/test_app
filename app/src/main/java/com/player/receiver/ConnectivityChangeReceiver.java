package com.player.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.player.foreground.services.ConnectivityChangeSyncService;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = ConnectivityChangeReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "Connectivity state has changed");
        if (intent.getExtras() != null) {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                Log.i(LOG_TAG, "Network " + networkInfo.getTypeName() + " connected");
                context.startService(ConnectivityChangeSyncService.getIntent(context, true));
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
                Log.d(LOG_TAG, "There's no network connectivity");
                context.startService(ConnectivityChangeSyncService.getIntent(context, false));
            }
        }
    }

}
