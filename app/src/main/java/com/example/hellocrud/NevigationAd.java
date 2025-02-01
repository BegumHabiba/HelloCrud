package com.example.hellocrud;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class NevigationAd extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_IMAGE_URI = "profileImageUri";

    DrawerLayout drawerLayout;
    ImageButton imageButton;
    NavigationView navigationView;
    TextView headerName, headerEmail;
    ImageView headerImage;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nevigation_ad);

        drawerLayout = findViewById(R.id.main);
        imageButton = findViewById(R.id.ButtomToggle);
        navigationView = findViewById(R.id.NevigationView1); // Ensure the ID matches your XML

        // Set up the header layout views
        View headerView = navigationView.getHeaderView(0);
        headerName = headerView.findViewById(R.id.header1);
        headerEmail = headerView.findViewById(R.id.header2);
        headerImage = headerView.findViewById(R.id.imageview);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load saved image from SharedPreferences
        loadProfileImage();

        // Set up image click listener to open gallery
        headerImage.setOnClickListener(v -> openGallery());

        // Fetch teacher data from Firestore
        fetchTeacherData();

        // Open navigation drawer when the toggle button is clicked
        imageButton.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(items1 -> {
            int itemId = items1.getItemId();

            if (itemId == R.id.navCre) {
                showToastAndNavigate("Create Clicked", CreateActivity.class);
            } else if (itemId == R.id.navRe) {
                showToastAndNavigate("Read Clicked", TeaReadActivity.class);
            } else if (itemId == R.id.navLogout) {
                Toast.makeText(NevigationAd.this, "Logging Out...", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(NevigationAd.this, TeaLogin.class));
                finish();
            } else {
                Toast.makeText(NevigationAd.this, "Unknown Option", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START); // Close the drawer
            return true;
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Display the selected image in the header ImageView
                headerImage.setImageURI(selectedImageUri);
                // Save image URI to SharedPreferences
                saveProfileImage(selectedImageUri.toString());
            }
        }
    }

    private void fetchTeacherData() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("Teachers").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            String name = document.getString("name");
                            String email = document.getString("email");

                            // Update the header with teacher data
                            headerName.setText(name);
                            headerEmail.setText(email);
                        } else {
                            Toast.makeText(NevigationAd.this, "Failed to fetch teacher data", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void showToastAndNavigate(String message, Class<?> targetActivity) {
        Toast.makeText(NevigationAd.this, message, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(NevigationAd.this, targetActivity));
    }

    private void saveProfileImage(String imageUri) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_IMAGE_URI, imageUri);
        editor.apply();
    }

    private void loadProfileImage() {
        String savedImageUri = sharedPreferences.getString(KEY_IMAGE_URI, null);
        if (savedImageUri != null) {
            headerImage.setImageURI(Uri.parse(savedImageUri));
        }
    }
}
