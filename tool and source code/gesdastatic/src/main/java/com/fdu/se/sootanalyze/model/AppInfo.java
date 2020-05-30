package com.fdu.se.sootanalyze.model;

import java.util.List;

public class AppInfo {
    private String packName;
    private List<String> activities;

    public AppInfo(String packName, List<String> activities) {
        this.packName = packName;
        this.activities = activities;
    }

    public AppInfo() {
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public List<String> getActivities() {
        return activities;
    }

    public void setActivities(List<String> activities) {
        this.activities = activities;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "packName='" + packName + '\'' +
                ", activities=" + activities +
                '}';
    }
}
