package uk.ac.bris.cs.spe.navigationaltool.navigator;

import java.util.ArrayList;
import java.util.List;

import uk.ac.bris.cs.spe.navigationaltool.graph.Graph;
import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;
import uk.ac.bris.cs.spe.navigationaltool.graph.User;

public class BreadthFirstNavigator implements Navigator {

    public List<Path> navigate(Location start, Location end, Graph graph, User user){
        // Variables to stop infinite loop when the graph is poorly constructed.
        boolean newLocationsFound;
        ArrayList<Location> foundLocations = new ArrayList<Location>();

        // Breadth First Search Variables
        Tree<Location> routes = new Tree<Location>(start);
        ArrayList<Path> pathList = new ArrayList<Path>();

        // While no route has found the end
        while(!routes.contains(end)){
            newLocationsFound = false;

            // Go through each of the end nodes
            for(Node<Location> node : routes.getLeafs()) {

                // Add each allowed joined node on the end, forming a new 'route'
                for (Path p : graph.getPathsFromLocation(node.data)) {
                    if (p.allowsUser(user)) {
                        node.addChild(p.getOtherLocation(node.data));

                        if (!foundLocations.contains(p.getOtherLocation(node.data))) {
                            foundLocations.add(p.getOtherLocation(node.data));
                            newLocationsFound = true;
                        }
                    }
                }
            }

            if(!newLocationsFound){
                throw new IllegalArgumentException("Can't find route, check access levels or graph construction.");
            }
        }

        // Once 'end' is in the structure, go through all leafs (end must be a leaf) and find it.
        for(Node<Location> node : routes.getLeafs()){
            if(node.data.equals(end)){
                Node<Location> currentNode = node;

                // If you haven't yet reached the start location
                while(!currentNode.data.equals(start)){
                    Path p = null;

                    // Search all paths leading from the current Node, for one that goes to the node's parent's node.
                    for(Path possiblePath : graph.getPathsFromLocation(currentNode.data))
                        if(possiblePath.getOtherLocation(currentNode.data).equals(currentNode.parent.data))
                            p = possiblePath;

                    // Add this path to the pathList and set the parent node as the new current Node.
                    pathList.add(p);
                    currentNode = currentNode.parent;
                }

            }
        }

        ArrayList<Path> flippedPath = new ArrayList<Path>();
        for(int i = pathList.size()-1; i >= 0; i--) flippedPath.add(pathList.get(i));

        return flippedPath;
    }


    // Quick tree implementation
    private class Tree<T> {
        public Node<T> root;

        public Tree(T rootData) {
            root = new Node<T>(rootData);
        }

        public boolean contains(T object){
            return root.contains(object);
        }

        public ArrayList<Node<T>> getLeafs(){
            return root.getLeafs();
        }
    }

    public class Node<T> {
        public final T data;
        public Node<T> parent;
        public List<Node<T>> children;

        public Node(T t){
            data = t;
            children = new ArrayList<Node<T>>();
        }

        public void addChild(T t){
            Node<T> n = new Node<T>(t);
            children.add(n);
            n.parent = this;
        }

        public ArrayList<Node<T>> getLeafs(){
            ArrayList<Node<T>> leafs = new ArrayList<Node<T>>();

            if(children == null || children.isEmpty())
                leafs.add(this);
            else
                children.forEach(c -> leafs.addAll(c.getLeafs()));

            return leafs;
        }

        public boolean contains(T object){
            if(object.equals(data)) return true;

            boolean found = false;

            for(Node<T> c : children){
                if(c.contains(object)) found = true;
            }

            return found;
        }
    }

}
