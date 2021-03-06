package uk.ac.cam.cl.waytotheclinic;

import java.io.Serializable;
import java.util.ArrayList;

public class Edge implements Serializable {

    private static final long serialVersionUID = 1L;

    private Vertex in;
    private Vertex out;
    private int cost;
    private double angle;
    private ArrayList<String> labels;
    private boolean s = false;

    public Edge(Vertex in, Vertex out, int cost) {
        this.in = in;
        this.out = out;
        this.cost = cost;
        this.labels = out.getLabels();

        // "in: A, out: B" means A -> B
        in.addOutEdge(this);
        out.addInEdge(this);
    }

    public Edge(Vertex in, Vertex out, int cost, double angle) {
        this(in, out, cost);
        this.angle = angle;
    }

    public void makeStairs() {
        s = true;
    }

    public boolean isStairs() {
        return s;
    }

    @Override
    public String toString() {
        String str = "";
        str +=  "\n Edge : " + labels + "\n";
        str +=  " In Vertex : " + in.toString() + "\n";
        str +=  " Out Vertex : " + out.toString() + "\n";
        str +=  " Cost: " + cost + "\n";
        str +=  " Angle: " + angle + "\n";

        return str;
    }

    public int getCost() { return cost; }

    public double getAngle() { return angle; }

    public Vertex getInVertex() { return in; }

    public Vertex getOutVertex() { return out; }
}
