package spe.net.navigationaltool;

import spe.net.navigationaltool.graph.Graph;
import spe.net.navigationaltool.navigator.Navigator;

public class Building {

    private Graph graph;
    private  Navigator navigator;
    private String name;

    public Building(String fileName, Navigator nav){
        name = fileName;
        navigator = nav;

        rebuildGraph();
    }

    public void rebuildGraph(){

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
