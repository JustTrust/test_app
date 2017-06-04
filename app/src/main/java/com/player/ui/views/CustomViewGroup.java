package com.player.ui.views;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.player.R;
import com.player.receiver.MyPhoneStateListener;
import com.player.util.AppUtils;
import com.player.util.PermissionUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Test-Gupta on 4/1/2017.
 */

public class CustomViewGroup extends RelativeLayout {

    BroadcastReceiver timeChangeReceiver;
    BroadcastReceiver batInfoReceiver;
    MyPhoneStateListener myPhoneStateListener;
    TelephonyManager telephonyManager;

    @BindView(R.id.txt_serialNumber)
    TextView mTxt_serialNumber;

    @BindView(R.id.txt_battery_status)
    TextView mTxt_battery_status;

    @BindView(R.id.txt_current_time)
    TextView mTxt_current_time;

    @BindView(R.id.view_network_status)
    View mView_network_status;

    @BindView(R.id.view_battery_status)
    View mView_battery_status;

    @BindView(R.id.view_gps_status)
    View mView_gps_status;

    @BindView(R.id.main_container)
    View mView_mainContainer;

    public CustomViewGroup(Context context) {
        super(context);
        init(context);
    }

    public CustomViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.status_header, this);
        ButterKnife.bind(this, this);
        setBackgroundResource(R.drawable.header_bg);
        String number = null;
        if (PermissionUtil.checkPhonePermissions(getContext())) {
            number = AppUtils.getMobileNumber(getContext());
        }
        mTxt_serialNumber.setText(number != null ? number : getContext().getString(R.string.empty_phone));

        setCurrentTime();
        this.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                startListen();
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                stopListen();
            }
        });
    }

    private void stopListen() {
        if (timeChangeReceiver != null) {
            getContext().unregisterReceiver(timeChangeReceiver);
            timeChangeReceiver = null;
        }
        if (batInfoReceiver != null) {
            getContext().unregisterReceiver(batInfoReceiver);
            batInfoReceiver = null;
        }

        telephonyManager = null;
        myPhoneStateListener = null;
    }

    private void startListen() {
        myPhoneStateListener = new MyPhoneStateListener(percent -> {
            setNetworkSignal(percent);
        });
        telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        timeChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setCurrentTime();
            }
        };
        batInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                setBatteryChangeLevel(level);
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        getContext().registerReceiver(timeChangeReceiver, intentFilter);
        getContext().registerReceiver(batInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    public void setCurrentTime() {
        mTxt_current_time.setText(AppUtils.getCurrentTimeInHHmm());
    }

    public void setBarVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.INVISIBLE;
        mView_mainContainer.setVisibility(visibility);

        if (visible) {
            setBackgroundResource(R.drawable.header_bg);
        } else {
            setBackgroundColor(AppUtils.getColor(getContext(), R.color.shadow_background_color));
        }
    }

    public void setBatteryChangeLevel(int level) {
        mTxt_battery_status.setText(String.format("%d%%", level));
        mView_battery_status.getBackground().setLevel(level * 100);
    }

    public void setNetworkSignal(int percentage) {
        mView_network_status.getBackground().setLevel(percentage * 100);
    }

    public void setGpsSignal(boolean isGpsOn) {
        mView_gps_status.setVisibility(isGpsOn ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

}