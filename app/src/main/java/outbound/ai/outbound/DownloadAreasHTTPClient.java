package outbound.ai.outbound;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

import cz.msebera.android.httpclient.Header;

public class DownloadAreasHTTPClient {

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


    public void getAirspaces()  {
        System.out.println("HTTPREQUEST");
        DownloadAreasHTTPClient.get("airspaces.geojson", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                System.out.println("HTTPRESPONSE with JSONObject");

                try {
                    JSONArray features = response.getJSONArray("features");
                    List<Airspace> airspaceList = new LinkedList<Airspace>();

                    for( int i = 0; i < features.length(); i++) {

                        try {
                            JSONObject feature  = (JSONObject) features.get(i);


                            JSONObject properties  = (JSONObject) feature.get("properties");

                            Airspace a = new Airspace();
                            a.setName( properties.optString("name"));
                            a.setCallsign( properties.optString("callsign"));
                            a.setActivity( properties.optString("activity"));
                            a.setAirspaceClass(properties.optString("airspaceclass"));
                            a.setRmk(properties.optString("rmk"));
                            a.setUpper(properties.optString("upper"));
                            a.setLower(properties.optString("lower"));
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
                    // Pull out the first event on the public timeline
//                    JSONObject firstEvent = (JSONObject)response.getJSONObject("features");
 //                   String tweetText = firstEvent.getString("text");
  //                  System.out.println("HTTPRESPONSE");
                    // Do something with the response
  //                  System.out.println(tweetText);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent();
                intent.setAction("outbound.ai.outbound.AIRSPACE_UPDATED");
                activity.sendBroadcast(intent);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                try {
                    // Pull out the first event on the public timeline
                    JSONObject firstEvent = (JSONObject)timeline.get(0);
                    String tweetText = firstEvent.getString("text");
                    System.out.println("HTTPRESPONSE");
                        // Do something with the response
                        System.out.println(tweetText);
                } catch (Exception e) {
                    e.printStackTrace();
                }
         }
        });
    }

    public void getSupplements()  {
        System.out.println("HTTPREQUEST");
        DownloadAreasHTTPClient.get("supplements.geojson", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                System.out.println("HTTPRESPONSE with JSONObject");

                try {
                    JSONArray features = response.getJSONArray("features");
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
                    // Pull out the first event on the public timeline
//                    JSONObject firstEvent = (JSONObject)response.getJSONObject("features");
                    //                   String tweetText = firstEvent.getString("text");
                    //                  System.out.println("HTTPRESPONSE");
                    // Do something with the response
                    //                  System.out.println(tweetText);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent();
                intent.setAction("outbound.ai.outbound.SUPPLEMENTS_UPDATED");
                activity.sendBroadcast(intent);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                try {
                    // Pull out the first event on the public timeline
                    JSONObject firstEvent = (JSONObject)timeline.get(0);
                    String tweetText = firstEvent.getString("text");
                    System.out.println("HTTPRESPONSE");
                    // Do something with the response
                    System.out.println(tweetText);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getReservations()  {
        System.out.println("HTTPREQUEST");
        DownloadAreasHTTPClient.get("reservations.geojson", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                System.out.println("HTTPRESPONSE with JSONObject");

                try {
                    JSONArray features = response.getJSONArray("features");
                    List<Reservation> airspaceList = new LinkedList<Reservation>();

                    for( int i = 0; i < features.length(); i++) {

                        try {

                            JSONObject feature  = (JSONObject) features.get(i);

                            JSONObject properties  = (JSONObject) feature.get("properties");

                            Reservation a = new Reservation();
                            a.setName( properties.optString("name"));
                            a.setDescription( properties.optString("description"));
                            a.setAirspaceClass(properties.optString("airspaceclass"));
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

                    LocalData.reservations = airspaceList;
                    // Pull out the first event on the public timeline
//                    JSONObject firstEvent = (JSONObject)response.getJSONObject("features");
                    //                   String tweetText = firstEvent.getString("text");
                    //                  System.out.println("HTTPRESPONSE");
                    // Do something with the response
                    //                  System.out.println(tweetText);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent();
                intent.setAction("outbound.ai.outbound.RESERVATIONS_UPDATED");
                activity.sendBroadcast(intent);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                try {
                    // Pull out the first event on the public timeline
                    JSONObject firstEvent = (JSONObject)timeline.get(0);
                    String tweetText = firstEvent.getString("text");
                    System.out.println("HTTPRESPONSE");
                    // Do something with the response
                    System.out.println(tweetText);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getAerodromes()  {
        System.out.println("HTTPREQUEST");
        DownloadAreasHTTPClient.get("aerodromes.geojson", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                System.out.println("HTTPRESPONSE with JSONObject");

                try {
                    JSONArray features = response.getJSONArray("features");
                    List<Aerodrome> airspaceList = new LinkedList<Aerodrome>();

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
                            airspaceList.add(a);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    LocalData.aerodromes = airspaceList;
                    // Pull out the first event on the public timeline
//                    JSONObject firstEvent = (JSONObject)response.getJSONObject("features");
                    //                   String tweetText = firstEvent.getString("text");
                    //                  System.out.println("HTTPRESPONSE");
                    // Do something with the response
                    //                  System.out.println(tweetText);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent();
                intent.setAction("outbound.ai.outbound.AERODROMES_UPDATED");
                activity.sendBroadcast(intent);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                try {
                    // Pull out the first event on the public timeline
                    JSONObject firstEvent = (JSONObject)timeline.get(0);
                    String tweetText = firstEvent.getString("text");
                    System.out.println("HTTPRESPONSE");
                    // Do something with the response
                    System.out.println(tweetText);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }




    public void getAirports()  {
        System.out.println("HTTPREQUEST");
        DownloadAreasHTTPClient.get("runways.geojson", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                System.out.println("HTTPRESPONSE with JSONObject");

                try {
                    JSONArray features = response.getJSONArray("features");
                    List<Airport> airspaceList = new LinkedList<Airport>();

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
                            airspaceList.add(a);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    LocalData.airports = airspaceList;
                    // Pull out the first event on the public timeline
//                    JSONObject firstEvent = (JSONObject)response.getJSONObject("features");
                    //                   String tweetText = firstEvent.getString("text");
                    //                  System.out.println("HTTPRESPONSE");
                    // Do something with the response
                    //                  System.out.println(tweetText);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent();
                intent.setAction("outbound.ai.outbound.AIRPORTS_UPDATED");
                activity.sendBroadcast(intent);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                try {
                    // Pull out the first event on the public timeline
                    JSONObject firstEvent = (JSONObject)timeline.get(0);
                    String tweetText = firstEvent.getString("text");
                    System.out.println("HTTPRESPONSE");
                    // Do something with the response
                    System.out.println(tweetText);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }



}
