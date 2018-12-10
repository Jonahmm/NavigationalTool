package uk.ac.bris.cs.spe.navigationaltool.navigator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.bris.cs.spe.navigationaltool.graph.Graph;
import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;
import uk.ac.bris.cs.spe.navigationaltool.graph.User;

public class DijkstraNavigator implements Navigator{

    // WARNING: this relies on the fact that the images & locations for floors line up, otherwise this will have to be altered.

    public List<Path> navigate(Location start, Location end, Graph graph, User user){
        // Standard dijkstra, should be more accurate than breadth first search as it takes distance into account. Is also way more memory efficient bc no trees \(°ㅂ°)/

        HashMap<Location, Integer> shortestDistTo = new HashMap<>();
        HashMap<Location, ArrayList<Path>> shortestPathTo = new HashMap<>();
        ArrayList<Location> exploredLocations = new ArrayList<>();

        shortestDistTo.put(start, 0);

        while(!shortestPathTo.containsKey(end)){
            if(!exploredLocations.containsAll(shortestDistTo.keySet())){
          //      throw new RuntimeException("Can't find route, graph must be poorly constructed.");
            }

            // TODO: check that this is the best way of finding the shortest one.
            Location closest = shortestDistTo.entrySet().stream()
                                                                .filter(e -> !exploredLocations.contains(e))
                                                                .min((e, e2) -> e.getValue() > e2.getValue() ? 1 : e.getValue() == e2.getValue() ? 0 : -1)
                                                                .get().getKey(); // If the get fails, it's all rip, no point in an orElse()

            for(Path p : (Path[]) graph.getPathsFromLocation(closest).stream().filter(e -> (!e.allowsUser(user))).toArray()){
                Location other = p.getOtherLocation(closest);
                int distance = (int) Math.sqrt(Math.pow(other.x - closest.x, 2) + Math.pow(other.y - closest.y, 2)) + shortestDistTo.get(closest);

                if(shortestDistTo.containsKey(other)){
                    if(shortestDistTo.get(other) > distance){
                        shortestDistTo.put(other, distance);

                        ArrayList<Path> newPath = new ArrayList<>(shortestPathTo.get(closest));
                        newPath.add(p);
                        shortestPathTo.put(other, newPath);
                    }
                }
                else{
                    shortestDistTo.put(other, distance);

                    ArrayList<Path> newPath = new ArrayList<>(shortestPathTo.get(closest));
                    newPath.add(p);
                    shortestPathTo.put(other, newPath);
                }
            }

            exploredLocations.add(closest);

        }

        return shortestPathTo.get(end);
    }

}
