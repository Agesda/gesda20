package com.fdu.se.sootanalyze.model;

public class MenuItem extends Widget {
    private String text;
    private int itemId;

    public MenuItem() {
        this.setEvent("click");
        this.setWidgetType("android.view.MenuItem");
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
}
