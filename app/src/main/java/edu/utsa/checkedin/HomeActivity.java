package edu.utsa.checkedin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import android.Manifest;

public class HomeActivity extends AppCompatActivity {

    private ToggleButton beginToggleButton = findViewById(R.id.beginToggleButton);
    private Button findFriendsButton = findViewById(R.id.findFriendsButton);
    private Button addFriendsButton =  findViewById(R.id.addFriendsButton);
    private Button settingsButton = findViewById(R.id.settingsButton);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

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

//        findFriendsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(FindFriendsActivity.this, MainActivity.class);
//                startActivity(intent);
//            }
//        });
//
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



    }

}
