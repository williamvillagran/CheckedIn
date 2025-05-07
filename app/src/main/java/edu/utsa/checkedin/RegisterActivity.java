package edu.utsa.checkedin;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailTextView, passwordTextView;
    private ImageButton button;
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        emailTextView = findViewById(R.id.emailRegister);
        passwordTextView = findViewById(R.id.passwordRegister);
        button = findViewById(R.id.registerButton);

        button.setOnClickListener(v -> registerNewUser());
    }


    private void registerNewUser() {
        String email = emailTextView.getText().toString().trim();
        String password = passwordTextView.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter credentials", Toast.LENGTH_LONG).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        user = auth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();

                            // 🔥 Firestore reference
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            // Create user profile
                            Map<String, Object> userProfile = new HashMap<>();
                            userProfile.put("email", email);
                            userProfile.put("friends", new HashMap<>()); // empty friends map

                            db.collection("users")
                                    .document(uid)
                                    .set(userProfile)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterActivity.this, "Failed to save user profile", Toast.LENGTH_LONG).show();
                                    });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed! Please try again later", Toast.LENGTH_LONG).show();
                    }
                });
    }

}