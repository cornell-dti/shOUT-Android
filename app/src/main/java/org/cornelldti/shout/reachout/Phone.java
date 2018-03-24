package org.cornelldti.shout.reachout;

import java.io.Serializable;

/**
 * Created by kaushikr on 3/19/18.
 */

public class Phone implements Serializable {

    private String number, label = "Phone", description = "";

    public Phone() {
    }

    public Phone(String number, String label) {
        this.label = label;
        this.number = number;
        this.description = "";
    }

    public Phone(String number, String label, String description) {
        this.label = label;
        this.number = number;
        this.description = description;
    }

    String getLabel() {
        return label;
    }

    String getNumber() {
        return number;
    }

    String getDescription() {
        return description;
    }
}
