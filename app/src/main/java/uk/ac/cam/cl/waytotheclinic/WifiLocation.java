package uk.ac.cam.cl.waytotheclinic;

import android.location.Location;
import java.io.Serializable;
import java.util.Map;

class WifiLocation implements Serializable {
    private int floorNumber;
    private Location location;
    private Map<String, Integer> strengths; // Map of BSSID to strength

    WifiLocation(Location l, Map<String, Integer> s) {
        location = l;
        strengths = s;
    }

    WifiLocation(double lat, double lon, int floor, Map<String, Integer> s) {
        location = new Location("Provided to WifiLocation");
        location.setLatitude(lat);
        location.setLongitude(lon);

        strengths = s;
    }

    public int getFloor() {
        return floorNumber;
    }

    public Location getLocation() {
        return location;
    }

    public Map<String, Integer> getStrengths() {
        return strengths;
    }
}