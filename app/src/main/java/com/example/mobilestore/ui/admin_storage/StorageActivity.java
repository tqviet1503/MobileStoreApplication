package com.example.mobilestore.ui.admin_storage;

import android.os.Bundle;
import com.example.mobilestore.R;
import com.example.mobilestore.ui.base.BaseActivity;

public class StorageActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_storage);
        setupBottomNavigation(R.id.navigation_storage);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new StorageFragment())
                .commit();
        }
    }
}
