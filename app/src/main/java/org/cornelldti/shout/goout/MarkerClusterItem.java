package org.cornelldti.shout.goout;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by kaushikr on 3/9/18.
 */

public class MarkerClusterItem implements ClusterItem {
    private final LatLng mPosition;
    private String mTitle;
    private String mSnippet;

    MarkerClusterItem(double lat, double lng, String title, String snippet) {
        this(lat, lng);
        mTitle = title;
        mSnippet = snippet;
    }

    MarkerClusterItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }
}
