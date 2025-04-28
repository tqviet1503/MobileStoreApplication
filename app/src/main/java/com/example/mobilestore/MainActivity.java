package com.example.mobilestore;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilestore.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
}