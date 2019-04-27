package uk.ac.bris.cs.spe.navigationaltool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;

/**
 * A class to generate directions and keep track of the user's progress along the route.
 */
class Route {
    // Raw input
    private final Location start;
    private final Location end;
    private final List<Path> allPaths;
    // Output in steps
    private List<List<Path>>  steps = new ArrayList<>();
    private List<String> directions = new ArrayList<>();
    private List<Location>   endPts = new ArrayList<>();
    // Start at the first step
    private int current = 0;

    Route (Location from, Location to, List<Path> paths, Map<String, String> floors) {
        start  = from;
        end    = to;
        allPaths = paths;
        generateSteps(start, end, allPaths, floors);
    }

    /**
     * Generate a set of 'steps' (String-Collection<Path> pairings) according to a set of rules
     */
    private void generateSteps(Location start, Location end, List<Path> paths, Map<String, String> floors) {
        paths = new ArrayList<>(paths); //This list will be destroyed as we iterate through
        // First step
        steps.add(new ArrayList<>()); //No paths associated with the first step
        directions.add("Start at " + (start.hasName() ? start.getName() : start.getCode()));
        endPts.add(start);
        // Subsequent steps
        Location endStep = start;
        while (!endStep.equals(end)) endStep = generateNextStep(endStep,paths,floors);
    }

    /**
     * Helper to get a string description of a location
     */
    private String descriptor(Location l) {
        return l.hasName() ? l.getName() : l.getCode();
    }

    /**
     * Helper to determine if a location is part of a staircase or lift
     */
    private boolean stairOrLift(Location l) {
        String s = l.getCode();
        return s.startsWith("L") || s.endsWith("S");
    }

    /**
     * Generates a single step ({@code String-Collection<Path>} pair) from the bottom of the list
     * given.
     */
    private Location generateNextStep(Location stepStart, List<Path> paths, Map<String, String> floors) {
        List<Path> thisStep = new ArrayList<>();
        Location stepEnd = stepStart;
        boolean stlft = stairOrLift(stepStart);
        BiPredicate<Location, Location> cont = stlft ? ((s,e) -> stairOrLift(e))
                : ((s,e) -> s.getCode().equals(e.getCode()));
        while (!paths.isEmpty() && cont.test(stepStart, stepEnd)) {
            Path p = paths.remove(0);
            stepEnd = p.getOtherLocation(stepEnd);
            thisStep.add(p);
        }
        endPts.add(stepEnd);
        steps.add(thisStep);
        String instruction;
        if (thisStep.size() <= 1) instruction = "Continue to " + descriptor(stepEnd);
        else if (stlft) //Staircase or lift
             instruction = "Take " + descriptor(stepStart) + " to " + floors.get(stepEnd.getFloor());
        else instruction = "Continue through " + descriptor(stepStart) + " to " + descriptor(stepEnd);

        directions.add(instruction);
        return stepEnd;
    }

    Location getStart() {
        return start;
    }

    Location getEnd() {
        return end;
    }

    List<Path> getAllPaths() {
        return allPaths;
    }
    

    List<Path> getDonePaths() {
        List<Path> done = new ArrayList<>();
        steps.subList(0, current + 1).forEach(done::addAll);
        return done;
    }

    List<Path> getPathsToDo() {
        int mx = steps.size();
        List<Path> toDo = new ArrayList<>();
        steps.subList(current + 1, mx).forEach(toDo::addAll);
//        return (current < mx)
//                ? steps.subList(current, mx)
//                : new ArrayList<>();
        return toDo;
    }

    Location getCurrentStepStartPoint() {
        return endPts.get(current);
    }

    String getCurrentInstruction() {
        return directions.get(current);
    }

    public Collection<Path> getCurrentPaths() {
        return steps.get(current);
    }

    private boolean canNext() {
        return current < directions.size() - 1;

    }

    private boolean canPrev() {
        return current > 0;
    }

    boolean next() {
        boolean b = canNext();
        if (b) ++current;
        return b;
    }

    boolean prev() {
        boolean b = canPrev();
        if (b) --current;
        return b;
    }
}
