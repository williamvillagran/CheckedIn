package edu.utsa.checkedin;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.Manifest;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private TripManager tripManager;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        ToggleButton beginToggleButton = findViewById(R.id.beginToggleButton);
        ImageButton findFriendsButton = findViewById(R.id.findFriendsButton);
        ImageButton addFriendsButton =  findViewById(R.id.addFriendsButton);
        ImageButton settingsButton = findViewById(R.id.settingsButton);

        requestPermissions();

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tripManager = new TripManager(this, userId);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Toggle button behavior
        beginToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startSharingLocation();
                Toast.makeText(getApplicationContext(), "Started Sharing Location", Toast.LENGTH_SHORT).show();
            } else {
                stopSharingLocation();
                Toast.makeText(getApplicationContext(), "Sharing Has Stopped", Toast.LENGTH_SHORT).show();
            }
        });

        findFriendsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FindFriendsActivity.class);
            startActivity(intent);
        });

        addFriendsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddFriendsActivity.class);
            startActivity(intent);
        });

//        settingsButton.setOnClickListener(v -> {
//            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
//            startActivity(intent);
//        });
    }

    private void startSharingLocation() {
        LocationRequest request = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(3000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // ✅ Update Firestore
                    tripManager.updateTripLocation(location);

                    // ✅ Update Realtime Database
                    DatabaseReference userLocationRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(userId)
                            .child("location");

                    Map<String, Object> locMap = new HashMap<>();
                    locMap.put("latitude", location.getLatitude());
                    locMap.put("longitude", location.getLongitude());

                    userLocationRef.setValue(locMap)
                            .addOnFailureListener(e -> Toast.makeText(HomeActivity.this, "Failed to update location in RTDB", Toast.LENGTH_SHORT).show());
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        // Log the start of the trip with the first location
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                tripManager.beginNewTrip(location);
            }
        });

        fusedLocationClient.requestLocationUpdates(request, locationCallback, getMainLooper());
    }

//    private void stopSharingLocation() {
//        if (locationCallback != null) {
//            fusedLocationClient.removeLocationUpdates(locationCallback);
//        }
//        tripManager.stopTrip();
//    }

    private void stopSharingLocation() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        // 🔥 Delete location from Realtime Database
        DatabaseReference locationRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("location");

        locationRef.removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(HomeActivity.this, "Location removed from RTDB", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(HomeActivity.this, "Failed to remove location", Toast.LENGTH_SHORT).show());

        tripManager.stopTrip();
    }


    private void requestPermissions() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.ACCESS_NETWORK_STATE
        };

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE
            );
        } else {
            Toast.makeText(this, "All permissions already granted", Toast.LENGTH_SHORT).show();
        }
    }
}
