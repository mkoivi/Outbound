package outbound.ai.outbound;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Reservation extends Airspace {

    String description;
    List<String> notams;
    LatLng center;

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
}
