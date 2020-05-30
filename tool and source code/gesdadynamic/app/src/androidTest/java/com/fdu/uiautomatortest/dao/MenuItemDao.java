package com.fdu.uiautomatortest.dao;

import com.fdu.uiautomatortest.model.MenuItem;
import com.fdu.uiautomatortest.utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MenuItemDao {
    /**
     * get SubMenuItems from SubMenu id
     * @param subId
     * @return
     */
    public List<MenuItem> getSubItems(long subId){
        List<MenuItem> subItems = new ArrayList<>();
        try{
            Connection conn = DBUtil.getConnection();
            String sql = "select * from menu_item where sub_id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setLong(1, subId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                MenuItem subItem = new MenuItem();
                subItem.setId(resultSet.getLong("id"));
                subItem.setWidgetType(resultSet.getString("widget_type"));
                subItem.setEvent(resultSet.getString("event"));
                subItem.setItemId(resultSet.getInt("item_id"));
                subItem.setText(resultSet.getString("text"));
                subItems.add(subItem);
            }
            DBUtil.closeResultset(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(conn);
        }catch(Exception e){
            e.printStackTrace();
        }
        return subItems;
    }

    /**
     * get MenuItems from Menu id(exclude SubMenuItems)
     * @param menuId
     * @return
     */
    public List<MenuItem> getItems(long menuId){
        List<MenuItem> items = new ArrayList<>();
        try{
            Connection conn = DBUtil.getConnection();
            String sql = "select * from menu_item where menu_id = ? and sub_id = -1";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setLong(1,menuId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                MenuItem item = new MenuItem();
                item.setId(resultSet.getLong("id"));
                item.setWidgetType(resultSet.getString("widget_type"));
                item.setEvent(resultSet.getString("event"));
                item.setItemId(resultSet.getInt("item_id"));
                item.setText(resultSet.getString("text"));
                items.add(item);
            }
            DBUtil.closeResultset(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(conn);
        }catch(Exception e){
            e.printStackTrace();
        }
        return items;
    }
}
