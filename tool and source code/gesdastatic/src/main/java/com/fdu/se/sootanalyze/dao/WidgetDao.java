package com.fdu.se.sootanalyze.dao;

import com.fdu.se.sootanalyze.model.Widget;
import com.fdu.se.sootanalyze.utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class WidgetDao {
    public void insertWidget(Widget w,long actId){
        try{
            Connection conn = DBUtil.getConnection();
            String sql = "insert into widget(id,widget_type,widget_id_name,event,listener_name," +
                    "event_method,layout_register,act_id,dep_widgets) values (?,?,?,?,?,?,?,?,?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setLong(1,w.getId());
            preparedStatement.setString(2,w.getWidgetType());
            preparedStatement.setString(3, w.getWidgetId());
            preparedStatement.setString(4,w.getEvent());
            preparedStatement.setString(5, w.getListenerName());
            preparedStatement.setString(6, w.getEventMethod());
            preparedStatement.setInt(7,w.getLayoutRegister());
            preparedStatement.setLong(8,actId);
            List<Widget> dWidgets = w.getdWidgets();
            preparedStatement.setString(9,convertToString(dWidgets));
            int changeRows = preparedStatement.executeUpdate();
            if(changeRows > 0){
                System.out.println("insert widget " + w.getId() + " successfully");
            }
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(conn);
            if(!dWidgets.isEmpty()){
                for(Widget dWidget:dWidgets){
                    insertDepWidget(dWidget);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void insertDepWidget(Widget w){
        try{
            Connection conn = DBUtil.getConnection();
            String sql = "insert into dep_widget(id,widget_type,widget_id_name) values (?,?,?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setLong(1,w.getId());
            preparedStatement.setString(2,w.getWidgetType());
            preparedStatement.setString(3, w.getWidgetId());
            int changeRows = preparedStatement.executeUpdate();
            if(changeRows > 0){
                System.out.println("insert dep_widget " + w.getId() + " successfully");
            }
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(conn);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private String convertToString(List<Widget> depWidgets){
        if(!depWidgets.isEmpty()){
            StringBuilder builder = new StringBuilder();
            for(Widget depWidget:depWidgets){
                long id = depWidget.getId();
                builder.append(id);
                builder.append(',');
            }
            String str = builder.toString();
            return str.substring(0,str.length()-1);
        }
        return null;
    }
}
