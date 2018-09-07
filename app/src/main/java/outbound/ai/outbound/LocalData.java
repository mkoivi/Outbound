package outbound.ai.outbound;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class LocalData {

    public static List<Airspace> airspaces= new LinkedList<>();
    public static List<Supplement> supplements= new LinkedList<>();

    public static List<Airport> airports= new LinkedList<>();
    public static List<Reservation> reservations= new LinkedList<>();
    public static HashMap<String, Aerodrome> aerodromes= new HashMap<>();
    public static List<Waypoint> waypoints= new LinkedList<>();
    public static List<Obstacle> obstacles = new LinkedList<>();
}
