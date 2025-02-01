package com.example.hellocrud;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.cloudinary.android.MediaManager;
import com.example.hellocrud.MainActivity;
import com.example.hellocrud.R;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.
                FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // Show the activity in full screen

        setContentView(R.layout.activity_main);

        try {
            initConfig();
        } catch (Exception e) {
            Log.d("Media", String.valueOf(e));
        }

        // Navigate to MainActivity after a delay
        new Handler().postDelayed(() -> {
            startActivity(new Intent(getApplicationContext(), StuSignUp.class));
            finish();
        }, 1500);
    }

    private void initConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dcug05lid");
        config.put("api_key", "511283816475658");
        config.put("api_secret", "uYqO4iHGMbNutRuNxMaDwuARo2k");
        // config.put("secure", true);
        MediaManager.init(this, config);
    }
}