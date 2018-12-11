package uk.ac.bris.cs.spe.navigationaltool;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import static java.util.Objects.requireNonNull;

import uk.ac.bris.cs.spe.navigationaltool.graph.Graph;
import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;
import uk.ac.bris.cs.spe.navigationaltool.graph.User;
import uk.ac.bris.cs.spe.navigationaltool.navigator.Navigator;

public class Building {

    private Graph graph;
    private Navigator navigator;
    private String name;
    private Context context;

    public Building(String fileName, Navigator nav, Context c) throws IOException {
        name = requireNonNull(fileName);
        navigator = requireNonNull(nav);
        context = requireNonNull(c);
        buildGraph();
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
                graph.addLocation(new Location(0, 0, fields[2], fields[0], fields[1]));
            }
        }

        //for (Location l : graph.getAllLocations()) Log.d( "Building: Added ", l.code);

        ////////////////
        // Load Paths //
        ////////////////
        buffer = new BufferedReader( new InputStreamReader(
                context.getAssets().open(name+".paths")));
        while ((ln = buffer.readLine()) != null) {
            if(!ln.startsWith("#")) {
                String[] fields = ln.split(",");
                Log.d("adding path: ", fields[0] + "<->" + fields[1]);
                graph.addPath(new Path(graph.getLocationByCode(fields[0]), graph.getLocationByCode(fields[1]),
                        Arrays.stream(fields[2].split(" ")).map(this::getUserFromString).
                                collect(Collectors.toList())));
            }
        }

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
}
