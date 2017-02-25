package com.nielsenninjas.wafernav;

import android.location.Location;
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

public class MyMapFragment extends SupportMapFragment implements OnMapReadyCallback {

    private static final LatLng IRVINE_LAT_LNG = new LatLng(33.6839, -117.7947);
    private GoogleMap mMap;
    private float zoom = 18.0f;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(IRVINE_LAT_LNG).title("Irvine"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(IRVINE_LAT_LNG));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                zoom = mMap.getCameraPosition().zoom;
            }
        });
    }

    public void updateMap(Location loc) {
        LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title("Current location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
    }
}
