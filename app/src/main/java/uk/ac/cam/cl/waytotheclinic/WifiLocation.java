package uk.ac.cam.cl.waytotheclinic;

import android.location.Location;
import java.io.Serializable;
import java.util.Map;

class WifiLocation implements Serializable {
	private Location location;
	private Map<String, Integer> strengths;	// Map of BSSID to strength

	WifiLocation(Location l, Map<String, Integer> s) {
		location = l;
		strengths = s;
	}

	WifiLocation(double lat, double lon, Map<String, Integer> s) {
		location = new Location("Provided to WifiLocation");
		location.setLatitude(lat);
		location.setLongitude(lon);

		strengths = s;
	}

	public Location getLocation() {
		return location;
	}

	public Map<String, Integer> getStrengths() {
		return strengths;
	}
}