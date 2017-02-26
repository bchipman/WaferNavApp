package com.nielsenninjas.wafernav;

import android.location.Location;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

/**
 Created by Brian on 2/22/2017.
 */

public class MockData {

    private static Map<String, Location> data;
    public static final String MOCK_DATA_PROVIDER = "MOCK_DATA_PROVIDER";

    public static Map<String, Location> getData() {
        if (data == null) {
            populateMap();
        }
        return data;
    }

    private static void populateMap() {
        data = new HashMap<>();
        data.put("W", createLocation(33.647242, -117.711847));
        data.put("W", createLocation(33.647242, -117.711847));
        data.put("S", createLocation(33.646938, -117.711417));
        data.put("E", createLocation(33.647256, -117.711089));
        data.put("N", createLocation(33.647489, -117.711617));
        data.put("WEST", data.get("W"));
        data.put("SOUTH", data.get("S"));
        data.put("EAST", data.get("E"));
        data.put("NORTH", data.get("N"));
        data.put("UCI", createLocation(33.645840, -117.842842));
        data.put("DBH", createLocation(33.643164, -117.841815));
        data.put("ICS", createLocation(33.644259, -117.841748));
    }

    private static Location createLocation(double lat, double lng) {
        Location loc = new Location(MOCK_DATA_PROVIDER);
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        return loc;
    }
}
