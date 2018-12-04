package uk.ac.bris.cs.spe.navigationaltool.graph;

import java.util.HashMap;
import java.util.ArrayList;
import static java.util.Objects.requireNonNull;

public class Graph {

    private HashMap<Location, ArrayList<Path>> graph = new HashMap<Location, ArrayList<Path>>();

    public void addLocation(Location n){
        requireNonNull(n);
        if(graph.containsKey(n)) throw new IllegalArgumentException("Cannot add node that is already in the graph");

        if(graph.keySet().stream().anyMatch(n2 -> n2.code.equals(n.code)))
            throw new IllegalArgumentException("Another node with the same code already exists in the graph");

        //if(graph.keySet().stream().anyMatch(n2 -> (n2.location.x == n.location.x) && (n2.location.y == n.location.y)))
         //   throw new IllegalArgumentException("Another node at the same point already exists in the graph");

        graph.put(n, new ArrayList<Path>());
    }

    public void addLocation(Location n, ArrayList<Path> paths){
        requireNonNull(n);
        if(graph.containsKey(n)) throw new IllegalArgumentException("Cannot add node that is already in the graph");

        if(graph.keySet().stream().anyMatch(n2 -> n2.code.equals(n.code)))
            throw new IllegalArgumentException("Another node with the same code already exists in the graph");

//        if(graph.keySet().stream().anyMatch(n2 -> n2.location.x == n.location.x && n2.location.y == n.location.y))
//            throw new IllegalArgumentException("Another node at the same point already exists in the graph");

        graph.put(n, paths);
    }

    public void addPath(Path e){
        if(!graph.containsKey(e.locA) || !graph.containsKey(e.locB)) throw new IllegalArgumentException(
                "Cannot add an edge that connects to nodes not in the graph");
        if(e.locA.equals(e.locB)) throw new IllegalArgumentException(
                "Cannot add an edge that connects the same node to itself");
        graph.get(e.locA).add(e);
        graph.get(e.locB).add(e);
    }

    public Location getLocationByCode(String s){
        Location n = graph.keySet().stream().filter(n2 -> n2.code.equals(s) || n2.name.equals(s)).findFirst().orElse(null);
        return n;
    }

    public ArrayList<Path> getPathsFromLocation(Location n){
        if(graph.containsKey(n)) return graph.get(n);
        throw new RuntimeException("Tried to get edges from node that does not exist on the graph.");
    }

    public ArrayList<Path> getPathsFromLocationCode(String s){
        return getPathsFromLocation(getLocationByCode(s));
    }

}
