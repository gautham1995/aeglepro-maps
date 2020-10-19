package com.example.myapplication;

import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
//    DatabaseReference myRef = database.getReference("message2");
    DatabaseReference myRef2 = database.getReference("CurrentLocation/");
    private Double plat, plong, dlat, dlong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        // Write a message to the database
//        myRef.setValue("Hello, World!!!!!!!!!!!");

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
                LatLng doc = new LatLng(dlat, dlong);
                LatLng pat = new LatLng(plat, plong);
                mMap.addMarker(new MarkerOptions().position(doc).title("Doctor's Location"));
                mMap.addMarker(new MarkerOptions().position(pat).title("Patient's Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(doc, 11));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast toast = Toast.makeText(MapsActivity.this, "onCancelled Firebase", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }


}