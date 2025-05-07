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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.utsa.checkedin.model.Friend;

public class AddFriendsActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button searchButton;
    private Button addFriendButton;
    private LinearLayout friendContainer;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private DatabaseReference friendsRef;
    private List<Friend> myFriends = new ArrayList<>();
    LayoutInflater inflater = LayoutInflater.from(this);

    private String currentUserId;
    private String foundUserId;  // Stores UID of searched user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriends);

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        searchButton = findViewById(R.id.searchButton);
        addFriendButton = findViewById(R.id.addFriendButton);

        friendContainer = findViewById(R.id.friendContainer);


        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
        }

        currentUserId = user.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        friendsRef = FirebaseDatabase.getInstance().getReference("friends");

        loadFriendCircle();

        // Search user by email
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

        // Add friend to both users' friend lists
        addFriendButton.setOnClickListener(v -> {
            if (foundUserId != null && !foundUserId.isEmpty()) {
                usersRef.child(foundUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String foundUserEmail = snapshot.child("email").getValue(String.class);

                            // Add friend to current user's list
                            Friend friends = new Friend(foundUserId, foundUserEmail);
                            friendsRef.child(currentUserId).child(foundUserId).setValue(friends);

                            // Add current user to friend's list
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            Friend reverseEntry = new Friend(currentUser.getUid(), currentUser.getEmail());
                            friendsRef.child(foundUserId).child(currentUserId).setValue(reverseEntry);

                            Toast.makeText(AddFriendsActivity.this, "Friend added!", Toast.LENGTH_SHORT).show();
                            addFriendButton.setEnabled(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddFriendsActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        addFriendButton.setEnabled(false);
    }

    private void loadFriendCircle() {
        myFriends.clear();

        friendsRef.child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                            Friend friend = friendSnapshot.getValue(Friend.class);
                            if (friend != null) {
                                myFriends.add(friend);


                                View cardView = inflater.inflate(R.layout.friend_card, friendContainer, false);
                                TextView friendName = cardView.findViewById(R.id.textFriendEmail);
                                friendName.setText(friend.getEmail()); // or getName() if you store names
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
