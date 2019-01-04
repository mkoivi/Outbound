package outbound.ai.outbound.datasources;

import android.app.Activity;
import android.content.Context;
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
import cz.msebera.android.httpclient.ParseException;
import outbound.ai.outbound.Aerodrome;
import outbound.ai.outbound.Airport;
import outbound.ai.outbound.Airspace;
import outbound.ai.outbound.CloudLayer;
import outbound.ai.outbound.Constants;
import outbound.ai.outbound.LocalData;
import outbound.ai.outbound.Metar;
import outbound.ai.outbound.Obstacle;
import outbound.ai.outbound.Reservation;
import outbound.ai.outbound.Runway;
import outbound.ai.outbound.Supplement;
import outbound.ai.outbound.Waypoint;

public class AWSMetarClient {


    private final static String TAG = "OB:DownloadAreasHTTPC";
    Context activity;

    public AWSMetarClient(Context activity) {
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

                                // station ILHF
                                m.setStation(st.nextToken());

                                // time, 260740Z
                                String time = st.nextToken();
                                m.setTime(time.substring(2, 4) + time.substring(4, 6));

                                while (st.hasMoreTokens()) {
                                    String next = st.nextToken();

                                    if (next.endsWith("CB")) {
                                        m.setCb(true);
                                    }

                                    if( next.equals("AUTO"))
                                        continue;
                                    if( next.equals("NIL"))
                                        break;
                                    if( next.equals("RMK"))
                                        continue;
                                    if( next.contains("////"))
                                        continue;

                                    // wind
                                    if (next.contains("KT")) {
                                        if (!next.contains("VRB"))
                                            m.setMeanWindDirection(Integer.parseInt(next.substring(0, 3)));
                                        m.setMeanWindSpeed(Integer.parseInt(next.substring(3, 5)));
                                        if (next.contains("G")) {
                                            m.setGustWindSpeed(Integer.parseInt(next.substring(6, 8)));
                                        }
                                    }
                                    if (next.equals("CAVOK"))
                                        m.setCavok(true);

                                    // visibility: this is a number=parse integer works
                                    try {
                                        m.setVisibility( Integer.parseInt(next));
                                    }
                                    catch (NumberFormatException e) {
                                    }

                                    if (next.contains("FEW") || next.contains("SCT") || next.contains("BKN") || next.contains("OVC")) {
                                        try {
                                            CloudLayer mc = new CloudLayer();
                                            mc.layerType = next.substring(0, 3);
                                            mc.baseHeight = Integer.parseInt(next.substring(3));
                                            m.getClouds().add(mc);
                                        }
                                        catch (NumberFormatException e) {
                                            e.printStackTrace();
                                        }

                                    } else if (next.startsWith("VV")) {
                                        try {
                                            CloudLayer mc = new CloudLayer();
                                            mc.layerType = "VV";
                                            mc.baseHeight = Integer.parseInt(next.substring(2));
                                            m.getClouds().add(mc);
                                        }
                                        catch (NumberFormatException e) {
                                            e.printStackTrace();
                                        }
                                    } else if (next.startsWith("Q") && !next.contains("/")) {
                                        String qnh = next;
                                        qnh = qnh.replace("Q", "");
                                        qnh = qnh.replace("=", "");
                                        m.setQnh(Integer.parseInt(qnh));
                                    }
                                    else if (next.replaceAll("M","").length() == 5 && next.replaceAll("M","").lastIndexOf("/")== 2) {
                                        String temp = next.substring(0, next.indexOf("/"));
                                        temp = temp.replace("M", "-");
                                        m.setTemp(Integer.parseInt(temp));

                                        String dew = next.substring(next.indexOf("/") + 1);
                                        dew = dew.replace("M", "-");
                                        m.setDewPoint(Integer.parseInt(dew));
                                    } else if (!next.contains("/"))
                                        m.setPresentWeather(next);



                                }
                                LocalData.awsmetars.put(m.getStation(),m);
                            }
                            catch (Exception e ) {
                                System.err.println("Error parsing feature" );
                                e.printStackTrace();
                            }


                            //        updateObstaclesData(features);
                        }

                      Intent intent = new Intent();
                    intent.setAction(Constants.RESPONSE_ACTION);
                    intent.putExtra(Constants.PARAM_COMMAND, Constants.PARAM_GET_AWSMETARS);
                    activity.sendBroadcast(intent);

                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
