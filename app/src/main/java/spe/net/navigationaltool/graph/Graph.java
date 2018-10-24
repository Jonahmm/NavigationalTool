package spe.net.navigationaltool.graph;

import java.util.HashMap;
import java.util.ArrayList;

public class Graph {

    private HashMap<Node, ArrayList<Edge>> graph = new HashMap<Node, ArrayList<Edge>>();

    public void addNode(Node n){
        if(graph.containsKey(n)) throw new IllegalArgumentException("Cannot add node that is already in the graph");
        graph.put(n, new ArrayList<Edge>());
    }

    public void addNode(Node n, ArrayList<Edge> edges){
        if(graph.containsKey(n)) throw new IllegalArgumentException("Cannot add node that is already in the graph");
        graph.put(n, edges);
    }

    public void addEdge(Edge e){
        if(!graph.containsKey(e.nodeB) || !graph.containsKey(e.nodeB)) throw new IllegalArgumentException(
                "Cannot add an edge that connects to nodes not in the graph");
        graph.get(e.nodeA).add(e);
        graph.get(e.nodeB).add(e);
    }

    public Node getNodeFromName(String s){
        Node n = graph.keySet().stream().filter(n2 -> n2.roomCode.equals(s) || n2.additionalName.equals(s)).findFirst().orElse(null);
        return n;
    }

    public ArrayList<Edge> getEdgesFromNode(Node n){
        if(graph.containsKey(n)) return graph.get(n);
        throw new RuntimeException("Tried to get edges from node that does not exist on the graph.");
    }

    public ArrayList<Edge> getEdgesFromNodeName(String s){
        return getEdgesFromNode(getNodeFromName(s));
    }

}
