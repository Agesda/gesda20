package com.fdu.uiautomatortest.dao;

import com.fdu.uiautomatortest.model.MenuItem;
import com.fdu.uiautomatortest.model.SubMenu;
import com.fdu.uiautomatortest.model.Widget;
import com.fdu.uiautomatortest.model.WindowNode;
import com.fdu.uiautomatortest.model.WindowType;
import com.fdu.uiautomatortest.utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class WindowNodeDao {
    private WidgetDao widgetDao = new WidgetDao();
    private MenuItemDao menuItemDao = new MenuItemDao();
    private SubMenuDao subMenuDao = new SubMenuDao();

    public List<WindowNode> getNodes(String label){
        List<WindowNode> nodes = new ArrayList<>();
        try{
            Connection conn = DBUtil.getConnection();
            String sql = "select * from window_node where window_label = ? and window_type = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,label);
            preparedStatement.setString(2, WindowType.ACT);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                WindowNode node = new WindowNode();
                long node_id = resultSet.getLong("id");
                node.setId(node_id);
                node.setName(resultSet.getString("window_name"));
                node.setLabel(resultSet.getString("window_label"));
                node.setType(resultSet.getString("window_type"));
                int hasOptMenu = resultSet.getInt("has_opt_menu");
                node.setHasOptionsMenu(hasOptMenu);
                if(1 == hasOptMenu){
                    WindowNode menuNode = getMenuNode(resultSet.getLong("opt_menu_id"));
                    node.setOptionsMenuNode(menuNode);
                }
                List<Widget> widges = widgetDao.getWidgetsByActId(node_id);
                node.setWidgets(widges);
                nodes.add(node);
            }
            DBUtil.closeResultset(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(conn);
        }catch(Exception e){
            e.printStackTrace();
        }
        return nodes;
    }

    private WindowNode getMenuNode(long id){
        try{
            WindowNode node = new WindowNode();
            Connection conn = DBUtil.getConnection();
            String sql = "select * from window_node where id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setLong(1,id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                //long node_id = resultSet.getLong("id");
                node.setId(id);
                node.setName(resultSet.getString("window_name"));
                node.setLabel(resultSet.getString("window_label"));
                node.setType(resultSet.getString("window_type"));
                node.setHasOptionsMenu(resultSet.getInt("has_opt_menu"));
                List<Widget> menuWidgets = new ArrayList<>();
                List<SubMenu> subMenus = subMenuDao.getSubMenus(id);
                menuWidgets.addAll(subMenus);
                List<MenuItem> menuItems = menuItemDao.getItems(id);
                menuWidgets.addAll(menuItems);
                node.setWidgets(menuWidgets);
                return node;
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
