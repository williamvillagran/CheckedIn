package edu.utsa.checkedin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailTextView, passwordTextView;
    private Button button;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance();

        emailTextView = findViewById(R.id.emailRegister);
        passwordTextView = findViewById(R.id.passwordRegister);
        button = findViewById(R.id.registerButton);

        button.setOnClickListener(v -> registerNewUser());
    }



    private void registerNewUser() {
        // Get values from input fields
        String email = emailTextView.getText().toString().trim();
        String password = passwordTextView.getText().toString().trim();

        // Validate input
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter credentials", Toast.LENGTH_LONG).show();
            return;
        }

        // Register new user with Firebase
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_LONG).show();
                            // Navigate to MainActivity
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            finish();
                        } else {
                            // Registration failed
                            Toast.makeText(RegisterActivity.this, "Registration failed! Please try again later", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
