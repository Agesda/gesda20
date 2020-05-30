package com.fdu.uiautomatortest.dao;

import com.fdu.uiautomatortest.model.Widget;
import com.fdu.uiautomatortest.utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class WidgetDao {
    public List<Widget> getWidgetsByActId(long act_id){
        List<Widget> widgets = new ArrayList<>();
        try{
            Connection conn = DBUtil.getConnection();
            String sql = "select * from widget where act_id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setLong(1,act_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                Widget w = new Widget();
                w.setId(resultSet.getLong("id"));
                w.setWidgetType(resultSet.getString("widget_type"));
                w.setWidgetId(resultSet.getString("widget_id_name"));
                w.setEvent(resultSet.getString("event"));
                w.setListenerName(resultSet.getString("listener_name"));
                w.setEventMethod(resultSet.getString("event_method"));
                w.setLayoutRegister(resultSet.getInt("layout_register"));
                String depwStr = resultSet.getString("dep_widgets");
                if(depwStr != null){
                    List<Widget> depWidgets = getDepWidgets(depwStr);
                    w.setdWidgets(depWidgets);
                }
                widgets.add(w);
            }
            DBUtil.closeResultset(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(conn);
        }catch(Exception e){
            e.printStackTrace();
        }
        return widgets;
    }

    private List<Widget> getDepWidgets(String depw){
        List<Widget> depWidgets = new ArrayList<>();
        String[] idArr = depw.split(",");
        for(String idStr:idArr){
            long id = Long.parseLong(idStr);
            Widget depWidget = getDepWidgetById(id);
            if(depWidget != null){
                depWidgets.add(depWidget);
            }
        }
        return depWidgets;
    }

    private Widget getDepWidgetById(long id){
        try{
            Connection conn = DBUtil.getConnection();
            String sql = "select * from dep_widget where id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setLong(1,id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                Widget depWidget = new Widget();
                depWidget.setId(id);
                depWidget.setWidgetType(resultSet.getString("widget_type"));
                depWidget.setWidgetId(resultSet.getString("widget_id_name"));
                return depWidget;
            }
            DBUtil.closeResultset(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(conn);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
