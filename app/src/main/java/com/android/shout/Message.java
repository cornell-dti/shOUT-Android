package com.android.shout;

/**
 * Representation of a message in the Firebase database.
 * <p>
 * Created by kaushikr on 2/1/18.
 * Updated by Evan Welsh on 2/28/18
 */

public class Message {

    private String body, date, time, title, ID;

    Message(String body, String date, String time, String title) {
        this.body = body;
        this.date = date;
        this.title = title;
        this.time = time;
    }

    public Message() {
        // NO ARGUMENT CONSTRUCTOR
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getID() {
        return ID;
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

    public String getTime() {
        return time;
    }
}