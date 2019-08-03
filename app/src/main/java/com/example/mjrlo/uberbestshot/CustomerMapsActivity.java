package com.example.mjrlo.uberbestshot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CustomerMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, OnSuccessListener, LocationListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;

    private FirebaseAuth mAuth;

    private LocationCallback mLocationCallback;

    private FusedLocationProviderClient mFusedLocationClient;
    Location mLastLocation;
    private GoogleApiClient googleApiClient;
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


        CallUberButton.setOnClickListener(onClick);


        LogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FirebaseAuth.getInstance().signOut();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(CustomerMapsActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }



    public View.OnClickListener onClick= new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();


            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
            GeoFire geoFire= new GeoFire(ref);
            geoFire.setLocation(userid, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                }
            });
            position = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(position).title("Pick up Here"));
            // Do something in response to button click


            findClosestDriver();
        }

    };







    private int radius=1;
    private Boolean driverFound= false;
    private String driverFoundID;
    private DatabaseReference databaseReference;
    private DatabaseReference driverReference;

    private void findClosestDriver(){

           databaseReference = FirebaseDatabase.getInstance().getReference("DriversAvailable");
          GeoFire geoFire = new GeoFire(databaseReference);
          GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(position.latitude,position.longitude),radius);
          geoQuery.removeAllListeners();

          geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
              @Override
              public void onKeyEntered(String key, GeoLocation location) {

                  if (!driverFound){
                      driverFound = true;
                      driverFoundID= key;

                       driverReference = FirebaseDatabase.getInstance().getReference("Users").child("Drivers").child(driverFoundID);
                      String customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                      HashMap map = new HashMap();

                      map.put("customerRideId",customerID);
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

                  if(!driverFound){
                      radius++;
                      findClosestDriver();
                  }


              }

              @Override
              public void onGeoQueryError(DatabaseError error) {

              }
          });

      }
      private void takeDriverOutOfDiversAvailableIntoDriversWorking(DatabaseReference fromPath, final DatabaseReference toPath){





      }

      private Marker DriverMarker;
      private DatabaseReference driverLocationReferenceGetLocation;
      private GeoFire driverLocationFire;
      private Location driverAvailableLocationLatitude;
      private Location driverAvailableLocationLongitude;
      private ValueEventListener driverLocationListener;
      private DatabaseReference dWork;




      private void getDriverLocation(){



         driverLocationReferenceGetLocation = FirebaseDatabase.getInstance().getReference("DriversWorking").child(driverFoundID).child("l");
         driverLocationReferenceGetLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLong =0;
                    if (map.get(0)!=null) {
                        locationLat= Double.parseDouble(map.get(0).toString());

                    }
                    if (map.get(1)!=null){
                        locationLong = Double.parseDouble(map.get(1).toString());
                    }

                    LatLng driverLatLong = new LatLng(locationLat,locationLong);
                    if(DriverMarker!=null){
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

        // Add a marker in Sydney and move the camera
       /* LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
*/


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        buildGoogleApiclient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiclient() {

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

    }

    final int LOCATION_REQUEST_CODE = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:
                if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                    mapFragment.getMapAsync(this);
                }else{

                    Toast.makeText(getApplicationContext(),"plese provide permission",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onSuccess(Object o) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));




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

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(1000);
        mlocationRequest.setFastestInterval(1000);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


            ActivityCompat.requestPermissions(CustomerMapsActivity.this,new  String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mlocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    protected void onStop() {
super.onStop();
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        //String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(RemoveUserId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
    }
}
