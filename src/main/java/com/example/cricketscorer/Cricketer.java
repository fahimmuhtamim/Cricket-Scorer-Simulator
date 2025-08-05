package com.example.cricketscorer;

public class Cricketer {
    private String name;

    public Cricketer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return name;
    }
}
