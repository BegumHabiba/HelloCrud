package com.example.hellocrud;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class TeaLogin extends AppCompatActivity {
    private EditText emailEditText, passEditText;
    private Button submit;
    private TextView register;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tea_login);

        // Initialize UI components
        emailEditText = findViewById(R.id.email);
        passEditText = findViewById(R.id.pass);
        submit = findViewById(R.id.submit);
        register = findViewById(R.id.register);
        progressBar = findViewById(R.id.progressBar);
        auth = FirebaseAuth.getInstance();

        // Login Button Click Listener
        submit.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String pass = passEditText.getText().toString().trim();

            // Validation for input fields
            if (email.isEmpty()) {
                emailEditText.setError("Email cannot be empty!");
                emailEditText.requestFocus();
                return;
            }
            if (pass.isEmpty()) {
                passEditText.setError("Password cannot be empty!");
                passEditText.requestFocus();
                return;
            }

            // Show progress bar
            progressBar.setVisibility(View.VISIBLE);

            // Firebase authentication for login
            auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // Hide progress bar
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            // Login successful and email verified
                            Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), Nevigation.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            finish();
                        } else {
                            // Email not verified
                            Toast.makeText(getApplicationContext(), "Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                            auth.signOut();
                        }
                    } else {
                        // Login failed
                        Toast.makeText(getApplicationContext(), "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        // Register Button Click Listener
        register.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), TeaSignUp.class));
        });
    }
}
