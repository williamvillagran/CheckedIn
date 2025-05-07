package edu.utsa.checkedin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;


public class FindFriendsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_friends_activity);

        db = FirebaseFirestore.getInstance();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.id_map);
        mapFragment.getMapAsync(this);

    }

    // Get a handle to the GoogleMap object and display marker.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
    }

    public void fetchLatestLocation() {
        db.collection("users")
                .document(friendUserId)
                .collection("trips")
                .orderBy("startTime", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot tripDoc = queryDocumentSnapshots.getDocuments().get(0);

                        Double lat = tripDoc.getDouble("latitude");
                        Double lng = tripDoc.getDouble("longitude");

                        if (lat != null && lng != null) {
                            LatLng location = new LatLng(lat, lng);
                            googleMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .title("Friend's Location"));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }
}
