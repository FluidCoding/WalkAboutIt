package com.fluidcoding.brian.walkaboutit;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
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

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
{

    Button btnStartWalk;
    Button btnStopWalk;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDb;
    private final String TAG = "MAPS";
    private GoogleMap mMap;
    private LocationManager locmn, locmg;
    private ArrayList<Geotag> listOfGs;
    private Location yourloc, dest;
    private Marker mark, mark2;
    private Spinner spin;
    private Geofence gf;
    static int p_points = 0;
    private boolean map_state = false;
    private boolean isWalking = false;
    //private MapView vMap;
    GoogleApiClient mGoogleApiClient = null;
    float distance;

    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public void onLocationChanged(Location location) {
        if(mark != null)
        mark.remove();

        if(map_state) {
            mark = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Location"));


            mMap.moveCamera(CameraUpdateFactory.zoomTo(19.0f));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
        }
        else{
            return;
        }
        btnStartWalk.setEnabled(true);
        yourloc = location;

        if(isWalking) {
            distance = yourloc.distanceTo(dest);
            Log.d("Distance: ", ""+ distance);
            //TODO: circle logic

            p_points = (int) distance / 10 + 1;

            if (distance < 10) {
                //TODO: YOU WIN
                Toast.makeText(this.getBaseContext(), "YOU WIN: " + p_points + " POINTS", Toast.LENGTH_LONG).show();

                isWalking = false;
                mMap.clear();
                mDb.child("GBP").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        GBP g = new GBP(0);
                        g = dataSnapshot.getValue(GBP.class);

                        if(g==null){ mDb.child("GBP").child(mAuth.getCurrentUser().getUid()).setValue(new GBP(p_points));}
                        else{
                            g.setPoints(g.getPoints()+p_points);
                            mDb.child("GBP").child(mAuth.getCurrentUser().getUid()).setValue(g);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }





    @Override
    protected void onCreate(Bundle savedInstanceState) throws SecurityException {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        setSupportActionBar(toolbar);
        SupportMapFragment mf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mf.getMapAsync(this);
        Log.d(TAG, "Created Map Activity");

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance().getReference();




        mDb.child("GBP").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GBP gs = new GBP(0);
                gs = dataSnapshot.getValue(GBP.class);

                if(gs!=null){Toast.makeText(getBaseContext(), "Welcome back, you have " + gs.getPoints() + " points", Toast.LENGTH_LONG).show(); }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        listOfGs = new ArrayList<Geotag>();
        // Firebase

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
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    listOfGs.add(d.getValue(Geotag.class));
                    Log.d(TAG, listOfGs.get(0).name);
                }

                ArrayList<String> strs = new ArrayList<String>();
                //strs.add("All");
                if (mMap != null) {
                    for (Geotag gg : listOfGs)
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
        btnStartWalk = (Button) findViewById(R.id.btnStart);
        btnStopWalk = (Button) findViewById(R.id.btnStop);

        btnStartWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Get Selected Item If any
                init_location(yourloc);
                isWalking = true;
                Log.d("isWalking: ", "true");
                Geotag selectedTag = listOfGs.get(spin.getSelectedItemPosition());
                LatLng temp = new LatLng(selectedTag.getLat(), selectedTag.getLng());
                mark2 = mMap.addMarker(new MarkerOptions().position(temp).
                        title(selectedTag.getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

                mMap.addCircle(new CircleOptions().center(temp).radius(10.0));

                // TODO: Points
                dest = new Location("");
                dest.setLatitude(selectedTag.getLat());
                dest.setLongitude(selectedTag.getLng());
                distance = yourloc.distanceTo(dest);
                //TODO: circle logic

                p_points = (int) distance / 10 + 1;
                Snackbar.make(v, "Possible Points: " + p_points, Snackbar.LENGTH_SHORT).show();


                // TODO: Start Collecting Points
            }
        });

        btnStopWalk.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v){
                // TODO: Kill Geofencing
                init_location();
            }
        });}
        void init_location() throws   SecurityException
        {

        Location tmpl = yourloc;
        if (tmpl == null){
            tmpl = new Location("");
            tmpl.setLongitude(0.0);
            tmpl.setLatitude(0.0);
        }
            init_location(tmpl);
        }
    void init_location(Location l) throws   SecurityException{
        if(l==null)
            init_location();
        isWalking = false;
        mMap.clear();
        onLocationChanged(l);
    }




    @Override
    protected void onStart() throws SecurityException {
        mGoogleApiClient.connect();
        super.onStart();
        locmn.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 3, this);
        locmg.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 3, this);

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
        init_location();
        Log.d(TAG, "Maps Are Readyyyyy");
        map_state = true;
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
