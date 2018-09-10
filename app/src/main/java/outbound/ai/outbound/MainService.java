package outbound.ai.outbound;

import android.app.Service;
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

public class MainService extends Service implements LocationListener {

    private final static String TAG = "OB:MainService";

    private final IBinder locationTrackerBinder = new LocationTrackerBinder();
    private LocationManager locationManager;  // tracking handler
    private static MainService instance;


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


        instance = this;
        initSession();


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
            getLocationManager().requestLocationUpdates(gpsProvider, 1000, 1, this);
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



        super.onDestroy();

    }

}
