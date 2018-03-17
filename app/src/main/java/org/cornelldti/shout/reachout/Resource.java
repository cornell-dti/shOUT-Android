package org.cornelldti.shout.reachout;

/**
 * Represents a resource in the RecyclerView
 * Created by Evan Welsh on 3/1/18.
 */

public class Resource {
    private String website, title, description;
    private int ordering;

    public Resource() {
    }

    public Resource(String website, String title, String description, int ordering) {
        this.website = website;
        this.title = title;
        this.description = description;
        this.ordering = ordering;
    }

    public String getWebsite() {
        return website;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getOrdering() {
        return ordering;
    }

}
