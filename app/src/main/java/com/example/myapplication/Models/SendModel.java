package com.example.myapplication.Models;

public class SendModel {
    private String name;
    private String description;
    private String gps;
    private String request_type;
    private String image;

    public SendModel(String name, String description, String gps, String request_type, String image) {
        this.name = name;
        this.description = description;
        this.gps = gps;
        this.request_type = request_type;
        this.image = image;
    }

}
