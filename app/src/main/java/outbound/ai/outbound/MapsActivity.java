package outbound.ai.outbound;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPolygonClickListener {

    private GoogleMap mMap;
    DownloadAreasHTTPClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
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

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(a.getCenter())
                    .title(a.getCode()));


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

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(a.getCenter())
                    .title(a.getCode()));


      /*      for( LatLng ll:a.getCoordinates()) {
                rectOptions.add( ll);
            }
            Polygon polygon = mMap.addPolygon(rectOptions);
            polygon.setTag(a.name);
            polygon.setClickable(true);
*/

        }
    }


    @Override
    public void onPolygonClick(Polygon p) {
        // Flip from solid stroke to dotted stroke pattern.
        System.out.println("clicked polygon " + p.getTag().toString());
        Toast.makeText(this, "Route type " + p.getTag().toString(),
                Toast.LENGTH_SHORT).show();
    }



}
