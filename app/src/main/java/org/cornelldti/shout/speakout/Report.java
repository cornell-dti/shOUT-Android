package org.cornelldti.shout.speakout;

import java.io.Serializable;

/**
 * Representation of a message in the Firebase database.
 * <p>
 * Created by kaushikr on 2/1/18.
 * Updated by Evan Welsh on 2/28/18
 */

public class Report implements Serializable {

    // TODO[BACKEND] prevent uid from being locally synced...

    public transient static final String HAS_BODY = "hasbody";
    public transient static final String BODY = "body";
    public transient static final String TITLE = "title";
    public transient static final String LOCATION = "location";
    public transient static final String TIMESTAMP = "timestamp";

    private String body, title, location;
    private long timestamp;
    private boolean hasbody;

    Report(String body, String title, String location, long timestamp, boolean hasbody) {
        this.body = body;
        this.title = title;
        this.timestamp = timestamp;
        this.location = location;
        this.hasbody = hasbody;
    }

    public Report() {
    }

    public String getBody() {
        return body;
    }

    public String getTitle() {
        return title;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getLocation() {
        return location;
    }

    public boolean getHasBody() {
        return hasbody;
    }

}