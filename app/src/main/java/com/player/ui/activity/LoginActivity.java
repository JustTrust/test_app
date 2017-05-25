package com.player.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.player.DataSingleton;
import com.player.PlayerApplication;
import com.player.R;
import com.player.model.User;
import com.player.model.UserConnectionStatus;
import com.player.parseModel.ConnectionStatus;
import com.player.ui.dialog.ProgressDialog;

import javax.inject.Inject;

/**
 * @desc LoginActivity for register new device
 */
public class LoginActivity extends Activity {

    public static final String USER_NAME = "com.player.ui.activity.userName";
    private Button mBtn_register;
    private EditText mEdit_deviceName;
    private ProgressDialog mPrgDlg;
    private String deviceId;

    @Inject
    DataSingleton dataSingleton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlayerApplication.getAppComponent().inject(this);
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        DatabaseReference usr = FirebaseDatabase.getInstance().getReference().child("users");
        usr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(deviceId)) {
                    User user = (User) dataSnapshot.child(deviceId).getValue();
                    goToMainActivity(user.userName);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        setContentView(R.layout.activity_login);
        initUI();
    }

    private void goToMainActivity(String name) {
        startActivity(new Intent(this, PlayerActivity.class).putExtra(USER_NAME, name));
        finish();
    }

    private void initUI() {
        mPrgDlg = new ProgressDialog(this);
        mBtn_register = (Button) this.findViewById(R.id.btn_register);
        mBtn_register.setOnClickListener(v -> registerDevice());
        mEdit_deviceName = (EditText) this.findViewById(R.id.edit_deviceName);
    }

    private void registerDevice() {
        if (mEdit_deviceName.getText().toString().isEmpty()) {
            Toast.makeText(this, getString(R.string.warning_login), Toast.LENGTH_SHORT).show();
        } else {
            UserConnectionStatus userConnectionStatus = new UserConnectionStatus(deviceId, mEdit_deviceName.getText().toString());
            DatabaseReference usr = FirebaseDatabase.getInstance().getReference().child("users");
            usr.setValue(new User(deviceId, mEdit_deviceName.getText().toString()));
            DatabaseReference connected_usr = FirebaseDatabase.getInstance().getReference().child("connected_users");
            connected_usr.setValue(userConnectionStatus);
            dataSingleton.userConnectionStatus = userConnectionStatus;
            PlayerApplication.showToast(getString(R.string.show_login_success), Toast.LENGTH_LONG);
            goToMainActivity(mEdit_deviceName.getText().toString());
        }
    }
}

