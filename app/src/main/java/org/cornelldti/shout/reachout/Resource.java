package org.cornelldti.shout.reachout;

import java.util.Collection;

/**
 * Represents a resource in the RecyclerView
 * Created by Evan Welsh on 3/1/18.
 */

public class Resource {
    private String url, name, description;
    private int ordering;
    private Collection<Phone> phones;

    public Resource() {
    }

    public Resource(String url, String name, String description, Collection<Phone> phones, int ordering) {
        this.url = url;
        this.name = name;
        this.description = description;
        this.ordering = ordering;
        this.phones = phones;
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

//    public Query getPhones()
//    {
//        Query query = FirebaseFirestore.getInstance().collection("resources");
//    }

}
