package com.fdu.se.sootanalyze;

import com.fdu.se.sootanalyze.dao.EdgeDao;
import com.fdu.se.sootanalyze.dao.TransitionGraphDao;
import com.fdu.se.sootanalyze.dao.WidgetDao;
import com.fdu.se.sootanalyze.dao.WindowNodeDao;
import com.fdu.se.sootanalyze.model.*;
import com.fdu.se.sootanalyze.utils.DBUtil;
import com.fdu.se.sootanalyze.utils.FileUtil;
import soot.SootClass;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        SootAnalyze sootAnalyze = new SootAnalyze();
//        List<WindowNode> nodes = sootAnalyze.analyse();
//        TransitionGraph g = sootAnalyze.generateTransitionGraph(nodes);
//        List<WindowNode> wnodes = g.getNodes();
//        if(!wnodes.isEmpty()){
//            for(WindowNode wnode:wnodes){
//                System.out.println("node id: " + wnode.getId());
//                System.out.println("node name: " + wnode.getName());
//                List<Widget> widgets = wnode.getWidgets();
//                if(!widgets.isEmpty()){
//                    for(Widget widget:widgets){
//                        System.out.println("widget id: " + widget.getId() + "\twidget idName: " + widget.getWidgetId());
//                    }
//                }
//            }
//        }
//        List<TransitionEdge> edges = g.getEdges();
//        if(!edges.isEmpty()){
//            for(TransitionEdge edge:edges){
//                System.out.println("edge id: " + edge.getId());
//                System.out.println("sourceWindow: " + edge.getSource().getName());
//                System.out.println("targetWindow: " + edge.getTarget().getName());
//                System.out.println("widget: " + edge.getWidget().getWidgetId());
//                System.out.println("event: " + edge.getWidget().getEvent());
//                System.out.println("yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
//            }
//        }
//        TransitionGraphDao graphDao = new TransitionGraphDao();
//        graphDao.insertGraph(g);
        List<String> apkPaths = FileUtil.getApkPaths("E:\\E backup\\AndroidTesting\\test app\\apps");
        for(String apkPath:apkPaths){
            long startTime = System.currentTimeMillis();
            sootAnalyze.init(apkPath);
            List<WindowNode> nodes = sootAnalyze.analyse();
            sootAnalyze.analyseDependencies(nodes);
            TransitionGraph g = sootAnalyze.generateTransitionGraph(nodes);
            TransitionGraphDao graphDao = new TransitionGraphDao();
            graphDao.insertGraph(g);
            long endTime = System.currentTimeMillis();
            long executeTime = endTime - startTime;
            FileUtil.filePrintln("time.txt",apkPath+"\t"+executeTime+"");
        }
//        sootAnalyze.init(sootAnalyze.getApkPath());
//        List<WindowNode> nodes = sootAnalyze.analyse();
//        sootAnalyze.analyseDependencies(nodes);
//        TransitionGraph g = sootAnalyze.generateTransitionGraph(nodes);
//        TransitionGraphDao graphDao = new TransitionGraphDao();
//        graphDao.insertGraph(g);
//        for(WindowNode n:nodes){
//            if(n.getHasOptionsMenu()==1){
//                n.printOptMenuWidgets();
//            }
//        }
//        for(WindowNode wn:nodes){
//            List<Widget> widgets = wn.getWidgets();
//            if(!widgets.isEmpty()){
//                for(Widget w:widgets){
//                    List<Widget> dWidgets = w.getdWidgets();
//                    if(!dWidgets.isEmpty()){
//                        for(Widget dw:dWidgets){
//                            System.out.println("dependency widget info: "+dw.getId() + "\t" + dw.getWidgetId() + "\t" + dw.getWidgetType());
//                        }
//                    }
//                }
//            }
//        }
    }
}
