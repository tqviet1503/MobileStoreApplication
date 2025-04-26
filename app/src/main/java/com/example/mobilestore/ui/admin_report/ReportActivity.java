package com.example.mobilestore.ui.admin_report;

import android.os.Bundle;
import com.example.mobilestore.R;
import com.example.mobilestore.ui.base.BaseActivity;

public class ReportActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_report);
        setupBottomNavigation(R.id.navigation_dashboard);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ReportFragment())
                .commit();
        }
    }
}
