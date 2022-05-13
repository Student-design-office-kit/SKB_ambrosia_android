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
    private int marker_type;
    @SerializedName("image")
    private String image;

    public SendModel(String name, String description, String gps, int marker_type, String image) {
        this.name = name;
        this.description = description;
        this.gps = gps;
        this.marker_type = marker_type;
        this.image = image;
    }

    @Override
    public String toString() {
        return "SendModel{" +
                "name:\"" + name + '\"' +
                ", description:\"" + description + '\"' +
                ", gps:\"" + gps + '\"' +
                ", marker_type:" + marker_type +
                ", image:\"" + image + '\"' +
                '}';
    }
}
