package com.fdu.uiautomatortest.dao;


import com.fdu.uiautomatortest.model.TransitionEdge;
import com.fdu.uiautomatortest.model.TransitionGraph;
import com.fdu.uiautomatortest.model.Widget;
import com.fdu.uiautomatortest.model.WindowNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransitionGraphDao {
    private WindowNodeDao nodeDao = new WindowNodeDao();
    private EdgeDao edgeDao = new EdgeDao();

    public TransitionGraph getTransitionGraph(String label){
        TransitionGraph g = new TransitionGraph();
        List<WindowNode> nodes = nodeDao.getNodes(label);
        List<TransitionEdge> edges = edgeDao.getEdges(label);
        for(WindowNode node:nodes){
            Set<TransitionEdge> outEdges = findOutEdges(node,edges);
            node.setOutEdges(outEdges);
        }
        g.setEdges(edges);
        g.setNodes(nodes);
        return g;
    }

    private Set<TransitionEdge> findOutEdges(WindowNode n, List<TransitionEdge> edges){
        Set<TransitionEdge> outEdges = new HashSet<>();
        long n_id = n.getId();
        for(TransitionEdge edge:edges){
            WindowNode source = edge.getSource();
            if(n_id == source.getId()){
                outEdges.add(edge);
            }
        }
        return outEdges;
    }

    public List<Map<String,String>> findTransitionResults(TransitionGraph g,String curACT, String type,String pack){
        List<Map<String,String>> results = new ArrayList<>();
        WindowNode tNode = null;
        for(WindowNode n:g.getNodes()){
            if(n.getName().contains(pack) && n.getName().contains(curACT) && n.getType().endsWith(type)){
                tNode = n;
                break;
            }
        }
        if(tNode != null){
            List<Widget> widgets = new ArrayList<>();
            if(!widgets.isEmpty()){
                for(Widget w:widgets){
                    Map<String, String> result = new HashMap<>();
                    String event = w.getEvent();
                    String widgetId = w.getWidgetId();
                    String widgetType = w.getWidgetType();
                    result.put("event",event);
                    result.put("widgetId", widgetId);
                    result.put("widgetType", widgetType);
                    results.add(result);
                }
            }
        }
        return results;
    }

    public WindowNode getNodeByCurWindow(TransitionGraph g,String curACT, String type,String pack){
        if(type.equals("ACT")){
            for(WindowNode node:g.getNodes()){
                if(node.getName().contains(pack) && node.getName().contains(curACT) && node.getType().equals(type)){
                    return node;
                }
            }
        }
        if(type.equals("MENU")){
            for(WindowNode node:g.getNodes()){
                if(node.getName().contains(pack) && node.getName().contains(curACT) && node.getType().equals("ACT")){
                    if(1 == node.getHasOptionsMenu()){
                        return node.getOptionsMenuNode();
                    }
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        TransitionGraphDao graphDao = new TransitionGraphDao();
        TransitionGraph g = graphDao.getTransitionGraph("meiting");
        for(WindowNode n:g.getNodes()){
            if(1 == n.getHasOptionsMenu()){
                n.printOptMenuWidgets();
            }
            List<Widget> widgets = n.getWidgets();
            for(Widget w:widgets){
                for(Widget depw:w.getdWidgets()){
                    System.out.println("dep widget type: "+depw.getWidgetType()+"\t"+"dep widget resource_id: "+w.getWidgetId());
                }
            }
        }
    }
}
