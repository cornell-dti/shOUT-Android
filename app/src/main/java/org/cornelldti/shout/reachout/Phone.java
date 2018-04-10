package org.cornelldti.shout.reachout;

import java.io.Serializable;

/**
 * Created by kaushikr on 3/19/18.
 * Updated by Evan Welsh on 4/9/18.
 */

public class Phone implements Serializable {

    private String number, label, description;
    private boolean emergency;

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
