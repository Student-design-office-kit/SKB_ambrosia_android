package com.example.myapplication.Models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Markers {
    @SerializedName("markers")
    private List<Response> markers = null;

    public List<Response> getMarkers() {
        return markers;
    }

    public void setMarkers(List<Response> markers) {
        this.markers = markers;
    }
}
