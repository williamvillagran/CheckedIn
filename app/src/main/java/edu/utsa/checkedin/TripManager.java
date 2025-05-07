package edu.utsa.checkedin;

import android.content.Context;
import android.location.Location;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class TripManager {

    private final Context context;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final DatabaseReference rtdb = FirebaseDatabase.getInstance().getReference();
    private final String userId;

    public TripManager(Context context, String userId) {
        this.context = context;
        this.userId = userId;
    }

    public void beginNewTrip(Location location) {
        // Firestore: new trip
        Map<String, Object> tripData = new HashMap<>();
        tripData.put("startTime", FieldValue.serverTimestamp());
        tripData.put("latitude", location.getLatitude());
        tripData.put("longitude", location.getLongitude());

        db.collection("trips")
                .document(userId)
                .set(tripData);

        // Realtime DB: set initial location
        Map<String, Object> locMap = new HashMap<>();
        locMap.put("latitude", location.getLatitude());
        locMap.put("longitude", location.getLongitude());

        rtdb.child("users").child(userId).child("location").setValue(locMap);
    }

    public void stopTrip() {
        // Firestore: mark trip end
        db.collection("trips")
                .document(userId)
                .update("endTime", FieldValue.serverTimestamp());

        // Realtime DB: remove location
        rtdb.child("users").child(userId).child("location").removeValue();
    }

    public void updateTripLocation(Location location) {
        // Firestore: live update
        db.collection("trips")
                .document(userId)
                .update("latitude", location.getLatitude(),
                        "longitude", location.getLongitude(),
                        "lastUpdated", FieldValue.serverTimestamp());

        // Realtime DB: live update
        Map<String, Object> locMap = new HashMap<>();
        locMap.put("latitude", location.getLatitude());
        locMap.put("longitude", location.getLongitude());
        locMap.put("timestamp", System.currentTimeMillis());

        rtdb.child("users").child(userId).child("location").setValue(locMap);
    }
}
