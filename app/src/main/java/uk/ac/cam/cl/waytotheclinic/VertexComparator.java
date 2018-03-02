package uk.ac.cam.cl.waytotheclinic;

import java.util.Comparator;

import uk.ac.cam.cl.waytotheclinic.Vertex;

public class VertexComparator implements Comparator<Vertex> {

    private static final int LIFT_COST = 1000;
    private static final int STAIR_COST = 450;

    private Vertex end;

    public VertexComparator(Vertex end) {
        this.end = end;
    }

    public static int manhattanDistance(Vertex a, Vertex b) {
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        int dz = Math.abs(a.getZ() - b.getZ());

        // A* heuristic must underestimate to be admissible
        int best_dz = Math.min(STAIR_COST * dz, LIFT_COST);

        return dx + dy + best_dz;
    }

    public static int ManhattanDistance2D(Vertex v, Vertex w) {
        int dx = Math.abs(v.getX() - w.getX());
        int dy = Math.abs(v.getY() - w.getY());

        return dx + dy;
    }

    public int compare(Vertex a, Vertex b) {
        return (manhattanDistance(a, end) - manhattanDistance(b, end));
    }
}

