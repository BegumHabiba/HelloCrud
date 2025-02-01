package com.example.hellocrud;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class StudentDashBoard extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dash_board);

        // Initialize buttons
        Button btnCreate = findViewById(R.id.btnCreate);
        Button btnRead = findViewById(R.id.btnRead);
        Button btnCGPA = findViewById(R.id.btnCGPA); // Initialize CGPA button

        // Set click listeners for each button
        btnCreate.setOnClickListener(v -> openCreateActivity());
        btnRead.setOnClickListener(v -> openReadActivity());
        btnCGPA.setOnClickListener(v -> openCGPAActivity()); // Set listener for CGPA button
    }

    // Method to open CreateActivity
    private void openCreateActivity() {
        Intent intent = new Intent(StudentDashBoard.this, CreateActivity.class);
        startActivity(intent);
    }

    // Method to open ReadActivity
    private void openReadActivity() {
        Intent intent = new Intent(StudentDashBoard.this, StuReadActivity.class);
        startActivity(intent);
    }

    // Method to open CGPACalculatorActivity
    private void openCGPAActivity() {
        Intent intent = new Intent(StudentDashBoard.this, CGPACalculatorActivity.class);
        startActivity(intent);
    }
}
