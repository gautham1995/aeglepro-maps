package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference docLat = database.getReference("CurrentLocation2/Doctor/Latitude");
    DatabaseReference docLong = database.getReference("CurrentLocation2/Doctor/Longitude");
    DatabaseReference myRef2 = database.getReference("CurrentLocation2/");
    private Double plat, plong, dlat, dlong;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 15000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        getCurrentLocation();


    }

    @Override
    protected void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delay);
                Toast.makeText(MapsActivity.this, "This method is run every 15 seconds", Toast.LENGTH_SHORT).show();
                getCurrentLocation();
            }
        }, delay);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); //stop handler when activity not visible super.onPause();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void getCurrentLocation() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        }

        LocationServices.getFusedLocationProviderClient(MapsActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(MapsActivity.this)
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            double latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            double longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
//                            Toast.makeText(MapsActivity.this, String.format("Latitude: %s\nLongitude: %s", latitude,longitude), Toast.LENGTH_SHORT).show();
                            docLat.setValue(latitude);
                            docLong.setValue(longitude);
                        }
                    }
                }, Looper.getMainLooper());
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        addValueEventListener();
    }

    private void addValueEventListener() {
        if (mMap == null) return;
        // Read from the database
        myRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                plat = dataSnapshot.child("Patient").child("Latitude").getValue(Double.class);
                plong = dataSnapshot.child("Patient").child("Longitude").getValue(Double.class);
                dlat = dataSnapshot.child("Doctor").child("Latitude").getValue(Double.class);
                dlong = dataSnapshot.child("Doctor").child("Longitude").getValue(Double.class);

                mMap.clear();
                mMap.getUiSettings().setMapToolbarEnabled(false);
                LatLng doc = new LatLng(dlat, dlong);
                LatLng pat = new LatLng(plat, plong);
//                mMap.addMarker(new MarkerOptions().position(doc).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("Doctor's Location"));
//                mMap.addMarker(new MarkerOptions().position(pat).title("Patient's Location"));
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(doc, 11));

                ArrayList<Marker> markersList = new ArrayList<>();
                LatLngBounds.Builder builder;
                final CameraUpdate cu;

                Marker doctor = mMap.addMarker(new MarkerOptions().position(doc).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("Doctor's Location"));
                Marker patient = mMap.addMarker(new MarkerOptions().position(pat).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("Patient's Location"));
                /*Put all the markers into arraylist*/
                markersList.add(doctor);
                markersList.add(patient);
                /*create for loop for get the latLngbuilder from the marker list*/
                builder = new LatLngBounds.Builder();
                for (Marker m : markersList) {
                    builder.include(m.getPosition());
                }
                /*initialize the padding for map boundary*/
//                int padding = 50;
                int padding = 200;
                /*create the bounds from latlngBuilder to set into map camera*/
                LatLngBounds bounds = builder.build();
                /*create the camera with bounds and padding to set into map*/
                cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                /*call the map call back to know map is loaded or not*/
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        /*set move zoom camera into map*/
                        mMap.moveCamera(cu);
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast toast = Toast.makeText(MapsActivity.this, "onCancelled Firebase", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }


}