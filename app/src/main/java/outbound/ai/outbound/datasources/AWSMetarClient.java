package outbound.ai.outbound.datasources;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.data.ExclusionFilteredDataBuffer;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.KeyStore;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import cz.msebera.android.httpclient.Header;
import outbound.ai.outbound.Aerodrome;
import outbound.ai.outbound.Airport;
import outbound.ai.outbound.Airspace;
import outbound.ai.outbound.CloudLayer;
import outbound.ai.outbound.LocalData;
import outbound.ai.outbound.Metar;
import outbound.ai.outbound.Obstacle;
import outbound.ai.outbound.Reservation;
import outbound.ai.outbound.Runway;
import outbound.ai.outbound.Supplement;
import outbound.ai.outbound.Waypoint;

public class AWSMetarClient {


    private final static String TAG = "OB:DownloadAreasHTTPC";
    Activity activity;

    public AWSMetarClient(Activity activity) {
        this.activity = activity;
        client = new AsyncHttpClient();
        try {
            /// We initialize a default Keystore
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
// We load the KeyStore
            trustStore.load(null, null);
// We initialize a new SSLSocketFacrory
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
// We set that all host names are allowed in the socket factory
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
// We initialize the Async Client
            AsyncHttpClient client = new AsyncHttpClient();
// We set the timeout to 30 seconds
            client.setTimeout(30 * 1000);
// We set the SSL Factory
            client.setSSLSocketFactory(socketFactory);
        }
        catch( Exception e) {
            e.printStackTrace();
        }
    }




    private static AsyncHttpClient client = null;

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(url, params, responseHandler);
    }



    public void getWeather() {
        Log.i(TAG, "getWeather AWS");
        final String url = "https://www.ilmailusaa.fi/backend.php?{%22mode%22:%22autometar%22,%22radius%22:%22100%22,%22points%22:[{%22_area%22:%221%22}]}";

             try {

            AWSMetarClient.get(url, null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    System.out.println("return AWS metar data");

                        JSONArray features = response.names();
                        for(int i = 0; i < features.length();i++) {

                            try {
                                JSONObject obj = (JSONObject) response.get(features.get(i).toString());
                                System.out.println( obj.toString());
                                Metar m = new Metar();
                                m.setLocation(new LatLng(Double.parseDouble(obj.getString("lat")), Double.parseDouble(obj.getString("lon"))));
                                m.setTitle(obj.getString("coordinate"));
                                m.setMessage(obj.getString("p1"));
                                String msg = obj.getString("p1");
                                StringTokenizer st = new StringTokenizer(msg, " ");
                                m.setStation(st.nextToken());
                                String time = st.nextToken();
                                m.setTime(time.substring(2, 4) + time.substring(4, 6));
                                st.nextToken(); // AUTO
                                String wind = st.nextToken();
                                if (!wind.contains("/")) {
                                    if (!wind.contains("VRB"))
                                        m.setMeanWindDirection(Integer.parseInt(wind.substring(0, 3)));
                                    m.setMeanWindSpeed(Integer.parseInt(wind.substring(3, 5)));
                                    if (wind.contains("G")) {
                                        m.setGustWindSpeed(Integer.parseInt(wind.substring(6, 8)));
                                    }
                                }

                                String vis = st.nextToken();
                                if (vis.equals("CAVOK"))
                                    m.setCavok(true);
                                else if (!vis.contains("/"))
                                    m.setVisibility(Integer.parseInt(vis));
                                while (st.hasMoreTokens()) {
                                    String next = st.nextToken();
                                    if (next.contains("FEW") || next.contains("SCT") || next.contains("BKN") || next.contains("OVC")) {
                                        CloudLayer mc = new CloudLayer();
                                        mc.layerType = next.substring(0, 3);
                                        mc.baseHeight = Integer.parseInt(next.substring(3));
                                        m.getClouds().add(mc);
                                    } else if (next.startsWith("Q") && !next.contains("/")) {
                                        String qnh = next;
                                        qnh = qnh.replace("Q", "");
                                        qnh = qnh.replace("=", "");
                                        m.setQnh(Integer.parseInt(qnh));
                                    } else if (next.length() == 5 && next.contains("/")) {
                                        String temp = next.substring(0, next.indexOf("/"));
                                        temp = temp.replace("M", "-");
                                        m.setTemp(Integer.parseInt(temp));

                                        String dew = next.substring(next.indexOf("/") + 1);
                                        dew = dew.replace("M", "-");
                                        m.setDewPoint(Integer.parseInt(dew));
                                    } else if (!next.contains("/"))
                                        m.setPresentWeather(next);

                                    if (next.endsWith("CB")) {
                                        m.setCb(true);
                                    }

                                }
                                LocalData.awsmetars.put(m.getStation(),m);
                            }
                            catch (Exception e ) {
                                System.err.println("Error parsing feature" );
                                e.printStackTrace();
                            }


                            //        updateObstaclesData(features);
                        }


                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
