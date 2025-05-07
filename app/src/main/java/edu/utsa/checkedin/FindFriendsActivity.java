package edu.utsa.checkedin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

import edu.utsa.checkedin.model.Friend;


public class FindFriendsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Friend> myFriends = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_friends_activity);

        db = FirebaseFirestore.getInstance();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.id_map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        loadAndDisplayFriendsLocations();
    }


    private void loadAndDisplayFriendsLocations() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference("friends").child(currentUserId);

        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myFriends.clear();
                for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                    Friend friend = friendSnapshot.getValue(Friend.class);
                    if (friend != null) {
                        myFriends.add(friend);

                        // 🧭 Get friend's latest trip location from Firestore
                        String friendUid = friend.getUid();
                        FirebaseFirestore.getInstance().collection("trips")
                                .document(friendUid)
                                .get()
                                .addOnSuccessListener(doc -> {
                                    if (doc.exists()) {
                                        Double lat = doc.getDouble("latitude");
                                        Double lng = doc.getDouble("longitude");
                                        if (lat != null && lng != null && googleMap != null) {
                                            LatLng friendLoc = new LatLng(lat, lng);
                                            googleMap.addMarker(new MarkerOptions()
                                                    .position(friendLoc)
                                                    .title(friend.getEmail()));
                                        }
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FindFriendsActivity.this, "Failed to load friends", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Get a handle to the GoogleMap object and display marker.
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        googleMap.addMarker(new MarkerOptions()
//                .position(new LatLng(0, 0))
//                .title("Marker"));
//    }

//    public void fetchLatestLocation() {
//        db.collection("users")
//                .document(friendUserId)
//                .collection("trips")
//                .orderBy("startTime", Query.Direction.DESCENDING)
//                .limit(1)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    if (!queryDocumentSnapshots.isEmpty()) {
//                        DocumentSnapshot tripDoc = queryDocumentSnapshots.getDocuments().get(0);
//
//                        Double lat = tripDoc.getDouble("latitude");
//                        Double lng = tripDoc.getDouble("longitude");
//
//                        if (lat != null && lng != null) {
//                            LatLng location = new LatLng(lat, lng);
//                            googleMap.addMarker(new MarkerOptions()
//                                    .position(location)
//                                    .title("Friend's Location"));
//                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
//                        }
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    // Handle failure
//                });
//    }
}
