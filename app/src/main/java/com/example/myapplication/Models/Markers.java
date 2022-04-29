package com.example.myapplication.Models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Markers {
    @SerializedName("id")
    private Integer id;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("marker_type")
    private Integer markerType;
    @SerializedName("gps")
    private String gps;
    @SerializedName("image")
    private String image;
    @SerializedName("get_image")
    private String getImage;
    @SerializedName("get_thumbnail")
    private String getThumbnail;
    @SerializedName("created_on")
    private String createdOn;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getMarkerType() {
        return markerType;
    }

    public String getGps() {
        return gps;
    }

    public String getImage() {
        return image;
    }

    public String getGetImage() {
        return getImage;
    }

    public String getGetThumbnail() {
        return getThumbnail;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public double getLon(){
        return Double.parseDouble((gps.split(","))[1]);
    }

    public double getLat(){
        return Double.parseDouble((gps.split(","))[0]);
    }

    @Override
    public String toString() {
        return "Markers{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", markerType=" + markerType +
                ", gps='" + gps + '\'' +
                ", image='" + image + '\'' +
                ", getImage='" + getImage + '\'' +
                ", getThumbnail='" + getThumbnail + '\'' +
                ", createdOn='" + createdOn + '\'' +
                '}';
    }
}
