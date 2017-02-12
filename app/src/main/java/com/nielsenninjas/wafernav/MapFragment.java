package com.nielsenninjas.wafernav;

import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 Created by brian on 2/12/2017.
 */

public class MapFragment extends SupportMapFragment implements OnMapReadyCallback {

    private static final LatLng IRVINE_LAT_LNG = new LatLng(33.6839, -117.7947);
    private GoogleMap mMap;

    private static MapFragment mapFragment;

    public static MapFragment getInstance() {
        return mapFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getMapAsync(this);
        mapFragment = this;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(IRVINE_LAT_LNG).title("Irvine"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(IRVINE_LAT_LNG));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
    }

    public void updateMap(LatLng loc) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(loc).title("Current location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));
    }
}
