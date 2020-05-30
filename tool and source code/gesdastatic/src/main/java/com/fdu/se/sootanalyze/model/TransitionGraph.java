package com.fdu.se.sootanalyze.model;

import java.util.ArrayList;
import java.util.List;

public class TransitionGraph {
    private List<WindowNode> nodes = new ArrayList<>();
    private List<TransitionEdge> edges = new ArrayList<>();

    public TransitionGraph() {
    }

    public List<WindowNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<WindowNode> nodes) {
        this.nodes = nodes;
    }

    public List<TransitionEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<TransitionEdge> edges) {
        this.edges = edges;
    }
}
