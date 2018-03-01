package com.android.shout.util;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * A basic utility class to store common locations and for common methods needed
 * to manipulate Android locations.
 * <p>
 * Created by Evan Welsh 2/28/18
 */

public class LocationUtil {
    private static final LatLng ITHACA_BOUND_A = new LatLng(42.498525d, -76.569787d);
    private static final LatLng ITHACA_BOUND_B = new LatLng(42.394149d, -76.414069d);

    public static LatLngBounds getIthacaBounds() {
        return LatLngBounds.builder().include(ITHACA_BOUND_A).include(ITHACA_BOUND_B).build();
    }

    public static final LatLng CORNELL_CENTER = new LatLng(42.448795d, -76.483939d);

    public static Location latLngToLocation(LatLng latLng) {
        Location location = new Location("");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }
}
