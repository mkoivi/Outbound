package outbound.ai.outbound;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import outbound.ai.outbound.datasources.AISDataClient;
import outbound.ai.outbound.datasources.AWSMetarClient;
import outbound.ai.outbound.datasources.MetarClient;

import static outbound.ai.outbound.Constants.RESPONSE_ACTION;

public class MainService extends Service implements LocationListener {

    private final static String TAG = "OB:MainService";

    private final IBinder locationTrackerBinder = new LocationTrackerBinder();
    private LocationManager locationManager;  // tracking handler
    private static MainService instance;

    private Location prevLoc = null;

    AISDataClient aisDataClient;
    MetarClient metarClient;
    AWSMetarClient awsMetarClient;

    public MainService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind ");
        return locationTrackerBinder;
    }

    public class LocationTrackerBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        LocalData.gpsLocation = location;

        postLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void postLocation(Location location) {
        Log.d(TAG, "postLocation ");

        Map<String, String> locationParameters = new HashMap<String, String>();
        locationParameters.put(Constants.PARAM_LATITUDE, "" + location.getLatitude());
        locationParameters.put(Constants.PARAM_LONGITUDE, "" + location.getLongitude());
        locationParameters.put(Constants.PARAM_BEARING, "" + location.getBearing());
        locationParameters.put(Constants.PARAM_ALTITUDE, "" + location.getAltitude());
        locationParameters.put(Constants.PARAM_ACCURACY, "" + location.getAccuracy());
        locationParameters.put(Constants.PARAM_SPEED, "" + location.getSpeed() * 3.6f); // m/s to km/h!
        locationParameters.put(Constants.PARAM_TIME, "" + location.getTime());

        locationParameters.put("extras", "" + location.getExtras());
        LocalData.gpsLocation = location;

        if( prevLoc != null) {
            LocalData.distance += HelperLibrary.distance(location.getLatitude(), prevLoc.getLatitude(),location.getLongitude(),prevLoc.getLongitude(), location.getAltitude(), prevLoc.getAltitude());
            Log.d(TAG, "distance " + LocalData.distance);
        }
        prevLoc = location;


        Intent intent = new Intent(Constants.LOCATION_UPDATE_ACTION);
        for (String param : locationParameters.keySet()) {
            intent.putExtra(param, locationParameters.get(param));
            //    	  Log.d(TAG, "param " + param + "=" + params.get(param));
        }
        MainService.this.sendBroadcast(intent);
    }


    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");

        registerReceiver(mRequestActionReceiver,
                new IntentFilter(Constants.REQUEST_ACTION));

        instance = this;
    //    initSession();

        aisDataClient = new AISDataClient(this);
        metarClient = new MetarClient(this);
        awsMetarClient = new AWSMetarClient(this);


    }


    public void initSession() {
        Log.i(TAG, "initsession called");

        String gpsProvider = getGpsProvider();
        if( gpsProvider == null) {
            Toast.makeText(this, "GPS location not supported or enabled!"
                    , Toast.LENGTH_LONG)
                    .show();
            return;
        }

        try {
            getLocationManager().requestLocationUpdates(gpsProvider, 2000, 10, this);
            Log.i(TAG, "Location tracking active");
        } catch (SecurityException e) {
            Toast.makeText(this, "GPS Location not allowed in security settings, please enable!"
                    , Toast.LENGTH_LONG)
                    .show();
            e.printStackTrace();

        }

    }

    //~ Location handling ----------------------------------------------------------------------------------------------

    private LocationManager getLocationManager() {
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }
        return locationManager;
    }

    public String getGpsProvider() {

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setCostAllowed(true);
        criteria.setBearingAccuracy(Criteria.ACCURACY_FINE);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setSpeedRequired(true);
        String provider = getLocationManager().getBestProvider(criteria, false);

        return provider;


    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");

        try {
            if (locationManager != null) {
                try {
                    locationManager.removeUpdates(this);
                } catch (SecurityException e) {
                    Toast.makeText(this, "GPS Location not allowed in security, please enable!"
                            , Toast.LENGTH_LONG)
                            .show();
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            unregisterReceiver( mRequestActionReceiver);
        }
        catch( Exception e) {
            e.printStackTrace();
        }

        super.onDestroy();

    }


    private final BroadcastReceiver mRequestActionReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    String cmd = intent.getExtras().getString(Constants.PARAM_COMMAND);
                    Log.i(TAG, "Request action: " + cmd);
                    if (cmd != null) {

                        if (cmd.equals(Constants.PARAM_GET_AIRSPACE)) {
                            aisDataClient.getAirspaces();
                        }
                        else if (cmd.equals(Constants.PARAM_GET_AIRPORTS)) {
                            aisDataClient.getAirports();
                        }
                        else if (cmd.equals(Constants.PARAM_GET_AERODROMES)) {
                            aisDataClient.getAerodromes();
                        }
                        else if (cmd.equals(Constants.PARAM_GET_SUPPLEMENTS)) {
                            aisDataClient.getSupplements();
                        }
                        else if (cmd.equals(Constants.PARAM_GET_OBSTACLES)) {
                            aisDataClient.getObstacles();
                        }
                        else if (cmd.equals(Constants.PARAM_GET_WAYPOINTS)) {
                            aisDataClient.getWaypoints();
                        }
                        else if (cmd.equals(Constants.PARAM_GET_RESERVATIONS)) {
                            aisDataClient.getReservations();
                        }
                        else if (cmd.equals(Constants.PARAM_GET_METARS)) {
                            metarClient.getWeather();
                        }
                        else if (cmd.equals(Constants.PARAM_GET_AWSMETARS)) {
                            awsMetarClient.getWeather();
                        }
                        else if (cmd.equals(Constants.PARAM_START_TRACKING)) {
                            initSession();
                        }
                        else if (cmd.equals(Constants.PARAM_STOP_TRACKING)) {
                            getLocationManager().removeUpdates(MainService.this);
                        }

                    }
                }
            };

    private void sendServiceRequest(String command) {
        Log.d(TAG, "Service request: " + command);
        Intent intent = new Intent();
        intent.setAction(RESPONSE_ACTION);
        sendBroadcast(intent);
    }

}
