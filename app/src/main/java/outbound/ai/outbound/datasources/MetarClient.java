package outbound.ai.outbound.datasources;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.LinearLayout;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.SaxAsyncHttpResponseHandler;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.LinkedList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import outbound.ai.outbound.Constants;
import outbound.ai.outbound.LocalData;
import outbound.ai.outbound.Metar;
import outbound.ai.outbound.metar.MetarParserHandler;

public class MetarClient {

    private static AsyncHttpClient client = new AsyncHttpClient();

    private final static String TAG = "OB:DownloadAreasHTTPC";
    Context activity;

    public MetarClient(Context activity) {
        this.activity = activity;
    }

public static void get(SaxAsyncHttpResponseHandler responseHandler) {


        client.get(getDefaultURL(), responseHandler);
    }


    public void getWeather() {
        Log.i(TAG, "getWeather METAR");
        try {
            MetarClient.get(new SaxAsyncHttpResponseHandler<MetarParserHandler>(new MetarParserHandler()) {
                @Override
                public void onStart() {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, MetarParserHandler saxTreeStructure) {

                    LocalData.metars.clear();
                    for (Metar m : saxTreeStructure.getMetars()) {
                        LocalData.metars.put(m.getStation(), m);

                    }

                    Intent intent = new Intent();
                    intent.setAction(Constants.RESPONSE_ACTION);
                    intent.putExtra(Constants.PARAM_COMMAND, Constants.PARAM_GET_METARS);
                    activity.sendBroadcast(intent);

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, MetarParserHandler saxTreeStructure) {


                }

                private void debugHandler(MetarParserHandler handler) {

                }
            });

        }
        catch(Exception e) {
            e.printStackTrace();
        }
       return;
    }



        public static String getDefaultURL() {
            return "http://data.fmi.fi/fmi-apikey/62a15a9d-ae4b-489e-9816-90d852e748e8/wfs?request=getFeature&storedquery_id=fmi::avi::observations::finland::latest::iwxxm";
        }



    }