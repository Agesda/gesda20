package com.fdu.se.sootanalyze.dao;

import com.fdu.se.sootanalyze.model.TransitionEdge;
import com.fdu.se.sootanalyze.utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class EdgeDao {
    public void insertEdge(TransitionEdge edge){
        try{
            Connection conn = DBUtil.getConnection();
            String sql = "insert into trans_edge(id,edge_label,widget_id,swindow_id,twindow_id) " +
                    "values (?,?,?,?,?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setLong(1,edge.getId());
            preparedStatement.setString(2,edge.getLabel());
            preparedStatement.setLong(3,edge.getWidget().getId());
            preparedStatement.setLong(4,edge.getSource().getId());
            preparedStatement.setLong(5, edge.getTarget().getId());
            int changeRows = preparedStatement.executeUpdate();
            if(changeRows > 0){
                System.out.println("insert TransitionEdge " + edge.getId() + " successfully");
            }
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(conn);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
