package com.admin.ui.dialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import com.admin.AppConstant;
import com.admin.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationDialog extends Activity {

    @BindView(R.id.image)
    VideoView videoView;
    @BindView(R.id.btn_Ok)
    Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout);
        ButterKnife.bind(this);
        initUI();

        Intent intent = getIntent();
        String deviceName = intent.getStringExtra(AppConstant.EXTRA_DEVICE_NAME);
        if (!TextUtils.isEmpty(deviceName)) setTitle(deviceName);
        Uri message = intent.getParcelableExtra(AppConstant.FIELD_MESSAGE_DATA);
        setVideo(message);
    }

    private void initUI() {
        btnOk.setOnClickListener(v -> {
            finish();
        });
    }

    private void setVideo(Uri uri) {
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.start();
    }
}
