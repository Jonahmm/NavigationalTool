package uk.ac.bris.cs.spe.navigationaltool;

import android.content.Context;
import android.util.ArrayMap;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import uk.ac.bris.cs.spe.navigationaltool.graph.Graph;
import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;
import uk.ac.bris.cs.spe.navigationaltool.graph.User;
import uk.ac.bris.cs.spe.navigationaltool.navigator.Navigator;

import static java.util.Objects.requireNonNull;

public class Building {

    private Graph graph;
    private Navigator navigator;
    private String name;
    private Context context;
    private Map<String, String> floorNames = new ArrayMap<>();
    private String defaultFloor;

    public Building(String fileName, Navigator nav, Context c) throws IOException {
        name = requireNonNull(fileName);
        navigator = requireNonNull(nav);
        context = requireNonNull(c);
        //buildGraph();
        build(fileName);
    }

    private void build(String masterFileName) throws IOException {
        graph = new Graph();

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                context.getAssets().open(masterFileName+".building")
        ));

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
        for (String f : floorNames.keySet()) loadFloor(f);

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
                //x,y,floor,code,name
                Log.d("Adding ", ln);
                graph.addLocation(new Location(Integer.parseInt(fields[0]), (fields.length > 4 ? Integer.parseInt(fields[4]) : 0),
                        (fields.length > 5 ? Integer.parseInt(fields[5]) : 0), fields[3], fields[1], fields[2]));
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

    public void buildGraph() throws IOException {
        ////////////////////
        // Load Locations //
        ////////////////////
        graph = new Graph();
        BufferedReader buffer = new BufferedReader(new InputStreamReader(
                context.getAssets().open(name+".locations")));

        String ln;

        while ((ln = buffer.readLine()) != null) {
            if(!ln.startsWith("#")) { //Support comments
                String[] fields = ln.split(",");
                //x,y,floor,code,name
                //Log.d("Adding ", ln);
                graph.addLocation(new Location(Integer.parseInt(fields[0]), (fields.length > 4 ? Integer.parseInt(fields[4]) : 0),
                        (fields.length > 5 ? Integer.parseInt(fields[5]) : 0), fields[3], fields[1], fields[2]));
            }
        }


        ////////////////
        // Load Paths //
        ////////////////
        buffer = new BufferedReader( new InputStreamReader(
                context.getAssets().open(name+".paths")));
        while ((ln = buffer.readLine()) != null) {
            if(!ln.startsWith("#")) {
                String[] fields = ln.split(",");
                //Log.d("adding path: ", fields[0] + "<->" + fields[1]);
                graph.addPath(
                        new Path(graph.getLocationById(Integer.parseInt(fields[0])),
                                 graph.getLocationById(Integer.parseInt(fields[1])),
                        Arrays.stream(fields[2].split(" ")).map(this::getUserFromString).
                                collect(Collectors.toList())));
            }
        }

    }

    public Map<String, String> getFloorMap() {
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

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    public Graph getGraph() {
        return graph;
    }

    public Navigator getNavigator() {
        return navigator;
    }

    public String getName() {
        return name;
    }

    public String getDefaultFloor() {
        return  defaultFloor;
    }
}
