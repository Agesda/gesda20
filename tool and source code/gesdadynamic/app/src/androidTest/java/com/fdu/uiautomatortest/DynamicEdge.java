package com.fdu.uiautomatortest;

public class DynamicEdge {
    private String sourceAct;
    private String targetAct;
    private String elementId;
    private String event = "click";

    public DynamicEdge(){

    }

    public DynamicEdge(String sourceAct, String targetAct, String elementId, String event) {
        this.sourceAct = sourceAct;
        this.targetAct = targetAct;
        this.elementId = elementId;
        this.event = event;
    }

    public DynamicEdge(String sourceAct, String targetAct, String elementId) {
        this.sourceAct = sourceAct;
        this.targetAct = targetAct;
        this.elementId = elementId;
    }
}
