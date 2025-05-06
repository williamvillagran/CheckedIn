package edu.utsa.checkedin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

//From G4G for "EditText"
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText loginEntry;
    private EditText passwordEntry;
    private ImageButton loginButton;
    private ImageButton registerButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        loginEntry = findViewById(R.id.loginEntry);
        passwordEntry = findViewById(R.id.passwordEntry);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(v -> loginUserAccount());

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });



    }
    private void loginUserAccount(){
        String email = loginEntry.getText().toString().trim();
        String password = passwordEntry.getText().toString().trim();

        // Validate input
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Login existing user
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login successful
                            Toast.makeText(MainActivity.this, "Login successful!!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            // Login failed
                            Toast.makeText(MainActivity.this, "Login failed!!", Toast.LENGTH_LONG).show();
                        }
                    }
                });






    }


}