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
import com.example.mobilestore.data.repository.ProductRepository;
import com.example.mobilestore.model.Brand;
import com.example.mobilestore.model.Phone;
import com.example.mobilestore.ui.shopping.ShoppingActivity;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private Phone selectedPhone;
    private ProductRepository repository;
    private int quantity = 1;
    private boolean productSelected = false;
    private double basePrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.product_detail);

            // Initialize repository
            repository = ProductRepository.getInstance(this);

            // Initialize views
            initializeViews();

            // Load product data
            loadProductData();

            // Setup buttons and controls
            setupButtons();

        } catch (Resources.NotFoundException e) {
            Toast.makeText(this, "Layout not found: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
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
                boolean fromCart = intent.getBooleanExtra("FROM_CART", false);

                // Extract base price from formatted price string
                basePrice = extractPrice(price);

                // Find the actual Phone object from repository based on name
                selectedPhone = findPhoneByName(name);

                if (selectedPhone != null) {
                    // Use actual phone data from repository
                    String specs = generateSpecsFromPhone(selectedPhone);

                    // If no basePrice was extracted from the formatted price string,
                    // use the price from the Phone object
                    if (basePrice <= 0) {
                        basePrice = selectedPhone.getPrice();
                    }

                    // Create product with data from repository
                    selectedProduct = new Product(
                            selectedPhone.getPhoneName(),
                            formatPrice(basePrice),
                            specs);

                    productSelected = true;

                    // Show product view, hide no product message
                    showProductDetails();

                    // Update UI with product data
                    updateProductUI();

                    // Check if coming from Add to Cart and display appropriate message
                    if (fromCart) {
                        Toast.makeText(this, "Product has been added to your cart", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // No product found in repository - create a simple product with given name and price
                    StringBuilder basicSpecs = new StringBuilder();
                    basicSpecs.append("PRODUCT: ").append(name).append("\n\n");
                    basicSpecs.append("Information not available in database.\n");
                    basicSpecs.append("Please contact administrator.");

                    selectedProduct = new Product(
                            name != null ? name : "Unknown Product",
                            price != null ? price : "0đ",
                            basicSpecs.toString());

                    productSelected = true;
                    showProductDetails();
                    updateProductUI();

                    // Show a toast to notify admin
                    Toast.makeText(this, "Warning: Product not found in database", Toast.LENGTH_SHORT).show();
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

    /**
     * Extract numeric price from a formatted price string
     * Handles various formats like "1,500,000đ", "$1,500", etc.
     */
    private double extractPrice(String formattedPrice) {
        if (formattedPrice == null || formattedPrice.isEmpty()) {
            return 0;
        }

        try {
            // Remove all non-numeric characters except decimal point
            String numericString = formattedPrice.replaceAll("[^0-9.]", "");
            return Double.parseDouble(numericString);
        } catch (NumberFormatException e) {
            // If parsing fails, return 0
            return 0;
        }
    }

    /**
     * Format price according to Vietnamese currency format
     */
    private String formatPrice(double price) {
        return String.format(Locale.getDefault(), "%,.0fđ", price);
    }

    /**
     * Find a phone in the repository by its name with improved search
     */
    private Phone findPhoneByName(String phoneName) {
        if (phoneName == null || repository == null) return null;

        // Get all brands
        List<String> brandNames = new ArrayList<>();
        for (Brand brand : repository.getAllBrands()) {
            brandNames.add(brand.getName());
        }

        // Strategy 1: Direct match by name
        for (String brandName : brandNames) {
            List<Phone> phones = repository.getPhonesForBrand(brandName);
            for (Phone phone : phones) {
                if (phoneName.equals(phone.getPhoneName())) {
                    return phone;
                }
            }
        }

        // Strategy 2: Case-insensitive match
        String phoneNameLower = phoneName.toLowerCase();
        for (String brandName : brandNames) {
            List<Phone> phones = repository.getPhonesForBrand(brandName);
            for (Phone phone : phones) {
                if (phoneNameLower.equals(phone.getPhoneName().toLowerCase())) {
                    return phone;
                }
            }
        }

        // Strategy 3: Partial match
        for (String brandName : brandNames) {
            List<Phone> phones = repository.getPhonesForBrand(brandName);
            for (Phone phone : phones) {
                if (phoneNameLower.contains(phone.getPhoneName().toLowerCase()) ||
                        phone.getPhoneName().toLowerCase().contains(phoneNameLower)) {
                    return phone;
                }
            }
        }

        // Strategy 4: Brand-based match
        for (String brandName : brandNames) {
            if (phoneNameLower.contains(brandName.toLowerCase())) {
                List<Phone> phones = repository.getPhonesForBrand(brandName);
                if (!phones.isEmpty()) {
                    return phones.get(0);
                }
            }
        }

        // All strategies failed, return null
        return null;
    }

    /**
     * Generate detailed specifications string from Phone object
     * With phone name displayed prominently at the top
     */
    private String generateSpecsFromPhone(Phone phone) {
        StringBuilder specs = new StringBuilder();

        // Add model and brand info
        specs.append("PRODUCT DETAILS: ").append("\n");
        specs.append("  • Model: ").append(phone.getModel()).append("\n");
        specs.append("  • Brand: ").append(phone.getBrand()).append("\n");
        Phone.PhonePerformance performance = phone.getPerformance();
        if (performance != null) {
            specs.append("  • Processor: ").append(performance.getProcessor()).append("\n");
            specs.append("  • RAM: ").append(performance.getRamGB()).append("GB\n");
            specs.append("  • Storage: ").append(performance.getStorageGB()).append("GB\n");
            specs.append("  • Battery: ").append(performance.getBatteryCapacity()).append("\n");
        }

        // Add stock info
        specs.append("  • Stock Available: ").append(phone.getStockQuantity()).append(" units");

        return specs.toString();
    }

    private void updateProductUI() {
        if (selectedProduct == null) return;

        // Update product title in the screen header
        if (titleTextView != null) {
            titleTextView.setText(selectedProduct.getName());
        }

        // Update product details
        if (txtProductDetails != null) {
            txtProductDetails.setText(selectedProduct.getSpecs());
        }

        if (txtProductPrice != null) {
            txtProductPrice.setText("Price: " + selectedProduct.getPrice());
        }

        // Update product image if available
        if (imgProduct != null) {
            try {
                // Show image based on brand info from specs
                String specs = selectedProduct.getSpecs().toLowerCase();
                if (specs.contains("brand: apple") || specs.contains("iphone")) {
                    imgProduct.setImageResource(R.drawable.phone_sample);
                } else if (specs.contains("brand: samsung") || specs.contains("galaxy")) {
                    imgProduct.setImageResource(R.drawable.samsung);
                } else if (specs.contains("brand: redmi")) {
                    imgProduct.setImageResource(R.drawable.redmi);
                } else if (specs.contains("brand: oppo")) {
                    imgProduct.setImageResource(R.drawable.oppo);
                } else {
                    imgProduct.setImageResource(R.drawable.default_phone);
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
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error navigating to products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
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
                // Calculate total price
                double totalPrice = basePrice * quantity;

                // Format and display total price
                String formattedPrice = formatPrice(totalPrice);
                txtProductPrice.setText("Price: " + formattedPrice);

                // Also update the price in the product object
                selectedProduct = new Product(
                        selectedProduct.getName(),
                        formattedPrice,
                        selectedProduct.getSpecs()
                );
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error updating price: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupCheckoutButton() {
        try {
            if (btnCheckout != null) {
                btnCheckout.setOnClickListener(view -> {
                    // Kiểm tra tồn kho trước khi chuyển đến trang thanh toán
                    if (selectedPhone != null) {
                        // Kiểm tra tồn kho thông qua repository
                        if (repository.hasEnoughStock(selectedPhone.getPhoneName(), quantity)) {
                            // Đủ tồn kho, tiến hành đặt hàng
                            proceedToCheckout();
                        } else {
                            // Không đủ tồn kho, hiển thị thông báo
                            showOutOfStockMessage(selectedPhone.getPhoneName(),
                                    repository.getPhoneStock(selectedPhone.getPhoneName()));
                        }
                    } else {
                        proceedToCheckout();
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up checkout button: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Phương thức hiển thị thông báo hết hàng
    private void showOutOfStockMessage(String productName, int availableStock) {
        new AlertDialog.Builder(this)
                .setTitle("Insufficient Stock")
                .setMessage("We're sorry, but we don't have enough " + productName + " in stock. " +
                        "Currently available: " + availableStock + " units.\n\n" +
                        "Please reduce your quantity or choose another product.")
                .setPositiveButton("OK", null)
                .show();
    }

    // Phương thức chuyển đến trang thanh toán
    private void proceedToCheckout() {
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