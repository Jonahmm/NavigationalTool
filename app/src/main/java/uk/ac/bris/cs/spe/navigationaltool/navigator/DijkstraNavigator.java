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

    // WARNING: this relies on the fact that the images & locations for floors line up

    public List<Path> navigate(Location start, Location end, Graph graph, User user){
        // Standard dijkstra, should be more accurate than breadth first search as it takes distance into account. Is also way more memory efficient bc no trees \(°ㅂ°)/

        HashMap<Location, Double> shortestDistTo = new HashMap<>();
        HashMap<Location, ArrayList<Path>> shortestPathTo = new HashMap<>();
        ArrayList<Location> exploredLocations = new ArrayList<>();

        shortestDistTo.put(start, 0D);

        while(!shortestPathTo.containsKey(end)){
            Location closest = shortestDistTo.entrySet().stream()
                    .filter(e -> !exploredLocations.contains(e.getKey()))
                    .min((e, e2) -> e.getValue() > e2.getValue() ? 1 : e.getValue() == e2.getValue() ? 0 : -1)
                    .get().getKey(); // If the get fails, it's all rip, no point in an orElse()

            for(Path p : graph.getPathsFromLocation(closest)){
                if(p.allowsUser(user)) {
                    Location other = p.getOtherLocation(closest);
                    double distance = Math.sqrt(Math.pow(other.x - closest.x, 2) + Math.pow(other.y - closest.y, 2)) + shortestDistTo.get(closest);

                    if (shortestDistTo.containsKey(other)) {
                        if (shortestDistTo.get(other) > distance) {
                            shortestDistTo.put(other, distance);

                            ArrayList<Path> newPath = new ArrayList<>(shortestPathTo.get(closest));
                            newPath.add(p);
                            shortestPathTo.put(other, newPath);
                        }
                    } else {
                        shortestDistTo.put(other, distance);

                        ArrayList<Path> newPath;
                        if(shortestPathTo.containsKey(closest))
                            newPath = new ArrayList<>(shortestPathTo.get(closest));
                        else
                            newPath = new ArrayList<>();

                        newPath.add(p);
                        shortestPathTo.put(other, newPath);
                    }
                }
            }

            exploredLocations.add(closest);

            if(exploredLocations.containsAll(shortestDistTo.keySet())){
                throw new IllegalArgumentException("Can't find route, graph must be poorly constructed.");
            }

        }

        return shortestPathTo.get(end);
    }

}
