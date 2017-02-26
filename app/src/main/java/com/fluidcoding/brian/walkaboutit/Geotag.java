package com.fluidcoding.brian.walkaboutit;

/**
 * Created by George on 2/26/2017.
 */

public class Geotag {
    public double lat, lng;
    public String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Geotag(double lat, double lng, String name) {
        this.lat = lat;
        this.lng = lng;
        this.name = name;
    }

    public Geotag(){}



    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }




    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}
