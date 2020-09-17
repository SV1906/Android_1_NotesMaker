package com.example.notesmaker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends AppCompatActivity {
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        View overlay = findViewById(R.id.mylayout);

        overlay.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                |  View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                |  View.SYSTEM_UI_FLAG_FULLSCREEN);


        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
                finish();
            }
        }, 3000);
    }
}