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
    // Map lưu trữ trạng thái chọn của mỗi sản phẩm
    private final Map<String, Boolean> selectedItems = new HashMap<>();
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
            // Mặc định mọi sản phẩm đều được chọn khi thêm vào giỏ hàng
            selectedItems.put(phoneId, true);
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
            selectedItems.remove(phoneId);
            saveCartToPreferences();
        }
    }

    /**
     * Remove an item from cart using phone object
     */
    public void removeItem(Phone phone) {
        if (phone == null) return;
        removeItem(phone.getId());
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
     * Get total price of selected items only
     */
    public double getSelectedItemsPrice() {
        double total = 0;
        for (Map.Entry<String, CartItem> entry : items.entrySet()) {
            String phoneId = entry.getKey();
            // Chỉ tính tổng cho những sản phẩm được chọn
            if (isItemSelected(phoneId)) {
                total += entry.getValue().getTotalPrice();
            }
        }
        return total;
    }

    /**
     * Get number of selected items
     */
    public int getSelectedItemCount() {
        int count = 0;
        for (Map.Entry<String, Boolean> entry : selectedItems.entrySet()) {
            if (entry.getValue() && items.containsKey(entry.getKey())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get total quantity of selected items
     */
    public int getSelectedQuantity() {
        int total = 0;
        for (Map.Entry<String, CartItem> entry : items.entrySet()) {
            String phoneId = entry.getKey();
            // Chỉ tính cho những sản phẩm được chọn
            if (isItemSelected(phoneId)) {
                total += entry.getValue().quantity;
            }
        }
        return total;
    }

    /**
     * Check if a specific item is selected
     */
    public boolean isItemSelected(String phoneId) {
        return selectedItems.containsKey(phoneId) && selectedItems.get(phoneId);
    }

    /**
     * Set selection state for a specific item
     */
    public void setItemSelected(String phoneId, boolean selected) {
        if (items.containsKey(phoneId)) {
            selectedItems.put(phoneId, selected);
            saveCartToPreferences();
        }
    }

    /**
     * Set selection state for all items
     */
    public void selectAllItems(boolean selected) {
        for (String phoneId : items.keySet()) {
            selectedItems.put(phoneId, selected);
        }
        saveCartToPreferences();
    }

    /**
     * Get list of selected cart items
     */
    public List<CartItem> getSelectedItems() {
        List<CartItem> selected = new ArrayList<>();
        for (Map.Entry<String, CartItem> entry : items.entrySet()) {
            String phoneId = entry.getKey();
            if (isItemSelected(phoneId)) {
                selected.add(entry.getValue());
            }
        }
        return selected;
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
        selectedItems.clear();
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
            // Lưu trạng thái chọn của sản phẩm
            editor.putBoolean("item_" + index + "_selected", isItemSelected(phoneId));
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
        selectedItems.clear();

        // Get the number of items
        int itemCount = preferences.getInt("item_count", 0);

        // Load each item
        for (int i = 0; i < itemCount; i++) {
            String phoneId = preferences.getString("item_" + i + "_id", "");
            int quantity = preferences.getInt("item_" + i + "_quantity", 0);
            boolean isSelected = preferences.getBoolean("item_" + i + "_selected", true);

            if (!phoneId.isEmpty() && quantity > 0) {
                // Find phone by ID
                Phone phone = repository.findPhoneByName(phoneId);
                if (phone != null) {
                    items.put(phoneId, new CartItem(phone, quantity));
                    selectedItems.put(phoneId, isSelected);
                }
            }
        }
    }
}