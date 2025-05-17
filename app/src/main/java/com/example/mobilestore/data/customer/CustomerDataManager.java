package com.example.mobilestore.data.customer;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Lớp quản lý dữ liệu khách hàng và thông tin mua hàng
 * Sử dụng SharedPreferences để lưu trữ dữ liệu giữa các activity
 */
public class CustomerDataManager {
    private static final String PREFS_NAME = "CustomerPrefs";
    private static final String KEY_CUSTOMER_NAME = "customer_name";
    private static final String KEY_CUSTOMER_ID = "customer_id";
    private static final String KEY_CUSTOMER_STATUS = "customer_status";
    private static final String KEY_CUSTOMER_PHONE = "customer_phone";
    private static final String KEY_CUSTOMER_EMAIL = "customer_email";
    private static final String KEY_CUSTOMER_ADDRESS = "customer_address";
    private static final String KEY_TOTAL_ORDERS = "total_orders";
    private static final String KEY_TOTAL_SPENT = "total_spent";
    private static final String KEY_LOYALTY_POINTS = "loyalty_points";

    private static CustomerDataManager instance;
    private final SharedPreferences prefs;

    // Singleton pattern
    private CustomerDataManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        initDefaultDataIfNeeded();
    }

    public static synchronized CustomerDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new CustomerDataManager(context);
        }
        return instance;
    }

    private void initDefaultDataIfNeeded() {
        if (!prefs.contains(KEY_CUSTOMER_NAME)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_CUSTOMER_NAME, "Quoc Viet");
            editor.putString(KEY_CUSTOMER_ID, "#12345");
            editor.putString(KEY_CUSTOMER_STATUS, "Active Customer");
            editor.putString(KEY_CUSTOMER_PHONE, "+84 12 456 789");
            editor.putString(KEY_CUSTOMER_EMAIL, "customer@example.com");
            editor.putString(KEY_CUSTOMER_ADDRESS, "Thu Duc City, HCMC");
            editor.putInt(KEY_TOTAL_ORDERS, 0);
            editor.putFloat(KEY_TOTAL_SPENT, 0);
            editor.putInt(KEY_LOYALTY_POINTS, 0);
            editor.apply();
        }
    }

    // Các phương thức lấy thông tin khách hàng
    public String getCustomerName() {
        return prefs.getString(KEY_CUSTOMER_NAME, "");
    }

    public String getCustomerId() {
        return prefs.getString(KEY_CUSTOMER_ID, "");
    }

    public String getCustomerStatus() {
        return prefs.getString(KEY_CUSTOMER_STATUS, "");
    }

    public String getCustomerPhone() {
        return prefs.getString(KEY_CUSTOMER_PHONE, "");
    }

    public String getCustomerEmail() {
        return prefs.getString(KEY_CUSTOMER_EMAIL, "");
    }

    public String getCustomerAddress() {
        return prefs.getString(KEY_CUSTOMER_ADDRESS, "");
    }

    public int getTotalOrders() {
        return prefs.getInt(KEY_TOTAL_ORDERS, 0);
    }

    public double getTotalSpent() {
        return prefs.getFloat(KEY_TOTAL_SPENT, 0);
    }

    public int getLoyaltyPoints() {
        return prefs.getInt(KEY_LOYALTY_POINTS, 0);
    }

    // Các phương thức cập nhật thông tin khách hàng
    public void setCustomerPhone(String phone) {
        prefs.edit().putString(KEY_CUSTOMER_PHONE, phone).apply();
    }

    public void setCustomerEmail(String email) {
        prefs.edit().putString(KEY_CUSTOMER_EMAIL, email).apply();
    }

    public void setCustomerAddress(String address) {
        prefs.edit().putString(KEY_CUSTOMER_ADDRESS, address).apply();
    }

    // Phương thức xử lý khi có đơn hàng mới
    public void processNewOrder(double orderAmount) {
        SharedPreferences.Editor editor = prefs.edit();

        // Tăng số lượng đơn hàng
        int currentOrders = prefs.getInt(KEY_TOTAL_ORDERS, 0);
        editor.putInt(KEY_TOTAL_ORDERS, currentOrders + 1);

        // Cập nhật tổng số tiền đã chi
        float currentSpent = prefs.getFloat(KEY_TOTAL_SPENT, 0);
        float newTotalSpent = currentSpent + (float) orderAmount;
        editor.putFloat(KEY_TOTAL_SPENT, newTotalSpent);

        // Cập nhật điểm tích lũy (1% giá trị đơn hàng)
        int currentPoints = prefs.getInt(KEY_LOYALTY_POINTS, 0);
        int pointsEarned = (int) (orderAmount * 0.0001); // 0.01% của đơn hàng
        editor.putInt(KEY_LOYALTY_POINTS, currentPoints + pointsEarned);

        // Lưu các thay đổi
        editor.apply();
    }

    // Định dạng tiền tệ cho tổng chi tiêu
    public String getFormattedTotalSpent() {
        double totalSpent = getTotalSpent();
        return String.format("%,.0fđ", totalSpent);
    }
}