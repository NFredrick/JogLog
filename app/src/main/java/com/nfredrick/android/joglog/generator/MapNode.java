package com.nfredrick.android.joglog.generator;

import java.io.Serializable;

public class MapNode implements Serializable {
    private long id;
    private double latitude;
    private double longitude;
    private int visitCount;

    public MapNode(long id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.visitCount = 0;
    }

    public double getLatitude() {
        return latitude;
    }


    public double getLongitude() {
        return longitude;
    }

    public long getId() {
        return id;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public void incrementVisit() {
        this.setVisitCount(this.getVisitCount() + 1);
    }

    public void decrementVisit() {
        this.setVisitCount(this.getVisitCount() - 1);
    }
}
