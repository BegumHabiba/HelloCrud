package com.example.hellocrud;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class StuSignUp extends AppCompatActivity {
    private EditText nameEditText, emailEditText, passEditText, batchEditText, idEditText;
    private String name, email, pass, batch, id;
    private Button submit, adminSignupButton, teacherSignupButton;
    private TextView login;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    // Regular expressions for validation
    private final Pattern namePattern = Pattern.compile("[a-zA-Z ._]+");
    private final Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private final Pattern passPattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stu_sign_up);

        // Adjust window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        nameEditText = findViewById(R.id.name);
        emailEditText = findViewById(R.id.email);
        passEditText = findViewById(R.id.pass);
        batchEditText = findViewById(R.id.et_batch);
        idEditText = findViewById(R.id.et_id);
        submit = findViewById(R.id.submit);
        login = findViewById(R.id.login);
        progressBar = findViewById(R.id.progressBar);
        adminSignupButton = findViewById(R.id.admin_signup);
        teacherSignupButton = findViewById(R.id.teacher_signup);

        // Firebase setup
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Handle submit button click (Student Signup)
        submit.setOnClickListener(v -> {
            name = nameEditText.getText().toString().trim();
            email = emailEditText.getText().toString().trim();
            pass = passEditText.getText().toString().trim();
            batch = batchEditText.getText().toString().trim();
            id = idEditText.getText().toString().trim();

            // Validate inputs
            if (!validateInputs()) return;

            // Show progress bar
            progressBar.setVisibility(View.VISIBLE);

            // Register the student user
            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    sendEmailVerification(user); // Send email verification

                    // Store user info in Firestore (excluding the password)
                    assert user != null;
                    DocumentReference df = firestore.collection("Students").document(user.getUid());
                    Map<String, String> userInfo = new HashMap<>();
                    userInfo.put("email", email);
                    userInfo.put("name", name);
                    userInfo.put("batch", batch);
                    userInfo.put("id", id);
                    userInfo.put("uid", user.getUid());

                    df.set(userInfo).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Successfully Registered!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), StuLoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error saving data: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "User Already Exists!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        // Handle login button click
        login.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), StuLoginActivity.class));
            finish();
        });

        // Handle Admin Signup button click
        adminSignupButton.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), AdminSignUp.class)); // Replace with your Admin activity
            finish();
        });

        // Handle Teacher Signup button click
        teacherSignupButton.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), TeaSignUp.class)); // Replace with your Teacher activity
            finish();
        });
    }

    // Validate inputs
    private boolean validateInputs() {
        if (name.isEmpty()) {
            nameEditText.setError("Name is required!");
            nameEditText.requestFocus();
            return false;
        } else if (!namePattern.matcher(name).matches()) {
            nameEditText.setError("Name can only contain alphabets!");
            nameEditText.requestFocus();
            return false;
        } else if (email.isEmpty()) {
            emailEditText.setError("Email is required!");
            emailEditText.requestFocus();
            return false;
        } else if (!emailPattern.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email format!");
            emailEditText.requestFocus();
            return false;
        } else if (pass.isEmpty()) {
            passEditText.setError("Password is required!");
            passEditText.requestFocus();
            return false;
        } else if (!passPattern.matcher(pass).matches()) {
            passEditText.setError("Password must include uppercase, lowercase, digit, and be 8-20 characters long!");
            passEditText.requestFocus();
            return false;
        } else if (batch.isEmpty()) {
            batchEditText.setError("Batch is required!");
            batchEditText.requestFocus();
            return false;
        } else if (id.isEmpty()) {
            idEditText.setError("ID is required!");
            idEditText.requestFocus();
            return false;
        }
        return true;
    }

    // Send email verification
    private void sendEmailVerification(FirebaseUser user) {
        if (user != null && !user.isEmailVerified()) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Verification email sent.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error sending verification email.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
