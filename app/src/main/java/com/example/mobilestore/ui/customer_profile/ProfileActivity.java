package com.example.mobilestore.ui.customer_profile;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri; // Thêm import Uri
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mobilestore.R;
import com.example.mobilestore.data.customer.CustomerDataManager;

public class ProfileActivity extends AppCompatActivity {

    // UI components
    private TextView titleTextView;
    private ImageView avatarImageView, customerPhotoImageView;
    private TextView customerNameTextView, customerIdTextView, customerStatusTextView;
    private TextView customerPhoneTextView, customerEmailTextView, customerAddressTextView;
    private TextView totalOrdersTextView, totalSpentTextView, loyaltyPointsTextView;
    private ImageButton editPhoneButton, editEmailButton, editAddressButton;
    private Button viewOrdersButton, contactCustomerButton;

    // Customer data manager
    private CustomerDataManager customerDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_profile);

            // Khởi tạo CustomerDataManager
            customerDataManager = CustomerDataManager.getInstance(this);

            // Initialize views
            initializeViews();

            // Update UI with customer data
            updateUI();

            // Set up button listeners
            setupButtonListeners();
        } catch (Resources.NotFoundException e) {
            Toast.makeText(this, "Profile layout not found: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish(); // Close activity if layout not found
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing profile screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void initializeViews() {
        try {
            // Find all views by ID
            titleTextView = findViewById(R.id.titleTextView);
            avatarImageView = findViewById(R.id.avatarImageView);
            customerPhotoImageView = findViewById(R.id.customerPhotoImageView);
            customerNameTextView = findViewById(R.id.customerNameTextView);
            customerIdTextView = findViewById(R.id.customerIdTextView);
            customerStatusTextView = findViewById(R.id.customerStatusTextView);
            customerPhoneTextView = findViewById(R.id.customerPhoneTextView);
            customerEmailTextView = findViewById(R.id.customerEmailTextView);
            customerAddressTextView = findViewById(R.id.customerAddressTextView);
            totalOrdersTextView = findViewById(R.id.totalOrdersTextView);
            totalSpentTextView = findViewById(R.id.totalSpentTextView);
            loyaltyPointsTextView = findViewById(R.id.loyaltyPointsTextView);
            editPhoneButton = findViewById(R.id.editPhoneButton);
            editEmailButton = findViewById(R.id.editEmailButton);
            editAddressButton = findViewById(R.id.editAddressButton);
            viewOrdersButton = findViewById(R.id.viewOrdersButton);
            contactCustomerButton = findViewById(R.id.contactCustomerButton);
        } catch (Exception e) {
            Toast.makeText(this, "Error finding profile views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        try {
            // Set customer photo
            if (customerPhotoImageView != null) {
                try {
                    customerPhotoImageView.setImageResource(R.drawable.default_user);
                } catch (Resources.NotFoundException e) {
                    // Use a default image if resource not found
                    customerPhotoImageView.setImageResource(android.R.drawable.ic_menu_myplaces);
                }
            }

            // Lấy thông tin khách hàng từ CustomerDataManager
            if (customerNameTextView != null) {
                customerNameTextView.setText(customerDataManager.getCustomerName());
            }

            if (customerIdTextView != null) {
                customerIdTextView.setText("ID: " + customerDataManager.getCustomerId());
            }

            if (customerStatusTextView != null) {
                customerStatusTextView.setText(customerDataManager.getCustomerStatus());
            }

            if (customerPhoneTextView != null) {
                customerPhoneTextView.setText(customerDataManager.getCustomerPhone());
            }

            if (customerEmailTextView != null) {
                customerEmailTextView.setText(customerDataManager.getCustomerEmail());
            }

            if (customerAddressTextView != null) {
                customerAddressTextView.setText(customerDataManager.getCustomerAddress());
            }

            // Hiển thị thông tin mua hàng
            if (totalOrdersTextView != null) {
                totalOrdersTextView.setText(String.valueOf(customerDataManager.getTotalOrders()));
            }

            if (totalSpentTextView != null) {
                totalSpentTextView.setText(customerDataManager.getFormattedTotalSpent());
            }

            if (loyaltyPointsTextView != null) {
                loyaltyPointsTextView.setText(String.valueOf(customerDataManager.getLoyaltyPoints()));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error updating profile UI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupButtonListeners() {
        try {
            // Edit phone button
            if (editPhoneButton != null) {
                editPhoneButton.setOnClickListener(view -> {
                    showEditDialog("Phone Number", customerDataManager.getCustomerPhone(), phone -> {
                        customerDataManager.setCustomerPhone(phone);
                        customerPhoneTextView.setText(phone);
                        Toast.makeText(ProfileActivity.this, "Phone number updated", Toast.LENGTH_SHORT).show();
                    });
                });
            }

            // Edit email button
            if (editEmailButton != null) {
                editEmailButton.setOnClickListener(view -> {
                    showEditDialog("Email", customerDataManager.getCustomerEmail(), email -> {
                        customerDataManager.setCustomerEmail(email);
                        customerEmailTextView.setText(email);
                        Toast.makeText(ProfileActivity.this, "Email updated", Toast.LENGTH_SHORT).show();
                    });
                });
            }

            // Edit address button
            if (editAddressButton != null) {
                editAddressButton.setOnClickListener(view -> {
                    showEditDialog("Address", customerDataManager.getCustomerAddress(), address -> {
                        customerDataManager.setCustomerAddress(address);
                        customerAddressTextView.setText(address);
                        Toast.makeText(ProfileActivity.this, "Address updated", Toast.LENGTH_SHORT).show();
                    });
                });
            }

            // View orders button
            if (viewOrdersButton != null) {
                viewOrdersButton.setOnClickListener(view -> {
                    // In a real app, this would navigate to an orders history screen
                    Toast.makeText(ProfileActivity.this, "Viewing orders history", Toast.LENGTH_SHORT).show();
                });
            }

            // Contact admin button
            if (contactCustomerButton != null) {
                contactCustomerButton.setOnClickListener(view -> {
                    // Mở ứng dụng email với địa chỉ admin@gmail.com
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:admin@gmail.com"));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Contact from Mobile Store App");

                    try {
                        startActivity(Intent.createChooser(emailIntent, "Send email using..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(ProfileActivity.this, "No email clients installed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up profile buttons: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditDialog(String fieldName, String currentValue, OnTextUpdatedListener listener) {
        try {
            // Create an EditText with the current value
            final android.widget.EditText input = new android.widget.EditText(this);
            input.setText(currentValue != null ? currentValue : "");

            // Create and show the dialog
            new AlertDialog.Builder(this)
                    .setTitle("Edit " + fieldName)
                    .setView(input)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String newValue = input.getText().toString().trim();
                        if (!newValue.isEmpty() && listener != null) {
                            listener.onTextUpdated(newValue);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing edit dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Interface for the edit dialog callbacks
    private interface OnTextUpdatedListener {
        void onTextUpdated(String newText);
    }

    // Handle back button
    @Override
    public void onBackPressed() {
        finish(); // Just finish activity instead of creating a new one
    }
}