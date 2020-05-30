package com.fdu.uiautomatortest.dao;


import com.fdu.uiautomatortest.model.MenuItem;
import com.fdu.uiautomatortest.model.SubMenu;
import com.fdu.uiautomatortest.utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SubMenuDao {
    private MenuItemDao menuItemDao = new MenuItemDao();

    public List<SubMenu> getSubMenus(long menuId){
        List<SubMenu> subMenus = new ArrayList<>();
        try{
            Connection conn = DBUtil.getConnection();
            String sql = "select * from sub_menu where menu_id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setLong(1,menuId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                SubMenu subMenu = new SubMenu();
                long subId = resultSet.getLong("id");
                subMenu.setId(subId);
                subMenu.setWidgetType(resultSet.getString("widget_type"));
                subMenu.setEvent(resultSet.getString("event"));
                subMenu.setSubMenuId(resultSet.getInt("sub_menu_id"));
                subMenu.setText(resultSet.getString("text"));
                List<MenuItem> subItems = menuItemDao.getSubItems(subId);
                subMenu.setItems(subItems);
                subMenus.add(subMenu);
            }
            DBUtil.closeResultset(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(conn);
        }catch(Exception e){
            e.printStackTrace();
        }
        return subMenus;
    }
}
