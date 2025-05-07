package edu.utsa.checkedin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

import edu.utsa.checkedin.model.Friend;

public class AddFriendsActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button searchButton;
    private Button addFriendButton;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private DatabaseReference friendsRef;
    private List<Friend> myFriends = new ArrayList<>();

    private String currentUserId;
    private String foundUserId;  // Stores UID of searched user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriends);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference("friends").child(currentUserId);

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        searchButton = findViewById(R.id.searchButton);
        addFriendButton = findViewById(R.id.addFriendButton);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
        }

        currentUserId = user.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

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
                friendsRef.child(currentUserId).child(foundUserId).setValue(true);
                friendsRef.child(foundUserId).child(currentUserId).setValue(true);
                Toast.makeText(this, "Friend added!", Toast.LENGTH_SHORT).show();
                addFriendButton.setEnabled(false);
            }
        });

        addFriendButton.setEnabled(false);  // Initially disabled
    }
}
