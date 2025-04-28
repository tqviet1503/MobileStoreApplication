package com.example.mobilestore.ui.admin_customer_bill;

import android.os.Bundle;

import com.example.mobilestore.R;
import com.example.mobilestore.ui.base.BaseActivity;

public class CustomerBillActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_customer_and_bill);
        setupBottomNavigation(R.id.navigation_customer);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CustomerBillFragment())
                .commit();
        }
    }
}
