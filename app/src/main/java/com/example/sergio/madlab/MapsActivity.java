package com.example.sergio.madlab;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

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
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(getBaseContext(), ViewBook.class);
                intent.putExtra("keyISBN", marker.getSnippet());

                startActivity(intent);
            }
        });

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        putBookLocations(ref);
    }

    private void putBookLocations(DatabaseReference database) {
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot locationsSnapshot = dataSnapshot.child("locations");
                Iterable<DataSnapshot> bookChildren = locationsSnapshot.getChildren();
                for (DataSnapshot book : bookChildren) {
                    double latitude = (double) book.child("l").child("0").getValue();
                    double longitude = (double) book.child("l").child("1").getValue();
                    LatLng location = new LatLng(latitude, longitude);
                    addMarker(location, book.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addMarker(final LatLng location, final String key) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference titleRef = ref.child("books").child(key).child("title");
        titleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String title = dataSnapshot.getValue(String.class);
                mMap.addMarker(
                        new MarkerOptions()
                            .position(location)
                            .title(title))
                            .setSnippet(key);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void doSearch() {
        Intent intent = getIntent();
        String searchFactor = intent.getStringExtra("searchFactor");
        String searchValue = intent.getStringExtra("searchValue");
        //TODO

    }

}
