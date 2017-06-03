package com.player.ui.activity;

import android.app.Activity;
import android.util.Pair;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Anton
 * mail to a.belichenko@gmail.com
 */

public class BaseActivity extends Activity {

    ArrayList<Pair<DatabaseReference, ValueEventListener>> fbListeners = new ArrayList<>();

    protected void registerFbListener(DatabaseReference reference, ValueEventListener listener) {
        fbListeners.add(new Pair<>(reference, listener));
    }

    void clearAllFbListeners() {
        for (Pair<DatabaseReference, ValueEventListener> fbListener : fbListeners) {
            if (fbListener.first != null && fbListener.second != null) {
                fbListener.first.removeEventListener(fbListener.second);
            }
        }
        fbListeners.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearAllFbListeners();
    }
}
