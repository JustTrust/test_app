package com.admin.ui;

import android.support.v7.app.AppCompatActivity;
import android.util.Pair;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Anton
 * mail to a.belichenko@gmail.com
 */

public class BaseActivity extends AppCompatActivity {

    ArrayList<Pair<DatabaseReference, Object>> fbListeners = new ArrayList<>();

    protected void registerFbListener(DatabaseReference reference, ChildEventListener listener) {
        fbListeners.add(new Pair<>(reference, listener));
    }

    protected void registerFbListener(DatabaseReference reference, ValueEventListener listener) {
        fbListeners.add(new Pair<>(reference, listener));
    }

    void clearAllFbListeners() {
        for (Pair<DatabaseReference, Object> fbListener : fbListeners) {
            if (fbListener.first != null && fbListener.second != null) {
                if (fbListener.second instanceof ChildEventListener) {
                    fbListener.first.removeEventListener((ChildEventListener) fbListener.second);
                }else if(fbListener.second instanceof ValueEventListener) {
                    fbListener.first.removeEventListener((ValueEventListener) fbListener.second);
                }else{
                    throw new IllegalArgumentException("Wrong 2-d parameter, not EventListener type");
                }
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
