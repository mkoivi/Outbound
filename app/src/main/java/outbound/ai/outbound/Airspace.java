package outbound.ai.outbound;

import com.google.android.gms.maps.model.LatLng;
import java.util.List;

public class Airspace {

    String name;
    String callsign;

    List<LatLng> coordinates;

    String airspaceClass;
    String activity;
    String lang;
    String rmk;

    String upper;
    String lower;
    int upperFt;
    int lowerFt;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public List<LatLng> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<LatLng> coordinates) {
        this.coordinates = coordinates;
    }

    public String getAirspaceClass() {
        return airspaceClass;
    }

    public void setAirspaceClass(String airspaceClass) {
        this.airspaceClass = airspaceClass;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getRmk() {
        return rmk;
    }

    public void setRmk(String rmk) {
        this.rmk = rmk;
    }

    public String getUpper() {
        return upper;
    }

    public void setUpper(String upper) {
        this.upper = upper;
    }

    public String getLower() {
        return lower;
    }

    public void setLower(String lower) {
        this.lower = lower;
    }

    public int getUpperFt() {
        return upperFt;
    }

    public void setUpperFt(int upperFt) {
        this.upperFt = upperFt;
    }

    public int getLowerFt() {
        return lowerFt;
    }

    public void setLowerFt(int lowerFt) {
        this.lowerFt = lowerFt;
    }
}
