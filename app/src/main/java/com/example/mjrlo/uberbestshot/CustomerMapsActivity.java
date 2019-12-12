package com.example.mjrlo.uberbestshot;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import androidx.core.content.ContextCompat;

import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CustomerMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "";
    private GoogleMap mMap;

    private LocationListener locListener;
    private LocationManager locManager;
    private FirebaseAuth mAuth;

    private LocationCallback mLocationCallback;

    private FusedLocationProviderClient mFusedLocationClient;
    Location mLastLocation;
    LocationRequest mlocationRequest;
    LatLng position;
    private SupportMapFragment mapFragment;
    private Button CallUberButton;
    private String RemoveUserId;
    private Button LogOutButton;
    Double theDirverLat;
    Double theDiriverLong;

    // final int LOCATION_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        CallUberButton = (Button) findViewById(R.id.call_uber_button);
        LogOutButton = (Button) findViewById(R.id.Log_Out_Button);


        RemoveUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        CallUberButton.setOnClickListener(onClick);


        LogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FirebaseAuth.getInstance().signOut();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(CustomerMapsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }


    public View.OnClickListener onClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();


            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
            GeoFire geoFire = new GeoFire(ref);
            geoFire.setLocation(userid, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                }
            });
            position = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(position).title("Pick up Here"));
            // Do something in response to button click


            findClosestDriver();
        }

    };


    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundID;
    private DatabaseReference databaseReference;
    private DatabaseReference driverReference;

    private void findClosestDriver() {

        databaseReference = FirebaseDatabase.getInstance().getReference("DriversAvailable");
        GeoFire geoFire = new GeoFire(databaseReference);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(position.latitude, position.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if (!driverFound) {
                    driverFound = true;
                    driverFoundID = key;

                    driverReference = FirebaseDatabase.getInstance().getReference("Users").child("Drivers").child(driverFoundID);
                    String customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();

                    map.put("customerRideId", customerID);
                    driverReference.updateChildren(map);

                    CallUberButton.setText("Looking For Drivers Location");

                    getDriverLocation();
                }

                radius++;


            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if (!driverFound) {
                    radius++;
                    findClosestDriver();
                }


            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }


    private Marker DriverMarker;
    private DatabaseReference driverLocationReferenceGetLocation;
    private GeoFire driverLocationFire;
    private Location driverAvailableLocationLatitude;
    private Location driverAvailableLocationLongitude;
    private ValueEventListener driverLocationListener;
    private DatabaseReference dWork;


    private void getDriverLocation() {


        driverLocationReferenceGetLocation = FirebaseDatabase.getInstance().getReference("DriversWorking").child(driverFoundID).child("l");
        driverLocationReferenceGetLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLong = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());

                    }
                    if (map.get(1) != null) {
                        locationLong = Double.parseDouble(map.get(1).toString());
                    }

                    LatLng driverLatLong = new LatLng(locationLat, locationLong);
                    if (DriverMarker != null) {
                        DriverMarker.remove();
                    }
                    mMap.addMarker(new MarkerOptions().position(driverLatLong).title("Your Driver Is Here!"));
                    CallUberButton.setText("Driver Found");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(1000);
        mlocationRequest.setFastestInterval(1000);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            } else {
                checkLocationPermission();
            }


        }

        mFusedLocationClient.requestLocationUpdates(mlocationRequest, locationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {

                if(getApplicationContext()!=null) {
                    mLastLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));


                }
            }
        }
    };

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission messaage")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(CustomerMapsActivity.this, new String[]{
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                }, 1);
                            }
                        })
                        .create()
                        .show();

            } else {
                ActivityCompat.requestPermissions(CustomerMapsActivity.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, 1);
            }



        }
    }

    final int LOCATION_REQUEST_CODE = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


                        mFusedLocationClient.requestLocationUpdates(mlocationRequest, locationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else {

                    Toast.makeText(getApplicationContext(), "plese provide permission", Toast.LENGTH_LONG).show();
                }
                break;

            }
        }


    }

    private void stopLocationUpdates () {
        if (mFusedLocationClient != null) {


            try {
                final Task<Void> voidTask = mFusedLocationClient.removeLocationUpdates(locationCallback);
                if (voidTask.isSuccessful()) {

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");

                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(RemoveUserId, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });
                    Log.d(TAG, "StopLocation updates successful! ");
                } else {
                    Log.d(TAG, "StopLocation updates unsuccessful! " + voidTask.toString());
                }
            } catch (SecurityException exp) {
                Log.d(TAG, " Security exception while removeLocationUpdates");
            }
        }
    }

}