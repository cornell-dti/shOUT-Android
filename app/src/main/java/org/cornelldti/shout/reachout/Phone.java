package org.cornelldti.shout.reachout;

/**
 * Created by kaushikr on 3/19/18.
 */

public class Phone {

    private String number;
    private String label;
    private String description = new String();

    public Phone()
    {

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

    public String getLabel()
    {
        return label;
    }

    public String getNumber()
    {
        return number;
    }
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
