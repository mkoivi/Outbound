package outbound.ai.outbound;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import outbound.ai.outbound.datasources.AISDataClient;
import outbound.ai.outbound.datasources.AWSMetarClient;
import outbound.ai.outbound.datasources.MetarClient;

import static outbound.ai.outbound.Constants.PARAM_COMMAND;
import static outbound.ai.outbound.Constants.PARAM_GET_AERODROMES;
import static outbound.ai.outbound.Constants.PARAM_GET_AIRPORTS;
import static outbound.ai.outbound.Constants.PARAM_GET_AIRSPACE;
import static outbound.ai.outbound.Constants.PARAM_GET_AWSMETARS;
import static outbound.ai.outbound.Constants.PARAM_GET_METARS;
import static outbound.ai.outbound.Constants.PARAM_GET_OBSTACLES;
import static outbound.ai.outbound.Constants.PARAM_GET_RESERVATIONS;
import static outbound.ai.outbound.Constants.PARAM_GET_SUPPLEMENTS;
import static outbound.ai.outbound.Constants.PARAM_GET_WAYPOINTS;
import static outbound.ai.outbound.Constants.REQUEST_ACTION;

public class MenuActivity extends AppCompatActivity implements OnMapReadyCallback,  ActivityCompat.OnRequestPermissionsResultCallback ,GoogleMap.OnCameraMoveListener,GoogleMap.OnCameraIdleListener,GoogleMap.OnPolygonClickListener ,  GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {


    private final static String TAG = "OB:MenuActivity";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;


    static HashMap<String, Bitmap> markerCache = new HashMap<>();

    boolean gettingWeatherData = false;
    boolean weatherDataReady = false;

    Menu headerMenu;

    List<Marker> airspaceMarkers = new LinkedList<>();
    List<Marker> airportMarkers = new LinkedList<Marker>();
    List<Marker> aerodromeMarkers = new LinkedList<Marker>();
    List<Marker> waypointMarkers = new LinkedList<>();
    List<Marker> obstacleMarkers = new LinkedList<>();
    List<Marker> awsMarkers = new LinkedList<>();


    List<BroadcastReceiver> receivers = new LinkedList<>();

    private CameraPosition prevCameraPosition = null;
    private TextView distanceTF;

    boolean followingUser = false;

    public final float WAYPOINT_ZOOM_LIMIT = 8f;
    public final float AIRSPACE_LABEL_ZOOM_LIMIT = 9.6f;
    public final float OBSTACLE_ZOOM_LIMIT = 9.3f;
    private ConstraintLayout aerodromeView;
    private LinearLayout flightInfo;

    private LatLng mapLoc; // current map 'target'
    private Location loc;
    private LatLng prevLl;

    private Marker myPlaneMarker = null;
    private boolean flightMode = false;

    Chronometer flightChronometer;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //                mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    //                  mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
//                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };
    private FloatingActionButton myLocButton;
    private ToggleButton weatherToggleButton;

    private ProgressBar progressBar;

    List<Polyline> track = new LinkedList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        aerodromeView = (ConstraintLayout) findViewById(R.id.aerodrome);
        aerodromeView.setVisibility(View.GONE);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        //     infoLayout = (ConstraintLayout) findViewById(R.id.in);
        //     infoLayout.setVisibility(View.GONE);

        flightInfo = (LinearLayout) findViewById(R.id.flight_info);
        flightInfo.setVisibility(View.GONE);

        Switch onOffSwitch = (Switch)  findViewById(R.id.metarSwitch);
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showMetar(isChecked);
            }

        });

        distanceTF = (TextView) findViewById(R.id.distance);

        myLocButton = (FloatingActionButton) findViewById(R.id.myLocation);
        myLocButton.setVisibility(View.INVISIBLE);

      //  weatherToggleButton = (ToggleButton) findViewById(R.id.toggleWeather);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        registerReceiver(mResponseActionReceiver,
                new IntentFilter(Constants.RESPONSE_ACTION));

        startMainServiceIfNotStarted();

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng deflt = new LatLng(61.2, 24.4);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(deflt, 6.8f));
        mMap.setOnPolygonClickListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnMapClickListener(this);
        //     mMap.setOnMyLocationButtonClickListener(this);
        //     mMap.setOnMyLocationClickListener(this);
        //     enableMyLocation();

        GoogleMapOptions options = new GoogleMapOptions();
        options.compassEnabled(true)
                .rotateGesturesEnabled(true)
                .tiltGesturesEnabled(false).zOrderOnTop(false).zoomGesturesEnabled(true).zoomControlsEnabled(true);


        mMap.setOnMarkerClickListener(this);

        setupData();

    }


 /*   private void getWeatherData() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                IntentFilter filter8 = new IntentFilter("outbound.ai.outbound.METAR_UPDATED");
                BroadcastReceiver receiver8 = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        System.out.println("Received metar data");

                        awsMetarClient.getWeather();


                    }
                };
                registerReceiver(receiver8, filter8);

                IntentFilter filter9 = new IntentFilter("outbound.ai.outbound.AWS_METAR_UPDATED");
                BroadcastReceiver receiver9 = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        System.out.println("Received AWS metar data");
                        updateWeather();
                        updateAirports();
                        updateAerodromes();
                        weatherDataReady = true;
                        gettingWeatherData = false;
                        weatherToggleButton.setImageResource(R.drawable.cloud_lighted);

                    }
                };
                registerReceiver(receiver9, filter9);
                metarClient.getWeather();
            }
        }
        );
    }*/

    private void sendServiceRequest(String command) {
        Log.d(TAG, "Service request: " + command);
        Intent intent = new Intent();
        intent.setAction(REQUEST_ACTION);
        intent.putExtra(PARAM_COMMAND, command);
        sendBroadcast(intent);
    }

    private void setupData() {

        final Handler handler0 = new Handler();
        handler0.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendServiceRequest(PARAM_GET_AIRSPACE);
            }
        }, 500);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendServiceRequest(PARAM_GET_SUPPLEMENTS);
            }
        }, 1200);
        final Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendServiceRequest(PARAM_GET_AERODROMES);
            }
        }, 2000);
        final Handler handler3 = new Handler();
        handler3.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendServiceRequest(PARAM_GET_AIRPORTS);
            }
        }, 3000);
        final Handler handler4 = new Handler();
        handler4.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendServiceRequest(PARAM_GET_RESERVATIONS);
            }
        }, 4000);
        final Handler handler5 = new Handler();
        handler5.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendServiceRequest(PARAM_GET_WAYPOINTS);
            }
        }, 5000);
        final Handler handler6 = new Handler();
        handler6.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendServiceRequest(PARAM_GET_OBSTACLES);
                progressBar.setVisibility(View.GONE);
            }
        }, 6000);


    }

    private final BroadcastReceiver mResponseActionReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    String cmd = intent.getExtras().getString(Constants.PARAM_COMMAND);
                    Log.d(TAG, "Response: " + cmd);
                    if (cmd != null) {

                        if (cmd.equals(Constants.PARAM_GET_AIRSPACE)) {
                            updateAirspace();
                        } else if (cmd.equals(Constants.PARAM_GET_SUPPLEMENTS)) {
                            updateSupplements();
                        } else if (cmd.equals(Constants.PARAM_GET_AERODROMES)) {
                            updateAerodromes();
                        } else if (cmd.equals(Constants.PARAM_GET_AIRPORTS)) {
                            updateAirports();
                        } else if (cmd.equals(Constants.PARAM_GET_RESERVATIONS)) {
                            updateReservations();
                        } else if (cmd.equals(Constants.PARAM_GET_WAYPOINTS)) {
                            updateWaypoints();
                        } else if (cmd.equals(Constants.PARAM_GET_OBSTACLES)) {
                            updateObstacles();
                        }
                         else if (cmd.equals(Constants.PARAM_GET_METARS)) {
                              // No action, wait for AWS metars
                        }
                        else if (cmd.equals(Constants.PARAM_GET_AWSMETARS)) {
                            updateWeather();
                        }

                    }
                }
            };



    public void updateAirspace() {
        for (Marker m : airspaceMarkers) {
            m.remove();
        }
        airspaceMarkers.clear();

        for (Airspace a : LocalData.airspaces) {

            if (a.getAirspaceClass().equals("Danger") && !a.isActive())
                continue;


            PolygonOptions rectOptions = new PolygonOptions();

            //      if( !a.getLower().startsWith("FL")) {
            //         rectOptions.fillColor(Color.argb(100,255,160,90));
            //      }
            if ((a.getAirspaceClass().equals("A") || a.getAirspaceClass().equals("B") || a.getAirspaceClass().equals("C") || a.getAirspaceClass().equals("D")) && !a.getLower().contains("FL")) {
                rectOptions.fillColor(Color.argb(50, 255, 160, 90));
            }
            if ((a.getAirspaceClass().equals("Prohibited"))) {
                rectOptions.fillColor(Color.argb(160, 255, 0, 0));
            }
            if ((a.getAirspaceClass().equals("Restricted") && a.getActivity().equals("H24") || (a.getAirspaceClass().equals("Restricted") && a.isActive()))) {
                rectOptions.fillColor(Color.argb(90, 255, 0, 0));
            }
            if ((a.getActivityType().equals("NO-NOISE"))) {
                rectOptions.fillColor(Color.argb(100, 100, 100, 100));
            }
            rectOptions.strokeWidth(3.5f);

            double centerLat = 0;
            double centerLng = 0;
            int counter = 0;
            LatLng prevLL = null;
            LatLng center = a.getCenter();
            for (LatLng ll : a.getCoordinates()) {
                rectOptions.add(ll);
                centerLat += ll.latitude;
                centerLng += ll.longitude;
                if (counter > 0 && HelperLibrary.distance(ll, prevLL) > 6000 && !a.getAirspaceClass().equals("Danger") && !a.getAirspaceClass().equals("Restricted")) {
                    Marker m = addAirspaceMarker(new LatLng((ll.latitude + prevLL.latitude) / 2, (ll.longitude + prevLL.longitude) / 2), a.getAirspaceClass(), a.getUpper(), a.getLower(), a.getName());
                    float ax = 0.5f;
                    float ay = 0.5f;
                    if (center.longitude < ll.longitude)
                        ax = 1f;
                    else
                        ax = 0f;
                    if (center.latitude < ll.latitude)
                        ay = 0f;
                    else
                        ay = 1f;

                    m.setAnchor(ax, ay);
                    airspaceMarkers.add(m);

                }

                prevLL = ll;
                counter++;
            }
            centerLat = centerLat / a.getCoordinates().size();
            centerLng = centerLng / a.getCoordinates().size();

            Polygon polygon = mMap.addPolygon(rectOptions);
            polygon.setTag(a.name);
            polygon.setClickable(true);


            airspaceMarkers.add(addAirspaceMarker(new LatLng(centerLat, centerLng), a.getAirspaceClass(), a.getUpper(), a.getLower(), a.getName()));

        }

    }


    public void updateSupplements() {

        for (Supplement a : LocalData.supplements) {
            PolygonOptions rectOptions = new PolygonOptions();

            //      if( !a.getLower().startsWith("FL")) {
            //         rectOptions.fillColor(Color.argb(100,255,160,90));
            //      }

            if (a.isActive())
                rectOptions.fillColor(Color.argb(70, 255, 160, 90));

            rectOptions.strokeWidth(3.5f);

            double centerLat = 0;
            double centerLng = 0;
            int counter = 0;
            LatLng prevLL = null;
            for (LatLng ll : a.getCoordinates()) {
                rectOptions.add(ll);
                centerLat += ll.latitude;
                centerLng += ll.longitude;
                if (counter > 0 && HelperLibrary.distance(ll, prevLL) > 6000 && a.isActive())
                    airspaceMarkers.add(addAirspaceMarker(new LatLng((ll.latitude + prevLL.latitude) / 2, (ll.longitude + prevLL.longitude) / 2), a.getAirspaceClass(), a.getUpper(), a.getLower(), a.getName()));
                prevLL = ll;
                counter++;
            }
            centerLat = centerLat / a.getCoordinates().size();
            centerLng = centerLng / a.getCoordinates().size();

            Polygon polygon = mMap.addPolygon(rectOptions);
            polygon.setTag(a.name);
            polygon.setClickable(true);

            airspaceMarkers.add(addAirspaceMarker(new LatLng(centerLat, centerLng), a.getAirspaceClass(), a.getUpper(), a.getLower(), a.getName()));

        }
    }

    public void updateReservations() {

        for (Reservation a : LocalData.reservations) {

            if (!a.isActive())
                continue;

            PolygonOptions rectOptions = new PolygonOptions();

            if (a.isActive()) // TODO: set configurable lower setting
                rectOptions.fillColor(Color.argb(20, 0, 0, 200));

            rectOptions.strokeWidth(3.5f);

            double centerLat = 0;
            double centerLng = 0;
            int counter = 0;
            LatLng prevLL = null;
            for (LatLng ll : a.getCoordinates()) {
                rectOptions.add(ll);
                centerLat += ll.latitude;
                centerLng += ll.longitude;
                if (counter > 0 && HelperLibrary.distance(ll, prevLL) > 6000 && a.isActive())
                    airspaceMarkers.add(addAirspaceMarker(new LatLng((ll.latitude + prevLL.latitude) / 2, (ll.longitude + prevLL.longitude) / 2), a.getAirspaceClass(), a.getUpper(), a.getLower(), a.getName()));
                prevLL = ll;
                counter++;
            }
            centerLat = centerLat / a.getCoordinates().size();
            centerLng = centerLng / a.getCoordinates().size();

            Polygon polygon = mMap.addPolygon(rectOptions);
            polygon.setTag(a.name);
            polygon.setClickable(true);

            airspaceMarkers.add(addAirspaceMarker(new LatLng(centerLat, centerLng), a.getAirspaceClass(), a.getUpper(), a.getLower(), a.getName()));

        }
    }


    public void updateAirports() {

        for (Marker m : airportMarkers) {
            m.remove();
        }
        airportMarkers.clear();

        for (Airport a : LocalData.airports.values()) {

            Metar metar = LocalData.metars.get(a.getCode());


            List<Runway> rs = a.getRunways();
            for (Runway r : rs) {
                try {

                    int rotation = 0;
                    rotation = Integer.parseInt(r.getNames().get(0).substring(0, 2) + "0") + 7;
                    if( metar != null) {
                        int windSpeed = metar.getMeanWindSpeed();
                        if( metar.getGustWindSpeed() > 0 ) {
                            windSpeed = metar.getGustWindSpeed();
                        }
                        airportMarkers.add(addAerodromeMarker(a.getCenter(), a.getCode(), rotation, a.getCode(), true, metar.getMeanWindDirection(), windSpeed, metar.getVisibility(), metar.getCloudBase(), metar.getQnh(), metar.isCb()));
                    }
                    else {
                        airportMarkers.add(addAerodromeMarker(a.getCenter(), a.getCode(), rotation, a.getCode(), false, 0, 0, 0, 0,0, false));

                   }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        //    airportMarkers.add(addAerodromeMarker(a.getCenter(), a.getCode(), 90, a.getCode(), false, 0, 0, 0, 0));
        }
    }


    public void updateAerodromes() {
        for (Marker m : aerodromeMarkers) {
            m.remove();
        }
        aerodromeMarkers.clear();
        for (Aerodrome a : LocalData.aerodromes.values()) {

            List<Runway> rs = a.getRunways();
            for (Runway r : rs) {
                try {
                    int rotation = 0;
                    rotation = Integer.parseInt(r.id.substring(0, 2) + "0") + 7;
                    aerodromeMarkers.add(addAerodromeMarker(a.getCenter(), a.getCode(), rotation, a.getCode(), false, 0, 0, 0, 0,0, false));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }


    public void updateWaypoints() {
        for (Marker m : waypointMarkers) {
            m.remove();
        }
        waypointMarkers.clear();
        for (Waypoint a : LocalData.waypoints) {
            waypointMarkers.add(addWaypointMarker(a.getCenter(), a.getName()));
        }
    }

    public void updateObstacles() {
        for (Marker m : obstacleMarkers) {
            m.remove();
        }
        obstacleMarkers.clear();
        for (Obstacle a : LocalData.obstacles) {
            obstacleMarkers.add(addObstacleMarker(a.getCenter(), a.getName(), a.getType(), a.getElevation()));

        }
    }

    private void clearWeatherData() {
        for (Marker m : awsMarkers) {
            m.remove();
        }
        awsMarkers.clear();
        LocalData.awsmetars.clear();
        LocalData.metars.clear();
        updateAerodromes();
        updateAirports();


    }


    private void updateWeather() {

        for (Marker m : awsMarkers) {
            m.remove();
        }
        awsMarkers.clear();

        for( Metar metar: LocalData.awsmetars.values()) {

            int windSpeed = metar.getMeanWindSpeed();
            if( metar.getGustWindSpeed() > 0 ) {
                windSpeed = metar.getGustWindSpeed();
            }
            awsMarkers.add(addAWSMarker(metar.getLocation(), metar.getStation(), metar.getMeanWindDirection(), windSpeed, metar.getVisibility(), metar.getCloudBase(), metar.getQnh(), metar.getMessage(), metar.isCb()));
        }
        updateAirports();
        updateAerodromes();
        weatherDataReady = true;
        gettingWeatherData = false;
   //     weatherToggleButton.setImageResource(R.drawable.cloud_lighted);
    }

    @Override
    public void onPolygonClick(Polygon p) {
        // Flip from solid stroke to dotted stroke pattern.
        System.out.println("clicked polygon " + p.getTag().toString());
        hideAerodromeView();
        //Toast.makeText(this, ""+ p.getTag().toString(),
        //        Toast.LENGTH_SHORT).show();
    }


    public Marker addAirspaceMarker(LatLng pos, String cls, String upper, String lower, String tooltipTitle) {

        upper = upper.replace("msl", "");
        lower = lower.replace("msl", "");
        lower = lower.replace("0agl", "SFC");
        upper = upper.replace("FT MSL", "");
        lower = lower.replace("FT MSL", "");
        cls = cls.replace("Prohibited", "P");
        cls = cls.replace("Restricted", "R");

        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.airspace_label, null);
        TextView numTxt = (TextView) marker.findViewById(R.id.class_txt);
        numTxt.setText(cls);
        TextView upTxt = (TextView) marker.findViewById(R.id.upper_txt);
        upTxt.setText(upper);
        TextView loTxt = (TextView) marker.findViewById(R.id.lower_txt);
        loTxt.setText(lower);
        Marker customMarker = mMap.addMarker(new MarkerOptions()
                .position(pos)
                .title(tooltipTitle)
                .snippet("Details")
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker, "as_" + cls + upper + lower, true))));


        final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        if (mMap.getCameraPosition().zoom < AIRSPACE_LABEL_ZOOM_LIMIT)
            customMarker.setVisible(false);
        /*    if (mapView.getViewTreeObserver().isAlive()) {
                mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @SuppressLint("NewApi")
                    // We check which build version we are using.
                    @Override
                    public void onGlobalLayout() {
                        LatLngBounds bounds = new LatLngBounds.Builder().include(markerLatLng).build();
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                    }
                });
            }*/
        return customMarker;
    }

    public Marker addAerodromeMarker(LatLng pos, String title, int runwayDirection, String code, boolean weatherEnabled, int windDirection, int windSpeed, int visibility, int cloudBase, int qnh, boolean cb) {

        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.aerodrome_symbol, null);
        TextView numTxt = (TextView) marker.findViewById(R.id.num_txt);
        numTxt.setText(title);
        ImageView runwaySym = marker.findViewById(R.id.runway_symbol);
        runwaySym.setRotation(runwayDirection);

        if( weatherEnabled ) {
            ((LinearLayout) marker.findViewById(R.id.weather)).setVisibility(View.VISIBLE);
            ImageView windDir = marker.findViewById(R.id.wind_direction);
            windDir.setRotation(windDirection);

            if (windSpeed < 5)
                windDir.setColorFilter(Color.argb(255, 0, 255, 255));
            if (windSpeed >= 5)
                windDir.setColorFilter(Color.GREEN);
            if (windSpeed >= 10)
                windDir.setColorFilter(Color.YELLOW);
            if (windSpeed >=15)
                windDir.setColorFilter(Color.argb(255, 255, 150, 0));
            if (windSpeed >= 20)
                windDir.setColorFilter(Color.RED);
            if (visibility == -1)
                windDir.setColorFilter(Color.GRAY);



            ImageView visi = marker.findViewById(R.id.visibility);
            if (visibility == 9999) visi.setColorFilter(Color.WHITE);
            if (visibility < 9999)
                visi.setColorFilter(Color.argb(255, 0, 255, 255));
            if (visibility < 8000)
                visi.setColorFilter(Color.GREEN);
            if (visibility < 5000)
                visi.setColorFilter(Color.YELLOW);
            if (visibility < 3000)
                visi.setColorFilter(Color.argb(255, 255, 150, 0));
            if (visibility < 1500)
                visi.setColorFilter(Color.RED);
            if (visibility == -1)
                visi.setColorFilter(Color.GRAY);


            ImageView clo = marker.findViewById(R.id.clouds);
            if (cloudBase >= 50 ) clo.setColorFilter(Color.WHITE);
            if (cloudBase < 50 )
                clo.setColorFilter(Color.argb(255, 0, 255, 255));
            if (cloudBase < 20)
                clo.setColorFilter(Color.GREEN);
            if (cloudBase < 15)
                clo.setColorFilter(Color.YELLOW);
            if (cloudBase < 10)
                clo.setColorFilter(Color.argb(255, 255, 150, 0));
            if (cloudBase <= 5)
                clo.setColorFilter(Color.RED);
            if (cloudBase == -1)
                clo.setColorFilter(Color.GRAY);

            TextView qnhT = (TextView) marker.findViewById(R.id.qnhad);
            qnhT.setText(""+qnh);

            ImageView cbV = marker.findViewById(R.id.cb);
            if( cb)
                cbV.setVisibility(View.VISIBLE);

        }
        else {
            ((LinearLayout) marker.findViewById(R.id.weather)).setVisibility(View.GONE);
        }


        Marker customMarker = mMap.addMarker(new MarkerOptions()
                .position(pos)
                .title(code)
                .snippet("Details")
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker, "ad_" + title + runwayDirection, false))));


        final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();

        /*    if (mapView.getViewTreeObserver().isAlive()) {
                mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @SuppressLint("NewApi")
                    // We check which build version we are using.
                    @Override
                    public void onGlobalLayout() {
                        LatLngBounds bounds = new LatLngBounds.Builder().include(markerLatLng).build();
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                    }
                });
            }*/
        return customMarker;
    }



    public Marker addAWSMarker(LatLng pos, String title, int windDirection, int windSpeed, int visibility, int cloudBase, int qnh, String message, boolean cb) {

        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.aws_symbol, null);
    //    TextView numTxt = (TextView) marker.findViewById(R.id.num_txt);
    //    numTxt.setText(title);

            ((LinearLayout) marker.findViewById(R.id.weather)).setVisibility(View.VISIBLE);
            ImageView windDir = marker.findViewById(R.id.wind_direction);

            if( windDirection == -1) {
                windDir.setVisibility(View.GONE);
            }
            else
                windDir.setRotation(windDirection);

            if (windSpeed < 5)
                windDir.setColorFilter(Color.argb(255, 0, 255, 255));
            if (windSpeed >= 5)
                windDir.setColorFilter(Color.GREEN);
            if (windSpeed >= 10)
                windDir.setColorFilter(Color.YELLOW);
            if (windSpeed >=15)
                windDir.setColorFilter(Color.argb(255, 255, 150, 0));
            if (windSpeed >= 20)
                windDir.setColorFilter(Color.RED);
            if (visibility == -1)
                windDir.setColorFilter(Color.GRAY);

            ImageView visi = marker.findViewById(R.id.visibility);
            if (visibility == 9999) visi.setColorFilter(Color.WHITE);
            if (visibility < 9999)
                visi.setColorFilter(Color.argb(255, 0, 255, 255));
            if (visibility < 8000)
                visi.setColorFilter(Color.GREEN);
            if (visibility < 5000)
                visi.setColorFilter(Color.YELLOW);
            if (visibility < 3000)
                visi.setColorFilter(Color.argb(255, 255, 150, 0));
            if (visibility < 1500)
                visi.setColorFilter(Color.RED);
            if (visibility == -1)
                visi.setColorFilter(Color.GRAY);


            ImageView clo = marker.findViewById(R.id.clouds);
            if (cloudBase >= 50 ) clo.setColorFilter(Color.WHITE);
            if (cloudBase < 50 )
                clo.setColorFilter(Color.argb(255, 0, 255, 255));
            if (cloudBase < 20)
                clo.setColorFilter(Color.GREEN);
            if (cloudBase < 15)
                clo.setColorFilter(Color.YELLOW);
            if (cloudBase < 10)
                clo.setColorFilter(Color.argb(255, 255, 150, 0));
            if (cloudBase <= 5)
                clo.setColorFilter(Color.RED);
            if (cloudBase == -1)
                clo.setColorFilter(Color.GRAY);

            TextView qnhT = marker.findViewById(R.id.qnh_aws);
            if( qnh == -1)
                qnhT.setVisibility(View.GONE);
            else
                qnhT.setText(""+qnh);

        ImageView cbV = marker.findViewById(R.id.cb);
        if( cb)
            cbV.setVisibility(View.VISIBLE);

        Marker customMarker = mMap.addMarker(new MarkerOptions()
                .position(pos)
                .title(message)
                .snippet(title)
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker,  title, false ))));



        return customMarker;
    }



    public Marker addWaypointMarker(LatLng pos, String title) {
        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.waypoint_symbol, null);
        TextView numTxt = (TextView) marker.findViewById(R.id.num_txt);
        numTxt.setText(title);
        Marker customMarker = mMap.addMarker(new MarkerOptions()
                .position(pos)
                .title("Title")
                .snippet("Description")
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker, "wp_" + title, true))));


        final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        if (mMap.getCameraPosition().zoom < WAYPOINT_ZOOM_LIMIT)
            customMarker.setVisible(false);

        /*    if (mapView.getViewTreeObserver().isAlive()) {
                mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @SuppressLint("NewApi")
                    // We check which build version we are using.
                    @Override
                    public void onGlobalLayout() {
                        LatLngBounds bounds = new LatLngBounds.Builder().include(markerLatLng).build();
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                    }
                });
            }*/
        return customMarker;
    }

    public Marker addObstacleMarker(LatLng pos, String title, String type, int elevation) {
        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.obstacle_marker, null);
        TextView numTxt = (TextView) marker.findViewById(R.id.elevation_txt);
        numTxt.setText("" + elevation);
        Marker customMarker = mMap.addMarker(new MarkerOptions()
                .position(pos)
                .title(title)
                .snippet(type)
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker, "obs_" + title + type + elevation, true) )));


        final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        //   if( mMap.getCameraPosition().zoom < WAYPOINT_ZOOM_LIMIT)
        customMarker.setVisible(false);

        /*    if (mapView.getViewTreeObserver().isAlive()) {
                mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @SuppressLint("NewApi")
                    // We check which build version we are using.
                    @Override
                    public void onGlobalLayout() {
                        LatLngBounds bounds = new LatLngBounds.Builder().include(markerLatLng).build();
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                    }
                });
            }*/
        return customMarker;
    }


    // Convert a view to bitmap
    public static Bitmap createDrawableFromView(Context context, View view, String id, boolean cacheable) {

        if (cacheable && markerCache.containsKey(id)) {
            return markerCache.get(id);
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        markerCache.put(id, bitmap);
        return bitmap;
    }

    @Override
    public void onCameraMove() {
        CameraPosition position = mMap.getCameraPosition();

        if (prevCameraPosition != null) {
            if (prevCameraPosition.zoom < WAYPOINT_ZOOM_LIMIT && position.zoom > WAYPOINT_ZOOM_LIMIT) {
                for (Marker m : waypointMarkers) {
                    m.setVisible(true);
                }
            } else if (prevCameraPosition.zoom > WAYPOINT_ZOOM_LIMIT && position.zoom < WAYPOINT_ZOOM_LIMIT) {
                for (Marker m : waypointMarkers) {
                    m.setVisible(false);
                }
            }
        }
        if (prevCameraPosition != null) {
            if (prevCameraPosition.zoom < AIRSPACE_LABEL_ZOOM_LIMIT && position.zoom > AIRSPACE_LABEL_ZOOM_LIMIT) {
                for (Marker m : airspaceMarkers) {
                    m.setVisible(true);
                }
            } else if (prevCameraPosition.zoom > AIRSPACE_LABEL_ZOOM_LIMIT && position.zoom < AIRSPACE_LABEL_ZOOM_LIMIT) {
                for (Marker m : airspaceMarkers) {
                    m.setVisible(false);
                }
            }
        }
        if (prevCameraPosition != null && mMap.getCameraPosition() != null && prevCameraPosition.zoom > OBSTACLE_ZOOM_LIMIT && mMap.getCameraPosition().zoom < OBSTACLE_ZOOM_LIMIT) {
            for (Marker m : obstacleMarkers) {
                m.setVisible(false);
            }
        }

        prevCameraPosition = position;
    }

    @Override
    public void onCameraIdle() {
        LatLng ll = mMap.getCameraPosition().target;


        if (mMap.getCameraPosition().zoom > OBSTACLE_ZOOM_LIMIT) {
            for (Marker m : obstacleMarkers) {
                if (Math.abs(m.getPosition().latitude - ll.latitude) < 0.3 && Math.abs(m.getPosition().longitude - ll.longitude) < 0.35) {
                    if (!m.isVisible())
                        m.setVisible(true);
                } else {
                    if (m.isVisible())
                        m.setVisible(false);
                }

            }
        }

        if (prevCameraPosition != null && mMap.getCameraPosition() != null && prevCameraPosition.zoom > OBSTACLE_ZOOM_LIMIT && mMap.getCameraPosition().zoom < OBSTACLE_ZOOM_LIMIT) {
            for (Marker m : obstacleMarkers) {
                m.setVisible(false);
            }
        }

        if (isMapCenterNearMyLocation(ll) || (ll.latitude < 0.01 && ll.latitude > -0.01)) {
            Log.d(TAG, "Map following user");
            this.setLockMap(true);
        } else {
            Log.d(TAG, "Map not following user");
            this.setLockMap(false);
        }

    }

    public void clickedFocusToMyLocation( View view) {
        Log.d(TAG, "clickedFocusToMyLocation");
       focusToMyLocation();
    }


    public void focusToMyLocation() {
        if( mMap == null ) {
            Log.d(TAG, "focusToMyLocation: MapFragment not inited, return_here without map update");
            return;
        }
        if( loc == null ) {
            Log.d(TAG, "UpdateBearing: location null, return" );
            return;
        }
        CameraPosition currPos = mMap.getCameraPosition();

        this.setLockMap(false);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(loc.getLatitude(), loc.getLongitude()))
                .zoom(currPos.zoom).tilt(currPos.tilt)
                .bearing(currPos.bearing)
                .build()));
    }


    public boolean isMapCenterNearMyLocation( LatLng mapL) {
        if( loc == null ) return false;
        float[] res = new float[2];
        Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), mapL.latitude, mapL.longitude, res);

        CameraPosition currPos = mMap.getCameraPosition();

        Log.d(TAG, "isMapCenterNearMyLocation: dist " + res[0] + ", zoom: " + currPos.zoom);
        if( currPos.zoom > 14 && res[0] > 100 ) return false;
        if( res[0] > 300) return false;
        return true;

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
   //         enableMyLocation();
            startFlightMode();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    public void showAirportView(String id) {
        Airport a = LocalData.airports.get(id);
        TextView name = (TextView) aerodromeView.findViewById(R.id.adName);
        name.setText(a.getName());

        TextView metar = (TextView) aerodromeView.findViewById(R.id.metar);
        if( LocalData.metars.containsKey(id)) {
            Metar m = LocalData.metars.get(id);
            metar.setText(m.getMessage());
        }
        else
            metar.setText("");



              TextView elev = (TextView) aerodromeView.findViewById(R.id.adelev);
        elev.setText("" );

        TableLayout tl = (TableLayout) aerodromeView.findViewById(R.id.rwtable);

        tl.removeAllViews();
        TableRow row = (TableRow) LayoutInflater.from(this).inflate(R.layout.rw_table_row, null);
        ((TextView) row.findViewById(R.id.rw_id)).setText("RW");
        ((TextView) row.findViewById(R.id.rw_length)).setText("Length m");
        ((TextView) row.findViewById(R.id.rw_crosswind)).setText("Crosswind");
        ((TextView) row.findViewById(R.id.rw_headwind)).setText("Headwind");
        tl.addView(row);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    /*    for (Runway rw : a.getRunways()) {
            // Inflate your row "template" and fill out the fields.

            if (rw.getId().contains("/")) {
                TableRow row2 = (TableRow) inflater.inflate(R.layout.rw_table_row, null);
                ((TextView) row2.findViewById(R.id.rw_id)).setText(rw.id.substring(0, rw.id.indexOf("/")));
                TextView tvLen = (TextView) row2.findViewById(R.id.rw_length);
                tvLen.setText("" + rw.length);
                ((TextView) row2.findViewById(R.id.rw_surface)).setText(rw.surface);
                ((TextView) row2.findViewById(R.id.rw_crosswind)).setText("XX");
                ((TextView) row2.findViewById(R.id.rw_headwind)).setText("XX");
                tl.addView(row2);

                TableRow row3 = (TableRow) LayoutInflater.from(this).inflate(R.layout.rw_table_row, null);
                ((TextView) row3.findViewById(R.id.rw_id)).setText(rw.id.substring(rw.id.indexOf("/") + 1, rw.id.length()));
                tvLen = (TextView) row3.findViewById(R.id.rw_length);
                tvLen.setText("" + rw.length);
                ((TextView) row3.findViewById(R.id.rw_surface)).setText(rw.surface);
                ((TextView) row3.findViewById(R.id.rw_crosswind)).setText("XX");
                ((TextView) row3.findViewById(R.id.rw_headwind)).setText("XX");
                tl.addView(row3);
            } else {
                TableRow row2 = (TableRow) LayoutInflater.from(this).inflate(R.layout.rw_table_row, null);
                ((TextView) row2.findViewById(R.id.rw_id)).setText(rw.id);
                ((TextView) row2.findViewById(R.id.rw_surface)).setText(rw.surface);
                ((TextView) row2.findViewById(R.id.rw_length)).setText("" + rw.length);
                ((TextView) row2.findViewById(R.id.rw_crosswind)).setText("XX");
                ((TextView) row2.findViewById(R.id.rw_headwind)).setText("XX");
                tl.addView(row2);
            }
        }*/
        aerodromeView.setVisibility(View.VISIBLE);

    }


    public void showAerodromeView(String id) {
        Aerodrome a = LocalData.aerodromes.get(id);
        TextView name = (TextView) aerodromeView.findViewById(R.id.adName);
        name.setText(a.getName());

        TextView metar = (TextView) aerodromeView.findViewById(R.id.metar);
        if( LocalData.metars.containsKey(id)) {
            Metar m = LocalData.metars.get(id);
            metar.setText(m.getMessage());
        }
        else
            metar.setText("");

        TextView elev = (TextView) aerodromeView.findViewById(R.id.adelev);
        elev.setText("" + a.getElevation() + " ft MSL");

        TableLayout tl = (TableLayout) aerodromeView.findViewById(R.id.rwtable);

        tl.removeAllViews();
        TableRow row = (TableRow) LayoutInflater.from(this).inflate(R.layout.rw_table_row, null);
        ((TextView) row.findViewById(R.id.rw_id)).setText("RW");
        ((TextView) row.findViewById(R.id.rw_length)).setText("Length m");
        ((TextView) row.findViewById(R.id.rw_crosswind)).setText("Crosswind");
        ((TextView) row.findViewById(R.id.rw_headwind)).setText("Headwind");
        tl.addView(row);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (Runway rw : a.getRunways()) {
            // Inflate your row "template" and fill out the fields.

            if (rw.getId().contains("/")) {
                TableRow row2 = (TableRow) inflater.inflate(R.layout.rw_table_row, null);
                ((TextView) row2.findViewById(R.id.rw_id)).setText(rw.id.substring(0, rw.id.indexOf("/")));
                TextView tvLen = (TextView) row2.findViewById(R.id.rw_length);
                tvLen.setText("" + rw.length);
                ((TextView) row2.findViewById(R.id.rw_surface)).setText(rw.surface);
                ((TextView) row2.findViewById(R.id.rw_crosswind)).setText("XX");
                ((TextView) row2.findViewById(R.id.rw_headwind)).setText("XX");
                tl.addView(row2);

                TableRow row3 = (TableRow) LayoutInflater.from(this).inflate(R.layout.rw_table_row, null);
                ((TextView) row3.findViewById(R.id.rw_id)).setText(rw.id.substring(rw.id.indexOf("/") + 1, rw.id.length()));
                tvLen = (TextView) row3.findViewById(R.id.rw_length);
                tvLen.setText("" + rw.length);
                ((TextView) row3.findViewById(R.id.rw_surface)).setText(rw.surface);
                ((TextView) row3.findViewById(R.id.rw_crosswind)).setText("XX");
                ((TextView) row3.findViewById(R.id.rw_headwind)).setText("XX");
                tl.addView(row3);
            } else {
                TableRow row2 = (TableRow) LayoutInflater.from(this).inflate(R.layout.rw_table_row, null);
                ((TextView) row2.findViewById(R.id.rw_id)).setText(rw.id);
                ((TextView) row2.findViewById(R.id.rw_surface)).setText(rw.surface);
                ((TextView) row2.findViewById(R.id.rw_length)).setText("" + rw.length);
                ((TextView) row2.findViewById(R.id.rw_crosswind)).setText("XX");
                ((TextView) row2.findViewById(R.id.rw_headwind)).setText("XX");
                tl.addView(row2);
            }
        }
        aerodromeView.setVisibility(View.VISIBLE);

    }

    public void hideAerodromeView() {
        aerodromeView.setVisibility(View.GONE);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        hideAerodromeView();

        if (LocalData.aerodromes.get(marker.getTitle()) != null) {
            showAerodromeView(marker.getTitle());
        }
        if (LocalData.airports.get(marker.getTitle()) != null) {
            showAirportView(marker.getTitle());
        }
        return false;


    }

    @Override
    public void onMapClick(LatLng latLng) {
        hideAerodromeView();
    }

    public void updateMyLocation(Location location) {
        System.out.print("location update");
        this.loc = location;
        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
        if (myPlaneMarker == null) {

            int height = 50;
            int width = 50;
            BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.myplane);
            Bitmap b=bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);


            myPlaneMarker = mMap.addMarker(new MarkerOptions()
                    .title("My location")
                    .snippet("Click for details")
                    .position(ll)
                    .anchor(0.5f,0.5f)
                    .flat(true)
                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
        }
        myPlaneMarker.setPosition(ll);

        if (location.hasBearing()) {
            System.out.print("bearing " + location.getBearing());
            myPlaneMarker.setRotation(location.getBearing());
        } else
            System.out.print("no bearing");

        if( flightMode) {
            if( location.hasBearing()) {
                DecimalFormat formatter = new DecimalFormat("000");
                String aFormatted = formatter.format((int) location.getBearing());
                ((TextView) flightInfo.findViewById(R.id.heading)).setText(aFormatted );
            }
            else
                ((TextView) flightInfo.findViewById(R.id.heading)).setText( "N/A");
            if( location.hasSpeed())
                ((TextView) flightInfo.findViewById(R.id.groundspeed)).setText( "" + (int)(location.getSpeed() *1.943) );
            else
                ((TextView) flightInfo.findViewById(R.id.groundspeed)).setText( "N/A");


            if (followingUser ) {
                CameraPosition currPos = mMap.getCameraPosition();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))
                        .zoom(currPos.zoom).tilt(currPos.tilt)
                        .bearing(currPos.bearing)
                        .build()));

            }


            distanceTF.setText( "" + (int)(LocalData.distance * 0.000539956803) );

            if( prevLl != null) {
                PolylineOptions trackOpt = new PolylineOptions().width(4).color(Color.argb(130, 61, 142, 242)).zIndex(99);
                trackOpt.add(prevLl, ll);
                track.add( mMap .addPolyline(trackOpt));
            }

            prevLl = ll;




        }


    }

    public void zoomTo(float zoom) {
        if( mMap == null ) {
            Log.d(TAG, "focusToMyLocation: MapFragment not inited, return_here without map update");
            return;
        }

        CameraPosition currPos = mMap.getCameraPosition();

        this.setLockMap(false);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(currPos.target)
                .zoom(zoom).tilt(currPos.tilt)
                .bearing(currPos.bearing)
                .build()));
    }

    public void clickedZoomIn( View view ) {
        Log.d(TAG, "clickedZoomIn");
        if( mMap == null ) {
            Log.d(TAG, "focusToMyLocation: MapFragment not inited, return_here without map update");
            return;
        }

        zoomTo( mMap.getCameraPosition().zoom + 1);
    }

    public void clickedZoomOut( View view ) {
        Log.d(TAG, "clickedZoomOut");
        if( mMap == null ) {
            Log.d(TAG, "focusToMyLocation: MapFragment not inited, return_here without map update");
            return;
        }

        zoomTo( mMap.getCameraPosition().zoom - 1);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.headerMenu = menu;
        getMenuInflater().inflate(R.menu.menu_plan, menu);
        return true;
    }



    private void startFlightMode() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            startMainServiceIfNotStarted();
            flightMode = true;
        }

        sendServiceRequest(Constants.PARAM_START_TRACKING);

        myLocButton.setVisibility(View.VISIBLE);

        registerReceiver(mUpdateLocationReceiver,
                new IntentFilter(Constants.LOCATION_UPDATE_ACTION));

        flightChronometer = (Chronometer) findViewById(R.id.flighttime); // initiate a chronometer
        flightChronometer.setBase(SystemClock.elapsedRealtime());
        flightChronometer.start(); // start a chronometer
        LocalData.distance = 0;
        flightInfo.setVisibility(View.VISIBLE);

        headerMenu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_fly, headerMenu);

    }

    private void stopFlightMode() {
        headerMenu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_landing, headerMenu);
        myLocButton.setVisibility(View.VISIBLE);

        flightChronometer.stop();
        sendServiceRequest(Constants.PARAM_STOP_TRACKING);

    }


    public void startMainServiceIfNotStarted() {

        if (isMyServiceRunning(MainService.class)) {
            // prepare navi
            Log.d(TAG, "MainService already running, don't start");


        } else {
            Log.d(TAG, "MainService not running, start!");
            startService(new Intent(this, MainService.class));

        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "Menu item: " + item.getItemId());
        switch (item.getItemId()) {
            case R.id.flight_mode:
                startFlightMode();
                break;
            case R.id.landing_mode:
                stopFlightMode();
                break;
            case R.id.flight_clear:
                clearFlight();
                break;
      /*      case R.id.action_map:
                if( mainView != MainView.MAP) {
                    showMainView( MainView.MAP);
                }
                getActionBar().setDisplayHomeAsUpEnabled(true);


                mapFragment.clickedPeek(null);
                return_here true;
            case R.id.settings:
                showMainView(MainView.AUDIO);
                return_here true;*/
            //  case R.id.action_map_search:
            //      showSearch();
            //     return true;
            //    case R.id.action_map_layers:
            //         clickedStartHelper(null);
        }
        return true;
    }

    private void clearFlight() {
        LocalData.distance = 0;

        flightInfo.setVisibility(View.GONE);
        for( Polyline trackItem:track) {
            trackItem.remove();
        }
        track.clear();
        myPlaneMarker.remove();
        myPlaneMarker = null;
        headerMenu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_plan, headerMenu);
    }

    private final BroadcastReceiver mUpdateLocationReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        Log.d(TAG, "Location update received");

                        if (LocalData.getGPSLocation() != null) {
                            updateMyLocation(LocalData.getGPSLocation());
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } ;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        try {
            stopService(new Intent(this, MainService.class));
        } catch (Exception e) {
        }
        try {
            unregisterReceiver( mUpdateLocationReceiver);
        }
        catch( Exception e) {
            e.printStackTrace();
        }
    }

    public void setLockMap( boolean enabled) {
        if( !enabled) {
            followingUser = false;
        }
        else {
            followingUser = true;
        }

    }


    public void showMetar( boolean enabled) {
        Log.d(TAG, "clickedToggleWeather");

        if( enabled) {
            sendServiceRequest(PARAM_GET_METARS);
            sendServiceRequest(PARAM_GET_AWSMETARS);

        }
        else  {
            clearWeatherData();
        }
    }
}