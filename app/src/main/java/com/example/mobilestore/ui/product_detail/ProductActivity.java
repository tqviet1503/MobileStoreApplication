package com.example.mobilestore.ui.product_detail;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.mobilestore.R;

public class ProductActivity extends AppCompatActivity {

    // UI components
    private TextView titleTextView, txtProductDetails, txtProductPrice, txtQuantity;
    private ImageView avatarImageView;
    private ImageButton btnDecrease, btnIncrease;
    private Button btnCheckout;
    private CardView headerLayout;

    // Data
    private Product selectedProduct;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.product_detail);

            // Initialize views
            initializeViews();

            // Load product data (in a real app, this would come from intent extras or a database)
            loadProductData();

            // Setup quantity controls
            setupQuantityControls();

            // Setup checkout button
            setupCheckoutButton();
        } catch (Resources.NotFoundException e) {
            Toast.makeText(this, "Layout not found: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish(); // Close activity if layout not found
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing product screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        try {
            // Find views by ID
            titleTextView = findViewById(R.id.titleTextView);
            avatarImageView = findViewById(R.id.avatarImageView);
            txtProductDetails = findViewById(R.id.txtProductDetails);
            txtProductPrice = findViewById(R.id.txtProductPrice);
            txtQuantity = findViewById(R.id.txtQuantity);
            btnDecrease = findViewById(R.id.btnDecrease);
            btnIncrease = findViewById(R.id.btnIncrease);
            btnCheckout = findViewById(R.id.btnCheckout);
            headerLayout = findViewById(R.id.headerLayout);
        } catch (Exception e) {
            Toast.makeText(this, "Error finding views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProductData() {
        try {
            // Get product data from intent
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("PRODUCT_NAME") && intent.hasExtra("PRODUCT_PRICE")) {
                String name = intent.getStringExtra("PRODUCT_NAME");
                String price = intent.getStringExtra("PRODUCT_PRICE");
                // Create product with valid data
                selectedProduct = new Product(
                        name != null ? name : "Unknown Product",
                        price != null ? price : "$0",
                        "256GB, Deep Purple");
            } else {
                // Default product if no intent data
                selectedProduct = new Product("iPhone 14 Pro Max", "$25,990", "256GB, Deep Purple");
            }

            // Update UI with product data
            if (txtProductDetails != null) {
                txtProductDetails.setText("Product: " + selectedProduct.getName() + "\n" + selectedProduct.getSpecs());
            }

            if (txtProductPrice != null) {
                txtProductPrice.setText("Price: " + selectedProduct.getPrice());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading product data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            selectedProduct = new Product("Sample Product", "$0", "No specs available");
        }
    }

    private void setupQuantityControls() {
        try {
            // Set initial quantity text
            updateQuantityText();

            // Setup decrease button
            if (btnDecrease != null) {
                btnDecrease.setOnClickListener(view -> {
                    if (quantity > 1) {
                        quantity--;
                        updateQuantityText();
                        updateProductTotalPrice();
                    }
                });
            }

            // Setup increase button
            if (btnIncrease != null) {
                btnIncrease.setOnClickListener(view -> {
                    quantity++;
                    updateQuantityText();
                    updateProductTotalPrice();
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up quantity controls: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateQuantityText() {
        if (txtQuantity != null) {
            txtQuantity.setText(String.valueOf(quantity));
        }
    }

    private void updateProductTotalPrice() {
        try {
            if (txtProductPrice != null && selectedProduct != null) {
                // Parse the price (removing the $ sign and commas)
                String priceStr = selectedProduct.getPrice().replace("$", "").replace(",", "");
                try {
                    double basePrice = Double.parseDouble(priceStr);
                    double totalPrice = basePrice * quantity;

                    // Format total price
                    String formattedPrice = String.format("$%,.0f", totalPrice);
                    txtProductPrice.setText("Price: " + formattedPrice);
                } catch (NumberFormatException e) {
                    // If parsing fails, just show the original price
                    txtProductPrice.setText("Price: " + selectedProduct.getPrice());
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error updating price: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupCheckoutButton() {
        try {
            if (btnCheckout != null) {
                btnCheckout.setOnClickListener(view -> {
                    // Proceed to checkout/payment
                    try {
                        // Create intent with product details and quantity
                        Intent intent = new Intent(ProductActivity.this, com.example.mobilestore.ui.payment.PaymentActivity.class);
                        intent.putExtra("PRODUCT_NAME", selectedProduct.getName());
                        intent.putExtra("PRODUCT_PRICE", selectedProduct.getPrice());
                        intent.putExtra("PRODUCT_QUANTITY", quantity);
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(this, "Error navigating to payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up checkout button: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Product model class
    public static class Product {
        private String name;
        private String price;
        private String specs;

        public Product(String name, String price, String specs) {
            this.name = name;
            this.price = price;
            this.specs = specs;
        }

        // Getters
        public String getName() {
            return name;
        }

        public String getPrice() {
            return price;
        }

        public String getSpecs() {
            return specs;
        }
    }

    // Handle back button to return to shopping activity
    @Override
    public void onBackPressed() {
        finish(); // Just finish the activity, don't create a new one
    }
}