package edu.utsa.checkedin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Button signoutButton = findViewById(R.id.signout_button);

        // Sign out button
        signoutButton.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();

            // Navigate to MainActivity after signing out
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            finish();
        });
    }
}
