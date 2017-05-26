package com.player.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.player.AppConstant;
import com.player.DataSingleton;
import com.player.PlayerApplication;
import com.player.R;
import com.player.model.UserConnectionStatus;
import com.player.util.DataManager;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @desc LoginActivity for register new device
 */
public class LoginActivity extends Activity {

    @BindView(R.id.edit_deviceName)
    EditText mEdit_deviceName;
    @BindView(R.id.btn_register)
    Button mBtn_register;
    @Inject
    DataSingleton dataSingleton;
    @Inject
    DataManager dataManager;

    private String deviceId;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlayerApplication.getAppComponent().inject(this);
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = dataManager.getCurrentUser();
        if (currentUser != null) {
            goToMainActivity();
        }
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mBtn_register.setOnClickListener(v -> tryToLogin());
        mEdit_deviceName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                tryToLogin();
                return true;
            }
            return false;
        });
    }

    private void goToMainActivity() {
        startActivity(new Intent(this, PlayerActivity.class));
        finish();
    }

    private void tryToLogin() {
        auth.signInWithEmailAndPassword(mEdit_deviceName.getText().toString(), AppConstant.USER_PASSWORD)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        goToMainActivity();
                    } else {
                        registerNewUser();
                    }
                });
    }

    private void registerNewUser() {
        if (mEdit_deviceName.getText().toString().isEmpty()) {
            Toast.makeText(this, getString(R.string.warning_login), Toast.LENGTH_SHORT).show();
        } else {
            auth.createUserWithEmailAndPassword(mEdit_deviceName.getText().toString(), AppConstant.USER_PASSWORD)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            storeUserConnection(user);
                            Toast.makeText(this, getString(R.string.show_login_success), Toast.LENGTH_LONG).show();
                            goToMainActivity();
                        } else {
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void storeUserConnection(FirebaseUser user) {
        UserConnectionStatus userConnectionStatus = new UserConnectionStatus(deviceId, user.getEmail());
        dataManager.storeUserConnection(userConnectionStatus);
    }
}

