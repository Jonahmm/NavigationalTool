package uk.ac.bris.cs.spe.navigationaltool;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import uk.ac.bris.cs.spe.navigationaltool.graph.Graph;
import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.navigator.Navigator;

public class Building {

    private Graph graph;
    private Navigator navigator;
    private String name;
    private Context context;

    public Building(String fileName, Navigator nav, Context c) throws IOException {
        name = fileName;
        navigator = nav;
        context = c;
        buildGraph();
    }

    public void buildGraph() throws IOException {
        graph = new Graph();
        BufferedReader buffer = new BufferedReader(new InputStreamReader(
                context.getAssets().open(name+".locations")));

        String ln;

        while((ln = buffer.readLine()) != null) {
            String[] fields = ln.split(",");
            //x,y,floor,code,name
            graph.addLocation(new Location(0,0,fields[2], fields[0], fields[1]));
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
