package spe.net.navigationaltool.graph;

import java.util.HashMap;
import java.util.ArrayList;

public class Graph {

    private HashMap<Location, ArrayList<Path>> graph = new HashMap<Location, ArrayList<Path>>();

    public void addNode(Location n){
        if(graph.containsKey(n)) throw new IllegalArgumentException("Cannot add node that is already in the graph");
        graph.put(n, new ArrayList<Path>());
    }

    public void addNode(Location n, ArrayList<Path> paths){
        if(graph.containsKey(n)) throw new IllegalArgumentException("Cannot add node that is already in the graph");
        graph.put(n, paths);
    }

    public void addEdge(Path e){
        if(!graph.containsKey(e.locationB) || !graph.containsKey(e.locationB)) throw new IllegalArgumentException(
                "Cannot add an edge that connects to nodes not in the graph");
        graph.get(e.locationA).add(e);
        graph.get(e.locationB).add(e);
    }

    public Location getNodeFromName(String s){
        Location n = graph.keySet().stream().filter(n2 -> n2.roomCode.equals(s) || n2.additionalName.equals(s)).findFirst().orElse(null);
        return n;
    }

    public ArrayList<Path> getEdgesFromNode(Location n){
        if(graph.containsKey(n)) return graph.get(n);
        throw new RuntimeException("Tried to get edges from node that does not exist on the graph.");
    }

    public ArrayList<Path> getEdgesFromNodeName(String s){
        return getEdgesFromNode(getNodeFromName(s));
    }

}
