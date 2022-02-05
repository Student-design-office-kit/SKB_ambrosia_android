package com.example.myapplication.Models;

public class MD5 {
    private String description;
    private String gps;
    private String image;
    private String name;
    private String request_type;

    public MD5(String description, String gps, String image, String name, String request_type) {
        this.description = description;
        this.gps = gps;
        this.image = image;
        this.name = name;
        this.request_type = request_type;
    }

    public String getDescription() {
        return description;
    }

    public String getGps() {
        return gps;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getRequest_type() {
        return request_type;
    }
}
