package com.example.hellocrud;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class TeaSignUp extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passEditText, departmentEditText;
    private String name, email, pass, department;
    private Button submitButton;
    private TextView loginTextView;
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
        setContentView(R.layout.activity_tea_sign_up);

        // Initialize UI elements
        nameEditText = findViewById(R.id.name);
        emailEditText = findViewById(R.id.email);
        passEditText = findViewById(R.id.pass);
        departmentEditText = findViewById(R.id.et_department);
        submitButton = findViewById(R.id.submit);
        loginTextView = findViewById(R.id.login);
        progressBar = findViewById(R.id.progressBar);

        // Firebase setup
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Handle submit button click
        submitButton.setOnClickListener(v -> {
            name = nameEditText.getText().toString().trim();
            email = emailEditText.getText().toString().trim();
            pass = passEditText.getText().toString().trim();
            department = departmentEditText.getText().toString().trim();

            // Validate inputs
            if (!validateInputs()) return;

            // Show progress bar
            progressBar.setVisibility(View.VISIBLE);

            // Register the user
            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    // Store user info in Firestore (excluding the password)
                    DocumentReference df = firestore.collection("Teachers").document(auth.getCurrentUser().getUid());
                    Map<String, String> userInfo = new HashMap<>();
                    userInfo.put("email", email);
                    userInfo.put("name", name);
                    userInfo.put("department", department);

                    df.set(userInfo).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Successfully Registered!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), TeaSignUp.class));
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error saving data: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Handle login button click
        loginTextView.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), TeaSignUp.class));
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
        } else if (department.isEmpty()) {
            departmentEditText.setError("Department is required!");
            departmentEditText.requestFocus();
            return false;
        }
        return true;
    }
}
