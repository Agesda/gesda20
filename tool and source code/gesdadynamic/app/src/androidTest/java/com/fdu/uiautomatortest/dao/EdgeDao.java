package com.fdu.uiautomatortest.dao;

import com.fdu.uiautomatortest.model.TransitionEdge;
import com.fdu.uiautomatortest.model.Widget;
import com.fdu.uiautomatortest.model.WindowNode;
import com.fdu.uiautomatortest.utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class EdgeDao {
    private WindowNodeDao nodeDao = new WindowNodeDao();
    public List<TransitionEdge> getEdges(String label){
        List<TransitionEdge> edges = new ArrayList<>();
        List<WindowNode> nodes = nodeDao.getNodes(label);
        try{
            Connection conn = DBUtil.getConnection();
            String sql = "select * from trans_edge where edge_label = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,label);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                TransitionEdge edge = new TransitionEdge();
                long edge_id = resultSet.getLong("id");
                String edge_label = resultSet.getString("edge_label");
                long widget_id = resultSet.getLong("widget_id");
                long swindow_id = resultSet.getLong("swindow_id");
                long twindow_id = resultSet.getLong("twindow_id");
                edge.setId(edge_id);
                edge.setLabel(edge_label);
                Widget widget = findWidgetById(nodes,widget_id);
                edge.setWidget(widget);
                WindowNode sWindowNode = findNodeById(nodes, swindow_id);
                WindowNode tWindowNode = findNodeById(nodes, twindow_id);
                edge.setSource(sWindowNode);
                edge.setTarget(tWindowNode);
                edges.add(edge);
            }
            DBUtil.closeResultset(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(conn);
        }catch(Exception e){
            e.printStackTrace();
        }
        return edges;
    }

    private WindowNode findNodeById(List<WindowNode> nodes,long node_id){
        for(WindowNode n:nodes){
            long id = n.getId();
            if(id == node_id){
                return n;
            }
        }
        return null;
    }

    private Widget findWidgetById(List<WindowNode> nodes,long widget_id){
        for(WindowNode n:nodes){
            List<Widget> widgets = n.getWidgets();
            if(!widgets.isEmpty()){
                for(Widget w:widgets){
                    long w_id = w.getId();
                    if(w_id == widget_id){
                        return w;
                    }
                }
            }
        }
        return null;
    }
}
