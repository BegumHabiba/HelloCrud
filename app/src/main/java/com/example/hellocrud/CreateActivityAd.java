package com.example.hellocrud;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class CreateActivityAd extends AppCompatActivity {

    private static final int PDF_REQ = 1;

    private EditText etAdminName, etCourseId, etCourseName, etYearName;
    private RadioGroup radioGroupExamType, radioGroupAdminType;
    private ImageView pdfIn;
    private ProgressBar progressBar;
    private Uri pdfUri;
    private DatabaseReference reference;
    private String pdfUrl;
    private String selectedSemester, selectedExamType, selectedPdfType;

    @SuppressLint("MissingInflatedId")
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ad);

        // Initialize UI components
        etAdminName = findViewById(R.id.et_admin_name1);
        etCourseId = findViewById(R.id.et_course_id_admin1);
        etCourseName = findViewById(R.id.et_course_name_admin1);
        etYearName = findViewById(R.id.et_admin_year1);
        Spinner spinner = findViewById(R.id.spinner_admin_semester1);
        radioGroupExamType = findViewById(R.id.radioGroup_exam_type1);
        radioGroupAdminType = findViewById(R.id.radioGroup_admin_type1);
        pdfIn = findViewById(R.id.pdf_in_admin1);
        Button btnInsertFile = findViewById(R.id.btnInsertFile_admin1);
        progressBar = findViewById(R.id.progressBar_admin1);

        // Set up the Spinner with semester options
        String[] semesters = {"Select a Semester", "Fall", "Summer", "Winter"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, semesters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Spinner item selection listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedSemester = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Optional: Handle case when no item is selected
            }
        });

        reference = FirebaseDatabase.getInstance().getReference().child("AdminPDFs");

        // Select PDF ImageView
        pdfIn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(CreateActivityAd.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openPdfSelector();
            } else {
                ActivityCompat.requestPermissions(CreateActivityAd.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PDF_REQ);
            }
        });

        // Submit Data Button
        btnInsertFile.setOnClickListener(v -> submitData());
    }

    private void openPdfSelector() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PDF_REQ);
    }

    private void submitData() {
        String adminName = etAdminName.getText().toString().trim();
        String courseId = etCourseId.getText().toString().trim();
        String courseName = etCourseName.getText().toString().trim();
        String yearName = etYearName.getText().toString().trim();
        int selectedExamTypeId = radioGroupExamType.getCheckedRadioButtonId();
        String examType = null;
        int selectedPdfTypeId = radioGroupAdminType.getCheckedRadioButtonId();
        String pdfType = null;

        if (selectedExamTypeId == R.id.radio_mid_admin1) {
            examType = "Mid";
        } else if (selectedExamTypeId == R.id.radio_final_admin1) {
            examType = "Final";
        }

        if (selectedPdfTypeId == R.id.radio_question_admin1) {
            pdfType = "Question";
        } else if (selectedPdfTypeId == R.id.radio_note_admin1) {
            pdfType = "Note";
        }

        if (adminName.isEmpty()) {
            etAdminName.setError("Admin Name is required");
            return;
        }
        if (courseId.isEmpty()) {
            etCourseId.setError("Course ID is required");
            return;
        }
        if (courseName.isEmpty()) {
            etCourseName.setError("Course Name is required");
            return;
        }
        if (yearName.isEmpty()) {
            etYearName.setError("Year is required");
            return;
        }
        if (examType == null) {
            Toast.makeText(CreateActivityAd.this, "Please select an exam type", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pdfType == null) {
            Toast.makeText(CreateActivityAd.this, "Please select a PDF type", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pdfUri == null) {
            Toast.makeText(CreateActivityAd.this, "Please select a PDF", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedSemester == null || selectedSemester.equals("Select a Semester")) {
            Toast.makeText(CreateActivityAd.this, "Please select a semester", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        uploadPdfToCloudinary(pdfUri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PDF_REQ && resultCode == RESULT_OK && data != null) {
            pdfUri = data.getData();
            pdfIn.setImageResource(R.drawable.select_pdf); // Update image to indicate selection
            Toast.makeText(CreateActivityAd.this, "PDF selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPdfToCloudinary(Uri pdfUri) {
        MediaManager.get().upload(pdfUri)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {

                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {

                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        pdfUrl = (String) resultData.get("secure_url");
                        uploadData(pdfUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(CreateActivityAd.this, "Error uploading PDF: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {

                    }
                }).dispatch();
    }

    private void uploadData(String pdfUrl) {
        String key = reference.push().getKey();
        String adminName = etAdminName.getText().toString().trim();
        String courseId = etCourseId.getText().toString().trim();
        String courseName = etCourseName.getText().toString().trim();
        String yearName = etYearName.getText().toString().trim();
        String examType = (radioGroupExamType.getCheckedRadioButtonId() == R.id.radio_mid_admin1) ? "Mid" : "Final";
        String pdfType = (radioGroupAdminType.getCheckedRadioButtonId() == R.id.radio_question_admin1) ? "Question" : "Note";

        // Create a model object to store the data
        Model data = new Model(adminName, courseId, courseName, examType, yearName, selectedSemester, pdfType, pdfUrl, key);
        reference.child(key).setValue(data)
                .addOnSuccessListener(unused -> {
                    // Reset form fields after successful upload
                    etAdminName.setText("");
                    etCourseId.setText("");
                    etCourseName.setText("");
                    etYearName.setText("");
                    pdfIn.setImageResource(R.drawable.pdf); // Reset image to default
                    Toast.makeText(CreateActivityAd.this, "PDF Added Successfully", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateActivityAd.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }
}
