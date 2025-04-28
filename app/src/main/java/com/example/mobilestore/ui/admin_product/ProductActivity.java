package com.example.mobilestore.ui.admin_product;

import android.os.Bundle;
import com.example.mobilestore.R;
import com.example.mobilestore.ui.base.BaseActivity;

public class ProductActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product);
        setupBottomNavigation(R.id.navigation_product);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ProductFragment())
                .commit();
        }
    }
}
