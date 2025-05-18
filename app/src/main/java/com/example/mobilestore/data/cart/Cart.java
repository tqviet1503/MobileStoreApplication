package com.example.mobilestore.data.cart;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mobilestore.data.repository.ProductRepository;
import com.example.mobilestore.model.Phone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton class to manage shopping cart data
 */
public class Cart {
    private static final String PREF_NAME = "cart_preferences";
    private static Cart instance;
    private final Map<String, CartItem> items = new HashMap<>();
    private Context context;
    private ProductRepository repository;

    public static class CartItem {
        public Phone phone;
        public int quantity;

        public CartItem(Phone phone, int quantity) {
            this.phone = phone;
            this.quantity = quantity;
        }

        public double getTotalPrice() {
            return phone.getPrice() * quantity;
        }
    }

    private Cart(Context context) {
        this.context = context.getApplicationContext();
        this.repository = ProductRepository.getInstance(this.context);
        loadCartFromPreferences();
    }

    public static synchronized Cart getInstance(Context context) {
        if (instance == null) {
            instance = new Cart(context);
        }
        return instance;
    }

    /**
     * Add a phone to cart with the specified quantity
     */
    public void addItem(Phone phone, int quantity) {
        if (phone == null || quantity <= 0) return;

        String phoneId = phone.getId();
        if (items.containsKey(phoneId)) {
            // If item already exists, update quantity
            CartItem item = items.get(phoneId);
            item.quantity += quantity;
        } else {
            // Add new item
            items.put(phoneId, new CartItem(phone, quantity));
        }

        // Save changes
        saveCartToPreferences();
    }

    /**
     * Update quantity of a specific item
     */
    public void updateItemQuantity(String phoneId, int newQuantity) {
        if (phoneId == null || newQuantity <= 0) return;

        if (items.containsKey(phoneId)) {
            CartItem item = items.get(phoneId);
            item.quantity = newQuantity;
            saveCartToPreferences();
        }
    }

    /**
     * Remove an item from cart
     */
    public void removeItem(String phoneId) {
        if (phoneId == null) return;

        if (items.containsKey(phoneId)) {
            items.remove(phoneId);
            saveCartToPreferences();
        }
    }

    /**
     * Get all cart items
     */
    public List<CartItem> getAllItems() {
        return new ArrayList<>(items.values());
    }

    /**
     * Get number of unique items in cart
     */
    public int getItemCount() {
        return items.size();
    }

    /**
     * Get total quantity of all items
     */
    public int getTotalQuantity() {
        int total = 0;
        for (CartItem item : items.values()) {
            total += item.quantity;
        }
        return total;
    }

    /**
     * Get total price of all items in cart
     */
    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : items.values()) {
            total += item.getTotalPrice();
        }
        return total;
    }

    /**
     * Check if cart is empty
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Clear all items from cart
     */
    public void clear() {
        items.clear();
        saveCartToPreferences();
    }

    /**
     * Save cart data to SharedPreferences
     */
    private void saveCartToPreferences() {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Clear existing data
        editor.clear();

        // Save the number of items
        editor.putInt("item_count", items.size());

        // Save each item
        int index = 0;
        for (Map.Entry<String, CartItem> entry : items.entrySet()) {
            String phoneId = entry.getKey();
            CartItem item = entry.getValue();

            editor.putString("item_" + index + "_id", phoneId);
            editor.putInt("item_" + index + "_quantity", item.quantity);
            index++;
        }

        editor.apply();
    }

    /**
     * Load cart data from SharedPreferences
     */
    private void loadCartFromPreferences() {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Clear existing data
        items.clear();

        // Get the number of items
        int itemCount = preferences.getInt("item_count", 0);

        // Load each item
        for (int i = 0; i < itemCount; i++) {
            String phoneId = preferences.getString("item_" + i + "_id", "");
            int quantity = preferences.getInt("item_" + i + "_quantity", 0);

            if (!phoneId.isEmpty() && quantity > 0) {
                // Find phone by ID
                Phone phone = repository.findPhoneByName(phoneId);
                if (phone != null) {
                    items.put(phoneId, new CartItem(phone, quantity));
                }
            }
        }
    }
}