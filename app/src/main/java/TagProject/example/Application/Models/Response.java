package TagProject.example.Application.Models;

import com.google.gson.annotations.SerializedName;

public class Response {
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("gps")
    private String gps;
    @SerializedName("marker_type")
    private String markerType;
    @SerializedName("slug")
    private String slug;

    public String getDescription() {
        return description;
    }

    public String getSlug() {
        return slug;
    }

    public String getGps(){
        return gps;
    }

    public double getLat() {
        double lat = Double.parseDouble(gps.split(", ")[0]);
        return lat;
    }

    public double getLon() {
        double lon = Double.parseDouble(gps.split(", ")[1]);
        return lon;
    }

    public String getMarkerType() {
        return markerType;
    }
}
