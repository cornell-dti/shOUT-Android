package org.cornelldti.shout.reachout;

import java.io.Serializable;

/**
 * Created by kaushikr on 3/19/18.
 */

public class Phone implements Serializable {

    public String number, label = "Phone", description = "";
    public boolean emergency;

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

    public boolean isEmergency() {
        return emergency;
    }

    public String getLabel() {
        return label;
    }

    public String getNumber() {
        return number;
    }

    public String getDescription() {
        return description;
    }

}
