package outbound.ai.outbound;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class LocalData {

    private static final String TAG = "OB:LocalData";

    private static Context mContext = null;

    // Singleton instance
    private static LocalData sInstance;


    public LocalData( Context ctx) {
        this.mContext = ctx;
    }


    public static LocalData getInstance(Context context) {

        if (sInstance == null) {
            sInstance = new LocalData(context);
        }

        return sInstance;
    }


    public List<LatLng> track = new LinkedList<>();

    public static List<Airspace> airspaces= new LinkedList<>();
    public static long airspacesUpdated = 0;

    public static List<Supplement> supplements= new LinkedList<>();
    public static long supplementsUpdated = 0;

    public static HashMap<String, Airport> airports= new HashMap<>();
    public static long airportsUpdated = 0;

    public static List<Reservation> reservations= new LinkedList<>();
    public static long reservationsUpdated = 0;

    public static HashMap<String, Aerodrome> aerodromes= new HashMap<>();
    public static long aerodromesUpdated = 0;

    public static List<Waypoint> waypoints= new LinkedList<>();
    public static long waypointsUpdated = 0;

    public static List<Obstacle> obstacles = new LinkedList<>();
    public static long obstaclesUpdated = 0;

    public static double distance = 0;

    public static HashMap<String,Metar> metars = new HashMap<>();
    public static HashMap<String, Metar> awsmetars = new HashMap<>();

    public static Location gpsLocation;

    public static Location getGPSLocation() {
        return gpsLocation;
    }


    public String getPreference( Context c, String key) {
        try {
            SharedPreferences sharedPref = c.getSharedPreferences(Constants.PREFERENCES_KEY, Context.MODE_PRIVATE);
            return sharedPref.getString(key, null);

        }
        catch( Exception e ) {
            Log.e(TAG, "Could not read value from prefs: " + key);
        }
        return null;
    }

    public String getPreference(  String key) {
        try {
            SharedPreferences sharedPref = mContext.getSharedPreferences(Constants.PREFERENCES_KEY, Context.MODE_PRIVATE);
            return sharedPref.getString(key, null);

        }
        catch( Exception e ) {
            Log.e(TAG, "Could not read value from prefs: " + key);
        }
        return null;
    }


    public void setPreference(String key, String value) {

        SharedPreferences sharedPref = mContext.getSharedPreferences(
                Constants.PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
        Log.d(TAG, "Preference saved: "  + key + " = "+ value);

    }

    public void setPreference(String key, int value) {

        SharedPreferences sharedPref = mContext.getSharedPreferences(
                Constants.PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.commit();
        Log.d(TAG, "Preference saved: "  + key + " = "+ value);

    }

    public void setPreference(String key, boolean value) {

        SharedPreferences sharedPref = mContext.getSharedPreferences(
                Constants.PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.commit();
        Log.d(TAG, "Preference saved: "  + key + " = "+ value);

    }


    public void storeStringData( String id, String data) {


        FileOutputStream outputStream;

        try {
            outputStream = mContext.openFileOutput(id, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

     public String loadStringData(String id) {

        String ret = "";

        try {
            InputStream inputStream = mContext.openFileInput(id);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }


    public long getFileAge( String id) {
        File file = mContext.getApplicationContext().getFileStreamPath(id);
        if( !file.exists()) {
            return -1;
        }
        return( new Date().getTime()- file.lastModified());
    }



    public List<LatLng> getTrack() {
        return track;
    }

    public void setTrack(List<LatLng> track) {
        this.track = track;
    }

    public void addTrackLocation( LatLng ll) {
        track.add(ll);
    }


}
