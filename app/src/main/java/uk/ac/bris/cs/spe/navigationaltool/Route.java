package uk.ac.bris.cs.spe.navigationaltool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import uk.ac.bris.cs.spe.navigationaltool.graph.Location;
import uk.ac.bris.cs.spe.navigationaltool.graph.Path;

class Route {
    // Raw input
    private final Location start;
    private final Location end;
    private final List<Path> allPaths;
    // Output in steps
    private List<Collection<Path>> steps = new ArrayList<>();
    private List<String> directions = new ArrayList<>();
    // Start at the first step
    private int current = 0;

    Route (Location from, Location to, List<Path> paths, Map<String, String> floors) {
        start = from;
        end = to;
        allPaths = paths;
        generateSteps(start, end, allPaths, floors);
    }

    /**
     * Generate a set of 'steps' (String-Collection<Path> pairings) according to a set of rules
     */
    private void generateSteps(Location start, Location end, List<Path> paths, Map<String, String> floors) {
        paths = new ArrayList<>(paths); //This list will be destroyed as we iterate through
        steps.add(new ArrayList<>()); //No paths associated with the first step
        directions.add("Start at " + (start.hasName() ? start.getName() : start.getCode()));
        Location endStep = start;
        while (!endStep.equals(end)) endStep = generateNextStep(endStep,paths,floors);
    }

    private String descriptor(Location l) {
        return l.hasName() ? l.getName() : l.getCode();
    }

    private boolean stairOrLift(Location l) {
        String s = l.getCode();
        return s.startsWith("L") || s.endsWith("S");
    }

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
        steps.add(thisStep);
        String instruction;
        if (thisStep.size() <= 1) instruction = "Continue to " + descriptor(stepEnd);
        else if (stlft) //Staircase or lift
             instruction = "Take " + descriptor(stepStart) + " to " + floors.get(stepEnd.getFloor());
        else instruction = "Continue through " + descriptor(stepStart) + " to " + descriptor(stepEnd);

        directions.add(instruction);
        return stepEnd;
    }

    public Location getStart() {
        return start;
    }

    public Location getEnd() {
        return end;
    }

    public List<Path> getAllPaths() {
        return allPaths;
    }

    public String getCurrentInstruction() {
        return directions.get(current);
    }

    public Collection<Path> getCurrentPaths() {
        return steps.get(current);
    }

    public boolean canNext() {
        return current < directions.size() - 1;
    }

    public boolean canPrev() {
        return current > 0;
    }

    public void next() {
        if (canNext()) ++current;
    }

    public void prev() {
        if (canPrev()) --current;
    }
}
