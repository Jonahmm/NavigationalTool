package uk.ac.bris.cs.spe.navigationaltool;

import android.content.Context;
import android.util.ArrayMap;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import uk.ac.bris.cs.spe.navigationaltool.graph.ChildLocation;
import uk.ac.bris.cs.spe.navigationaltool.graph.Graph;
import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;
import uk.ac.bris.cs.spe.navigationaltool.graph.User;
import uk.ac.bris.cs.spe.navigationaltool.navigator.Navigator;

import static java.util.Objects.requireNonNull;

/**
 * Holds the {@link Graph} and more information about the building
 */
public class Building {

    private Graph graph;
    private Navigator navigator;
    private String name;
    private Context context;
    private Map<String, String> floorNames = new ArrayMap<>();
    private ArrayList<Location> principals = new ArrayList<>();
    private String defaultFloor;

    public Building(String fileName, Navigator nav, Context c) throws IOException {
        name = requireNonNull(fileName);
        navigator = requireNonNull(nav);
        context = requireNonNull(c);
        //buildGraph();
        build(fileName);
    }

    /**
     * Uses the {@code .building} file to generate the graph, populating floor names, locations
     * (per floor), paths (per floor), and inter-floor paths in order.
     * @param masterFileName The building filename, not including the {@code .building} file
     *                       extension
     * @throws IOException if loading of any file fails
     */
    private void build(String masterFileName) throws IOException {
        graph = new Graph();

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                context.getAssets().open(masterFileName+".building")
        ));

        //gets the directory the file is in (if it's in a subfolder of assets - to support testing)
        String directory = masterFileName.contains("/") ? masterFileName.substring(0, masterFileName.lastIndexOf('/')+1) : "";

        String ln;
        //Get building name
        do {
            ln = reader.readLine();
        } while (ln.startsWith("#"));

        name = ln;
        boolean gotDefault = false;

        //Get floors
        while ((ln = reader.readLine()) != null) if (!ln.startsWith("#")) {
            String[] fields = ln.split(",");
            if (fields[0].startsWith("*")) {
                if (gotDefault) throw new IllegalArgumentException("Bad file format. " +
                        "Building should have only one default floor");
                else {
                    fields[0] = fields[0].substring(1);
                    defaultFloor = fields[0];
                    gotDefault = true;
                }
            }
            floorNames.put(fields[0], fields[1]);
        }
        if (!gotDefault) throw new IllegalArgumentException("Bad file format. " +
                "Building should have a default floor.");

        //Load all floors
        for (String f : floorNames.keySet()) loadFloor(directory + f);

        //Finally load cross-floor paths
        reader = new BufferedReader(new InputStreamReader(
                context.getAssets().open(masterFileName+".links")
        ));
        while ((ln = reader.readLine()) != null) {
            if(!ln.startsWith("#")) {
                String[] fields = ln.split(",");
                Log.d("adding link: ", fields[0] + "<->" + fields[1]);
                graph.addPath(
                        new Path(graph.getLocationById(Integer.parseInt(fields[0])),
                                graph.getLocationById(Integer.parseInt(fields[1])),
                                Arrays.stream(fields[2].split(" ")).map(this::getUserFromString).
                                        collect(Collectors.toList())));
            }
        }
    }

    /**
     * Loads all the locations and paths for a floor, using the {@code .locations} and
     * {@code .paths} files respectively. Once a location has been added, any others with the same
     * code will be added as {@link ChildLocation Child Locations}.
     * @param f The code of the floor to populate
     * @throws IOException If loading of either file mentioned above fails
     */
    private void loadFloor(String f) throws IOException {
        ////////////////////
        // Load Locations //
        ////////////////////
        BufferedReader buffer = new BufferedReader(new InputStreamReader(
                context.getAssets().open(f+".locations")));

        String ln;

        while ((ln = buffer.readLine()) != null) {
            if(!ln.startsWith("#")) { //Support comments
                String[] fields = ln.split(",");
                //Constructor format id,x,y,floor,code,name
                //File format ID,Code…
                Log.d("Adding ", ln);
                Location l = principals.stream().filter(s->s.getCode().equals(fields[1])).findFirst().orElse(null);
                if (l == null) {
                    l = new Location(Integer.parseInt(fields[0]), (fields.length > 4 ? Integer.parseInt(fields[4]) : 0),
                            (fields.length > 5 ? Integer.parseInt(fields[5]) : 0), fields[3], fields[1], fields[2]);
                    principals.add(l);
                }
                else l = new ChildLocation(Integer.parseInt(fields[0]), Integer.parseInt(fields[4]),
                        Integer.parseInt(fields[5]), fields[3], l);
                graph.addLocation(l);
            }
        }


        ////////////////
        // Load Paths //
        ////////////////
        buffer = new BufferedReader( new InputStreamReader(
                context.getAssets().open(f+".paths")));
        while ((ln = buffer.readLine()) != null) {
            if(!ln.startsWith("#")) {
                String[] fields = ln.split(",");
                Log.d("adding path: ", fields[0] + "<->" + fields[1]);
                graph.addPath(
                        new Path(graph.getLocationById(Integer.parseInt(fields[0])),
                                graph.getLocationById(Integer.parseInt(fields[1])),
                                Arrays.stream(fields[2].split(" ")).map(this::getUserFromString).
                                        collect(Collectors.toList())));
            }
        }
    }

    Map<String, String> getFloorMap() {
        return floorNames;
    }

    //This should probably go somewhere more sensible
    private User getUserFromString(String u) {
        switch (u) {
            case "DISABLED_STUDENT": return User.DISABLED_STUDENT;
            case "STAFF": return User.STAFF;
            case "DISABLED_STAFF": return User.DISABLED_STAFF;
            default: return User.STUDENT;
        }
    }

    void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    public Graph getGraph() {
        return graph;
    }

    Navigator getNavigator() {
        return navigator;
    }

    public String getName() {
        return name;
    }

    String getDefaultFloor() {
        return  defaultFloor;
    }

    ArrayList<Location> getPrincipalLocations() {
        return principals;
    }
}
