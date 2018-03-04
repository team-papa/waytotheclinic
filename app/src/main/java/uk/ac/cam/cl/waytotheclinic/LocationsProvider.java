package uk.ac.cam.cl.waytotheclinic;

import android.content.Context;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LocationsProvider {

    public static Set<Vertex> generateVertices(Context context) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                    context.getResources().getAssets().open("vertexSetFinal.ser")));

            HashSet<Vertex> vertexSet = (HashSet<Vertex>) ois.readObject();

            return vertexSet;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Set<String> generateLocations(Context context) {
        Set<Vertex> vertices = generateVertices(context);
        Set<String> allLabels = new HashSet<>();

        ArrayList<String> vertexLabelList = new ArrayList<String>();
      
        for(Vertex v: vertices) {
            vertexLabelList = v.getLabels();
            if(vertexLabelList != null && !vertexLabelList.contains(null)) { // TODO Solve nulls without having to handle them here
                allLabels.addAll(vertexLabelList);
            }
        }
      
        return allLabels;
    }
}
