package com.akouki.rssreader.models;

public class DrawerItem {
    private String favicon;
    private String name;
    private int counter;

    public DrawerItem(String favicon, String name, int counter) {
        this.favicon = favicon;
        this.name = name;
        this.counter = counter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public String getFavicon() {
        return favicon;
    }

    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }
}