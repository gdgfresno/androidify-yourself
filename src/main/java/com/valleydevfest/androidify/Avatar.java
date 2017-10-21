package com.valleydevfest.androidify;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Avatar {
    public String name;
    public Integer head;
    public Integer body;
    public Integer legs;

    public Avatar() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Avatar(String name, Integer head, Integer body, Integer legs) {
        this.name = name;
        this.head = head;
        this.body = body;
        this.legs = legs;
    }

}