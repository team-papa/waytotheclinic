package uk.ac.cam.cl.waytotheclinic;

import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WifiLocater {
    private List<WifiLocation> model;
    private WifiManager wifiM;

    public WifiLocater(WifiManager wm) {
        wifiM = wm;
    }

    private Map<String, Integer> scan() {
        List<ScanResult> res = wifiM.getScanResults();

        Map<String, Integer> strengths = new HashMap<>();

        for (ScanResult r : res) {
            String bssid = r.BSSID;
            if (strengths.get(bssid) == null) {
                // There may be several entries with the same BSSID, so find the mean of them all
                int meanStrength = 0;
                int num = 0;

                for (ScanResult result : res) {
                    if (result.BSSID == bssid) {
                        meanStrength += result.level;
                        num++;
                    }
                }

                meanStrength /= num;

                strengths.put(bssid, meanStrength);
            }
        }

        return strengths;
    }

    public void createModel(List<WifiLocation> trainingData) {
        model = trainingData;
    }

    public void loadModel(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        ObjectInputStream ois = new ObjectInputStream(fis);

        try {
            model = (List<WifiLocation>) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("File could not be read");
        }

        ois.close();
    }

    public void saveModel(File f) throws IOException {
        if (model == null) {
            throw new IOException("No active model");
        }

        FileOutputStream fos = new FileOutputStream(f);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(model);

        oos.close();
    }

    public Location getLocation() {
        return getLocation(scan());
    }

    public Location getLocation(Map<String, Integer> strengths) {
        Location l = new Location("Predicted by WifiLocater");

        if (model == null) {
            Log.w("waytotheclinic", "No WiFi model loaded");
            return l;
        }

        Map<WifiLocation, Integer> scores = new HashMap<>();
        int sumOfScores = 0;

        Map<String, Integer> scanResults = scan();

        for (WifiLocation wl : model) {
            int score = 0;
            for (Map.Entry<String, Integer> entry : wl.getStrengths().entrySet()) {
                Integer strength = scanResults.get(entry.getKey());
                if (strength != null) {
                    score += 100 - Math.abs(strength - entry.getValue());
                }
            }

            scores.put(wl, score);
            sumOfScores += score;
        }

        double lat = 0;
        double lon = 0;
        double floor = 0;
        for(Map.Entry<WifiLocation, Integer> score : scores.entrySet()) {
            lat += score.getKey().getLocation().getLatitude() * score.getValue() / sumOfScores;
            lon += score.getKey().getLocation().getLongitude() * score.getValue() / sumOfScores;
            floor += score.getKey().getFloor() * score.getValue() / sumOfScores;
        }

        l.setLatitude(lat);
        l.setLongitude(lon);
        l.setAltitude(floor);

        return l;
    }
}