package uk.ac.bris.cs.spe.navigationaltool.navigator;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.ac.bris.cs.spe.navigationaltool.graph.Graph;
import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;
import uk.ac.bris.cs.spe.navigationaltool.graph.User;

/*
 * Bidirectional Dijkstra search
 */
public class OptimisedNavigator implements Navigator {

    @Override
    public List<Path> navigate(Location start, Location end, Graph graph, User user) {
        HashMap<Location, Double> shortestDistToStart = new HashMap<>();
        HashMap<Location, ArrayList<Path>> shortestPathToStart = new HashMap<>();

        HashMap<Location, Double> shortestDistToEnd = new HashMap<>();
        HashMap<Location, ArrayList<Path>> shortestPathToEnd = new HashMap<>();

        ArrayList<Location> exploredLocationsStart = new ArrayList<>();
        ArrayList<Location> exploredLocationsEnd = new ArrayList<>();

        shortestDistToStart.put(start, 0D);
        shortestDistToEnd.put(end, 0D);

        boolean found = false;
        while(!found){
            // From start
            Location closest = shortestDistToStart.entrySet().stream()
                    .filter(e -> !exploredLocationsStart.contains(e.getKey()))
                    .min((e, e2) -> e.getValue() > e2.getValue() ? 1 : e.getValue() == e2.getValue() ? 0 : -1)
                    .get().getKey(); // If the get fails, it's all rip, no point in an orElse()

            for(Path p : graph.getPathsFromLocation(closest)){
                if(p.allowsUser(user)) {
                    Location other = p.getOtherLocation(closest);
                    double distance = Math.sqrt(Math.pow(other.x - closest.x, 2) + Math.pow(other.y - closest.y, 2)) + shortestDistToStart.get(closest);

                    if (shortestDistToStart.containsKey(other)) {
                        if (shortestDistToStart.get(other) > distance) {
                            shortestDistToStart.put(other, distance);

                            ArrayList<Path> newPath = new ArrayList<>(shortestPathToStart.get(closest));
                            newPath.add(p);
                            shortestPathToStart.put(other, newPath);
                        }
                    } else {
                        shortestDistToStart.put(other, distance);

                        ArrayList<Path> newPath;
                        if(shortestPathToStart.containsKey(closest))
                            newPath = new ArrayList<>(shortestPathToStart.get(closest));
                        else
                            newPath = new ArrayList<>();

                        newPath.add(p);
                        shortestPathToStart.put(other, newPath);
                    }
                }
            }

            if(exploredLocationsEnd.contains(closest)){
                ArrayList<Path> route = shortestPathToStart.get(closest);

                // Add the end of the route backwards
                ArrayList<Path> routeEnd = shortestPathToEnd.get(closest);
                if(routeEnd != null) for(int i = routeEnd.size()-1; i >= 0; i--) route.add(routeEnd.get(i));

                shortestPathToStart.put(end, route);

                found = true; // ends up not being used, but kinda acts as a failsafe ig?
                break;
            }

            // Essentially a failsafe, should _never_ trigger if all is working
            if(shortestPathToStart.containsKey(end)){
                found = true;
                break;
            }

            exploredLocationsStart.add(closest);

            // From end
            closest = shortestDistToEnd.entrySet().stream()
                    .filter(e -> !exploredLocationsEnd.contains(e.getKey()))
                    .min((e, e2) -> e.getValue() > e2.getValue() ? 1 : e.getValue() == e2.getValue() ? 0 : -1)
                    .get().getKey(); // If the get fails, it's all rip, no point in an orElse()

            for(Path p : graph.getPathsFromLocation(closest)){
                if(p.allowsUser(user)) {
                    Location other = p.getOtherLocation(closest);
                    double distance = Math.sqrt(Math.pow(other.x - closest.x, 2) + Math.pow(other.y - closest.y, 2)) + shortestDistToEnd.get(closest);

                    if (shortestDistToEnd.containsKey(other)) {
                        if (shortestDistToEnd.get(other) > distance) {
                            shortestDistToEnd.put(other, distance);

                            ArrayList<Path> newPath = new ArrayList<>(shortestPathToEnd.get(closest));
                            newPath.add(p);
                            shortestPathToEnd.put(other, newPath);
                        }
                    } else {
                        shortestDistToEnd.put(other, distance);

                        ArrayList<Path> newPath;
                        if(shortestPathToEnd.containsKey(closest))
                            newPath = new ArrayList<>(shortestPathToEnd.get(closest));
                        else
                            newPath = new ArrayList<>();

                        newPath.add(p);
                        shortestPathToEnd.put(other, newPath);
                    }
                }
            }

            if(exploredLocationsStart.contains(closest)){
                ArrayList<Path> route = shortestPathToStart.get(closest);
                if(route == null) route = new ArrayList<>();

                // Add the end of the route backwards
                ArrayList<Path> routeEnd = shortestPathToEnd.get(closest);
                if(routeEnd != null)
                    for(int i = routeEnd.size()-1; i >= 0; i--)
                        route.add(routeEnd.get(i));

                shortestPathToStart.put(end, route);

                found = true;
                break;
            }

            // Essentially a failsafe, should _never_ trigger if all is working
            if(shortestPathToEnd.containsKey(end)){
                ArrayList<Path> route = new ArrayList<>();
                ArrayList<Path> routeEnd = shortestPathToEnd.get(closest);
                for(int i = routeEnd.size()-1; i >= 0; i--)
                    route.add(routeEnd.get(i));

                shortestPathToStart.put(end, route);

                found = true;
                break;
            }

            exploredLocationsEnd.add(closest);

            // Fail when we've explored everything - could be improved by looking through both explored locations, but this may up success computation time.
            if(exploredLocationsStart.containsAll(shortestDistToStart.keySet())){
                System.out.println("Navigator failed");
                throw new IllegalArgumentException("Can't find route, graph must be poorly constructed.");
            }
        }

        return shortestPathToStart.get(end);
    }

}
