package com.fdu.se.sootanalyze.model;

import java.util.ArrayList;
import java.util.List;

public class Widget {
    private String widgetType;
    private String widgetId;
    private String event;
    private String listenerName;
    private String eventMethod;
    private int layoutRegister = 0;//whether the event of the widget is registered in the layout file, 1 yes, 0 no
    private long id;
    private List<Widget> dWidgets = new ArrayList<>();//the dependency of this widget

    public Widget() {
    }

    public String getWidgetType() {
        return widgetType;
    }

    public void setWidgetType(String widgetType) {
        this.widgetType = widgetType;
    }

    public String getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(String widgetId) {
        this.widgetId = widgetId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEventMethod() {
        return eventMethod;
    }

    public void setEventMethod(String eventMethod) {
        this.eventMethod = eventMethod;
    }

    public int getLayoutRegister() {
        return layoutRegister;
    }

    public void setLayoutRegister(int layoutRegister) {
        this.layoutRegister = layoutRegister;
    }

    public String getListenerName() {
        return listenerName;
    }

    public void setListenerName(String listenerName) {
        this.listenerName = listenerName;
    }

    public List<Widget> getdWidgets() {
        return dWidgets;
    }

    public void setdWidgets(List<Widget> dWidgets) {
        this.dWidgets = dWidgets;
    }

    @Override
    public String toString() {
        return "Widget{" +
                "widgetType='" + widgetType + '\'' +
                ", widgetId='" + widgetId + '\'' +
                ", event='" + event + '\'' +
                ", listenerName='" + listenerName + '\'' +
                ", eventMethod='" + eventMethod + '\'' +
                ", layoutRegister=" + layoutRegister +
                ", id=" + id +
                '}';
    }
}
