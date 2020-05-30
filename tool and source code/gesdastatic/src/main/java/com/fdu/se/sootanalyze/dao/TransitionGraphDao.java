package com.fdu.se.sootanalyze.dao;

import com.fdu.se.sootanalyze.model.TransitionEdge;
import com.fdu.se.sootanalyze.model.TransitionGraph;
import com.fdu.se.sootanalyze.model.Widget;
import com.fdu.se.sootanalyze.model.WindowNode;

import java.util.List;

public class TransitionGraphDao {
    private WidgetDao widgetDao = new WidgetDao();
    private WindowNodeDao nodeDao = new WindowNodeDao();
    private EdgeDao edgeDao = new EdgeDao();

    public void insertGraph(TransitionGraph g){
        List<TransitionEdge> edges = g.getEdges();
        List<WindowNode> nodes = g.getNodes();
        for(TransitionEdge e:edges){
            edgeDao.insertEdge(e);
        }
        for(WindowNode n:nodes){
            nodeDao.insertWindowNode(n);
            long node_id = n.getId();
            List<Widget> widgets = n.getWidgets();
            if(!widgets.isEmpty()){
                for(Widget w:widgets){
                    widgetDao.insertWidget(w,node_id);
                }
            }
        }
    }
}
