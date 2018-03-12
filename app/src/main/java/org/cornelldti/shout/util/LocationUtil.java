package org.cornelldti.shout.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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

    /**
     * Converts latitude and longitude to a location object.
     *
     * @param latLng - The LatLng object to convert
     * @return - A Location object with the appropriate latitude and longitude
     */
    public static Location latLngToLocation(LatLng latLng) {
        Location location = new Location("");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }

    /**
     * Converts latitude and longitude to an address object.
     *
     * @param context - The context to get addresses, formatting, etc. within
     * @param latLng  - The lat and lng of the goal address
     * @return - And address from lat and lng.
     */
    public static Address getAddressForLocation(Context context, LatLng latLng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        Address address = null;
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // todo 1 may not be enough ;)
            if (addresses.size() > 0) {
                address = addresses.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }
}
