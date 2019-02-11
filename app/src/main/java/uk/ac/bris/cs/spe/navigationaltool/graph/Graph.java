package uk.ac.bris.cs.spe.navigationaltool.graph;

import android.util.ArraySet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class Graph {

    private HashMap<Location, ArrayList<Path>> graph = new HashMap<Location, ArrayList<Path>>();

    public void addLocation(Location n){
        requireNonNull(n);
        if(graph.containsKey(n)) throw new IllegalArgumentException("Cannot add node that is already in the graph");

        if(graph.keySet().stream().anyMatch(n2 -> n2.getId() == n.getId()))
            throw new IllegalArgumentException("Another node with the same ID already exists in the graph");

        //Commented for testing before we have x, y recorded
        if(graph.keySet().stream().anyMatch(n2 -> (n2.x == n.x) && (n2.y == n.y) && (n2.getFloor().equals(n.getFloor()))))
            throw new IllegalArgumentException("Another node at the same point already exists in the graph");

        graph.put(n, new ArrayList<Path>());
    }

    public void addLocation(Location n, ArrayList<Path> paths){
        requireNonNull(n);
        if(graph.containsKey(n)) throw new IllegalArgumentException("Cannot add node that is already in the graph");

        if(graph.keySet().stream().anyMatch(n2 -> n2.getId() == n.getId()))
            throw new IllegalArgumentException("Another node with the same ID already exists in the graph");

        if(graph.keySet().stream().anyMatch(n2 -> n2.x == n.x && n2.y == n.y))
            throw new IllegalArgumentException("Another node at the same point already exists in the graph");

        graph.put(n, paths);
    }

    public void addPath(Path e){
        if(!graph.containsKey(e.locA) || !graph.containsKey(e.locB)) throw new IllegalArgumentException(
                "Cannot add an edge that connects to nodes not in the graph: occurred when adding "
                        + e.locA.getCode() + "," + e.locB.getCode());
        if(e.locA.equals(e.locB)) throw new IllegalArgumentException(
                "Cannot add an edge that connects the same node to itself");
        graph.get(e.locA).add(e);
        graph.get(e.locB).add(e);
    }

    public Location getLocationByCode(String s){
        Location n = graph.keySet().stream().filter(n2 -> n2.code.equals(s) /* Causes error and not required yet || n2.name.equals(s)*/).findFirst().orElse(null);
        if(n == null) throw new IllegalArgumentException("Location does not exist.");
        return n;
    }

    public Set<Location> getLocationsByCode(String s) {
        return graph.keySet().stream().filter(l -> l.code.equals(s)).collect(Collectors.toSet());
    }

    public Location getLocationById(int id) {
        return graph.keySet().stream().filter(l -> l.getId() == id).findFirst().orElse(null);
    }

    public Location getBestMatchLocation(String s) {
        return s.equals("") ? null : getAllLocations().stream().filter(
                l -> l.getCode().equalsIgnoreCase(s) || (l.hasName() && l.getName().equalsIgnoreCase(s))
        ).findFirst().orElseGet(() -> getAllLocations().stream().filter(
                l -> l.getCode().toUpperCase().startsWith(s.toUpperCase()) || (l.hasName() && l.getName().toUpperCase().startsWith(s.toUpperCase()))
        ).findFirst().orElse(null));
    }

    public ArrayList<Path> getPathsFromLocation(Location n){
        if(graph.containsKey(n)) return graph.get(n);
        throw new RuntimeException("Tried to get edges from node that does not exist on the graph.");
    }

    public ArrayList<Path> getPathsFromLocationCode(String s){
        return getPathsFromLocation(getLocationByCode(s));
    }

    public Set<Location> getAllLocations() {
        return graph.keySet();
    }

    public Set<Path> getAllPaths() {
        Set<Path> paths = new ArraySet<>();
        graph.values().forEach(paths::addAll);
        return paths.stream().distinct().collect(Collectors.toSet());
    }
}
