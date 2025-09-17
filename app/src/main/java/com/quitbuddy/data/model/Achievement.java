package com.quitbuddy.data.model;

public class Achievement {
    public final String name;
    public final boolean achieved;
    public final String description;

    public Achievement(String name, boolean achieved, String description) {
        this.name = name;
        this.achieved = achieved;
        this.description = description;
    }
}
