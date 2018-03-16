package org.cornelldti.shout.goout;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by kaushikr on 3/9/18.
 */

public class MarkerClusterItem implements ClusterItem {
    private final LatLng mPosition;
    private String reportId;

    MarkerClusterItem(double lat, double lng, String reportId) {
        this(lat, lng);
        this.reportId = reportId;
    }

    private MarkerClusterItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    /* These are no longer necessary due to our bottom sheet... */

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    public String getReportId() {
        return reportId;
    }
}
