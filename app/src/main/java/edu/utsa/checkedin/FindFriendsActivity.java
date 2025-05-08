package edu.utsa.checkedin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.utsa.checkedin.model.Friend;

public class FindFriendsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private List<Friend> myFriends = new ArrayList<>();
    private final Map<String, Marker> friendMarkers = new HashMap<>(); // Track active markers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_friends_activity);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.id_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Map initialization error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        // Initialize the map
        this.googleMap = map;
        loadAndDisplayFriendsLocations();
    }

    private void loadAndDisplayFriendsLocations() {

        // Get the current user's ID
        String currentUserId = auth.getCurrentUser().getUid();

        // Listen for changes in friends list
        usersRef.child(currentUserId).child("friends").addValueEventListener(new ValueEventListener() {

            // Handle changes in friends list
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear old friends list
                myFriends.clear();

                // Iterate through friends list
                for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                    Friend friend = friendSnapshot.getValue(Friend.class);
                    if (friend != null) {
                        // Add friend to list
                        myFriends.add(friend);
                        // Listen to location changes for each friend
                        fetchFriendLocation(friend);
                    }
                }
            }

            // Handle errors
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FindFriendsActivity.this, "Failed to load friends", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchFriendLocation(Friend friend) {
        // Listens for friend location updates in the database
        usersRef.child(friend.getUid()).child("location").addValueEventListener(new ValueEventListener() {
            // Handle changes in friend location
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get friend location
                Double lat = snapshot.child("latitude").getValue(Double.class);
                Double lng = snapshot.child("longitude").getValue(Double.class);

                // Update marker position
                if (lat != null && lng != null && googleMap != null) {
                    LatLng friendLoc = new LatLng(lat, lng);

                    // Remove old marker for friend
                    if (friendMarkers.containsKey(friend.getUid())) {
                        Marker oldMarker = friendMarkers.get(friend.getUid());
                        if (oldMarker != null) oldMarker.remove();
                    }

                    // Create new marker for friend
                    Marker newMarker = googleMap.addMarker(new MarkerOptions()
                            .position(friendLoc)
                            .title(friend.getEmail()));
                    // Store the new marker
                    friendMarkers.put(friend.getUid(), newMarker);
                }
            }
            // Handle errors
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FindFriendsActivity.this, "Failed to fetch location for " + friend.getEmail(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
