package com.duodeck.workout;

public class Stats {
    public int icon;
    public String title;
    public String value;
    public Stats(){
        super();
    }
    
    public Stats(int icon, String title, String value) {
        this.icon = icon;
        this.title = title;
        this.value = value;
    }
}