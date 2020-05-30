package com.fdu.se.sootanalyze.dao;

import com.fdu.se.sootanalyze.model.SubMenu;
import com.fdu.se.sootanalyze.utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SubMenuDao {
    public void insertSubMenu(SubMenu subMenu,long menuId){
        try{
            Connection conn = DBUtil.getConnection();
            String sql = "insert into sub_menu(id,widget_type,sub_menu_id,text,event,menu_id) " +
                    "values (?,?,?,?,?,?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setLong(1,subMenu.getId());
            preparedStatement.setString(2,subMenu.getWidgetType());
            preparedStatement.setInt(3,subMenu.getSubMenuId());
            preparedStatement.setString(4,subMenu.getText());
            preparedStatement.setString(5,subMenu.getEvent());
            preparedStatement.setLong(6, menuId);
            int changeRows = preparedStatement.executeUpdate();
            if(changeRows > 0){
                System.out.println("insert sub_menu " + subMenu.getId() + " successfully");
            }
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(conn);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
