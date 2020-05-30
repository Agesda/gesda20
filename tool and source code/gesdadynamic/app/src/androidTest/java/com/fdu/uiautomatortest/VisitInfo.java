package com.fdu.uiautomatortest;

import java.util.Objects;

public class VisitInfo {
    private Integer openType = 0;//0:original 1:dialog 2:menu
    private Boolean visit = true;
    private Integer frequency = 0;

    public VisitInfo(Integer openType, Boolean visit) {
        this.openType = openType;
        this.visit = visit;
    }

    public VisitInfo() {
    }

    public Integer getOpenType() {
        return openType;
    }

    public void setOpenType(Integer openType) {
        this.openType = openType;
    }

    public Boolean getVisit() {
        return visit;
    }

    public void setVisit(Boolean visit) {
        this.visit = visit;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VisitInfo visitInfo = (VisitInfo) o;
        return Objects.equals(openType, visitInfo.openType) &&
                Objects.equals(visit, visitInfo.visit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(openType, visit);
    }

    @Override
    public String toString() {
        return "VisitInfo{" +
                "openType=" + openType +
                ", visit=" + visit +
                ", frequency=" + frequency +
                '}';
    }
}
