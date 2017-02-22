package com.nielsenninjas.wafernav;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

/**
 Created by Brian on 2/22/2017.
 */

public class MockData {

    private static Map<String, LatLng> data;

    public static Map<String, LatLng> getData() {
        if (data == null) {
            populateMap();
        }
        return data;
    }

    private static void populateMap() {
        data = new HashMap<>();
        data.put("W", new LatLng(33.647242, -117.711847));
        data.put("S", new LatLng(33.646938, -117.711417));
        data.put("E", new LatLng(33.647256, -117.711089));
        data.put("N", new LatLng(33.647489, -117.711617));
    }

}
