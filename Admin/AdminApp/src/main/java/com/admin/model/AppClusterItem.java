package com.admin.model;

/**
 * Created by dipen on 10/3/16.
 */

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;


public class AppClusterItem implements ClusterItem {

    private final LatLng mPosition;
    private final String title,snippet;
    private final BitmapDescriptor color;

    public AppClusterItem(String title, String snippet,BitmapDescriptor color,double latitude, double longitude) {
        mPosition = new LatLng(latitude, longitude);
        this.title = title;
        this.snippet = snippet;
        this.color = color;

    }


    public String getTitle(){
        return title;
    }
    public String getSnippet(){
        return snippet;
    }
    public BitmapDescriptor getColor(){
        return color;
    }
    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}