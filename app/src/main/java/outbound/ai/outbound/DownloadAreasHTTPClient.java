package outbound.ai.outbound;

import android.app.Activity;
import android.content.ActivityNotFoundException;
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
        DownloadAreasHTTPClient.get("airspaces.json", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                System.out.println("HTTPRESPONSE with JSONObject");

                try {

                    JSONArray airspaces = response.getJSONArray("airspaces");
                    List<Airspace> airspaceList = new LinkedList<Airspace>();

                    for( int i = 0; i < airspaces.length(); i++) {

                        try {
                            JSONObject airspace  = (JSONObject) airspaces.get(i);

                            Airspace a = new Airspace();


                            a.setName( airspace.optString("name"));
                            a.setCallsign( airspace.optString("callsign"));
                            a.setActivity( airspace.optString("activity"));
                            a.setAirspaceClass(airspace.optString("airspaceClass"));
                            a.setRmk(airspace.optString("rmk"));
                            a.setUpper(airspace.optString("upper"));
                            a.setLower(airspace.optString("lower"));
                            JSONArray coords = airspace.getJSONArray("coordinates").getJSONArray(0);
                            List<LatLng> cs = new LinkedList<LatLng>();
                            for( int ii = 0; ii < coords.length(); ii++) {
                                JSONObject coord = (JSONObject) coords.get(ii);
                                LatLng ll = new LatLng( coord.getDouble("lat"), coord.getDouble("lng"));
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
//                    JSONObject firstEvent = (JSONObject)response.getJSONObject("airspaces");
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



}
