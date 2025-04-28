package com.example.mobilestore.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilestore.R;
import com.example.mobilestore.ui.admin_report.ReportActivity;
import com.example.mobilestore.ui.admin_customer_bill.CustomerBillActivity;
import com.example.mobilestore.ui.admin_storage.StorageActivity;
import com.example.mobilestore.ui.admin_product.ProductActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.function.Consumer;

public class Navigation {

    public static void navigateTo(Context context, Class<?> targetActivity) {
        Intent intent = new Intent(context, targetActivity);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    public static void navigateTo(Context context, Class<?> targetActivity, Consumer<Intent> configureIntent, boolean finish) {
        Intent intent = new Intent(context, targetActivity);
        if (configureIntent != null) {
            configureIntent.accept(intent);
        }
        context.startActivity(intent);
        if (finish && context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    public static void setupBottomNavigation(AppCompatActivity activity, BottomNavigationView navView, int selectedItemId) {
        navView.setSelectedItemId(selectedItemId);

        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d("Navigation", "Selected item ID: " + itemId);

            // Check if the selected item is already the current activity's item
            if (itemId == selectedItemId) {
                return true; // Already on the selected item, no action needed
            }

            Class<?> targetActivity = null;
            if (itemId == R.id.navigation_dashboard) {
                targetActivity = ReportActivity.class;
            } else if (itemId == R.id.navigation_storage) {
                targetActivity = StorageActivity.class;
            } else if (itemId == R.id.navigation_customer) {
                targetActivity = CustomerBillActivity.class;
            } else if (itemId == R.id.navigation_product) {
                targetActivity = ProductActivity.class;
            }

            if (targetActivity != null) {
                navigateTo(activity, targetActivity);
                activity.finish(); // Optional: Close current activity if needed
                return true;
            }
            return false;
        });
    }
}