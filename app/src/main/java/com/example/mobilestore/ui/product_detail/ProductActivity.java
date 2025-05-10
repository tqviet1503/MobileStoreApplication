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
import android.widget.RelativeLayout;
import com.example.mobilestore.R;
import com.example.mobilestore.ui.shopping.ShoppingActivity;

public class ProductActivity extends AppCompatActivity {

    // UI components
    private TextView titleTextView, txtProductDetails, txtProductPrice, txtQuantity;
    private ImageView avatarImageView, imgProduct;
    private ImageButton btnDecrease, btnIncrease;
    private Button btnCheckout, btnBackToProducts;
    private RelativeLayout headerLayout;
    private TextView txtImagePlaceholder;
    private CardView productContentView, noProductView;

    // Data
    private Product selectedProduct;
    private int quantity = 1;
    private boolean productSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.product_detail);

            // Initialize views
            initializeViews();

            // Load product data
            loadProductData();

            // Setup buttons and controls
            setupButtons();

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
            txtImagePlaceholder = findViewById(R.id.txtImagePlaceholder);
            imgProduct = findViewById(R.id.imgProduct);

            // Views for product content and no product message
            productContentView = findViewById(R.id.productContentView);
            noProductView = findViewById(R.id.noProductView);
            btnBackToProducts = findViewById(R.id.btnBackToProducts);

        } catch (Exception e) {
            Toast.makeText(this, "Error finding views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupButtons() {
        try {
            // Back to products button in the no product view
            if (btnBackToProducts != null) {
                btnBackToProducts.setOnClickListener(v -> {
                    navigateToShoppingScreen();
                });
            }

            // Only setup these controls if a product is selected
            if (productSelected) {
                // Setup quantity controls
                setupQuantityControls();

                // Setup checkout button
                setupCheckoutButton();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up buttons: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProductData() {
        try {
            // Get product data from intent
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("PRODUCT_NAME") && intent.hasExtra("PRODUCT_PRICE")) {
                String name = intent.getStringExtra("PRODUCT_NAME");
                String price = intent.getStringExtra("PRODUCT_PRICE");
                String specs = intent.getStringExtra("PRODUCT_SPECS");
                boolean fromCart = intent.getBooleanExtra("FROM_CART", false);

                // Nếu không có specs, tạo specs dựa trên tên sản phẩm
                if (specs == null) {
                    // Infer specs based on product name
                    if (name.contains("iPhone")) {
                        specs = "256GB, Deep Purple";
                    } else if (name.contains("Samsung")) {
                        specs = "512GB, Phantom Black";
                    } else if (name.contains("Xiaomi")) {
                        specs = "256GB, Ceramic White";
                    } else if (name.contains("Oppo")) {
                        specs = "256GB, Ocean Blue";
                    } else {
                        specs = "Standard Configuration";
                    }
                }

                // Create product with valid data
                selectedProduct = new Product(
                        name != null ? name : "Unknown Product",
                        price != null ? price : "$0",
                        specs);

                productSelected = true;

                // Show product view, hide no product message
                showProductDetails();

                // Update UI with product data
                updateProductUI();

                // Kiểm tra nếu đến từ nút Add to Cart và hiển thị thông báo phù hợp
                if (fromCart) {
                    Toast.makeText(this, "Product has been added to your cart", Toast.LENGTH_SHORT).show();
                }
            } else {
                // No product selected - show message
                productSelected = false;
                showNoProductSelectedMessage();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading product data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            productSelected = false;
            showNoProductSelectedMessage();
        }
    }

    private void updateProductUI() {
        if (selectedProduct == null) return;

        // Update product details
        if (txtProductDetails != null) {
            txtProductDetails.setText("Product: " + selectedProduct.getName() + "\n" + selectedProduct.getSpecs());
        }

        if (txtProductPrice != null) {
            txtProductPrice.setText("Price: " + selectedProduct.getPrice());
        }

        // Update product image if available
        if (imgProduct != null) {
            try {
                // Show image based on product name
                if (selectedProduct.getName().contains("iPhone")) {
                    imgProduct.setImageResource(R.drawable.phone_sample);
                } else if (selectedProduct.getName().contains("Samsung")) {
                    imgProduct.setImageResource(R.drawable.phone_sample);
                } else {
                    imgProduct.setImageResource(R.drawable.phone_sample);
                }

                // Hide placeholder, show image
                if (txtImagePlaceholder != null) {
                    txtImagePlaceholder.setVisibility(View.GONE);
                }
                imgProduct.setVisibility(View.VISIBLE);

            } catch (Resources.NotFoundException e) {
                // If image not found, keep placeholder visible
                imgProduct.setVisibility(View.GONE);
                if (txtImagePlaceholder != null) {
                    txtImagePlaceholder.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void showProductDetails() {
        if (productContentView != null) {
            productContentView.setVisibility(View.VISIBLE);
        }

        if (noProductView != null) {
            noProductView.setVisibility(View.GONE);
        }
    }

    private void showNoProductSelectedMessage() {
        if (productContentView != null) {
            productContentView.setVisibility(View.GONE);
        }

        if (noProductView != null) {
            noProductView.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToShoppingScreen() {
        try {
            Intent intent = new Intent(ProductActivity.this, ShoppingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear back stack
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error navigating to products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish(); // Just finish this activity if navigation fails
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
                        intent.putExtra("PRODUCT_SPECS", selectedProduct.getSpecs());
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
        navigateToShoppingScreen();
    }
}