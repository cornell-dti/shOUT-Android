package org.cornelldti.shout.reachout;

import java.io.Serializable;
import java.util.ArrayList;
<<<<<<< Updated upstream
=======
<<<<<<< HEAD
import java.util.Collection;
=======
>>>>>>> origin/master
>>>>>>> Stashed changes
import java.util.List;

/**
 * Represents a resource in the RecyclerView
 * Created by Evan Welsh on 3/1/18.
 */

public class Resource implements Serializable {
<<<<<<< Updated upstream
    private String url, name, description, address, section, email;
=======
<<<<<<< HEAD
    private String url, name, description, address, email;
=======
    private String url, name, description, address, section, email;
>>>>>>> origin/master
>>>>>>> Stashed changes
    private int ordering;

    private ArrayList<Phone> phones = new ArrayList<>();

    /* FIRESTORE KEYS */
    public static final String PHONES = "phones";

    public Resource() {
    }

<<<<<<< Updated upstream
    public Resource(String url, String name, String description, String address, String section, List<Phone> phones, int ordering) {
=======
<<<<<<< HEAD
    public Resource(String url, String name, String description, String address, List<Phone> phones, int ordering, String email) {
=======
    public Resource(String url, String name, String description, String address, String section, List<Phone> phones, int ordering) {
>>>>>>> origin/master
>>>>>>> Stashed changes
        this.url = url;
        this.name = name;
        this.description = description;
        this.address = address;
        this.section = section;
        this.ordering = ordering;
        this.phones = new ArrayList<>(phones);
        this.email = email;
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

    public String getSection() {
        return section;
    }

    public String getEmail() {
        return email;
    }

    public List<Phone> getPhoneNumbers() {
        return phones;
    }

<<<<<<< Updated upstream
    void addPhoneNumber(Phone number) {
        this.phones.add(number);
=======
<<<<<<< HEAD
    public String getEmail() {
        return email;
    }

    void setPhoneNumbers(Collection<Phone> numbers) {
        this.phones = new ArrayList<>(numbers);
=======
    void addPhoneNumber(Phone number) {
        this.phones.add(number);
>>>>>>> origin/master
>>>>>>> Stashed changes
    }

}
