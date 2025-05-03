package com.example.mobilestore.ui.customer_profile;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mobilestore.R;

public class ProfileActivity extends AppCompatActivity {

    // UI components
    private TextView titleTextView;
    private ImageView avatarImageView, customerPhotoImageView;
    private TextView customerNameTextView, customerIdTextView, customerStatusTextView;
    private TextView customerPhoneTextView, customerEmailTextView, customerAddressTextView;
    private TextView totalOrdersTextView, totalSpentTextView, loyaltyPointsTextView;
    private ImageButton editPhoneButton, editEmailButton, editAddressButton;
    private Button viewOrdersButton, contactCustomerButton;

    // Customer data
    private Customer customer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_profile);

            // Initialize views
            initializeViews();

            // Load customer data
            loadCustomerData();

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

    private void loadCustomerData() {
        try {
            // In a real app, this would load data from a database or shared preferences
            // For this example, we create a sample customer
            customer = new Customer(
                    "John Doe",
                    "#12345",
                    "Active Customer",
                    "+1 234 567 8900",
                    "john.doe@example.com",
                    "123 Main Street, City, Country",
                    12,
                    "$1,234",
                    250
            );
        } catch (Exception e) {
            Toast.makeText(this, "Error loading customer data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Create a default customer if loading fails
            customer = new Customer(
                    "Default User",
                    "#00000",
                    "Customer",
                    "+1 000 000 0000",
                    "default@example.com",
                    "Default Address",
                    0,
                    "$0",
                    0
            );
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

            // Safely set customer details with null checks
            if (customer != null) {
                if (customerNameTextView != null) {
                    customerNameTextView.setText(customer.getName());
                }

                if (customerIdTextView != null) {
                    customerIdTextView.setText("ID: " + customer.getId());
                }

                if (customerStatusTextView != null) {
                    customerStatusTextView.setText(customer.getStatus());
                }

                if (customerPhoneTextView != null) {
                    customerPhoneTextView.setText(customer.getPhone());
                }

                if (customerEmailTextView != null) {
                    customerEmailTextView.setText(customer.getEmail());
                }

                if (customerAddressTextView != null) {
                    customerAddressTextView.setText(customer.getAddress());
                }

                // Set purchase statistics
                if (totalOrdersTextView != null) {
                    totalOrdersTextView.setText(String.valueOf(customer.getTotalOrders()));
                }

                if (totalSpentTextView != null) {
                    totalSpentTextView.setText(customer.getTotalSpent());
                }

                if (loyaltyPointsTextView != null) {
                    loyaltyPointsTextView.setText(String.valueOf(customer.getLoyaltyPoints()));
                }
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
                    showEditDialog("Phone Number", customer.getPhone(), phone -> {
                        customer.setPhone(phone);
                        if (customerPhoneTextView != null) {
                            customerPhoneTextView.setText(phone);
                        }
                        Toast.makeText(ProfileActivity.this, "Phone number updated", Toast.LENGTH_SHORT).show();
                    });
                });
            }

            // Edit email button
            if (editEmailButton != null) {
                editEmailButton.setOnClickListener(view -> {
                    showEditDialog("Email", customer.getEmail(), email -> {
                        customer.setEmail(email);
                        if (customerEmailTextView != null) {
                            customerEmailTextView.setText(email);
                        }
                        Toast.makeText(ProfileActivity.this, "Email updated", Toast.LENGTH_SHORT).show();
                    });
                });
            }

            // Edit address button
            if (editAddressButton != null) {
                editAddressButton.setOnClickListener(view -> {
                    showEditDialog("Address", customer.getAddress(), address -> {
                        customer.setAddress(address);
                        if (customerAddressTextView != null) {
                            customerAddressTextView.setText(address);
                        }
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

            // Contact customer button
            if (contactCustomerButton != null) {
                contactCustomerButton.setOnClickListener(view -> {
                    // In a real app, this would open communication options
                    Toast.makeText(ProfileActivity.this, "Contact options would be shown here", Toast.LENGTH_SHORT).show();
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

    // Customer model class
    public static class Customer {
        private String name;
        private String id;
        private String status;
        private String phone;
        private String email;
        private String address;
        private int totalOrders;
        private String totalSpent;
        private int loyaltyPoints;

        public Customer(String name, String id, String status, String phone, String email,
                        String address, int totalOrders, String totalSpent, int loyaltyPoints) {
            this.name = name;
            this.id = id;
            this.status = status;
            this.phone = phone;
            this.email = email;
            this.address = address;
            this.totalOrders = totalOrders;
            this.totalSpent = totalSpent;
            this.loyaltyPoints = loyaltyPoints;
        }

        // Getters
        public String getName() {
            return name != null ? name : "";
        }

        public String getId() {
            return id != null ? id : "";
        }

        public String getStatus() {
            return status != null ? status : "";
        }

        public String getPhone() {
            return phone != null ? phone : "";
        }

        public String getEmail() {
            return email != null ? email : "";
        }

        public String getAddress() {
            return address != null ? address : "";
        }

        public int getTotalOrders() {
            return totalOrders;
        }

        public String getTotalSpent() {
            return totalSpent != null ? totalSpent : "$0";
        }

        public int getLoyaltyPoints() {
            return loyaltyPoints;
        }

        // Setters
        public void setPhone(String phone) {
            this.phone = phone;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }
}