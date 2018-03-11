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
    private boolean has_body;

    ApprovedReport(String body, String title, String location, long timestamp, boolean has_body) {
        this.body = body;
        this.title = title;
        this.timestamp = timestamp;
        this.location = location;
        this.has_body = has_body;
    }

    public ApprovedReport() {
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

    public boolean hasBody() {
        return getHas_Body();
    }

    // TODO Cleanup the backend or use a custom parser... ?
    public boolean getHas_Body() {
        return has_body;
    }
}