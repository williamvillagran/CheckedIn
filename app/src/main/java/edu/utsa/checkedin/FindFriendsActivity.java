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
import java.util.List;

import edu.utsa.checkedin.model.Friend;

public class FindFriendsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private DatabaseReference usersRef;
    private FirebaseAuth auth;
    private List<Friend> myFriends = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_friends_activity);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        auth = FirebaseAuth.getInstance();

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
        String currentUserId = auth.getCurrentUser().getUid();

        usersRef.child(currentUserId).child("friends")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myFriends.clear();
                        for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                            Friend friend = friendSnapshot.getValue(Friend.class);
                            if (friend != null) {
                                myFriends.add(friend);

                                String friendUid = friend.getUid();
                                usersRef.child(friendUid).child("location")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot locationSnapshot) {
                                                Double lat = locationSnapshot.child("latitude").getValue(Double.class);
                                                Double lng = locationSnapshot.child("longitude").getValue(Double.class);

                                                if (lat != null && lng != null && googleMap != null) {
                                                    LatLng friendLoc = new LatLng(lat, lng);
                                                    googleMap.addMarker(new MarkerOptions()
                                                            .position(friendLoc)
                                                            .title(friend.getEmail()));
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(FindFriendsActivity.this, "Failed to get friend location", Toast.LENGTH_SHORT).show();
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
}
