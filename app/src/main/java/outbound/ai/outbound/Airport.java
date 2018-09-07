package outbound.ai.outbound;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Airport {

    String name;
    String code;

    LatLng center;

    List<LatLng> coordinates;
    List<String> notams;
    List<Runway> runways;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<LatLng> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<LatLng> coordinates) {
        this.coordinates = coordinates;
    }

    public List<String> getNotams() {
        return notams;
    }

    public void setNotams(List<String> notams) {
        this.notams = notams;
    }

    public LatLng getCenter() {
        return center;
    }

    public void setCenter(LatLng center) {
        this.center = center;
    }

    public List<Runway> getRunways() {
        return runways;
    }

    public void setRunways(List<Runway> runways) {
        this.runways = runways;
    }
}
