package com.fdu.se.sootanalyze.model;

import java.util.ArrayList;
import java.util.List;

public class SubMenu extends Widget {
    private String text;
    private int subMenuId;
    private List<MenuItem> items = new ArrayList<>();

    public SubMenu() {
        this.setEvent("click");
        this.setWidgetType("android.view.SubMenu");
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getSubMenuId() {
        return subMenuId;
    }

    public void setSubMenuId(int subMenuId) {
        this.subMenuId = subMenuId;
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public void setItems(List<MenuItem> items) {
        this.items = items;
    }
}
