package com.fdu.uiautomatortest;

import android.graphics.Rect;

import java.util.Objects;

public class UIElement {
    private String text;
    private String resourceId;
    private String contentDesc;
    private String classType;
    private Rect visBounds;
    private String hostACT;
//    private Integer openType = 0; //0:original 1:dialog 2:menu
//    private Boolean visit = false;

    public UIElement() {
    }

    public UIElement(String text, String resourceId, String contentDesc, String classType, Rect visBounds, String hostACT) {
        this.text = text;
        this.resourceId = resourceId;
        this.contentDesc = contentDesc;
        this.classType = classType;
        this.visBounds = visBounds;
        this.hostACT = hostACT;
    }

    public UIElement(String text, String resourceId, String contentDesc, String classType, String hostACT) {
        this.text = text;
        this.resourceId = resourceId;
        this.contentDesc = contentDesc;
        this.classType = classType;
        this.hostACT = hostACT;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getContentDesc() {
        return contentDesc;
    }

    public void setContentDesc(String contentDesc) {
        this.contentDesc = contentDesc;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public Rect getVisBounds() {
        return visBounds;
    }

    public void setVisBounds(Rect visBounds) {
        this.visBounds = visBounds;
    }

    public String getHostACT() {
        return hostACT;
    }

    public void setHostACT(String hostACT) {
        this.hostACT = hostACT;
    }

//    public Integer getOpenType() {
//        return openType;
//    }
//
//    public void setOpenType(Integer openType) {
//        this.openType = openType;
//    }
//
//    public Boolean getVisit() {
//        return visit;
//    }
//
//    public void setVisit(Boolean visit) {
//        this.visit = visit;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UIElement uiElement = (UIElement) o;
        return Objects.equals(text, uiElement.text) &&
                Objects.equals(resourceId, uiElement.resourceId) &&
                Objects.equals(contentDesc, uiElement.contentDesc) &&
                Objects.equals(classType, uiElement.classType) &&
                Objects.equals(visBounds, uiElement.visBounds) &&
                Objects.equals(hostACT, uiElement.hostACT);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, resourceId, contentDesc, classType, visBounds, hostACT);
    }

//    @Override
//    public String toString() {
//        return "UIElement{" +
//                "text='" + text + '\'' +
//                ", resourceId='" + resourceId + '\'' +
//                ", contentDesc='" + contentDesc + '\'' +
//                ", classType='" + classType + '\'' +
//                ", visBounds=" + visBounds +
//                ", hostACT='" + hostACT + '\'' +
//                ", openType=" + openType +
//                ", visit=" + visit +
//                '}';
//    }

    @Override
    public String toString() {
        return "UIElement{" +
                "text='" + text + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", contentDesc='" + contentDesc + '\'' +
                ", classType='" + classType + '\'' +
                ", visBounds=" + visBounds +
                ", hostACT='" + hostACT + '\'' +
                '}';
    }
}
