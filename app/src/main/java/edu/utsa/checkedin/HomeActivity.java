package edu.utsa.checkedin;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private Button beginTripButton;
    private Button findFriendsButton;
    private Button addFriendsButton;
    private Button settingsButton;


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        beginTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CollectionActivity.this, MainActivity.class);
            startActivity(intent);
            }
        });

    }
}
