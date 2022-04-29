package com.example.myapplication.Models;

import com.google.gson.annotations.SerializedName;

public class SendModel {
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("gps")
    private String gps;
    @SerializedName("marker_type")
    private int requestType;
    @SerializedName("image")
    private String image;

    public SendModel(String name, String description, String gps, int requestType, String image) {
        this.name = name;
        this.description = description;
        this.gps = gps;
        this.requestType = requestType;
        this.image = image;
    }

}
