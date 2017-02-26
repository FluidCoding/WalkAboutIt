package com.fluidcoding.brian.walkaboutit;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
{

    Button btnStartWalk;
    Button btnStopWalk;
    Button btnViewStats;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDb;
    private final String TAG = "MAPS";
    private GoogleMap mMap;
    private LocationManager locmn, locmg;
    private ArrayList<Geotag> listOfGs;
    private Location yourloc, dest;
    private Marker mark;
    private Spinner spin;
    private Geofence gf;
    //private MapView vMap;
    GoogleApiClient mGoogleApiClient = null;
    public void onProviderEnabled(String provider){}

    @Override
    public void onProviderDisabled(String provider) {

    }


    public void onLocationChanged(Location location){

        if(mark != null)
            mMap.clear();

        mark = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Location"));


        mMap.moveCamera(CameraUpdateFactory.zoomTo(19.0f));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) throws SecurityException{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SupportMapFragment mf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mf.getMapAsync(this);
        Log.d(TAG, "Created Map Activity");
        listOfGs = new ArrayList<Geotag>();
        // Firebase
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance().getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // TODO: Push User back to login page.
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        // MAPS API
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        locmg = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locmn = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // End Maps API
        // ---------------------------------------------------------------
        // Firebase Loading
        // ---------------------------------------------------------------
        GBP p = new GBP(0);
        mDb.child("GPB").child(mAuth.getCurrentUser().getUid()).setValue(p);
//        Log.d(TAG, mDb.child("GBP").child(mAuth.getCurrentUser().getUid());
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("FireBase", "firebase init");
                // Get Post object and use the values to update the UI
                GBP p = dataSnapshot.getValue(GBP.class);
                Log.d(TAG, String.valueOf(p.getPoints()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDb.addValueEventListener(postListener);

        ValueEventListener geoListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("FireBase", "firebase init");
                // Get Post object and use the values to update the UI
                for(DataSnapshot d : dataSnapshot.getChildren()){
                    listOfGs.add(d.getValue(Geotag.class));
                    Log.d(TAG, listOfGs.get(0).name);
                }

                ArrayList<String> strs = new ArrayList<String>();
                strs.add("All");
                if(mMap != null)
                {
                    for(Geotag gg:listOfGs)
                    strs.add(gg.getName());

                }
                ArrayAdapter<String> stradp = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, strs);
                spin.setAdapter(stradp);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDb.child("LL").child("Philadelphia").addValueEventListener(geoListener);

        // ---------------------------------------------------------------
        // END FIREBASE LOADS
        // -----------------------------
        // UI Init/Events
        spin = (Spinner) findViewById(R.id.spinner);
        btnStartWalk = (Button)findViewById(R.id.btnStart);
        btnStopWalk = (Button)findViewById(R.id.btnStop);

        btnStartWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Get Selected Item If any

                Geotag selectedTag = listOfGs.get(spin.getSelectedItemPosition());

                mark = mMap.addMarker(new MarkerOptions().position(new LatLng(selectedTag.getLat(), selectedTag.getLng())).
                        title(selectedTag.getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

                // TODO: Geofencing
                dest = new Location(yourloc);
                dest.setLatitude(selectedTag.getLat());
                dest.setLongitude(selectedTag.getLng());
                float distance = yourloc.distanceTo(dest);
                int p_points = (int)distance / 100;
                Snackbar.make(v, "Account Creation Error", Snackbar.LENGTH_INDEFINITE).show();

                // TODO: Start Collecting Points
            }
        });

        btnStopWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Kill Geofencing
            }
        });
    }

    @Override
    protected void onStart() throws SecurityException {
        mGoogleApiClient.connect();
        super.onStart();
        locmn.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 5, this);
        locmg.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, this);
    }

    @Override
    protected void onStop() throws SecurityException {
        locmn.removeUpdates(this);
        locmg.removeUpdates(this);
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Maps Are Readyyyyy");

        mMap.animateCamera(CameraUpdateFactory.zoomTo(19.0f));
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) throws SecurityException{

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
