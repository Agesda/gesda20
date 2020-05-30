package com.fdu.se.sootanalyze.dao;

import com.fdu.se.sootanalyze.model.MenuItem;
import com.fdu.se.sootanalyze.utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class MenuItemDao {
    public void insertMenuItem(MenuItem menuItem,long subId,long menuId){
        try{
            Connection conn = DBUtil.getConnection();
            String sql = "insert into menu_item(id,widget_type,item_id,text,event,sub_id,menu_id) " +
                    "values (?,?,?,?,?,?,?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setLong(1,menuItem.getId());
            preparedStatement.setString(2,menuItem.getWidgetType());
            preparedStatement.setInt(3,menuItem.getItemId());
            preparedStatement.setString(4,menuItem.getText());
            preparedStatement.setString(5, menuItem.getEvent());
            preparedStatement.setLong(6,subId);
            preparedStatement.setLong(7, menuId);
            int changeRows = preparedStatement.executeUpdate();
            if(changeRows > 0){
                System.out.println("insert menu_item " + menuItem.getId() + " successfully");
            }
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(conn);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
