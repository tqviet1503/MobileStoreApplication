package com.example.mobilestore.ui.base;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilestore.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.mobilestore.navigation.Navigation;

public abstract class BaseActivity extends AppCompatActivity {
    protected void setupBottomNavigation(int selectedItemId) {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        Navigation.setupBottomNavigation(this, navView, selectedItemId);
    }
}
