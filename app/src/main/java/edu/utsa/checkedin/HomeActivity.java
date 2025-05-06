package edu.utsa.checkedin;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import android.Manifest;

public class HomeActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        ToggleButton beginToggleButton = findViewById(R.id.beginToggleButton);
        Button findFriendsButton = findViewById(R.id.findFriendsButton);
        Button addFriendsButton =  findViewById(R.id.addFriendsButton);
        Button settingsButton = findViewById(R.id.settingsButton);

        requestPermissions();

        beginToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    Toast.makeText(getApplicationContext(), "Sharing Has Started", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(getApplicationContext(), "Sharing Has Stopped", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, FindFriendsActivity.class);
                startActivity(intent);
            }
        });

//        addFriendsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(AddFriendsActivity.this, MainActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        settingsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
//                startActivity(intent);
//            }
//        });

    }

    private void requestPermissions() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.FOREGROUND_SERVICE
        };

        List<String> permissionsToRequest = new ArrayList<>();

        // Filter out the permissions that are not yet granted
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        // If there are permissions that need to be requested, ask the user for them
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]), // Convert list to array
                    PERMISSION_REQUEST_CODE // Pass the request code
            );
        } else {
            // All permissions are already granted
            Toast.makeText(this, "All permissions already granted", Toast.LENGTH_SHORT).show();
        }
    }


}
