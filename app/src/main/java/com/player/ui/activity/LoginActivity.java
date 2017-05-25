package com.player.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.player.AppConstant;
import com.player.DataSingleton;
import com.player.PlayerApplication;
import com.player.R;
import com.player.parseModel.ConnectionStatus;
import com.player.ui.dialog.ProgressDialog;

/**
 * @desc LoginActivity for register new device
 */
public class LoginActivity extends Activity implements View.OnClickListener {
    public static LoginActivity instance;
    private Button mBtn_register;
    private EditText mEdit_deviceName;
    private ProgressDialog mPrgDlg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(ParseUser.getCurrentUser() != null){
            startActivity(new Intent(this, PlayerActivity.class));
            finish();
        }else {
            setContentView(R.layout.activity_login);
            initUI();
        }
    }

    private void initUI(){
        mPrgDlg = new ProgressDialog(this);
        mBtn_register = (Button) this.findViewById(R.id.btn_register);
        mEdit_deviceName = (EditText) this.findViewById(R.id.edit_deviceName);
        mBtn_register.setOnClickListener(this);
    }

    private void registerDevice(){
        if(mEdit_deviceName.getText().toString().isEmpty()){
            PlayerApplication.showToast(getString(R.string.warning_login), Toast.LENGTH_SHORT);
            return;
        } else{
            ParseUser pUser = new ParseUser();
            pUser.setUsername(mEdit_deviceName.getText().toString());
            pUser.setPassword(AppConstant.USER_PASSWORD);
            mPrgDlg.show();
            pUser.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if( e == null){
                        makeConnectionInfo();
                    }else{
                        PlayerApplication.showToast(e.getMessage(), Toast.LENGTH_LONG);
                    }
                }
            });
        }
    }

    private void makeConnectionInfo(){
        final ConnectionStatus connStatus = new ConnectionStatus();
        connStatus.setDeviceID(ParseUser.getCurrentUser().getObjectId());
        connStatus.setDeviceName(ParseUser.getCurrentUser().getUsername());
        connStatus.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    DataSingleton.getInstance().mConnectionStatus = connStatus;
                    PlayerApplication.showToast(getString(R.string.show_login_success), Toast.LENGTH_LONG);
                    startActivity(new Intent(LoginActivity.this, PlayerActivity.class));
                    finish();
                } else {
                    makeConnectionInfo();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_register:
                registerDevice();
            break;
        }
    }
}

