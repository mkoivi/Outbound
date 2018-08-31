package outbound.ai.outbound;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Aerodrome {

    String name;
    String code;
    int elevation;
    String comFreq;
    String accSector;
    String accFreq;

    LatLng center;
    List<String> notams;
    List<Runway> runways;
    List<LatLng> coordinates;

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

    public int getElevation() {
        return elevation;
    }

    public void setElevation(int elevation) {
        this.elevation = elevation;
    }

    public String getComFreq() {
        return comFreq;
    }

    public void setComFreq(String comFreq) {
        this.comFreq = comFreq;
    }

    public String getAccSector() {
        return accSector;
    }

    public void setAccSector(String accSector) {
        this.accSector = accSector;
    }

    public String getAccFreq() {
        return accFreq;
    }

    public void setAccFreq(String accFreq) {
        this.accFreq = accFreq;
    }

    public LatLng getCenter() {
        return center;
    }

    public void setCenter(LatLng center) {
        this.center = center;
    }

    public List<String> getNotams() {
        return notams;
    }

    public void setNotams(List<String> notams) {
        this.notams = notams;
    }

    public List<Runway> getRunways() {
        return runways;
    }

    public void setRunways(List<Runway> runways) {
        this.runways = runways;
    }

    public List<LatLng> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<LatLng> coordinates) {
        this.coordinates = coordinates;
    }
}
