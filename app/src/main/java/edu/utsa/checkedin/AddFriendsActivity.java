package edu.utsa.checkedin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.utsa.checkedin.model.Friend;

public class AddFriendsActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button searchButton;
    private Button addFriendButton;
    private LinearLayout friendContainer;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private FirebaseFirestore fs;
    private List<Friend> myFriends = new ArrayList<>();
    private LayoutInflater inflater;

    private String currentUserId;
    private String foundUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriends);

        emailInput = findViewById(R.id.emailInput);
        searchButton = findViewById(R.id.searchButton);
        addFriendButton = findViewById(R.id.addFriendButton);
        friendContainer = findViewById(R.id.friendContainer);

        inflater = LayoutInflater.from(this);
        auth = FirebaseAuth.getInstance();
        fs = FirebaseFirestore.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = user.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadFriendCircle();

        searchButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show();
                return;
            }

            usersRef.orderByChild("email").equalTo(email)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                    foundUserId = userSnapshot.getKey();

                                    if (foundUserId.equals(currentUserId)) {
                                        Toast.makeText(AddFriendsActivity.this, "You can't add yourself!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    Toast.makeText(AddFriendsActivity.this, "User found! Tap 'Add Friend'.", Toast.LENGTH_SHORT).show();
                                    addFriendButton.setEnabled(true);
                                    return;
                                }
                            } else {
                                Toast.makeText(AddFriendsActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                                addFriendButton.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(AddFriendsActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        addFriendButton.setOnClickListener(v -> {
            if (foundUserId != null && !foundUserId.isEmpty()) {
                usersRef.child(foundUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(AddFriendsActivity.this, "Found user data missing in DB", Toast.LENGTH_LONG).show();
                            return;
                        }

                        String foundUserEmail = snapshot.child("email").getValue(String.class);
                        if (foundUserEmail == null) {
                            Toast.makeText(AddFriendsActivity.this, "Email field missing in target user", Toast.LENGTH_LONG).show();
                            return;
                        }

                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser == null || currentUser.getEmail() == null) {
                            Toast.makeText(AddFriendsActivity.this, "Current user invalid", Toast.LENGTH_LONG).show();
                            return;
                        }

                        Toast.makeText(AddFriendsActivity.this, "Writing friend info...", Toast.LENGTH_SHORT).show();

                        Friend friend = new Friend(foundUserId, foundUserEmail);
                        Friend reverseEntry = new Friend(currentUser.getUid(), currentUser.getEmail());

                        // Realtime DB: write to /users/uid/friends
                        usersRef.child(currentUserId).child("friends").child(foundUserId).setValue(friend)
                                .addOnSuccessListener(aVoid -> Toast.makeText(AddFriendsActivity.this, "RTDB write success", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(AddFriendsActivity.this, "RTDB write failed: " + e.getMessage(), Toast.LENGTH_LONG).show());

                        usersRef.child(foundUserId).child("friends").child(currentUserId).setValue(reverseEntry)
                                .addOnSuccessListener(aVoid -> {})
                                .addOnFailureListener(e -> Toast.makeText(AddFriendsActivity.this, "RTDB reverse write failed: " + e.getMessage(), Toast.LENGTH_LONG).show());

                        // Firestore
                        Map<String, Object> friendEntry = new HashMap<>();
                        friendEntry.put("email", foundUserEmail);
                        friendEntry.put("uid", foundUserId);

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(currentUserId)
                                .collection("friends")
                                .document(foundUserId)
                                .set(friendEntry)
                                .addOnSuccessListener(aVoid -> Toast.makeText(AddFriendsActivity.this, "FS write success", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(AddFriendsActivity.this, "FS write failed: " + e.getMessage(), Toast.LENGTH_LONG).show());

                        Map<String, Object> reverseEntryMap = new HashMap<>();
                        reverseEntryMap.put("email", currentUser.getEmail());
                        reverseEntryMap.put("uid", currentUser.getUid());


                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(foundUserId)
                                .collection("friends")
                                .document(currentUserId)
                                .set(reverseEntryMap)
                                .addOnFailureListener(e -> Toast.makeText(AddFriendsActivity.this, "FS reverse failed: " + e.getMessage(), Toast.LENGTH_LONG).show());

                        addFriendButton.setEnabled(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddFriendsActivity.this, "User fetch cancelled: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        addFriendButton.setEnabled(false);
    }

    private void loadFriendCircle() {
        myFriends.clear();

        usersRef.child(currentUserId).child("friends")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                            Friend friend = friendSnapshot.getValue(Friend.class);
                            if (friend != null) {
                                myFriends.add(friend);

                                View cardView = inflater.inflate(R.layout.friend_card, friendContainer, false);
                                TextView friendName = cardView.findViewById(R.id.textFriendEmail);
                                friendName.setText(friend.getEmail());
                                friendContainer.addView(cardView);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddFriendsActivity.this, "Could not load friends", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
