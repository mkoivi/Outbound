package outbound.ai.outbound;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Runway {
    String id;
    List<String> names;
    LatLng start;
    LatLng end;
    int length;
    String surface;

    public Runway(String id, int length, String surface) {
        this.id = id;
        this.length = length;
        this.surface = surface;
    }

    public Runway(List<String> names, LatLng start, LatLng end) {
        this.names = names;
        this.start  = start;
        this.end = end;

    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public LatLng getStart() {
        return start;
    }

    public void setStart(LatLng start) {
        this.start = start;
    }

    public LatLng getEnd() {
        return end;
    }

    public void setEnd(LatLng end) {
        this.end = end;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getSurface() {
        return surface;
    }

    public void setSurface(String surface) {
        this.surface = surface;
    }
}