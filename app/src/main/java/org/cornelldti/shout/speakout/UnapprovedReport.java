package org.cornelldti.shout.speakout;

import com.google.android.gms.maps.model.LatLng;

/**
 * Representation of a message in the Firebase database.
 * <p>
 * Created by kaushikr on 2/1/18.
 * Updated by Evan Welsh on 2/28/18
 */
@SuppressWarnings("unused")
public class UnapprovedReport {

    private double locationLong, locationLat;
    private String body, title, uid, locationLabel;
    private long timestamp;

    UnapprovedReport(String body, String title, String uid, String locationLabel, LatLng latLng, long timestamp) {
        this.body = body;
        this.title = title;
        this.uid = uid;
        this.timestamp = timestamp;
        this.locationLat = latLng.latitude;
        this.locationLong = latLng.longitude;
        this.locationLabel = locationLabel;
    }

    UnapprovedReport(String body, String title, String uid, long timestamp)
    {
        this.body = body;
        this.title = title;
        this.uid = uid;
        this.timestamp = timestamp;
        this.locationLabel = "";
        this.locationLat = 0;
        this.locationLong = 0;
    }

    public double getLocationLong() {
        return locationLong;
    }

    public double getLocationLat() {
        return locationLat;
    }

    public String getBody() {
        return body;
    }

    public String getTitle() {
        return title;
    }

    public String getUid() {
        return uid;
    }

    public String getLocationLabel() {
        return locationLabel;
    }

    public long getTimestamp() {
        return timestamp;
    }
}