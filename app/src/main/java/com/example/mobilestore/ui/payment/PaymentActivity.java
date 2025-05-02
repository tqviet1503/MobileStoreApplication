package com.example.mobilestore.ui.payment;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mobilestore.R;

public class PaymentActivity extends AppCompatActivity {

    // UI components
    private TextView titleTextView, txtProductName, txtQuantity;
    private ImageView avatarImageView, imgProduct;
    private EditText edtName, edtAddress;
    private Button btnApplyCode, btnNote, btnOrder;
    private ImageView btnEditName, btnEditAddress;

    // Data
    private String productName;
    private String productPrice;
    private int quantity;
    private boolean isEditingName = false;
    private boolean isEditingAddress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.payment);

            // Initialize views
            initializeViews();

            // Get data from intent
            getDataFromIntent();

            // Set up UI with data
            setupUI();

            // Set up button listeners
            setupButtonListeners();
        } catch (Resources.NotFoundException e) {
            Toast.makeText(this, "Payment layout not found: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish(); // Close activity if layout not found
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing payment screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        try {
            // Find all views by ID
            titleTextView = findViewById(R.id.titleTextView);
            avatarImageView = findViewById(R.id.avatarImageView);
            txtProductName = findViewById(R.id.txtProductName);
            txtQuantity = findViewById(R.id.txtQuantity);
            imgProduct = findViewById(R.id.imgProduct);
            edtName = findViewById(R.id.edtName);
            edtAddress = findViewById(R.id.edtAddress);
            btnApplyCode = findViewById(R.id.btnApplyCode);
            btnNote = findViewById(R.id.btnNote);
            btnOrder = findViewById(R.id.btnOrder);
            btnEditName = findViewById(R.id.btnEditName);
            btnEditAddress = findViewById(R.id.btnEditAddress);
        } catch (Exception e) {
            Toast.makeText(this, "Error finding payment views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getDataFromIntent() {
        try {
            Intent intent = getIntent();
            if (intent != null) {
                productName = intent.getStringExtra("PRODUCT_NAME");
                productPrice = intent.getStringExtra("PRODUCT_PRICE");
                quantity = intent.getIntExtra("PRODUCT_QUANTITY", 1);

                // Validate data - set defaults if null
                if (productName == null) productName = "Sample Product";
                if (productPrice == null) productPrice = "$0";
            } else {
                // Default values if no intent data
                productName = "Sample Product";
                productPrice = "$0";
                quantity = 1;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error getting intent data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Set defaults
            productName = "Sample Product";
            productPrice = "$0";
            quantity = 1;
        }
    }

    private void setupUI() {
        try {
            // Set product name and quantity
            if (txtProductName != null) {
                txtProductName.setText("Product: " + productName + " - " + productPrice);
            }

            if (txtQuantity != null) {
                txtQuantity.setText("Quantity: " + quantity);
            }

            // Set product image
            if (imgProduct != null) {
                try {
                    imgProduct.setImageResource(R.drawable.phone_sample);
                } catch (Resources.NotFoundException e) {
                    // Use a default image if resource not found
                    imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }

            // Set default text for name and address fields
            if (edtName != null) {
                edtName.setText("John Doe");
                edtName.setEnabled(false);
            }

            if (edtAddress != null) {
                edtAddress.setText("123 Main St, Anytown, USA");
                edtAddress.setEnabled(false);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up payment UI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupButtonListeners() {
        try {
            // Edit Name button
            if (btnEditName != null && edtName != null) {
                btnEditName.setOnClickListener(view -> {
                    isEditingName = !isEditingName;
                    edtName.setEnabled(isEditingName);
                    if (isEditingName) {
                        edtName.requestFocus();
                        showKeyboard(edtName);
                        Toast.makeText(PaymentActivity.this, "You can now edit your name", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Edit Address button
            if (btnEditAddress != null && edtAddress != null) {
                btnEditAddress.setOnClickListener(view -> {
                    isEditingAddress = !isEditingAddress;
                    edtAddress.setEnabled(isEditingAddress);
                    if (isEditingAddress) {
                        edtAddress.requestFocus();
                        showKeyboard(edtAddress);
                        Toast.makeText(PaymentActivity.this, "You can now edit your address", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Apply Discount Code button
            if (btnApplyCode != null) {
                btnApplyCode.setOnClickListener(view -> {
                    showDiscountCodeDialog();
                });
            }

            // Notes button
            if (btnNote != null) {
                btnNote.setOnClickListener(view -> {
                    showNotesDialog();
                });
            }

            // Place Order button
            if (btnOrder != null) {
                btnOrder.setOnClickListener(view -> {
                    // Validate fields
                    if (validateFields()) {
                        // Place the order
                        placeOrder();
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up payment buttons: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showKeyboard(View view) {
        try {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        } catch (Exception e) {
            // Ignore keyboard errors - non-critical
        }
    }

    private void showDiscountCodeDialog() {
        try {
            // Create an EditText for the discount code
            final EditText input = new EditText(this);
            input.setHint("Enter discount code");

            // Create and show the dialog
            new AlertDialog.Builder(this)
                    .setTitle("Apply Discount Code")
                    .setView(input)
                    .setPositiveButton("Apply", (dialog, which) -> {
                        String discountCode = input.getText().toString().trim();
                        if (!discountCode.isEmpty()) {
                            // In a real app, validate the discount code
                            // For this example, just show a toast
                            Toast.makeText(PaymentActivity.this, "Discount code applied: " + discountCode, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing discount dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showNotesDialog() {
        try {
            // Create an EditText for the notes
            final EditText input = new EditText(this);
            input.setHint("Enter notes for your order");

            // Create and show the dialog
            new AlertDialog.Builder(this)
                    .setTitle("Add Notes")
                    .setView(input)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String notes = input.getText().toString().trim();
                        if (!notes.isEmpty()) {
                            // In a real app, save these notes
                            Toast.makeText(PaymentActivity.this, "Notes added to your order", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing notes dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateFields() {
        try {
            // Check that name and address are not empty
            if (edtName == null || edtName.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (edtAddress == null || edtAddress.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Please enter your address", Toast.LENGTH_SHORT).show();
                return false;
            }

            return true;
        } catch (Exception e) {
            Toast.makeText(this, "Error validating fields: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void placeOrder() {
        try {
            // In a real app, this would send the order to a server
            // For this example, just show a success message and return to the main activity

            // Show confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Order Confirmation")
                    .setMessage("Your order for " + quantity + " " + productName + " has been placed successfully!")
                    .setPositiveButton("OK", (dialog, which) -> {
                        try {
                            // Navigate back to the main shopping activity
                            Intent intent = new Intent(PaymentActivity.this, com.example.mobilestore.ui.shopping.ShoppingActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                            // Show toast
                            Toast.makeText(PaymentActivity.this, "Thank you for your order!", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(PaymentActivity.this, "Error returning to shop: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            finish(); // Just finish if navigation fails
                        }
                    })
                    .setCancelable(false)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error placing order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Handle back button to return to product activity
    @Override
    public void onBackPressed() {
        finish(); // Just finish the activity, don't create a new instance
    }
}