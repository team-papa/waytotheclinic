package uk.ac.cam.cl.waytotheclinic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PathFinder {

    // final list will be backwards
    public List<Edge> getPath(Vertex start, Vertex end, boolean noStairs) {
        HashSet<Vertex> closedSet = new HashSet<>();

        HashSet<Vertex> openSet = new HashSet<>();
        openSet.add(start);

        HashMap<Vertex, Vertex> cameFrom = new HashMap<>();
        HashMap<Vertex, Edge> cameFromEdge = new HashMap<>();

        HashMap<Vertex, Integer> gScore = new HashMap<>();
        gScore.put(start, 0);

        HashMap<Vertex, Integer> fScore = new HashMap<>();

        fScore.put(start, VertexComparator.manhattanDistance(start, end));

        while (!openSet.isEmpty()) {
            // From Wikipedia
            Vertex current = pickBestNext(openSet, fScore);

            if (current.samePlaceAs(end)) {
                List<Edge> toRet = reconstructEdgePath(cameFrom, cameFromEdge, current);
                Collections.reverse(toRet);
                return toRet;
            }

            openSet.remove(current);
            closedSet.add(current);

            for (Edge adjEdge : current.getOutEdges()) {

                // Are stairs allowed? If not, skip stair edges
                if (noStairs && adjEdge.isStairs()) {
                    continue;
                }

                Vertex neighbour = adjEdge.getOutVertex();

                if (closedSet.contains(neighbour)) {
                    continue;
                }

                if (!openSet.contains(neighbour)) {
                    openSet.add(neighbour);
                }

                int tentative_gScore = tryGet(current, gScore) + adjEdge.getCost();

//                System.out.println("neighbour: " + neighbour);
                if (tentative_gScore >= tryGet(neighbour, gScore)) {
                    continue;
                }

                // This path is the best until now
//                System.out.println("" + neighbour + current + tentative_gScore);
                cameFrom.put(neighbour, current);
                cameFromEdge.put(neighbour, adjEdge);
                gScore.put(neighbour, tentative_gScore);
                fScore.put(neighbour, tryGet(neighbour, gScore) + VertexComparator.manhattanDistance(neighbour, end));
            }
        }
        System.err.println("Returning null, OUCH");
        return new ArrayList<>();
    }

    public static List<Instruction> getTextDirections(List<Edge> path) {
        List<Instruction> directions = new ArrayList<>();

        double orientAngle = path != null && path.size() > 0 ? path.get(0).getAngle() : 0;

        String textDirection = "";

        // add all places you walk past on a straight to the list
        ArrayList<String> straightLabelList = new ArrayList<>();
        for (Edge e : path) {

            if (e.getInVertex().getZ() != e.getOutVertex().getZ()) {
                if (e.isStairs()) {
                    // Only add the last direction of where to take the stairs in this stairwell
                    // This turns this                         into this
                    // Take the stairs to level 1             Take the stairs to level 3
                    // Take the stairs to level 2
                    // Take the stairs to level 3
                    if (directions.get(directions.size() - 1)
                            .getInstructionText().contains("Take the stairs")) {
                        directions.remove(directions.size() - 1);
                    }
                    directions.add(new Instruction(R.drawable.stairs,
                            "Take the stairs to level " + (e.getOutVertex().getZ() + 1)));
                } else {
                    directions.add(new Instruction(R.drawable.lift,
                            "Take the lift to level " + (e.getOutVertex().getZ() + 1)));
                }
            } else {

                double newAngle = e.getAngle();
                assert (newAngle < 360 && newAngle >= 0);
                assert (orientAngle < 360 && orientAngle >= 0);

                double diffAngle = (orientAngle - newAngle + 360) % 360;

                TurnType turnType;

                if (Math.abs(diffAngle) == 180) {
                    turnType = TurnType.UTURN;
                } else if (diffAngle > 180 && diffAngle < 360 && diffAngle == 270 ) {
                    turnType = TurnType.LEFT;
                } else if (diffAngle > 0 && diffAngle < 180 && diffAngle == 90) {
                    turnType = TurnType.RIGHT;
                } else {
                    turnType = TurnType.STRAIGHT;
                }

                ArrayList<String> labels = e.getOutVertex().getLabels();

                String placeName = (labels.size() > 0) ? labels.get(0) : "";

                if (turnType != TurnType.STRAIGHT) {
                    // flush last instruction if needed
                    if (straightLabelList.size() > 0) {
                        for (int i = 0; i < straightLabelList.size(); i++) {
                            String label = straightLabelList.get(i);
                            if (label != null && !label.equals("")) {
                                if (i != straightLabelList.size() - 1) {
                                    textDirection += " past the " + label + ",";
                                } else {
                                    textDirection += " towards the " + label;
                                }
                            }
                        }

                        // remove trailing comma
                        textDirection = textDirection.replaceAll(",$", "");

                        directions.add(new Instruction(R.drawable.straight, textDirection));

                        straightLabelList.clear();
                        assert (straightLabelList.size() == 0);
                    }

                    int icon = 1;

                    switch (turnType) {
                        case UTURN:
                            icon = R.drawable.uturn;
                            textDirection = "Turn around";
                            break;

                        case LEFT:
                            icon = R.drawable.left;
                            textDirection = "Turn left";
                            break;

                        case RIGHT:
                            icon = R.drawable.right;
                            textDirection = "Turn right";
                            break;
                    }

                    if (!placeName.equals("")) {
                        textDirection += " towards the " + placeName;
                    }

                    directions.add(new Instruction(icon, textDirection));

                    textDirection = "";
                } else {
                    // if was straight, just add to list
                    if (straightLabelList.size() == 0) textDirection = "Go straight";
                    straightLabelList.add(placeName);
                }

                // point towards new direction
                orientAngle = newAngle;
            }
        }

        directions.add(new Instruction(R.drawable.destination, "You have arrived at your destination!"));

        return directions;
    }


    private Vertex pickBestNext(Set<Vertex> openSet, HashMap<Vertex, Integer> fScore) {
        Vertex toReturn = null;
        int best = Integer.MAX_VALUE;
        for (Vertex neighbour : openSet) {
            if (tryGet(neighbour, fScore) <= best) {
                best = tryGet(neighbour, fScore);
                toReturn = neighbour;
            }
        }
        return toReturn;
    }


    public List<Edge> reconstructEdgePath(HashMap<Vertex, Vertex> cameFrom,
                                          HashMap<Vertex, Edge> cameFromEdge, Vertex current) {
        List<Edge> totalPath = new ArrayList<>();

        Edge firstEdge = cameFromEdge.get(current);
        if(firstEdge == null) return totalPath; // if no edges return empty list

        totalPath.add(firstEdge);

        while (cameFromEdge.keySet().contains(current)) { // assumes will work
            current = cameFrom.get(current);
            if (cameFromEdge.get(current) != null) totalPath.add(cameFromEdge.get(current));
        }
        return totalPath;
    }


    public Integer tryGet(Vertex v, HashMap<Vertex, Integer> m) {
        Integer d = m.get(v);
        return d != null ? d : Integer.MAX_VALUE;
    }


}
