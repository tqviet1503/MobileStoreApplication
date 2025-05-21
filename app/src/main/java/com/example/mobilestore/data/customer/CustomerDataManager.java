package com.example.mobilestore.data.customer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.mobilestore.model.Order;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

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
    private static final String KEY_ORDERS_LIST = "orders_list";

    private static CustomerDataManager instance;
    private final SharedPreferences prefs;
    private final Context context;
    private List<Order> ordersList;
    private static final String KEY_INSTALLATION_ID = "installation_id";
    private static final String KEY_APP_VERSION = "app_version";

    // Singleton pattern
    private CustomerDataManager(Context context) {
        this.context = context.getApplicationContext();
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        initDefaultDataIfNeeded();
        loadOrdersList();
    }

    public static synchronized CustomerDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new CustomerDataManager(context);
        }
        return instance;
    }

    private void initDefaultDataIfNeeded() {
        boolean shouldReset = false;

        // Kiểm tra xem có phải là lần cài đặt mới không
        String currentInstallId = java.util.UUID.randomUUID().toString();
        String savedInstallId = prefs.getString(KEY_INSTALLATION_ID, null);

        // Lấy version code từ PackageInfo
        int currentVersion;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            currentVersion = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            currentVersion = 0; // Giá trị mặc định nếu không lấy được
            Log.e("CustomerDataManager", "Could not get package info", e);
        }

        int savedVersion = prefs.getInt(KEY_APP_VERSION, -1);

        // Nếu không có ID cài đặt hoặc khác version -> lần cài đặt mới
        if (savedInstallId == null || savedVersion != currentVersion) {
            shouldReset = true;
        }

        SharedPreferences.Editor editor = prefs.edit();

        // Lưu ID cài đặt và version mới
        editor.putString(KEY_INSTALLATION_ID, currentInstallId);
        editor.putInt(KEY_APP_VERSION, currentVersion);

        if (shouldReset || !prefs.contains(KEY_CUSTOMER_NAME)) {
            // Reset toàn bộ dữ liệu
            editor.putString(KEY_CUSTOMER_NAME, "Quoc Viet");
            editor.putString(KEY_CUSTOMER_ID, "#12345");
            editor.putString(KEY_CUSTOMER_STATUS, "Active Customer");
            editor.putString(KEY_CUSTOMER_PHONE, "+84 123 456 789");
            editor.putString(KEY_CUSTOMER_EMAIL, "customer@example.com");
            editor.putString(KEY_CUSTOMER_ADDRESS, "Thu Duc City, HCMC");
            editor.putInt(KEY_TOTAL_ORDERS, 0);
            editor.putFloat(KEY_TOTAL_SPENT, 0);
            editor.putInt(KEY_LOYALTY_POINTS, 0);

            // Xóa lịch sử đơn hàng
            editor.remove(KEY_ORDERS_LIST);

            // Khởi tạo danh sách đơn hàng trống
            ordersList = new ArrayList<>();
        }

        editor.apply();
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

        // Cập nhật điểm tích lũy (1 điểm cho mỗi 100,000đ)
        int currentPoints = prefs.getInt(KEY_LOYALTY_POINTS, 0);
        int pointsEarned = (int) (orderAmount / 100000); // 1 điểm cho mỗi 100,000đ
        editor.putInt(KEY_LOYALTY_POINTS, currentPoints + pointsEarned);

        // Lưu các thay đổi
        editor.apply();
    }

    // Định dạng tiền tệ cho tổng chi tiêu
    public String getFormattedTotalSpent() {
        double totalSpent = getTotalSpent();
        return String.format("%,.0fđ", totalSpent);
    }

    // Lưu đơn hàng mới
    public void saveOrder(Order order) {
        if (ordersList == null) {
            ordersList = new ArrayList<>();
        }

        // Thêm đơn hàng mới vào đầu danh sách (đơn hàng mới nhất đầu tiên)
        ordersList.add(0, order);
        saveOrdersList();

        // Cập nhật tổng tiền và số lượng đơn hàng
        processNewOrder(order.getTotalPrice());
    }

    // Lấy danh sách đơn hàng
    public List<Order> getOrdersList() {
        if (ordersList == null) {
            loadOrdersList();
        }
        return ordersList;
    }

    // Lưu danh sách đơn hàng vào SharedPreferences
    private void saveOrdersList() {
        try {
            SharedPreferences.Editor editor = prefs.edit();

            if (ordersList == null || ordersList.isEmpty()) {
                editor.remove(KEY_ORDERS_LIST);
                editor.apply();
                return;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(ordersList);
            oos.close();

            String serializedList = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.DEFAULT);
            editor.putString(KEY_ORDERS_LIST, serializedList);
            editor.apply();
        } catch (IOException e) {
            Log.e("CustomerDataManager", "Error saving orders list: " + e.getMessage());
        }
    }

    // Tải danh sách đơn hàng từ SharedPreferences
    @SuppressWarnings("unchecked")
    private void loadOrdersList() {
        try {
            String serializedList = prefs.getString(KEY_ORDERS_LIST, null);

            if (serializedList == null) {
                ordersList = new ArrayList<>();
                return;
            }

            byte[] data = android.util.Base64.decode(serializedList, android.util.Base64.DEFAULT);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            ordersList = (List<Order>) ois.readObject();
            ois.close();

            if (ordersList == null) {
                ordersList = new ArrayList<>();
            }
        } catch (Exception e) {
            Log.e("CustomerDataManager", "Error loading orders list: " + e.getMessage());
            ordersList = new ArrayList<>();

            // If we get a ClassCastException or ClassNotFoundException, it's likely because
            // we've updated the Order class structure. In this case, we'll just start with
            // a fresh orders list rather than crashing the app.
            if (e instanceof ClassCastException || e instanceof ClassNotFoundException) {
                Log.w("CustomerDataManager", "Order class structure may have changed. Starting with fresh orders list.");
                // Clear the old data
                prefs.edit().remove(KEY_ORDERS_LIST).apply();
            }
        }
    }
}