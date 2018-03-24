package org.cornelldti.shout.reachout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents a resource in the RecyclerView
 * Created by Evan Welsh on 3/1/18.
 */

public class Resource implements Serializable {
    private String url, name, description, address;
    private int ordering;

    private ArrayList<Phone> phones = new ArrayList<>();

    /* FIRESTORE KEYS */
    public static final String PHONES = "phones";

    public Resource() {
    }

    public Resource(String url, String name, String description, String address, List<Phone> phones, int ordering) {
        this.url = url;
        this.name = name;
        this.description = description;
        this.address = address;
        this.ordering = ordering;
        this.phones = new ArrayList<>(phones);
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getOrdering() {
        return ordering;
    }

    public String getAddress() {
        return address;
    }

    public List<Phone> getPhoneNumbers() {
        return phones;
    }

    void setPhoneNumbers(Collection<Phone> numbers) {
        this.phones = new ArrayList<>(numbers);
    }


}
