package outbound.ai.outbound;

import com.google.android.gms.maps.model.LatLng;

public class Waypoint {

    String name;
    boolean compulsory;

    LatLng center;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCompulsory() {
        return compulsory;
    }

    public void setCompulsory(boolean compulsory) {
        this.compulsory = compulsory;
    }

    public LatLng getCenter() {
        return center;
    }

    public void setCenter(LatLng center) {
        this.center = center;
    }
}
