package com.example.mobilestore.ui.payment;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilestore.R;
import com.example.mobilestore.data.repository.ProductRepository;
import com.example.mobilestore.data.customer.CustomerDataManager;
import com.example.mobilestore.model.Phone;
import com.example.mobilestore.model.Brand;
import com.example.mobilestore.ui.customer_profile.ProfileActivity;
import com.example.mobilestore.model.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import com.example.mobilestore.data.cart.Cart;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PaymentActivity extends AppCompatActivity {

    // UI components
    private TextView titleTextView, txtProductName, txtQuantity, txtTotalPrice, txtUnitPrice, txtVoucher;
    private ImageView avatarImageView, imgProduct;
    private EditText edtName, edtAddress, edtNotes;
    private Button btnApplyCode, btnOrder;
    private ImageView btnEditName, btnEditAddress, btnEditNotes;
    private RecyclerView rvOrderItems;
    private LinearLayout singleProductLayout, productNameLayout, unitPriceLayout;

    // Data
    private String productName;
    private int quantity;
    private double unitPrice = 0.0;
    private double totalPrice = 0.0;
    private double originalTotalPrice;
    private boolean isDiscountApplied = false;
    private int totalItems = 0;
    private boolean isMultipleItems = false;
    private ProductRepository repository;
    private CustomerDataManager customerDataManager;
    private List<Cart.CartItem> selectedCartItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.payment);

            // Initialize repository
            repository = ProductRepository.getInstance(this);
            // Khởi tạo CustomerDataManager
            customerDataManager = CustomerDataManager.getInstance(this);

            // Initialize views
            initializeViews();

            // Get data from intent
            getDataFromIntent();

            // Calculate total price
            calculateTotalPrice();

            // Lưu tổng giá gốc
            originalTotalPrice = totalPrice;

            // Set up UI with data
            setupUI();

            // Set up button listeners
            setupButtonListeners();

            // Debug log the values
            android.util.Log.d("PaymentActivity", "Product: " + productName);
            android.util.Log.d("PaymentActivity", "Unit price from repository: " + unitPrice);
            android.util.Log.d("PaymentActivity", "Quantity: " + quantity);
            android.util.Log.d("PaymentActivity", "Total Price: " + totalPrice);
        } catch (Resources.NotFoundException e) {
            Toast.makeText(this, "Payment layout not found: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing payment screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateTotalPrice() {
        // Nếu đến từ giỏ hàng với nhiều sản phẩm và đã có tổng giá, không cần tính lại
        if (isMultipleItems && totalItems > 1 && totalPrice > 0) {
            // Giá đã được đặt trong getDataFromIntent()
            android.util.Log.d("PaymentActivity", "Using cart total price: " + totalPrice);
        } else {
            // Tính giá bình thường cho một sản phẩm
            totalPrice = unitPrice * quantity;
            android.util.Log.d("PaymentActivity", "Calculating total: " + unitPrice + " * " + quantity + " = " + totalPrice);
        }

        // Safety check - ensure total price is not negative
        if (totalPrice < 0) {
            totalPrice = 0;
        }
    }

    private void initializeViews() {
        try {
            // Find all views by ID
            titleTextView = findViewById(R.id.titleTextView);
            avatarImageView = findViewById(R.id.avatarImageView);
            txtProductName = findViewById(R.id.txtProductName);
            txtQuantity = findViewById(R.id.txtQuantity);
            txtTotalPrice = findViewById(R.id.txtTotalPrice);
            txtUnitPrice = findViewById(R.id.txtUnitPrice);
            txtVoucher = findViewById(R.id.txtVoucher);
            imgProduct = findViewById(R.id.imgProduct);
            edtName = findViewById(R.id.edtName);
            edtAddress = findViewById(R.id.edtAddress);
            edtNotes = findViewById(R.id.edtNotes);
            btnEditNotes = findViewById(R.id.btnEditNotes);
            btnApplyCode = findViewById(R.id.btnApplyCode);
            btnOrder = findViewById(R.id.btnOrder);
            btnEditName = findViewById(R.id.btnEditName);
            btnEditAddress = findViewById(R.id.btnEditAddress);
            rvOrderItems = findViewById(R.id.rvOrderItems);
            singleProductLayout = findViewById(R.id.singleProductLayout);
            productNameLayout = findViewById(R.id.productNameLayout);
            unitPriceLayout = findViewById(R.id.unitPriceLayout);

            edtNotes.setEnabled(false);
        } catch (Exception e) {
            Toast.makeText(this, "Error finding payment views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getDataFromIntent() {
        try {
            Intent intent = getIntent();
            if (intent != null) {
                productName = intent.getStringExtra("PRODUCT_NAME");
                quantity = intent.getIntExtra("PRODUCT_QUANTITY", 1);
                String productPrice = intent.getStringExtra("PRODUCT_PRICE");

                // Kiểm tra nếu là đơn hàng nhiều sản phẩm từ giỏ hàng
                isMultipleItems = intent.getBooleanExtra("FROM_CART", false);
                if (isMultipleItems) {
                    // Lấy tổng số sản phẩm
                    totalItems = intent.getIntExtra("TOTAL_ITEMS", 1);

                    // Lấy tổng giá trị đơn hàng
                    totalPrice = intent.getDoubleExtra("TOTAL_PRICE", 0.0);

                    android.util.Log.d("PaymentActivity", "Total items: " + totalItems);
                    android.util.Log.d("PaymentActivity", "Total price: " + totalPrice);

                    // Lấy danh sách các sản phẩm được chọn từ Cart
                    selectedCartItems = Cart.getInstance(this).getSelectedItems();
                    android.util.Log.d("PaymentActivity", "Selected items: " + selectedCartItems.size());
                }

                // Validate data - set defaults if null
                if (productName == null) productName = "Sample Product";

                // Get the unit price from the repository based on product name
                if (!isMultipleItems || selectedCartItems.isEmpty()) {
                    unitPrice = getUnitPriceFromRepository(productName);

                    // If we couldn't find the price in the repository, try to extract from the formatted price string
                    if (unitPrice <= 0 && productPrice != null) {
                        unitPrice = extractPrice(productPrice);
                        android.util.Log.d("PaymentActivity", "Extracted price from formatted string: " + unitPrice + " from " + productPrice);
                    }
                }
            } else {
                // Default values if no intent data
                productName = "Sample Product";
                quantity = 1;
                unitPrice = 0.0;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error getting intent data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Set defaults
            productName = "Sample Product";
            quantity = 1;
            unitPrice = 0.0;
        }
    }

    private double extractPrice(String formattedPrice) {
        if (formattedPrice == null || formattedPrice.isEmpty()) {
            return 0;
        }

        try {
            android.util.Log.d("PaymentActivity", "Attempting to extract price from: " + formattedPrice);

            // First approach: Remove all non-numeric characters except decimal point
            String numericString = formattedPrice.replaceAll("[^0-9.]", "");
            android.util.Log.d("PaymentActivity", "First extraction attempt result: " + numericString);

            if (!numericString.isEmpty()) {
                double result = Double.parseDouble(numericString);
                android.util.Log.d("PaymentActivity", "Extracted price (method 1): " + result);
                return result;
            }

            // Second approach: Just get digits
            String digitsOnly = formattedPrice.replaceAll("[^\\d]", "");
            android.util.Log.d("PaymentActivity", "Second extraction attempt result: " + digitsOnly);

            if (!digitsOnly.isEmpty()) {
                double result = Double.parseDouble(digitsOnly);
                android.util.Log.d("PaymentActivity", "Extracted price (method 2): " + result);
                return result;
            }

            android.util.Log.d("PaymentActivity", "All extraction methods failed");
            return 0;
        } catch (NumberFormatException e) {
            // If parsing fails, return 0
            android.util.Log.e("PaymentActivity", "Failed to parse price: " + formattedPrice, e);
            return 0;
        }
    }

    private String formatPrice(double price) {
        return String.format(Locale.getDefault(), "%,.0fđ", price);
    }

    private double getUnitPriceFromRepository(String productName) {
        try {
            if (repository == null || productName == null) {
                android.util.Log.d("PaymentActivity", "Repository is null or product name is null");
                return 0.0;
            }

            android.util.Log.d("PaymentActivity", "Searching repository for product: " + productName);

            // Get all brands from repository
            List<String> brandNames = new ArrayList<>();
            for (Brand brand : repository.getAllBrands()) {
                brandNames.add(brand.getName());
            }

            android.util.Log.d("PaymentActivity", "Found " + brandNames.size() + " brands in repository");

            // Direct match strategy - exact match
            android.util.Log.d("PaymentActivity", "Trying exact match strategy");
            for (String brandName : brandNames) {
                List<Phone> phones = repository.getPhonesForBrand(brandName);
                android.util.Log.d("PaymentActivity", "Brand: " + brandName + " has " + phones.size() + " phones");

                for (Phone phone : phones) {
                    android.util.Log.d("PaymentActivity", "Checking phone: " + phone.getPhoneName() + " (Model: " + phone.getModel() + ", Price: " + phone.getPrice() + ")");

                    if (phone.getPhoneName().equals(productName)) {
                        android.util.Log.d("PaymentActivity", "Found exact product match: " + phone.getPhoneName() + " with price: " + phone.getPrice());
                        return phone.getPrice();
                    }
                }
            }

            // Case-insensitive match
            android.util.Log.d("PaymentActivity", "Trying case-insensitive match strategy");
            for (String brandName : brandNames) {
                List<Phone> phones = repository.getPhonesForBrand(brandName);
                for (Phone phone : phones) {
                    if (phone.getPhoneName().equalsIgnoreCase(productName) ||
                            phone.getModel().equalsIgnoreCase(productName)) {
                        android.util.Log.d("PaymentActivity", "Found case-insensitive match: " + phone.getPhoneName() + " with price: " + phone.getPrice());
                        return phone.getPrice();
                    }
                }
            }

            // Partial match strategy
            android.util.Log.d("PaymentActivity", "Trying partial match strategy");
            String productNameLower = productName.toLowerCase();
            for (String brandName : brandNames) {
                List<Phone> phones = repository.getPhonesForBrand(brandName);
                for (Phone phone : phones) {
                    String phoneNameLower = phone.getPhoneName().toLowerCase();
                    String modelLower = phone.getModel().toLowerCase();

                    if (phoneNameLower.contains(productNameLower) ||
                            productNameLower.contains(phoneNameLower) ||
                            modelLower.contains(productNameLower) ||
                            productNameLower.contains(modelLower)) {
                        android.util.Log.d("PaymentActivity", "Found partial match: " + phone.getPhoneName() + " with price: " + phone.getPrice());
                        return phone.getPrice();
                    }
                }
            }

            // Brand-based match strategy
            android.util.Log.d("PaymentActivity", "Trying brand-based match strategy");
            for (String brandName : brandNames) {
                if (productNameLower.contains(brandName.toLowerCase())) {
                    List<Phone> phones = repository.getPhonesForBrand(brandName);
                    if (!phones.isEmpty()) {
                        Phone phone = phones.get(0);
                        android.util.Log.d("PaymentActivity", "Found brand match: " + phone.getPhoneName() + " with price: " + phone.getPrice());
                        return phone.getPrice();
                    }
                }
            }

            // No match found after all strategies
            android.util.Log.d("PaymentActivity", "No matching product found in repository for: " + productName);
            return 0.0;
        } catch (Exception e) {
            android.util.Log.e("PaymentActivity", "Error getting price from repository: " + e.getMessage(), e);
            return 0.0;
        }
    }

    private void setupUI() {
        try {
            if (isMultipleItems && !selectedCartItems.isEmpty()) {
                // Trường hợp nhiều sản phẩm - hiển thị RecyclerView
                setupMultipleProductsUI();
            } else {
                // Trường hợp một sản phẩm - hiển thị thông tin sản phẩm đơn
                setupSingleProductUI();
            }

            // Set customer data from CustomerDataManager
            if (edtName != null) {
                edtName.setText(customerDataManager.getCustomerName());
                edtName.setEnabled(false);
            }

            if (edtAddress != null) {
                edtAddress.setText(customerDataManager.getCustomerAddress());
                edtAddress.setEnabled(false);
            }

            // Set default text for notes
            if (edtNotes != null) {
                edtNotes.setText("");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up payment UI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Phương thức mới để hiển thị nhiều sản phẩm
    private void setupMultipleProductsUI() {
        // Ẩn layout hiển thị một sản phẩm
        singleProductLayout.setVisibility(View.GONE);
        productNameLayout.setVisibility(View.GONE);
        unitPriceLayout.setVisibility(View.GONE);

        // Hiển thị RecyclerView danh sách sản phẩm
        rvOrderItems.setVisibility(View.VISIBLE);

        // Thiết lập RecyclerView với layout mới
        rvOrderItems.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        OrderItemAdapter adapter = new OrderItemAdapter(selectedCartItems);
        rvOrderItems.setAdapter(adapter);

        // Cập nhật số lượng và tổng giá
        if (txtQuantity != null) {
            txtQuantity.setText(String.valueOf(totalItems));
        }

        if (txtTotalPrice != null) {
            txtTotalPrice.setText(formatPrice(totalPrice));
        }

        if (txtVoucher != null) {
            txtVoucher.setText("0%");
        }
    }

    // Phương thức hiển thị một sản phẩm
    private void setupSingleProductUI() {
        // Hiển thị layout một sản phẩm
        singleProductLayout.setVisibility(View.VISIBLE);
        productNameLayout.setVisibility(View.VISIBLE);
        unitPriceLayout.setVisibility(View.VISIBLE);

        // Ẩn RecyclerView
        rvOrderItems.setVisibility(View.GONE);

        // Set product information on separate lines
        if (txtProductName != null) {
            txtProductName.setText(productName);
        }

        if (txtQuantity != null) {
            txtQuantity.setText(String.valueOf(quantity));
        }

        // Set unit price
        if (txtUnitPrice != null) {
            txtUnitPrice.setText(formatPrice(unitPrice));
        }

        if (txtTotalPrice != null) {
            txtTotalPrice.setText(formatPrice(totalPrice));
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

        if (txtVoucher != null) {
            txtVoucher.setText("0%");
        }
    }

    private void setupButtonListeners() {
        try {
            // Edit Name button
            if (btnEditName != null && edtName != null) {
                btnEditName.setOnClickListener(view -> {
                    edtName.setEnabled(!edtName.isEnabled());
                    if (edtName.isEnabled()) {
                        edtName.requestFocus();
                        showKeyboard(edtName);
                        Toast.makeText(PaymentActivity.this, "You can now edit your name", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Edit Address button
            if (btnEditAddress != null && edtAddress != null) {
                btnEditAddress.setOnClickListener(view -> {
                    edtAddress.setEnabled(!edtAddress.isEnabled());
                    if (edtAddress.isEnabled()) {
                        edtAddress.requestFocus();
                        showKeyboard(edtAddress);
                        Toast.makeText(PaymentActivity.this, "You can now edit your address", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Edit Notes button
            if (btnEditNotes != null && edtNotes != null) {
                btnEditNotes.setOnClickListener(view -> {
                    edtNotes.setEnabled(!edtNotes.isEnabled());
                    if (edtNotes.isEnabled()) {
                        edtNotes.requestFocus();
                        showKeyboard(edtNotes);
                        Toast.makeText(PaymentActivity.this, "You can now edit your notes", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Apply Discount Code button
            if (btnApplyCode != null) {
                btnApplyCode.setOnClickListener(view -> {
                    showDiscountCodeDialog();
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
                            if (discountCode.equals("HELLO")) {
                                applyDiscount(20); // Giảm giá 20%
                                txtVoucher.setText("-20%"); // Cập nhật voucher
                                isDiscountApplied = true; // Đánh dấu đã áp dụng giảm giá
                            } else {
                                txtVoucher.setText("0%"); // Voucher không hợp lệ
                                if (isDiscountApplied) {
                                    // Nếu đã áp dụng giảm giá trước đó, khôi phục tổng giá về giá gốc
                                    totalPrice = originalTotalPrice;
                                    txtTotalPrice.setText(formatPrice(totalPrice));
                                    isDiscountApplied = false; // Đánh dấu chưa áp dụng giảm giá
                                }
                                Toast.makeText(PaymentActivity.this, "Invalid discount code", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing discount dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void applyDiscount(int discountPercentage) {
        try {
            // Calculate discounted price
            double discountFactor = (100 - discountPercentage) / 100.0;
            totalPrice = totalPrice * discountFactor;

            // Update the UI
            if (txtTotalPrice != null) {
                txtTotalPrice.setText(formatPrice(totalPrice));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error applying discount: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private boolean checkStockAvailability(SQLiteDatabase db, String phoneName, int orderQuantity) {
        Cursor cursor = db.query(
                "phones",
                new String[]{"stock_quantity"},
                "phone_name = ?",
                new String[]{phoneName},
                null, null, null
        );

        try {
            if (cursor.moveToFirst()) {
                int stockQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("stock_quantity"));
                return stockQuantity >= orderQuantity;
            }
            return false;
        } finally {
            cursor.close();
        }
    }

    private void placeOrder() {
        try {
            // Lấy thông tin shipping
            String name = edtName.getText().toString().trim();
            String address = edtAddress.getText().toString().trim();
            String notes = edtNotes.getText().toString().trim();

            // Tạo đối tượng Order mới
            Order newOrder;

            if (isMultipleItems && !selectedCartItems.isEmpty()) {
                // Create an order with multiple items
                List<Order.OrderItem> orderItems = new ArrayList<>();

                // Convert cart items to order items
                for (Cart.CartItem cartItem : selectedCartItems) {
                    Order.OrderItem orderItem = new Order.OrderItem(
                            cartItem.phone.getPhoneName(),
                            cartItem.quantity,
                            cartItem.phone.getPrice()
                    );
                    orderItems.add(orderItem);
                }

                // Create the multi-item order
                newOrder = new Order(
                        orderItems,
                        totalPrice,
                        isDiscountApplied,
                        name,
                        address,
                        notes
                );
            } else {
                // Create a single-item order (using existing constructor)
                newOrder = new Order(
                        productName,
                        quantity,
                        unitPrice,
                        totalPrice,
                        isDiscountApplied,
                        name,
                        address,
                        notes
                );
            }

            // Lưu đơn hàng vào CustomerDataManager
            if (customerDataManager != null) {
                customerDataManager.saveOrder(newOrder);

                // Cập nhật địa chỉ nếu đã thay đổi
                if (!address.equals(customerDataManager.getCustomerAddress())) {
                    customerDataManager.setCustomerAddress(address);
                }
            }

            SQLiteDatabase db = repository.db;

            // Kiểm tra số lượng tồn kho cho đơn hàng
            if (!isMultipleItems || selectedCartItems.isEmpty()) {
                // Kiểm tra stock cho đơn hàng đơn
                if (!checkStockAvailability(db, productName, quantity)) {
                    Toast.makeText(this,
                            "Not enough stock available. Please reduce quantity.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            } else {
                // Đơn hàng nhiều mặt hàng - giả định đã kiểm tra ở màn hình giỏ hàng
                // và chỉ cho phép chọn những mặt hàng có đủ số lượng
            }

            db.beginTransaction();
            try {
                long customerId;
                Cursor customerCursor = db.query("customers",
                        new String[]{"id"},
                        "name = ?",
                        new String[]{name},
                        null, null, null);

                if (customerCursor.moveToFirst()) {
                    // update customer
                    customerId = customerCursor.getLong(customerCursor.getColumnIndexOrThrow("id"));
                    ContentValues customerValues = new ContentValues();
                    customerValues.put("address", address);
                    customerValues.put("updated_at", System.currentTimeMillis());
                    customerValues.put("total_orders", customerDataManager.getTotalOrders() + 1);
                    customerValues.put("total_spent", customerDataManager.getTotalSpent() + totalPrice);
                    customerValues.put("loyalty_points", customerDataManager.getLoyaltyPoints() + (int)(totalPrice/100000));

                    db.update("customers", customerValues, "id = ?",
                            new String[]{String.valueOf(customerId)});
                } else {
                    // thêm customer nếu chưa có
                    ContentValues customerValues = new ContentValues();
                    customerId = System.currentTimeMillis(); // Generate new ID
                    customerValues.put("id", customerId);
                    customerValues.put("name", name);
                    customerValues.put("phone", customerDataManager.getCustomerPhone()); // Add phone
                    customerValues.put("email", customerDataManager.getCustomerEmail()); // Add email
                    customerValues.put("address", address);
                    customerValues.put("total_orders", 1); // First order
                    customerValues.put("total_spent", totalPrice);
                    customerValues.put("loyalty_points", (int)(totalPrice/100000));
                    customerValues.put("created_at", System.currentTimeMillis());
                    customerValues.put("updated_at", System.currentTimeMillis());

                    customerId = db.insert("customers", null, customerValues);
                    if (customerId == -1) {
                        throw new Exception("Failed to insert customer");
                    }
                }
                customerCursor.close();

                // Tạo bill
                long billId = System.currentTimeMillis(); // Dùng timestamp làm ID

                ContentValues billValues = new ContentValues();
                billValues.put("id", billId);
                billValues.put("customer_id", customerId);
                billValues.put("total_amount", totalPrice);

                // Thêm thông tin về voucher đã áp dụng - chỉ xử lý giảm 20% cho mã "HELLO"
                int discountPercentage = 0;
                if (isDiscountApplied) {
                    discountPercentage = 20; // 20% cho mã HELLO
                }

                // Ghi nhận thông tin giảm giá vào bill
                try {
                    billValues.put("discount_percentage", discountPercentage);
                    android.util.Log.d("PaymentActivity", "Applied discount of " + discountPercentage + "% to bill: " + billId);
                } catch (Exception e) {
                    // Xử lý trường hợp cột discount_percentage chưa được thêm vào bảng
                    android.util.Log.e("PaymentActivity", "Failed to save discount_percentage: " + e.getMessage());
                }

                db.insert("bills", null, billValues);

                if (!isMultipleItems || selectedCartItems.isEmpty()) {
                    // Tạo chi tiết bill cho một sản phẩm
                    ContentValues detailValues = new ContentValues();
                    detailValues.put("bill_id", billId);
                    detailValues.put("phone_id", getPhoneIdByName(productName));
                    detailValues.put("quantity", quantity);
                    detailValues.put("unit_price", unitPrice);
                    db.insert("bill_details", null, detailValues);

                    // Cập nhật số lượng stock cho sản phẩm đơn
                    db.execSQL(
                            "UPDATE phones SET stock_quantity = stock_quantity - ? WHERE phone_name = ?",
                            new Object[]{quantity, productName}
                    );
                } else {
                    // Xử lý đơn hàng nhiều sản phẩm từ selectedCartItems
                    for (Cart.CartItem item : selectedCartItems) {
                        // Thêm chi tiết bill cho từng sản phẩm
                        ContentValues detailValues = new ContentValues();
                        detailValues.put("bill_id", billId);
                        detailValues.put("phone_id", getPhoneIdByName(item.phone.getPhoneName()));
                        detailValues.put("quantity", item.quantity);
                        detailValues.put("unit_price", item.phone.getPrice());
                        db.insert("bill_details", null, detailValues);

                        // Cập nhật số lượng stock cho từng sản phẩm
                        db.execSQL(
                                "UPDATE phones SET stock_quantity = stock_quantity - ? WHERE phone_name = ?",
                                new Object[]{item.quantity, item.phone.getPhoneName()}
                        );

                        android.util.Log.d("PaymentActivity", "Processed order item: " + item.phone.getPhoneName() +
                                ", quantity: " + item.quantity + ", price: " + item.phone.getPrice());
                    }

                    // Xóa chỉ các sản phẩm đã chọn khỏi giỏ hàng
                    for (Cart.CartItem item : selectedCartItems) {
                        Cart.getInstance(PaymentActivity.this).removeItem(item.phone);
                    }
                }

                // Cập nhật thông tin lợi nhuận
                Cursor profitCursor = db.query("profit", null, null, null,
                        null, null, null);

                if (profitCursor.moveToFirst()) {
                    // Nếu đã có profit thì update
                    int currentTotalSold = profitCursor.getInt(
                            profitCursor.getColumnIndexOrThrow("total_sold"));
                    double currentIncome = profitCursor.getDouble(
                            profitCursor.getColumnIndexOrThrow("income"));

                    ContentValues profitValues = new ContentValues();
                    profitValues.put("total_sold", currentTotalSold + (isMultipleItems ? totalItems : quantity));
                    profitValues.put("income", currentIncome + totalPrice);

                    db.update("profit", profitValues, null, null);
                } else {
                    // Nếu không thì tạo profit mới
                    ContentValues profitValues = new ContentValues();
                    profitValues.put("total_sold", isMultipleItems ? totalItems : quantity);
                    profitValues.put("income", totalPrice);

                    db.insert("profit", null, profitValues);
                }
                profitCursor.close();

                // Hoàn thành transaction
                db.setTransactionSuccessful();

                // Hiển thị hộp thoại xác nhận với thông tin đơn hàng đã cập nhật
                showOrderConfirmationDialog(newOrder);
            } finally {
                db.endTransaction();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error placing order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Add this new method to show the confirmation dialog
    private void showOrderConfirmationDialog(Order order) {
        StringBuilder orderDetails = new StringBuilder("Your order details:\n");

        if (!order.hasMultipleItems()) {
            // Chi tiết cho đơn hàng một sản phẩm
            orderDetails.append("  • Product: ").append(order.getProductName()).append("\n");
            orderDetails.append("  • Quantity: ").append(order.getQuantity()).append("\n");
            orderDetails.append("  • Unit Price: ").append(order.getFormattedPrice(order.getUnitPrice())).append("\n");
        } else {
            // Chi tiết cho đơn hàng nhiều sản phẩm
            orderDetails.append("  • Items ordered: ").append(order.getItemCount()).append("\n\n");
            orderDetails.append("  • Products:\n");

            for (Order.OrderItem item : order.getOrderItems()) {
                orderDetails.append("      - ")
                        .append(item.getProductName())
                        .append(" (x").append(item.getQuantity()).append("): ")
                        .append(order.getFormattedPrice(item.getTotalPrice()))
                        .append("\n");
            }
            orderDetails.append("\n");
        }

        // Thêm thông tin chung
        orderDetails.append("  • Voucher: ").append(order.isHasDiscount() ? "-20%" : "0%").append("\n");
        orderDetails.append("  • Total Price: ").append(order.getFormattedPrice(order.getTotalPrice())).append("\n\n");
        orderDetails.append("Shipping Information:\n");
        orderDetails.append("  • Name: ").append(order.getCustomerName()).append("\n");
        orderDetails.append("  • Address: ").append(order.getCustomerAddress()).append("\n");
        orderDetails.append("  • Notes: ").append(order.getNotes()).append("\n\n");
        orderDetails.append("Your order has been placed successfully!");

        // Hiển thị hộp thoại xác nhận
        new AlertDialog.Builder(this)
                .setTitle("Order Confirmation")
                .setMessage(orderDetails.toString())
                .setPositiveButton("View Profile", (dialog, which) -> {
                    try {
                        Intent profileIntent = new Intent(PaymentActivity.this, ProfileActivity.class);
                        startActivity(profileIntent);
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(PaymentActivity.this, "Error navigating to profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Back to Shopping", (dialog, which) -> {
                    try {
                        Intent intent = new Intent(PaymentActivity.this, com.example.mobilestore.ui.shopping.ShoppingActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                        // Hiển thị toast
                        Toast.makeText(PaymentActivity.this, "Thank you for your order!", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(PaymentActivity.this, "Error returning to shop: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    // Handle back button to return to product activity
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish(); // Just finish the activity, don't create a new instance
    }

    private String getPhoneIdByName(String phoneName) {
        String phoneId = null;
        Cursor cursor = repository.db.query(
                "phones",
                new String[]{"id"},
                "phone_name = ?",
                new String[]{phoneName},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            phoneId = cursor.getString(cursor.getColumnIndexOrThrow("id"));
        }
        cursor.close();
        return phoneId;
    }
    private class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

        private List<Cart.CartItem> items;

        public OrderItemAdapter(List<Cart.CartItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_payment, parent, false);  // Đã đổi thành item_payment.xml
            return new OrderItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
            Cart.CartItem item = items.get(position);

            // Hiển thị tên sản phẩm
            holder.tvPaymentItemName.setText(item.phone.getPhoneName());

            // Hiển thị số lượng
            holder.tvPaymentItemQuantity.setText(String.valueOf(item.quantity));

            // Hiển thị giá
            double itemPrice = item.phone.getPrice() * item.quantity;
            holder.tvPaymentItemPrice.setText(formatPrice(itemPrice));

            // Hiển thị ảnh sản phẩm dựa trên brand
            try {
                String brand = item.phone.getBrand().toLowerCase();
                if (brand.contains("apple") || brand.contains("iphone")) {
                    holder.imgPaymentItem.setImageResource(R.drawable.phone_sample);
                } else if (brand.contains("samsung") || brand.contains("galaxy")) {
                    holder.imgPaymentItem.setImageResource(R.drawable.samsung);
                } else if (brand.contains("xiaomi") || brand.contains("redmi")) {
                    holder.imgPaymentItem.setImageResource(R.drawable.redmi);
                } else if (brand.contains("oppo")) {
                    holder.imgPaymentItem.setImageResource(R.drawable.oppo);
                } else {
                    holder.imgPaymentItem.setImageResource(R.drawable.phone_sample);
                }
            } catch (Resources.NotFoundException e) {
                // Sử dụng ảnh mặc định nếu không tìm thấy
                holder.imgPaymentItem.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        class OrderItemViewHolder extends RecyclerView.ViewHolder {
            ImageView imgPaymentItem;  // Đã đổi tên ID
            TextView tvPaymentItemName, tvPaymentItemQuantity, tvPaymentItemPrice;  // Đã đổi tên ID

            public OrderItemViewHolder(@NonNull View itemView) {
                super(itemView);

                imgPaymentItem = itemView.findViewById(R.id.imgPaymentItem);
                tvPaymentItemName = itemView.findViewById(R.id.tvPaymentItemName);
                tvPaymentItemQuantity = itemView.findViewById(R.id.tvPaymentItemQuantity);
                tvPaymentItemPrice = itemView.findViewById(R.id.tvPaymentItemPrice);
            }
        }
    }
}