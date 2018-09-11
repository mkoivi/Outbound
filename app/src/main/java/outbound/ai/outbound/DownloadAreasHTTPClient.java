package outbound.ai.outbound;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;

import cz.msebera.android.httpclient.Header;

public class DownloadAreasHTTPClient {
    private final static String TAG = "OB:DownloadAreasHTTPC";
    Activity activity;

    public DownloadAreasHTTPClient( Activity activity) {
        this.activity = activity;
    }


    private static final String BASE_URL = "https://ilmailukartta.fi/api/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public void updateAirspaceData( JSONArray features ) {
        List<Airspace> airspaceList = new LinkedList<Airspace>();
        for( int i = 0; i < features.length(); i++) {

            try {
                JSONObject feature  = (JSONObject) features.get(i);


                JSONObject properties  = (JSONObject) feature.get("properties");

                Airspace a = new Airspace();
                a.setName( properties.optString("name"));
                a.setCallsign( properties.optString("callsign"));
                a.setActivity( properties.optString("activity"));
                a.setActivityType( properties.optString("activitytype"));
                a.setAirspaceClass(properties.optString("airspaceclass"));
                a.setRmk(properties.optString("rmk"));
                a.setUpper(properties.optString("upper"));
                a.setLower(properties.optString("lower"));
                a.setActive(properties.optBoolean("active"));
                a.setCenter(new LatLng( Double.parseDouble(properties.optString("lat")), Double.parseDouble(properties.optString("lng"))));
                JSONObject geometry  = (JSONObject) feature.get("geometry");
                JSONArray coords = geometry.getJSONArray("coordinates").getJSONArray(0);
                List<LatLng> cs = new LinkedList<LatLng>();
                for( int ii = 0; ii < coords.length(); ii++) {
                    JSONArray coord = (JSONArray) coords.get(ii);
                    LatLng ll = new LatLng( coord.getDouble(1), coord.getDouble(0));
                    cs.add(ll);
                }
                a.setCoordinates( cs);
                airspaceList.add(a);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        LocalData.airspaces = airspaceList;
        Intent intent = new Intent();
        intent.setAction("outbound.ai.outbound.AIRSPACE_UPDATED");
        activity.sendBroadcast(intent);
    }


    public void getAirspaces() {
        Log.i(TAG, "getAirspaces");
        final String fileName = "airspaces.geojson";
        long storedFileAge = LocalData.getInstance(activity).getFileAge(fileName);
        if ( storedFileAge == -1 || storedFileAge > 1000 * 60 * 60 * 24 * 10) { // update every 10 days

            DownloadAreasHTTPClient.get(fileName, null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    System.out.println("return airspace data");
                    try {
                        JSONArray features = response.getJSONArray("features");
                        updateAirspaceData(features);
                        LocalData.getInstance(activity).storeStringData(fileName, response.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.i(TAG, "get airspace data from cache");

            String data = LocalData.getInstance(activity).loadStringData(fileName);

            try {
                JSONArray features = new JSONObject(data).getJSONArray("features");
                updateAirspaceData(features);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void getSupplements() {
        Log.i(TAG, "getSupplements");
        final String fileName = "supplements.geojson";
        long storedFileAge = LocalData.getInstance(activity).getFileAge(fileName);
        if ( storedFileAge == -1 || storedFileAge > 1000 * 60 * 60 * 24 * 2) { // update every 2 day

            DownloadAreasHTTPClient.get(fileName, null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    System.out.println("return sup data");
                    try {
                        JSONArray features = response.getJSONArray("features");
                        updateSupplementsData(features);
                        LocalData.getInstance(activity).storeStringData(fileName, response.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.i(TAG, "get sup data from cache");

            String data = LocalData.getInstance(activity).loadStringData(fileName);

            try {
                JSONArray features = new JSONObject(data).getJSONArray("features");
                updateSupplementsData(features);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    public void updateSupplementsData( JSONArray features)  {

                    List<Supplement> airspaceList = new LinkedList<Supplement>();

                    for( int i = 0; i < features.length(); i++) {

                        try {

                            JSONObject feature  = (JSONObject) features.get(i);

                            JSONObject properties  = (JSONObject) feature.get("properties");

                            Supplement a = new Supplement();
                            a.setName( properties.optString("name"));
                            a.setActivity( properties.optString("activity"));
                            a.setAirspaceClass(properties.optString("airspaceclass"));
                            a.setRmk(properties.optString("rmk"));
                            a.setUrl(properties.optString("url"));
                             a.setUpper(properties.optString("upper"));
                            a.setLower(properties.optString("lower"));
                            //            a.setCenter(new LatLng( Double.parseDouble(properties.optString("lat")), Double.parseDouble(properties.optString("lng"))));
                            a.setActive(properties.optBoolean("active"));
                            JSONObject geometry  = (JSONObject) feature.get("geometry");
                            JSONArray coords = geometry.getJSONArray("coordinates").getJSONArray(0);
                            List<LatLng> cs = new LinkedList<LatLng>();
                            for( int ii = 0; ii < coords.length(); ii++) {
                                JSONArray coord = (JSONArray) coords.get(ii);
                                LatLng ll = new LatLng( coord.getDouble(1), coord.getDouble(0));
                                cs.add(ll);
                            }
                            a.setCoordinates( cs);
                            airspaceList.add(a);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    LocalData.supplements = airspaceList;

                Intent intent = new Intent();
                intent.setAction("outbound.ai.outbound.SUPPLEMENTS_UPDATED");
                activity.sendBroadcast(intent);

            }


    public void getReservations() {
        Log.i(TAG, "getReservations");
        final String fileName = "reservations.geojson";
        long storedFileAge = LocalData.getInstance(activity).getFileAge(fileName);
        if ( storedFileAge == -1 || storedFileAge > 1000 * 60 * 60 * 24 * 2) { // update every 2 day

            DownloadAreasHTTPClient.get(fileName, null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    System.out.println("return sup data");
                    try {
                        JSONArray features = response.getJSONArray("features");
                        updateReservationsData(features);
                        LocalData.getInstance(activity).storeStringData(fileName, response.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.i(TAG, "get reservations data from cache");

            String data = LocalData.getInstance(activity).loadStringData(fileName);

            try {
                JSONArray features = new JSONObject(data).getJSONArray("features");
                updateReservationsData(features);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void updateReservationsData( JSONArray features)  {

                    List<Reservation> airspaceList = new LinkedList<Reservation>();

                    for( int i = 0; i < features.length(); i++) {

                        try {

                            JSONObject feature  = (JSONObject) features.get(i);

                            JSONObject properties  = (JSONObject) feature.get("properties");

                            Reservation a = new Reservation();
                            a.setName( properties.optString("name"));
                            a.setDescription( properties.optString("description"));
                            a.setAirspaceClass(properties.optString("airspaceclass"));
                            a.setUpper(properties.optString("upper"));
                            a.setLower(properties.optString("lower"));
                //            a.setCenter(new LatLng( Double.parseDouble(properties.optString("lat")), Double.parseDouble(properties.optString("lng"))));
                            a.setActive(properties.optBoolean("active"));
                            JSONObject geometry  = (JSONObject) feature.get("geometry");
                            JSONArray coords = geometry.getJSONArray("coordinates").getJSONArray(0);
                            List<LatLng> cs = new LinkedList<LatLng>();
                            for( int ii = 0; ii < coords.length(); ii++) {
                                JSONArray coord = (JSONArray) coords.get(ii);
                                LatLng ll = new LatLng( coord.getDouble(1), coord.getDouble(0));
                                cs.add(ll);
                            }
                            a.setCoordinates( cs);
                            airspaceList.add(a);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    LocalData.reservations = airspaceList;


                Intent intent = new Intent();
                intent.setAction("outbound.ai.outbound.RESERVATIONS_UPDATED");
                activity.sendBroadcast(intent);

            }



    public void getAerodromes() {
        Log.i(TAG, "getAerodromes");
        final String fileName = "aerodromes.geojson";
        long storedFileAge = LocalData.getInstance(activity).getFileAge(fileName);
        if ( storedFileAge == -1 || storedFileAge > 1000 * 60 * 60 * 24 * 5) { // update every 2 day

            DownloadAreasHTTPClient.get(fileName, null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    System.out.println("return add data");
                    try {
                        JSONArray features = response.getJSONArray("features");
                        updateAerodromesData(features);
                        LocalData.getInstance(activity).storeStringData(fileName, response.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.i(TAG, "get add data from cache");

            String data = LocalData.getInstance(activity).loadStringData(fileName);

            try {
                JSONArray features = new JSONObject(data).getJSONArray("features");
                updateAerodromesData(features);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void updateAerodromesData( JSONArray features)  {
        System.out.println("updateAerodromesData");

                    for( int i = 0; i < features.length(); i++) {

                        try {

                            JSONObject feature  = (JSONObject) features.get(i);

                            JSONObject properties  = (JSONObject) feature.get("properties");

                            Aerodrome a = new Aerodrome();
                            a.setName( properties.optString("name"));
                            a.setCode( properties.optString("code"));
                            a.setElevation(properties.optInt( "elevation",0));
                            a.setComFreq( properties.optString("comFreq"));
                            a.setAccFreq( properties.optString("accFreq"));
                            a.setAccSector( properties.optString("accSector"));
                            a.setCenter(new LatLng( Double.parseDouble(properties.optString("lat")), Double.parseDouble(properties.optString("lng"))));

                            JSONArray rws = properties.getJSONArray("runways");
                            List<Runway> rs = new LinkedList<Runway>();

                            for( int iii = 0; iii < rws.length(); iii++) {
                                JSONObject rj  = (JSONObject) rws.get(iii);
                                rs.add(new Runway( rj.optString("id"),rj.optInt("length"),rj.optString("surface") ));
                            }
                            a.setRunways(rs);


                            JSONArray notams = properties.getJSONArray("notams");
                            List<String> ns = new LinkedList<String>();

                            for( int iii = 0; iii < notams.length(); iii++) {
                                ns.add((String)notams.get(iii));
                            }
                            a.setNotams(ns);
                            JSONObject geometry  = (JSONObject) feature.get("geometry");
                            JSONArray coords = geometry.getJSONArray("coordinates").getJSONArray(0);
                            List<LatLng> cs = new LinkedList<LatLng>();
                            for( int ii = 0; ii < coords.length(); ii++) {
                                JSONArray coord = (JSONArray) coords.get(ii);
                                LatLng ll = new LatLng( coord.getDouble(1), coord.getDouble(0));
                                cs.add(ll);
                            }
                            a.setCoordinates( cs);
                            LocalData.aerodromes.put(a.getCode(),a);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                  Intent intent = new Intent();
                intent.setAction("outbound.ai.outbound.AERODROMES_UPDATED");
                activity.sendBroadcast(intent);

    }


    public void getAirports() {
        Log.i(TAG, "getAirports");
        final String fileName = "airfields.geojson";
        long storedFileAge = LocalData.getInstance(activity).getFileAge(fileName);
        if ( storedFileAge == -1 || storedFileAge > 1000 * 60 * 60 * 24 * 5) { // update every 2 day

            DownloadAreasHTTPClient.get(fileName, null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    System.out.println("return add data");
                    try {
                        JSONArray features = response.getJSONArray("features");
                        updateAirportData(features);
                        LocalData.getInstance(activity).storeStringData(fileName, response.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.i(TAG, "get add data from cache");

            String data = LocalData.getInstance(activity).loadStringData(fileName);

            try {
                JSONArray features = new JSONObject(data).getJSONArray("features");
                updateAirportData(features);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void updateAirportData( JSONArray features)  {


                    for( int i = 0; i < features.length(); i++) {

                        try {

                            JSONObject feature  = (JSONObject) features.get(i);

                            JSONObject properties  = (JSONObject) feature.get("properties");

                            Airport a = new Airport();
                            a.setName( properties.optString("name"));
                            a.setCode( properties.optString("code"));
                            a.setCenter(new LatLng( Double.parseDouble(properties.optString("lat")), Double.parseDouble(properties.optString("lng"))));
                            JSONArray notams = properties.getJSONArray("notams");
                            List<String> ns = new LinkedList<String>();

                            for( int iii = 0; iii < notams.length(); iii++) {
                                ns.add((String)notams.get(iii));
                            }
                            a.setNotams(ns);
                            JSONObject geometry  = (JSONObject) feature.get("geometry");
                            JSONArray coords = geometry.getJSONArray("coordinates").getJSONArray(0);
                            List<LatLng> cs = new LinkedList<LatLng>();
                            for( int ii = 0; ii < coords.length(); ii++) {
                                JSONArray coord = (JSONArray) coords.get(ii);
                                LatLng ll = new LatLng( coord.getDouble(1), coord.getDouble(0));
                                cs.add(ll);
                            }
                            a.setCoordinates( cs);


                            JSONArray rws = properties.getJSONArray("runways");
                            List<Runway> rs = new LinkedList<Runway>();

                            for( int iii = 0; iii < rws.length(); iii++) {
                                JSONObject rj  = (JSONObject) rws.get(iii);
                                JSONArray nms = rj.getJSONArray("names");
                                List<String> names = new LinkedList<>();
                                for(int iiii=0; iiii<nms.length();iiii++) {
                                    names.add(nms.get(iiii).toString());
                                }

                                JSONObject st = rj.getJSONObject("start");
                                LatLng llstart = new LatLng( Double.parseDouble(st.optString("lat")), Double.parseDouble(st.optString("lng")));
                                LatLng llend = new LatLng( Double.parseDouble(st.optString("lat")), Double.parseDouble(st.optString("lng")));
                                 rs.add(new Runway( names, llstart,llend ));
                            }
                            a.setRunways(rs);

                            LocalData.airports.put(a.code,a);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }



                Intent intent = new Intent();
                intent.setAction("outbound.ai.outbound.AIRPORTS_UPDATED");
                activity.sendBroadcast(intent);

            }


    public void getWaypoints() {
        Log.i(TAG, "getWaypoints");
        final String fileName = "waypoints.geojson";
        long storedFileAge = LocalData.getInstance(activity).getFileAge(fileName);
        if ( storedFileAge == -1 || storedFileAge > 1000 * 60 * 60 * 24 * 5) { // update every 2 day

            DownloadAreasHTTPClient.get(fileName, null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    System.out.println("return add data");
                    try {
                        JSONArray features = response.getJSONArray("features");
                        updateWaypointsData(features);
                        LocalData.getInstance(activity).storeStringData(fileName, response.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.i(TAG, "get wp data from cache");

            String data = LocalData.getInstance(activity).loadStringData(fileName);

            try {
                JSONArray features = new JSONObject(data).getJSONArray("features");
                updateWaypointsData(features);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    public void updateWaypointsData( JSONArray features)  {

                    List<Waypoint> airspaceList = new LinkedList<Waypoint>();

                    for( int i = 0; i < features.length(); i++) {

                        try {

                            JSONObject feature  = (JSONObject) features.get(i);

                            JSONObject properties  = (JSONObject) feature.get("properties");

                            Waypoint a = new Waypoint();
                            a.setName( properties.optString("name"));
                            a.setCompulsory( properties.optBoolean("compulsory"));


                            JSONObject geometry  = (JSONObject) feature.get("geometry");
                            JSONArray coords = geometry.getJSONArray("coordinates").getJSONArray(0);
                           double lat = 0;
                           double lng = 0;
                            for( int ii = 0; ii < coords.length(); ii++) {
                                JSONArray coord = (JSONArray) coords.get(ii);
                                lat += coord.getDouble(1);
                                lng += coord.getDouble(0);
                            }

                            a.setCenter(new LatLng( lat/coords.length(), lng/coords.length()));


                            airspaceList.add(a);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    LocalData.waypoints = airspaceList;


                Intent intent = new Intent();
                intent.setAction("outbound.ai.outbound.WAYPOINTS_UPDATED");
                activity.sendBroadcast(intent);


    }


    public void getObstacles() {
        Log.i(TAG, "getObstacles");
        final String fileName = "obstacles.geojson";
        long storedFileAge = LocalData.getInstance(activity).getFileAge(fileName);
        if ( storedFileAge == -1 || storedFileAge > 1000 * 60 * 60 * 24 * 5) { // update every 2 day

            DownloadAreasHTTPClient.get(fileName, null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    System.out.println("return add data");
                    try {
                        JSONArray features = response.getJSONArray("features");
                        updateObstaclesData(features);
                        LocalData.getInstance(activity).storeStringData(fileName, response.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.i(TAG, "get obs data from cache");

            String data = LocalData.getInstance(activity).loadStringData(fileName);

            try {
                JSONArray features = new JSONObject(data).getJSONArray("features");
                updateObstaclesData(features);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void updateObstaclesData(JSONArray features)  {

                    List<Obstacle> airspaceList = new LinkedList<>();

                    for( int i = 0; i < features.length(); i++) {

                        try {

                            JSONObject feature  = (JSONObject) features.get(i);

                            JSONObject properties  = (JSONObject) feature.get("properties");

                            Obstacle a = new Obstacle();
                            a.setName( properties.optString("name"));
                            a.setType( properties.optString("type"));
                            a.setElevation(properties.optInt("elev"));
                            a.setCenter(new LatLng( Double.parseDouble(properties.optString("lat")), Double.parseDouble(properties.optString("lng"))));

                            JSONObject geometry  = (JSONObject) feature.get("geometry");
                            JSONArray coords = geometry.getJSONArray("coordinates").getJSONArray(0);
                            double lat = 0;
                            double lng = 0;
                            for( int ii = 0; ii < coords.length(); ii++) {
                                JSONArray coord = (JSONArray) coords.get(ii);
                                lat += coord.getDouble(1);
                                lng += coord.getDouble(0);
                            }

                            a.setCenter(new LatLng( lat/coords.length(), lng/coords.length()));


                            airspaceList.add(a);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    LocalData.obstacles = airspaceList;

                Intent intent = new Intent();
                intent.setAction("outbound.ai.outbound.OBSTACLES_UPDATED");
                activity.sendBroadcast(intent);

            }


    public void getWeather() {
        Log.i(TAG, "getWeather");
        final String url = "https://www.ilmailusaa.fi/backend.php?{%22mode%22:%22metartaf%22,%22radius%22:%22100%22,%22points%22:[{%22_lon%22:19.955322357127883,%22_lat%22:63.38045595056234},{%22_lon%22:20.48682808808789,%22_lat%22:59.53117760188318},{%22_lon%22:31.18856857414081,%22_lat%22:59.9210561813142},{%22_lon%22:31.96529049581418,%22_lat%22:68.22053794882052},{%22_lon%22:27.237080462936532,%22_lat%22:70.57196471992248},{%22_lon%22:22.203804656099354,%22_lat%22:69.44091169818802},{%22_lon%22:19.780870994518416,%22_lat%22:69.46770635226937},{%22_lon%22:19.973237996864583,%22_lat%22:63.30748777534764}]}";
        final String fileName = "metar.json";
        long storedFileAge = LocalData.getInstance(activity).getFileAge(fileName);
        if ( storedFileAge == -1 || storedFileAge > 1000 * 60 * 60 * 24 * 5) { // update every 2 day


                DownloadAreasHTTPClient.get(url, null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    System.out.println("return metar data");
                    try {
                        JSONArray features = response.getJSONArray("METAR0");
                        updateObstaclesData(features);
                        LocalData.getInstance(activity).storeStringData(fileName, response.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.i(TAG, "get obs data from cache");

            String data = LocalData.getInstance(activity).loadStringData(fileName);

            try {
                JSONArray features = new JSONObject(data).getJSONArray("METAR0");
                updateObstaclesData(features);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
