package outbound.ai.outbound;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Reservation extends Airspace {

    String description;
    List<String> notams;
    LatLng center;
    String upper;
    String lower;
    int upperFt;
    int lowerFt;

    boolean active = false;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String getUpper() {
        return upper;
    }

    @Override
    public void setUpper(String upper) {
        this.upper = upper;
    }

    @Override
    public String getLower() {
        return lower;
    }

    @Override
    public void setLower(String lower) {
        this.lower = lower;
    }

    @Override
    public int getUpperFt() {
        return upperFt;
    }

    @Override
    public void setUpperFt(int upperFt) {
        this.upperFt = upperFt;
    }

    @Override
    public int getLowerFt() {
        return lowerFt;
    }

    @Override
    public void setLowerFt(int lowerFt) {
        this.lowerFt = lowerFt;
    }
}
