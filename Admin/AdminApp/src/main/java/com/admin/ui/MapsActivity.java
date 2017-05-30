package com.admin.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.ArrayMap;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.admin.AppConstant;
import com.admin.R;
import com.admin.model.AppClusterItem;
import com.admin.model.PhoneSettings;
import com.admin.model.UserConnectionStatus;
import com.admin.util.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private View mWindow;
    private View mContents;
    private AutoCompleteTextView actSearch;

    private HashMap<String, Marker> markerMap = new HashMap<>();
    private ArrayList<String> deviceName = new ArrayList<>();
    private Map<UserConnectionStatus, PhoneSettings> mIsDeviceScheduleSetUpMap = new ArrayMap<>();
    private ArrayAdapter<String> adapter;
    private ClusterManager<AppClusterItem> mClusterManager;
    private Timer timer;

    private String strSearch = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        actSearch = (AutoCompleteTextView) findViewById(R.id.actSearch);

        actSearch.setOnItemClickListener((parent, view, position, id) -> {
            for (UserConnectionStatus connectionStatus : mIsDeviceScheduleSetUpMap.keySet()) {
                if (connectionStatus.deviceName.equalsIgnoreCase(actSearch.getText().toString())) {
                    hideKeyboard(MapsActivity.this);
                    LatLng mLatLong = new LatLng(Double.valueOf(connectionStatus.latitude), Double.valueOf(connectionStatus.longitude));
                    navigate(actSearch.getText().toString(), mLatLong);
                }
            }
        });

        adapter = new ArrayAdapter<>(MapsActivity.this, android.R.layout.simple_list_item_1, deviceName);
        actSearch.setAdapter(adapter);
        timer = new Timer();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        setUpClustering();
        getDevices();
    }

    private void getDevices() {
        FirebaseDatabase.getInstance().getReference().child(AppConstant.NODE_DEVICES)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mIsDeviceScheduleSetUpMap.clear();
                        if (dataSnapshot.getChildrenCount() > 0) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                UserConnectionStatus status = postSnapshot.getValue(UserConnectionStatus.class);
                                FirebaseDatabase.getInstance().getReference()
                                        .child(AppConstant.NODE_SETTING).child(status.deviceID)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                PhoneSettings setting = dataSnapshot.getValue(PhoneSettings.class);
                                                if (setting != null) {
                                                    mIsDeviceScheduleSetUpMap.put(status, setting);
                                                    addMarkersToMap();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null)
            timer.cancel();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void setUpClustering() {
        mClusterManager = new ClusterManager<>(this, mMap);
        mClusterManager.setRenderer(new ClusterRenderer());
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(cluster -> {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(), 20));
            return true;
        });
        addMarkersToMap();
    }


    /**
     * Add markers to map
     */
    private void addMarkersToMap() {
        mClusterManager.clearItems();
        for (UserConnectionStatus connectionStatus : mIsDeviceScheduleSetUpMap.keySet()) {
            if (connectionStatus != null) {
                PhoneSettings deviceSettings = mIsDeviceScheduleSetUpMap.get(connectionStatus);
                if (connectionStatus.latitude != null) {
                    LatLng mLatLong = new LatLng(Double.valueOf(connectionStatus.latitude), Double.valueOf(connectionStatus.longitude));
                    if (!deviceName.contains(connectionStatus.deviceName)) {
                        deviceName.add(connectionStatus.deviceName);
                    }
                    String status;
                    BitmapDescriptor markerColor;
                    if (Utils.isConnected(connectionStatus.createdAt)) {
                        int remainTime = connectionStatus.remain;
                        if (remainTime > 0) {
                            if (connectionStatus.isPlaying) {
                                status = "playing";
                                //markerColor = getMarkerIcon(getResources().getString(R.string.status_play));
                                markerColor = getMarkerIcon(R.drawable.play);
                            } else {
                                status = "pause";
                                //markerColor = getMarkerIcon(getResources().getString(R.string.status_pause));
                                markerColor = getMarkerIcon(R.drawable.pause);
                            }
                        } else {
                            if (mIsDeviceScheduleSetUpMap.containsKey(connectionStatus)) {
                                status = "pause";
                                // markerColor = getMarkerIcon(getResources().getString(R.string.status_pause));
                                markerColor = getMarkerIcon(R.drawable.pause);
                            } else {
                                status = "connected";
                                //markerColor = getMarkerIcon(getResources().getString(R.string.status_connected));
                                markerColor = getMarkerIcon(R.drawable.connect);
                            }
                        }
                    } else {
                        status = "disconnected";
                        //markerColor = getMarkerIcon(getResources().getString(R.string.status_disconnected));
                        markerColor = getMarkerIcon(R.drawable.disconnect);
                    }
                    AppClusterItem offsetItem = new AppClusterItem(connectionStatus.deviceName + "_:_" + status,
                            deviceSettings.startTime + "_:_" + deviceSettings.endTime + "_:_" + deviceSettings.songInterval + "_:_" + deviceSettings.pauseInterval,
                            markerColor, mLatLong.latitude, mLatLong.longitude);
                    mClusterManager.addItem(offsetItem);
                    mClusterManager.cluster();
                }
            }
        }
        markerMap.clear();
        for (Marker mm : mClusterManager.getMarkerCollection().getMarkers()) {
            markerMap.put(mm.getTitle().split("_:_")[0], mm);
        }
        adapter.notifyDataSetChanged();

    }

    /**
     * Navigate to device location
     *
     * @param name
     * @param LatLong
     */
    private void navigate(String name, LatLng LatLong) {
        boolean isFound = false;
        if (LatLong != null) {
            for (Marker mm : mClusterManager.getMarkerCollection().getMarkers()) {
                if (mm.getTitle().split("_:_")[0].equalsIgnoreCase(name)) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(LatLong).zoom(90).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    mm.showInfoWindow();
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                strSearch = name;
                CameraPosition cameraPosition = new CameraPosition.Builder().target(LatLong).zoom(90).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }

    /**
     * Render custom info window
     *
     * @param marker
     * @param view
     */
    private void render(Marker marker, final View view) {

        TextView txtUserName = ((TextView) view.findViewById(R.id.txtUserName));
        TextView txtPlayStatus = ((TextView) view.findViewById(R.id.txtPlayStatus));
        TextView txtStartTime = ((TextView) view.findViewById(R.id.txtStartTime));
        TextView txtEndTime = ((TextView) view.findViewById(R.id.txtEndTime));
        TextView txtPlayTime = ((TextView) view.findViewById(R.id.txtPlayTime));
        TextView txtPauseTime = ((TextView) view.findViewById(R.id.txtPauseTime));

        if ((marker != null) && marker.getTitle() != null) {
            String[] snippet = marker.getSnippet().split("_:_");
            String[] title = marker.getTitle().split("_:_");
            txtUserName.setText(title[0]);
            txtPlayStatus.setText(title[1]);

            if (title[1].equalsIgnoreCase("playing")) {
                txtPlayStatus.setText(Html.fromHtml(getString(R.string.playing)));
            } else if (title[1].equalsIgnoreCase("pause")) {
                txtPlayStatus.setText(Html.fromHtml(getString(R.string.pause)));
            } else if (title[1].equalsIgnoreCase("connected")) {
                txtPlayStatus.setText(Html.fromHtml(getString(R.string.connected)));
            } else if (title[1].equalsIgnoreCase("disconnected")) {
                txtPlayStatus.setText(Html.fromHtml(getString(R.string.disconnected)));
            }
            txtStartTime.setText("Start : " + snippet[0]);
            txtEndTime.setText("End : " + snippet[1]);
            txtPlayTime.setText("Play : " + snippet[2] + "mins");
            txtPauseTime.setText("Pause : " + snippet[3] + "mins");
        }

    }

    /**
     * Convert hex color to hsv for map icon
     *
     * @param id
     * @return
     */
    private BitmapDescriptor getMarkerIcon(int id) {
        return BitmapDescriptorFactory.fromResource(id);
    }

    /**
     * Hide keyboard
     *
     * @param mContext
     */
    public void hideKeyboard(Context mContext) {
        try {
            View view = ((Activity) mContext).getCurrentFocus();
            if (view != null) {
                InputMethodManager inputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Custom info window for marker
     */
    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mContents = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mContents);
            return mContents;
        }
    }

    /**
     * Custom cluster render
     */
    private class ClusterRenderer extends DefaultClusterRenderer<AppClusterItem> {

        public ClusterRenderer() {
            super(getApplicationContext(), mMap, mClusterManager);
        }

        @Override
        protected void onClusterItemRendered(AppClusterItem clusterItem, Marker marker) {
            super.onClusterItemRendered(clusterItem, marker);

            markerMap.put(clusterItem.getTitle().split("_:_")[0], marker);
            //BitmapDescriptor markerColor = getMarkerIcon(getResources().getString(R.string.status_connected));
            marker.setTitle(clusterItem.getTitle());
            marker.setSnippet(clusterItem.getSnippet());
            marker.setIcon(clusterItem.getColor());
            //System.out.println("ClusterRenderer.onClusterItemRendered strSearch : " + strSearch);
            if (!strSearch.equalsIgnoreCase("")) {
                if (clusterItem.getTitle().split("_:_")[0].equalsIgnoreCase(strSearch)) {
                    markerMap.get(clusterItem.getTitle().split("_:_")[0]).showInfoWindow();
                    strSearch = "";
                }
            }
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            return cluster.getSize() > 1;
        }
    }
}