package com.admin.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.admin.AppConstant;
import com.admin.R;
import com.admin.ui.adapters.VideoAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Anton
 * mail to a.belichenko@gmail.com
 */

public class VideoActivity extends BaseActivity {
    @BindView(R.id.video_list)
    RecyclerView videoList;
    @BindView(R.id.video_view)
    VideoView videoView;

    private String mStr_DeviceID;
    private ArrayList<String> list = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        ButterKnife.bind(this);
        initUI();
    }

    private void initUI() {
        mStr_DeviceID = getIntent().getStringExtra(AppConstant.FIELD_DEVICE_ID);
        if (TextUtils.isEmpty(mStr_DeviceID)) return;
        DatabaseReference videoRef = FirebaseDatabase.getInstance().getReference().child(AppConstant.NODE_VIDEO)
                .child(mStr_DeviceID);
        ValueEventListener videoListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String path = (String) postSnapshot.getValue();
                        if (!TextUtils.isEmpty(path)) {
                            list.add(path);
                        }
                    }
                    VideoAdapter adapter = new VideoAdapter(list, v -> showVideo(((TextView) v).getText()));
                    videoList.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        videoRef.addListenerForSingleValueEvent(videoListener);
        registerFbListener(videoRef, videoListener);

    }

    private void showVideo(CharSequence text) {
        FirebaseStorage.getInstance().getReference().child(mStr_DeviceID)
                .child(text.toString()).getDownloadUrl()
                .addOnSuccessListener(uri -> playVideo(uri));

    }

    private void playVideo(Uri uri) {
        if (videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.start();
    }

}
