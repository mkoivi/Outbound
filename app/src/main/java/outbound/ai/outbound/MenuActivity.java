package outbound.ai.outbound;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.List;

public class MenuActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraMoveListener,GoogleMap.OnPolygonClickListener {

    private GoogleMap mMap;
    DownloadAreasHTTPClient httpClient;

    private Marker customMarker;

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);


        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        LatLng deflt = new LatLng(63, 26);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(deflt,6f));
        mMap.setOnPolygonClickListener(this);
        mMap.setOnCameraMoveListener(this);
        httpClient = new DownloadAreasHTTPClient(this);
        httpClient.getAirspaces();


        IntentFilter filter = new IntentFilter("outbound.ai.outbound.AIRSPACE_UPDATED");
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("Received airpace data");
                updateAirspace();
                httpClient.getSupplements();
            }
        };
        registerReceiver(receiver, filter);

        IntentFilter filter2 = new IntentFilter("outbound.ai.outbound.SUPPLEMENTS_UPDATED");
        BroadcastReceiver receiver2 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("Received supplements data");
           updateSupplements();
                httpClient.getAerodromes();
            }
        };
        registerReceiver(receiver2, filter2);

        IntentFilter filter5 = new IntentFilter("outbound.ai.outbound.AERODROMES_UPDATED");
        BroadcastReceiver receiver5 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("Received aerodromes data");
                updateAerodromes();
                httpClient.getAirports();
                httpClient.getWaypoints();
            }
        };
        registerReceiver(receiver5, filter5);


        IntentFilter filter3 = new IntentFilter("outbound.ai.outbound.AIRPORTS_UPDATED");
        BroadcastReceiver receiver3 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("Received airports data");
                updateAirports();
                httpClient.getReservations();
            }
        };
        registerReceiver(receiver3, filter3);

        IntentFilter filter4 = new IntentFilter("outbound.ai.outbound.RESERVATIONS_UPDATED");
        BroadcastReceiver receiver4 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("Received reservations data");
                updateReservations();
            }
        };
        registerReceiver(receiver4, filter4);

        IntentFilter filter6 = new IntentFilter("outbound.ai.outbound.WAYPOINTS_UPDATED");
        BroadcastReceiver receiver6 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("Received reservations data");
                updateWaypoints();
            }
        };
        registerReceiver(receiver6, filter6);



        setUpMap();
    }

    public void updateAirspace() {

        for( Airspace a: LocalData.airspaces) {
            PolygonOptions rectOptions = new PolygonOptions();

      //      if( !a.getLower().startsWith("FL")) {
       //         rectOptions.fillColor(Color.argb(100,255,160,90));
      //      }
            if( (a.getAirspaceClass().equals("A") || a.getAirspaceClass().equals("B") || a.getAirspaceClass().equals("C") || a.getAirspaceClass().equals("D")) && a.getLower().equals("SFC") ) {
                rectOptions.fillColor(Color.argb(100,255,160,90));
            }
            if( (a.getAirspaceClass().equals("Prohibited")))  {
                rectOptions.fillColor(Color.argb(160,255,0,0));
            }
            if( (a.getAirspaceClass().equals("Restricted") && a.getActivity().equals("H24")))  {
                rectOptions.fillColor(Color.argb(130,230,115,25));
            }

            rectOptions.strokeWidth(3.5f);

            for( LatLng ll:a.getCoordinates()) {
                rectOptions.add( ll);
            }
            Polygon polygon = mMap.addPolygon(rectOptions);
            polygon.setTag(a.name);
            polygon.setClickable(true);

        }

   }


    public void updateSupplements() {

        for( Supplement a: LocalData.supplements) {
            PolygonOptions rectOptions = new PolygonOptions();

            //      if( !a.getLower().startsWith("FL")) {
            //         rectOptions.fillColor(Color.argb(100,255,160,90));
            //      }

             rectOptions.fillColor(Color.argb(130,0,215,0));


            rectOptions.strokeWidth(3.5f);

            for( LatLng ll:a.getCoordinates()) {
                rectOptions.add( ll);
            }
            Polygon polygon = mMap.addPolygon(rectOptions);
            polygon.setTag(a.name);
            polygon.setClickable(true);

        }
    }

    public void updateReservations() {

        for( Reservation a: LocalData.reservations) {
            PolygonOptions rectOptions = new PolygonOptions();

            //      if( !a.getLower().startsWith("FL")) {
            //         rectOptions.fillColor(Color.argb(100,255,160,90));
            //      }

            rectOptions.fillColor(Color.argb(130,0,0,200));


            rectOptions.strokeWidth(3.5f);

            for( LatLng ll:a.getCoordinates()) {
                rectOptions.add( ll);
            }
            Polygon polygon = mMap.addPolygon(rectOptions);
            polygon.setTag(a.name);
            polygon.setClickable(true);

        }
    }


    public void updateAirports() {

        for( Airport a: LocalData.airports) {
//            PolygonOptions rectOptions = new PolygonOptions();

            //      if( !a.getLower().startsWith("FL")) {
            //         rectOptions.fillColor(Color.argb(100,255,160,90));
            //      }

  //              rectOptions.fillColor(Color.rgb(50,50,50));

    //        rectOptions.strokeWidth(3.5f);

     /*       Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(a.getCenter())
                    .title(a.getCode()));
*/
     /*       int rotation = 0;
            try {
                rotation = Integer.parseInt( a.getRunways().get(0).id.substring(0,2)  + "0" );
            }
            catch( Exception e) {
                e.printStackTrace();
            }
*/
            addAerodromeMarker( a.getCenter(), a.getCode(), 90);
      /*      for( LatLng ll:a.getCoordinates()) {
                rectOptions.add( ll);
            }
            Polygon polygon = mMap.addPolygon(rectOptions);
            polygon.setTag(a.name);
            polygon.setClickable(true);
*/

        }
    }


    public void updateAerodromes() {

        for( Aerodrome a: LocalData.aerodromes) {
//            PolygonOptions rectOptions = new PolygonOptions();

            //      if( !a.getLower().startsWith("FL")) {
            //         rectOptions.fillColor(Color.argb(100,255,160,90));
            //      }

            //              rectOptions.fillColor(Color.rgb(50,50,50));

            //        rectOptions.strokeWidth(3.5f);

/*            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(a.getCenter())
                    .title(a.getCode()));
*/

            List<Runway> rs = a.getRunways();
            for( Runway r :rs) {
                try {
                    int rotation = 0;
                    rotation = Integer.parseInt(r.id.substring(0, 2) + "0");
                    addAerodromeMarker(a.getCenter(), a.getCode(), rotation);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

      /*      for( LatLng ll:a.getCoordinates()) {
                rectOptions.add( ll);
            }
            Polygon polygon = mMap.addPolygon(rectOptions);
            polygon.setTag(a.name);
            polygon.setClickable(true);
*/

        }
    }


    public void updateWaypoints() {

        for( Waypoint a: LocalData.waypoints) {
               addWaypointMarker(a.getCenter(),a.getName());
            }
    }

    @Override
    public void onPolygonClick(Polygon p) {
        // Flip from solid stroke to dotted stroke pattern.
        System.out.println("clicked polygon " + p.getTag().toString());
        Toast.makeText(this, "Route type " + p.getTag().toString(),
                Toast.LENGTH_SHORT).show();
    }





    private void setUpMap() {




    }

    public Marker addAerodromeMarker(LatLng pos, String title, int rotation) {
        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.aerodrome_symbol, null);
        TextView numTxt = (TextView) marker.findViewById(R.id.num_txt);
        numTxt.setText(title);
        ImageView runwaySym = marker.findViewById(R.id.runway_symbol);
        runwaySym.setRotation(rotation);
        Marker customMarker = mMap.addMarker(new MarkerOptions()
                .position(pos)
                .title("Title")
                .snippet("Description")
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker))));


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


    public Marker addWaypointMarker(LatLng pos, String title) {
        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.waypoint_symbol, null);
        TextView numTxt = (TextView) marker.findViewById(R.id.num_txt);
        numTxt.setText(title);
          Marker customMarker = mMap.addMarker(new MarkerOptions()
                .position(pos)
                .title("Title")
                .snippet("Description")
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker))));


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




    // Convert a view to bitmap
    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    @Override
    public void onCameraMove() {
        CameraPosition position = mMap.getCameraPosition();

    }
}
