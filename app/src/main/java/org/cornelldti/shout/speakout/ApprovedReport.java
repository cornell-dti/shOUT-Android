package org.cornelldti.shout.speakout;

/**
 * Representation of a message in the Firebase database.
 * <p>
 * Created by kaushikr on 2/1/18.
 * Updated by Evan Welsh on 2/28/18
 */

public class ApprovedReport {

    private String body, title, location;
    private long timestamp;

    ApprovedReport(String body, String title, String location, long timestamp) {
        this.body = body;
        this.title = title;
        this.timestamp = timestamp;
        this.location = location;
    }

    public ApprovedReport() {
    }

    public String getBody() {
        return body;
    }

    public String getTitle() {
        return title;
    }

    public long getTime() {
        return timestamp;
    }

    public String getLocation() {
        return location;
    }
}