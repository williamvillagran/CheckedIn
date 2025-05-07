package edu.utsa.checkedin;

import android.content.Context;
import android.location.Location;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class TripManager {

    private final Context context;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId;

    public TripManager(Context context, String userId) {
        this.context = context;
        this.userId = userId;
    }

    public void beginNewTrip(Location location) {
        // Start location tracking
        Map<String, Object> tripData = new HashMap<>();
        tripData.put("startTime", FieldValue.serverTimestamp());
        tripData.put("latitude", location.getLatitude());
        tripData.put("longitude", location.getLongitude());

        db.collection("trips")
                .document(userId)
                .set(tripData);
    }

    public void stopTrip() {
        // Stop location tracking
        db.collection("trips")
                .document(userId)
                .update("endTime", FieldValue.serverTimestamp());
    }

    public void updateTripLocation(Location location) {
        db.collection("trips")
                .document(userId)
                .update("latitude", location.getLatitude(),
                        "longitude", location.getLongitude(),
                        "lastUpdated", FieldValue.serverTimestamp());
    }
}