package com.example.mobilestore.ui.cart;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilestore.R;
import com.example.mobilestore.data.cart.Cart;
import com.example.mobilestore.data.repository.ProductRepository;
import com.example.mobilestore.model.Phone;
import com.example.mobilestore.ui.payment.PaymentActivity;
import com.example.mobilestore.ui.product_detail.ProductActivity;
import com.example.mobilestore.ui.shopping.ShoppingActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    // UI components
    private TextView titleTextView, cartTitleTextView, tvSelectedCount, tvTotalPrice;
    private ImageButton avatarImageView;
    private Button btnProducts, btnShop, btnPayment, btnProfile;
    private Button btnCheckout, btnStartShopping;
    private RecyclerView cartItemsRecyclerView;
    private LinearLayout emptyCartLayout;

    // Data
    private List<Cart.CartItem> cartItems;
    private CartAdapter cartAdapter;
    private Map<String, Integer> stockQuantities = new HashMap<>();
    private ProductRepository repository;
    private Cart cart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_cart);

            // Initialize repository và cart
            repository = ProductRepository.getInstance(this);
            cart = Cart.getInstance(this);

            // Initialize views
            initializeViews();

            // Set up navigation
            setupNavigationButtons();

            // Load cart data and set up the RecyclerView
            loadCartData();

            // Set up checkout button
            setupCheckoutButton();

        } catch (Exception e) {
            Toast.makeText(this, "Error initializing cart screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload cart data when returning to this screen
        loadCartData();
    }

    private void initializeViews() {
        try {
            titleTextView = findViewById(R.id.titleTextView);
            cartTitleTextView = findViewById(R.id.cartTitleTextView);
            tvSelectedCount = findViewById(R.id.tvSelectedCount);
            tvTotalPrice = findViewById(R.id.tvTotalPrice);
            avatarImageView = findViewById(R.id.avatarImageView);
            btnProducts = findViewById(R.id.btnProducts);
            btnShop = findViewById(R.id.btnShop);
            btnPayment = findViewById(R.id.btnPayment);
            btnProfile = findViewById(R.id.btnProfile);
            btnCheckout = findViewById(R.id.btnCheckout);
            btnStartShopping = findViewById(R.id.btnStartShopping);
            cartItemsRecyclerView = findViewById(R.id.cartItemsRecyclerView);
            emptyCartLayout = findViewById(R.id.emptyCartLayout);

            // Set up empty cart button
            btnStartShopping.setOnClickListener(v -> navigateToShopping());

        } catch (Exception e) {
            Toast.makeText(this, "Error finding views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupNavigationButtons() {
        try {
            if (btnProducts != null) {
                btnProducts.setOnClickListener(view -> navigateToShopping());
            }

            if (btnShop != null) {
                // Already on Cart screen, no action needed
            }

            if (btnPayment != null) {
                btnPayment.setOnClickListener(view -> {
                    if (cart.getSelectedItemCount() > 0) {
                        proceedToCheckout();
                    } else {
                        Toast.makeText(CartActivity.this, "Please select items to checkout", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (btnProfile != null) {
                btnProfile.setOnClickListener(view -> {
                    Intent intent = new Intent(CartActivity.this, com.example.mobilestore.ui.customer_profile.ProfileActivity.class);
                    startActivity(intent);
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up navigation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCartData() {
        try {
            // Get cart items
            cartItems = cart.getAllItems();

            // Load stock quantities for each item
            loadStockQuantities();

            // Update cart title with item count
            updateCartTitle();

            // Show empty cart message if needed
            if (cartItems.isEmpty()) {
                emptyCartLayout.setVisibility(View.VISIBLE);
                cartItemsRecyclerView.setVisibility(View.GONE);
            } else {
                emptyCartLayout.setVisibility(View.GONE);
                cartItemsRecyclerView.setVisibility(View.VISIBLE);

                // Set up RecyclerView
                cartItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                cartAdapter = new CartAdapter(cartItems);
                cartItemsRecyclerView.setAdapter(cartAdapter);
            }

            // Update selection summary
            updateSummary();

        } catch (Exception e) {
            Toast.makeText(this, "Error loading cart data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadStockQuantities() {
        try {
            SQLiteDatabase db = repository.db;
            stockQuantities.clear();

            for (Cart.CartItem item : cartItems) {
                String productId = item.phone.getId();
                String productName = item.phone.getPhoneName();

                Cursor cursor = db.query(
                        "phones",
                        new String[]{"stock_quantity"},
                        "phone_name = ?",
                        new String[]{productName},
                        null, null, null
                );

                if (cursor.moveToFirst()) {
                    int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("stock_quantity"));
                    stockQuantities.put(productId, quantity);
                } else {
                    // Default to 0 if not found
                    stockQuantities.put(productId, 0);
                }
                cursor.close();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error loading stock data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCartTitle() {
        if (cartTitleTextView != null) {
            cartTitleTextView.setText("Items in Your Cart (" + cartItems.size() + ")");
        }
    }

    private void updateSummary() {
        // Update selected count
        if (tvSelectedCount != null) {
            tvSelectedCount.setText(String.valueOf(cart.getSelectedItemCount()));
        }

        // Update total price
        if (tvTotalPrice != null) {
            tvTotalPrice.setText(formatPrice(cart.getSelectedItemsPrice()));
        }

        // Enable/disable checkout button
        if (btnCheckout != null) {
            boolean hasSelectedItems = cart.getSelectedItemCount() > 0;
            btnCheckout.setEnabled(hasSelectedItems);
            btnCheckout.setAlpha(hasSelectedItems ? 1.0f : 0.5f);
        }
    }

    private void setupCheckoutButton() {
        if (btnCheckout != null) {
            btnCheckout.setOnClickListener(v -> proceedToCheckout());
        }
    }

    private void proceedToCheckout() {
        try {
            // Check if any items are selected
            if (cart.getSelectedItemCount() == 0) {
                Toast.makeText(this, "Please select items to checkout", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate stock for selected items
            boolean allItemsInStock = true;
            String outOfStockItem = "";

            for (Cart.CartItem item : cart.getSelectedItems()) {
                String productId = item.phone.getId();
                int stockQuantity = stockQuantities.getOrDefault(productId, 0);
                if (item.quantity > stockQuantity) {
                    allItemsInStock = false;
                    outOfStockItem = item.phone.getPhoneName();
                    break;
                }
            }

            if (!allItemsInStock) {
                Toast.makeText(this,
                        "Sorry, " + outOfStockItem + " is out of stock or has insufficient quantity.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // Tính tổng số lượng sản phẩm
            int actualTotalItems = 0;
            for (Cart.CartItem item : cart.getSelectedItems()) {
                actualTotalItems += item.quantity;
            }

            // Tính lại tổng giá trị đơn hàng
            double totalPrice = 0.0;
            for (Cart.CartItem item : cart.getSelectedItems()) {
                totalPrice += item.phone.getPrice() * item.quantity;
            }

            // Get first selected item for checkout
            List<Cart.CartItem> selectedItems = cart.getSelectedItems();
            if (!selectedItems.isEmpty()) {
                Cart.CartItem selectedItem = selectedItems.get(0);

                // Navigate to payment activity with the selected item
                Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
                intent.putExtra("PRODUCT_NAME", selectedItem.phone.getPhoneName());
                intent.putExtra("PRODUCT_QUANTITY", selectedItem.quantity);
                intent.putExtra("PRODUCT_PRICE", formatPrice(selectedItem.phone.getPrice()));
                intent.putExtra("FROM_CART", true);
                intent.putExtra("TOTAL_ITEMS", actualTotalItems);
                intent.putExtra("TOTAL_PRICE", totalPrice);

                // Log thông tin để debug
                android.util.Log.d("CartActivity", "Checkout: Total Items = " + actualTotalItems);
                android.util.Log.d("CartActivity", "Checkout: Total Price = " + totalPrice);
                android.util.Log.d("CartActivity", "Checkout: Selected Item Count = " + cart.getSelectedItemCount());

                startActivity(intent);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error processing checkout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToShopping() {
        Intent intent = new Intent(CartActivity.this, ShoppingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void navigateToProductDetail(Phone phone) {
        try {
            Intent intent = new Intent(CartActivity.this, ProductActivity.class);
            intent.putExtra("PRODUCT_NAME", phone.getPhoneName());
            intent.putExtra("PRODUCT_PRICE", formatPrice(phone.getPrice()));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error navigating to product details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String formatPrice(double price) {
        return String.format("%,.0fđ", price);
    }

    // Adapter for cart items
    private class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

        private List<Cart.CartItem> items;

        public CartAdapter(List<Cart.CartItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cart, parent, false);
            return new CartViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
            Cart.CartItem item = items.get(position);
            Phone phone = item.phone;
            String productId = phone.getId();

            // Set product name
            holder.tvProductName.setText(phone.getPhoneName());

            // Set unit price
            holder.tvProductPrice.setText(formatPrice(phone.getPrice()));

            // Set quantity
            holder.tvQuantity.setText(String.valueOf(item.quantity));

            // Set product image based on brand
            try {
                String brand = phone.getBrand().toLowerCase();
                if (brand.contains("apple") || brand.contains("iphone")) {
                    holder.imgProduct.setImageResource(R.drawable.phone_sample);
                } else if (brand.contains("samsung") || brand.contains("galaxy")) {
                    holder.imgProduct.setImageResource(R.drawable.samsung);
                } else if (brand.contains("xiaomi") || brand.contains("redmi")) {
                    holder.imgProduct.setImageResource(R.drawable.redmi);
                } else if (brand.contains("oppo")) {
                    holder.imgProduct.setImageResource(R.drawable.oppo);
                } else {
                    holder.imgProduct.setImageResource(R.drawable.phone_sample);
                }
            } catch (Resources.NotFoundException e) {
                // Use default image if resource not found
                holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // Check stock status
            int stockQuantity = stockQuantities.getOrDefault(productId, 0);
            if (stockQuantity >= item.quantity) {
                holder.tvStockStatus.setText("In Stock");
                holder.tvStockStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (stockQuantity > 0) {
                holder.tvStockStatus.setText("Limited Stock (" + stockQuantity + ")");
                holder.tvStockStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                holder.tvStockStatus.setText("Out of Stock");
                holder.tvStockStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            // Set checkbox state
            boolean isSelected = cart.isItemSelected(productId);
            holder.checkboxSelect.setChecked(isSelected);

            // Only allow selection if item is in stock
            holder.checkboxSelect.setEnabled(stockQuantity >= item.quantity);

            // Handle checkbox selection
            holder.checkboxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Store selection state
                cart.setItemSelected(productId, isChecked);

                // Update UI
                updateSummary();
            });

            // Handle remove button click
            holder.btnRemove.setOnClickListener(v -> {
                // Remove from cart
                cart.removeItem(productId);

                // Update UI
                items.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, items.size());

                // Update cart title and summary
                updateCartTitle();
                updateSummary();

                // Show empty cart message if needed
                if (items.isEmpty()) {
                    emptyCartLayout.setVisibility(View.VISIBLE);
                    cartItemsRecyclerView.setVisibility(View.GONE);
                }
            });

            // Handle click on the item to navigate to product detail
            holder.itemView.setOnClickListener(v -> navigateToProductDetail(phone));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        // ViewHolder for cart items
        class CartViewHolder extends RecyclerView.ViewHolder {

            CheckBox checkboxSelect;
            ImageView imgProduct;
            TextView tvProductName, tvProductPrice, tvQuantity, tvStockStatus;
            ImageButton btnRemove;

            public CartViewHolder(@NonNull View itemView) {
                super(itemView);

                checkboxSelect = itemView.findViewById(R.id.checkboxSelect);
                imgProduct = itemView.findViewById(R.id.imgProduct);
                tvProductName = itemView.findViewById(R.id.tvProductName);
                tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                tvStockStatus = itemView.findViewById(R.id.tvStockStatus);
                btnRemove = itemView.findViewById(R.id.btnRemove);
            }
        }
    }
}