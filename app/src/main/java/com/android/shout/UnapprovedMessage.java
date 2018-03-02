package com.android.shout;

/**
 * Representation of a message in the Firebase database.
 * <p>
 * Created by kaushikr on 2/1/18.
 * Updated by Evan Welsh on 2/28/18
 */

public class Message {

    private String body, date, title;
    private long timestamp;

    Message(String body, String date, long timestamp) {
        this.body = body;
        this.date = date;
        this.title = title;
        this.timestamp = timestamp;
    }

    public Message() {
        // NO ARGUMENT CONSTRUCTOR
    }

    public String getBody() {
        return body;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public long getTime() {
        return timestamp;
    }
}