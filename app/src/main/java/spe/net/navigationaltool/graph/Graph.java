package spe.net.navigationaltool.graph;

import java.util.HashMap;
import java.util.ArrayList;

public class Graph {

    private HashMap<Node, ArrayList<Edge>> graph = new HashMap<Node, ArrayList<Edge>>();

    public void addNode(Node n){
        if(graph.containsKey(n)) System.err.println("Warning: Overwriting node that already exists.");
        graph.put(n, new ArrayList<Edge>());
    }

    public void addNode(Node n, ArrayList<Edge> edges){
        if(graph.containsKey(n)) System.err.println("Warning: Overwriting node that already exists.");
        graph.put(n, edges);
    }

    public void addEdge(Edge e){
        graph.get(e.nodeA).add(e);
        graph.get(e.nodeB).add(e);
    }

    public Node getNodeFromName(String s){
        //graph.keySet().(n -> n.);
        return null;
    }

    public ArrayList<Edge> getEdgesFromNode(Node n){
        return null;
    }

}
