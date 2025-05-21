package com.example.mobilestore.ui.admin_customer_bill;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mobilestore.R;
import com.example.mobilestore.adapter.BillAdapter;
import com.example.mobilestore.adapter.BillItemAdapter;
import com.example.mobilestore.bill.BillDetails;
import com.example.mobilestore.data.repository.ProductRepository;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.annotation.SuppressLint;

public class BillListFragment extends Fragment implements BillAdapter.OnBillClickListener {
    private static final String TAG = "BillListFragment";
    private RecyclerView recyclerView;
    private BillAdapter adapter;
    private ProductRepository repository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_bill_list, container, false);
        repository = ProductRepository.getInstance(requireContext());
        checkDatabaseStructure();
        recyclerView = view.findViewById(R.id.recycler_view_bill);
        setupRecyclerView();
        return view;
    }

    private void setupRecyclerView() {
        List<BillDetails> bills = getBillsFromDatabase();
        adapter = new BillAdapter(bills, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBillClick(BillDetails bill) {
        showBillDetailsDialog(bill);
    }

    private void showBillDetailsDialog(BillDetails bill) {
        try {
            Log.d(TAG, "Bill details: id=" + bill.getBillId() +
                    ", hasMultipleItems=" + bill.hasMultipleItems() +
                    ", quantity=" + bill.getQuantity());

            // Xem xét cả hasMultipleItems và số lượng sản phẩm
            boolean isComplexBill = bill.hasMultipleItems() || bill.getQuantity() > 1;

            if (isComplexBill) {
                Log.d(TAG, "Using multi-item dialog for bill " + bill.getBillId());
                showMultiItemBillDetailsDialog(bill);

            } else {
                Log.d(TAG, "Using single-item dialog for bill " + bill.getBillId());
                showSingleItemBillDetailsDialog(bill);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing bill dialog: " + e.getMessage(), e);
            // Fallback to simple dialog if any error occurs
            showSimpleBillDetailsDialog(bill);
        }
    }
    @SuppressLint("Range")
    private void checkDatabaseStructure() {
        SQLiteDatabase db = repository.db;

        Log.d(TAG, "Checking bills and customers:");

        // Check the first 2 bills
        Cursor billsCursor = db.rawQuery(
                "SELECT id, customer_id FROM bills ORDER BY id ASC LIMIT 2", null);

        int count = 0;
        while (billsCursor.moveToNext()) {
            count++;
            long billId = billsCursor.getLong(billsCursor.getColumnIndex("id"));
            String customerId = billsCursor.isNull(billsCursor.getColumnIndex("customer_id")) ?
                    "NULL" : billsCursor.getString(billsCursor.getColumnIndex("customer_id"));

            Log.d(TAG, "Bill #" + count + " - ID: " + billId + ", customer_id: " + customerId);

            // If there is a customer_id, check if the customer is found
            if (!"NULL".equals(customerId)) {
                Cursor customerCursor = db.rawQuery(
                        "SELECT id, name FROM customers WHERE id = ?",
                        new String[]{customerId});

                if (customerCursor.moveToFirst()) {
                    String customerName = customerCursor.getString(customerCursor.getColumnIndex("name"));
                    Log.d(TAG, "  → Found customer: " + customerName);
                } else {
                    Log.d(TAG, "  → COULD NOT find customer with ID: " + customerId);
                }
                customerCursor.close();
            }
        }
        billsCursor.close();

        // Check the structure of the bills table
        Cursor billsSchema = db.rawQuery("PRAGMA table_info(bills)", null);
        Log.d(TAG, "Structure of the bills table:");
        while (billsSchema.moveToNext()) {
            String name = billsSchema.getString(billsSchema.getColumnIndex("name"));
            String type = billsSchema.getString(billsSchema.getColumnIndex("type"));
            Log.d(TAG, "  - " + name + ": " + type);
        }
        billsSchema.close();

        // Check the structure of the customers table
        Cursor customersSchema = db.rawQuery("PRAGMA table_info(customers)", null);
        Log.d(TAG, "Structure of the customers table:");
        while (customersSchema.moveToNext()) {
            String name = customersSchema.getString(customersSchema.getColumnIndex("name"));
            String type = customersSchema.getString(customersSchema.getColumnIndex("type"));
            Log.d(TAG, "  - " + name + ": " + type);
        }
        customersSchema.close();
    }

    /**
     * Lấy thông tin khách hàng từ database, sử dụng LEFT JOIN để đảm bảo
     * luôn trả về dữ liệu bill ngay cả khi không tìm thấy khách hàng
     * @param billId ID của bill cần lấy thông tin
     * @return Map chứa thông tin khách hàng (name, phone, address)
     */
    @SuppressLint("Range")
    private Map<String, String> getCustomerInfo(long billId) {
        Map<String, String> customerInfo = new HashMap<>();
        customerInfo.put("name", "Walk-in Customer");
        customerInfo.put("phone", "-");
        customerInfo.put("address", "-");

        SQLiteDatabase db = repository.db;
        Cursor cursor = null;

        try {
            // Step 1: Check if the bill exists and has a customer_id
            Cursor billCheckCursor = db.rawQuery(
                    "SELECT customer_id FROM bills WHERE id = ? LIMIT 1",
                    new String[]{String.valueOf(billId)}
            );

            String customerId = null;
            if (billCheckCursor.moveToFirst()) {
                int customerIdIndex = billCheckCursor.getColumnIndex("customer_id");
                if (customerIdIndex != -1 && !billCheckCursor.isNull(customerIdIndex)) {
                    customerId = billCheckCursor.getString(customerIdIndex);
                    Log.d(TAG, "Bill " + billId + " has customer_id: " + customerId);
                } else {
                    Log.d(TAG, "Bill " + billId + " has a NULL customer_id");
                }
            }
            billCheckCursor.close();

            // Step 2: If there is a customer_id, try different ways to find the customer
            if (customerId != null) {
                // Method 1: Search by exact match
                cursor = db.rawQuery(
                        "SELECT name, phone, address FROM customers WHERE id = ? LIMIT 1",
                        new String[]{customerId}
                );

                // If not found, try searching with LIKE
                if (!cursor.moveToFirst()) {
                    cursor.close();
                    cursor = db.rawQuery(
                            "SELECT name, phone, address FROM customers WHERE id LIKE ? LIMIT 1",
                            new String[]{"%" + customerId + "%"}
                    );
                }

                // If still not found and customerId is numeric, try searching in different formats
                if (!cursor.moveToFirst() && isNumeric(customerId)) {
                    cursor.close();

                    // Try searching with id = "CUST_X" or other common formats
                    cursor = db.rawQuery(
                            "SELECT name, phone, address FROM customers WHERE id LIKE ? OR id LIKE ? LIMIT 1",
                            new String[]{"CUST_" + customerId + "%", "KH_" + customerId + "%"}
                    );
                }

                // If found, get the customer information
                if (cursor.moveToFirst()) {
                    customerInfo.put("name", cursor.getString(cursor.getColumnIndex("name")));
                    customerInfo.put("phone", cursor.getString(cursor.getColumnIndex("phone")));
                    customerInfo.put("address", cursor.getString(cursor.getColumnIndex("address")));
                    Log.d(TAG, "Found customer information: " + customerInfo.get("name"));
                } else {
                    Log.d(TAG, "No customer found with ID: " + customerId);
                }
            }

            // Step 3: Get the discount information from the bills table
            Cursor discountCursor = db.rawQuery(
                    "SELECT discount_percentage FROM bills WHERE id = ? LIMIT 1",
                    new String[]{String.valueOf(billId)}
            );

            if (discountCursor.moveToFirst()) {
                int discountIndex = discountCursor.getColumnIndex("discount_percentage");
                if (discountIndex != -1 && !discountCursor.isNull(discountIndex)) {
                    customerInfo.put("discount_percentage", String.valueOf(discountCursor.getInt(discountIndex)));
                } else {
                    customerInfo.put("discount_percentage", "0");
                }
            }
            discountCursor.close();

        } catch (Exception e) {
            Log.e(TAG, "Error getting customer information: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return customerInfo;
    }

    // Method to check if a string is numeric
    private boolean isNumeric(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private void showSimpleBillDetailsDialog(BillDetails bill) {
        try {
            SQLiteDatabase db = repository.db;

            // Lấy thông tin khách hàng bằng phương thức chung
            Map<String, String> customerInfo = getCustomerInfo(bill.getBillId());

            // Lấy thông tin chi tiết sản phẩm
            Cursor productCursor = null;
            String phoneName = "Unknown product";

            try {
                productCursor = db.rawQuery(
                        "SELECT bd.*, p.phone_name " +
                                "FROM bill_details bd " +
                                "INNER JOIN phones p ON bd.phone_id = p.id " +
                                "WHERE bd.bill_id = ? LIMIT 1",
                        new String[]{String.valueOf(bill.getBillId())}
                );

                if (productCursor.moveToFirst()) {
                    phoneName = productCursor.getString(productCursor.getColumnIndexOrThrow("phone_name"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting product info: " + e.getMessage(), e);
            } finally {
                if (productCursor != null) {
                    productCursor.close();
                }
            }

            View dialogView = LayoutInflater.from(getContext())
                    .inflate(R.layout.bill_details_dialog, null);

            TextView billIdText = dialogView.findViewById(R.id.text_bill_id);
            TextView billDateText = dialogView.findViewById(R.id.text_bill_date);
            TextView customerText = dialogView.findViewById(R.id.text_customer_info);
            TextView phoneText = dialogView.findViewById(R.id.text_phone_info);
            TextView quantityText = dialogView.findViewById(R.id.text_quantity);
            TextView totalText = dialogView.findViewById(R.id.text_total_amount);

            // Format bill ID to be shorter and more readable
            String formattedBillId = formatBillId(bill.getBillId());

            // Get timestamp from bill object and format it
            String formattedDate = bill.getFormattedDate();

            billIdText.setText("Bill " + formattedBillId);

            if (billDateText != null) {
                billDateText.setText("Date: " + formattedDate);
            }

            // Hiển thị thông tin khách hàng từ Map
            String customerName = customerInfo.get("name");
            String customerPhone = customerInfo.get("phone");
            String customerAddress = customerInfo.get("address");

            if ("Guest".equals(customerName) && "-".equals(customerPhone)) {
                customerText.setText("Guest Customer (No Details Available)");
            } else {
                customerText.setText(String.format("Customer: %s\nPhone: %s\nAddress: %s",
                        customerName, customerPhone, customerAddress));
            }

            phoneText.setText("Product: " + phoneName);
            quantityText.setText("Quantity: " + bill.getQuantity());

            // Tính lại tổng tiền từ các mặt hàng
            double calculatedTotal = 0;
            for (BillDetails.BillItem item : bill.getItems()) {
                calculatedTotal += item.getUnitPrice() * item.getQuantity();
            }

            // Áp dụng giảm giá nếu có
            if (customerInfo.containsKey("discount_percentage")) {
                int discountPercentage = Integer.parseInt(customerInfo.get("discount_percentage"));
                if (discountPercentage > 0) {
                    bill.setDiscountPercentage(discountPercentage);
                }
            }

            // Sử dụng getTotalAmount() của bill để lấy giá đã tính giảm giá
            double finalTotal = bill.hasDiscount() ? bill.getTotalAmount() : calculatedTotal;
            totalText.setText(String.format(Locale.getDefault(), "Total Price: %,.0fđ", finalTotal));

            new AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .setPositiveButton("Close", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error in showSimpleBillDetailsDialog: " + e.getMessage(), e);
            // Hiển thị một dialog lỗi đơn giản
            new AlertDialog.Builder(requireContext())
                    .setTitle("Error")
                    .setMessage("Could not load bill details.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    /**
     * Format bill ID to be shorter and more readable
     */
    private String formatBillId(long billId) {
        // Option 1: Take the last 5 digits
        return "#" + String.format("%05d", billId % 100000);

        // Option 2: Use a more human-readable format
        // return "#" + String.format("%d-%d", billId / 1000000, billId % 1000000);
    }

    /**
     * Định dạng timestamp
     */
    private String formatTimestamp(long timestamp) {
        try {
            // Log the original timestamp for debugging
            Log.d(TAG, "Original timestamp: " + timestamp);

            if (timestamp < 946684800000L) { // Jan 1, 2000
                if (timestamp > 946684800) { // Jan 1, 2000 in seconds
                    timestamp *= 1000; // Convert seconds to milliseconds
                    Log.d(TAG, "Converted timestamp from seconds: " + timestamp);
                } else {
                    timestamp = System.currentTimeMillis();
                    Log.d(TAG, "Using current time instead: " + timestamp);
                }
            }

            // Định dạng timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String formatted = sdf.format(new Date(timestamp));
            Log.d(TAG, "Formatted date: " + formatted);
            return formatted;
        } catch (Exception e) {
            Log.e(TAG, "Error formatting timestamp: " + e.getMessage(), e);
            // Trả về thời gian hiện tại
            return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(new Date(System.currentTimeMillis()));
        }
    }

    /**
     * Lấy timestamp chính xác từ cursor
     * @param cursor Database cursor
     * @param columnName Tên cột chứa timestamp
     * @return Timestamp đã được sửa (milli giây)
     */
    private long getTimestampFromCursor(Cursor cursor, String columnName) {
        try {
            int columnIndex = cursor.getColumnIndexOrThrow(columnName);
            long timestamp = cursor.getLong(columnIndex);

            Log.d(TAG, "Original timestamp from DB: " + timestamp + " for column " + columnName);

            if (timestamp < 946684800000L) { // Jan 1, 2000
                if (timestamp > 946684800) { // Jan 1, 2000 in seconds
                    timestamp *= 1000; // Convert seconds to milliseconds
                    Log.d(TAG, "Converted timestamp from seconds: " + timestamp);
                } else {
                    // Sử dụng thời gian hiện tại
                    timestamp = System.currentTimeMillis();
                    Log.d(TAG, "Using current time instead: " + timestamp);
                }
            }

            return timestamp;
        } catch (Exception e) {
            Log.e(TAG, "Error getting timestamp: " + e.getMessage(), e);
            // Trả về thời gian hiện tại
            return System.currentTimeMillis();
        }
    }

    private void showSingleItemBillDetailsDialog(BillDetails bill) {
        try {
            SQLiteDatabase db = repository.db;
            Log.d(TAG, "Showing single item bill dialog for bill ID: " + bill.getBillId());

            // Kiểm tra null
            if (bill == null) {
                Log.e(TAG, "Bill object is null");
                return;
            }

            // Kiểm tra bill items
            if (bill.getItems() == null || bill.getItems().isEmpty()) {
                Log.e(TAG, "Bill items list is null or empty for bill ID: " + bill.getBillId());
            }

            // Lấy thông tin khách hàng bằng phương thức chung
            Map<String, String> customerInfo = getCustomerInfo(bill.getBillId());

            // Lấy thông tin sản phẩm
            BillDetails.BillItem item = bill.getItems().isEmpty() ? null : bill.getItems().get(0);
            String phoneName = (item != null) ? item.getPhoneName() : "Unknown product";

            // Áp dụng discount từ thông tin khách hàng
            if (customerInfo.containsKey("discount_percentage")) {
                int discountPercentage = Integer.parseInt(customerInfo.get("discount_percentage"));
                if (discountPercentage > 0) {
                    bill.setDiscountPercentage(discountPercentage);
                    Log.d(TAG, "Applied discount " + discountPercentage + "% from customer info");
                }
            }

            View dialogView = LayoutInflater.from(getContext())
                    .inflate(R.layout.bill_details_dialog, null);

            if (dialogView == null) {
                Log.e(TAG, "Failed to inflate layout bill_details_dialog");
                showSimpleBillDetailsDialog(bill);
                return;
            }

            TextView billIdText = dialogView.findViewById(R.id.text_bill_id);
            TextView billDateText = dialogView.findViewById(R.id.text_bill_date);
            TextView customerText = dialogView.findViewById(R.id.text_customer_info);
            TextView phoneText = dialogView.findViewById(R.id.text_phone_info);
            TextView quantityText = dialogView.findViewById(R.id.text_quantity);
            TextView totalText = dialogView.findViewById(R.id.text_total_amount);
            TextView discountText = dialogView.findViewById(R.id.text_discount);

            Log.d(TAG, "Found views: billIdText=" + (billIdText != null) +
                    ", discountText=" + (discountText != null) +
                    ", totalText=" + (totalText != null));

            // Format bill ID to be shorter and more readable
            String formattedBillId = formatBillId(bill.getBillId());

            // Get timestamp from bill object and format it
            String formattedDate = bill.getFormattedDate();

            // Set UI fields
            if (billIdText != null) {
                billIdText.setText("Bill " + formattedBillId);
            }

            if (billDateText != null) {
                billDateText.setText("Date: " + formattedDate);
            }

            if (customerText != null) {
                customerText.setText(String.format("Customer: %s\nPhone: %s\nAddress: %s",
                        customerInfo.get("name"),
                        customerInfo.get("phone"),
                        customerInfo.get("address")
                ));
            }

            if (phoneText != null) {
                phoneText.setText("Product: " + phoneName);
            }

            if (quantityText != null) {
                quantityText.setText("Quantity: " + bill.getQuantity());
            }

            // Calculate subtotal - giá trước khi giảm giá
            double subtotal = 0;
            if (bill.getItems() != null) {
                for (BillDetails.BillItem billItem : bill.getItems()) {
                    subtotal += billItem.getUnitPrice() * billItem.getQuantity();
                }
            }
            Log.d(TAG, "Calculated subtotal: " + subtotal);

            // Hiển thị thông tin giảm giá
            if (discountText != null) {
                if (bill.hasDiscount()) {
                    discountText.setVisibility(View.VISIBLE);
                    discountText.setText(String.format("Discount: %s (-%s)",
                            bill.getFormattedDiscount(),
                            String.format(Locale.getDefault(), "%,.0fđ", bill.getDiscountAmount())));
                    discountText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
                    Log.d(TAG, "Displaying discount: " + bill.getFormattedDiscount());
                } else {
                    discountText.setVisibility(View.GONE);
                    Log.d(TAG, "No discount to display");
                }
            } else {
                Log.w(TAG, "discountText view not found in layout");
            }

            // Sử dụng getTotalAmount() của bill để lấy giá đã tính giảm giá
            double finalTotal;
            if (bill.hasDiscount() && totalText != null) {
                // Nếu có giảm giá, hiển thị tổng giá với thông tin giảm giá
                finalTotal = bill.getTotalAmount();
                Log.d(TAG, "Using discounted total: " + finalTotal);

                // Hiển thị cả giá gốc và giá sau giảm giá
                totalText.setText(String.format(Locale.getDefault(),
                        "Original Price: %,.0fđ\nTotal Price: %,.0fđ",
                        subtotal, finalTotal));
            } else if (totalText != null) {
                // Nếu không có giảm giá, hiển thị giá gốc
                finalTotal = subtotal;
                Log.d(TAG, "Using normal total: " + finalTotal);

                totalText.setText(String.format(Locale.getDefault(),
                        "Total Price: %,.0fđ", finalTotal));
            } else {
                Log.w(TAG, "totalText view not found in layout");
            }

            // Show dialog
            new AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .setPositiveButton("Close", null)
                    .show();
            Log.d(TAG, "Dialog shown successfully");

        } catch (Exception e) {
            Log.e(TAG, "Fatal error in showSingleItemBillDetailsDialog: " + e.getMessage(), e);
            showSimpleBillDetailsDialog(bill);
        }
    }

    /**
     * Hiển thị dialog chi tiết cho hóa đơn có nhiều sản phẩm
     */
    private void showMultiItemBillDetailsDialog(BillDetails bill) {
        try {
            View dialogView = LayoutInflater.from(getContext())
                    .inflate(R.layout.dialog_multi_item_bill, null);

            // Find views in the dialog layout
            TextView billIdText = dialogView.findViewById(R.id.text_bill_id);
            TextView billDateText = dialogView.findViewById(R.id.text_bill_date);
            TextView customerNameText = dialogView.findViewById(R.id.text_customer_name);
            TextView customerContactText = dialogView.findViewById(R.id.text_customer_contact);
            TextView totalItemsText = dialogView.findViewById(R.id.text_total_items);
            TextView originalPriceText = dialogView.findViewById(R.id.text_original_price);
            TextView discountText = dialogView.findViewById(R.id.text_discount);
            TextView totalAmountText = dialogView.findViewById(R.id.text_total_amount);
            RecyclerView itemsRecyclerView = dialogView.findViewById(R.id.recycler_view_bill_items);

            // Format bill ID to be shorter and more readable
            String formattedBillId = formatBillId(bill.getBillId());
            Log.d(TAG, "Displaying details for bill: " + bill.getBillId() + " (" + formattedBillId + ")");

            // Lấy thông tin khách hàng bằng phương thức chung
            Map<String, String> customerInfo = getCustomerInfo(bill.getBillId());

            // Áp dụng discount từ thông tin khách hàng
            if (customerInfo.containsKey("discount_percentage")) {
                int discountPercentage = Integer.parseInt(customerInfo.get("discount_percentage"));
                if (discountPercentage > 0) {
                    bill.setDiscountPercentage(discountPercentage);
                    Log.d(TAG, "Applied discount " + discountPercentage + "% from customer info");
                }
            }

            // Set customer information even if we can't get it from DB
            billIdText.setText("Bill " + formattedBillId);

            // Get timestamp from the bill object and format it
            String formattedDate = bill.getFormattedDate();
            billDateText.setText("Date: " + formattedDate);
            Log.d(TAG, "Setting date: " + formattedDate);

            // Hiển thị thông tin khách hàng từ Map
            customerNameText.setText("Customer: " + customerInfo.get("name"));
            customerContactText.setText(String.format("Phone: %s\nAddress: %s",
                    customerInfo.get("phone"), customerInfo.get("address")));

            // Tính tổng số lượng và tổng tiền từ danh sách sản phẩm
            int totalQuantity = 0;
            double subtotal = 0;

            if (bill.getItems() != null && !bill.getItems().isEmpty()) {
                for (BillDetails.BillItem item : bill.getItems()) {
                    totalQuantity += item.getQuantity();
                    subtotal += item.getUnitPrice() * item.getQuantity();
                }
            }

            Log.d(TAG, "Items count: " + bill.getItems().size() + ", Total quantity: " + totalQuantity);
            Log.d(TAG, "Calculated subtotal: " + subtotal);

            // Hiển thị các thông tin theo thứ tự: Quantity, Original Price, Voucher, Total Price

            // Quantity (Tổng số lượng)
            totalItemsText.setText("Quantity: " + totalQuantity);

            // Original Price (Giá gốc trước khi giảm)
            originalPriceText.setText("Original Price: " +
                    String.format(Locale.getDefault(), "%,.0fđ", subtotal));

            // Voucher (thông tin giảm giá)
            if (bill.hasDiscount()) {
                double discountAmount = bill.getDiscountAmount();
                discountText.setVisibility(View.VISIBLE);
                discountText.setText(String.format("Voucher: %s (-%s)",
                        bill.getFormattedDiscount(),
                        String.format(Locale.getDefault(), "%,.0fđ", discountAmount)));
                discountText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
            } else {
                discountText.setVisibility(View.GONE);
            }

            // Total Price (Giá sau khi giảm)
            double finalTotal = bill.hasDiscount() ? bill.getTotalAmount() : subtotal;
            totalAmountText.setText("Total Price: " +
                    String.format(Locale.getDefault(), "%,.0fđ", finalTotal));

            // Setup RecyclerView for bill items
            itemsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            BillItemAdapter itemAdapter = new BillItemAdapter(bill.getItems());
            itemsRecyclerView.setAdapter(itemAdapter);

            // To make sure RecyclerView displays all items
            itemsRecyclerView.setNestedScrollingEnabled(false);
            Log.d(TAG, "Setting up RecyclerView with " + bill.getItems().size() + " items");

            // Show dialog
            new AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .setPositiveButton("Close", null)
                    .show();
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Layout resource not found: " + e.getMessage(), e);
            // Fallback to simple dialog if custom layout not found
            showSimpleBillDetailsDialog(bill);
        } catch (Exception e) {
            Log.e(TAG, "Error showing dialog: " + e.getMessage(), e);
            showSimpleBillDetailsDialog(bill);
        }
    }

    private List<BillDetails> getBillsFromDatabase() {
        Log.d(TAG, "Getting bills from database");
        // Use a Map to group bill details by bill_id
        Map<Long, BillDetails> billsMap = new HashMap<>();
        SQLiteDatabase db = repository.db;

        try {
            // First, get a list of all bills - Thêm discount_percentage vào câu truy vấn
            Cursor billsCursor = db.rawQuery(
                    "SELECT DISTINCT b.id as bill_id, b.customer_id, b.total_amount, b.created_at, " +
                            "COUNT(bd.id) as item_count, b.discount_percentage " +  // Thêm discount_percentage
                            "FROM bills b " +
                            "LEFT JOIN bill_details bd ON b.id = bd.bill_id " +
                            "GROUP BY b.id " +
                            "ORDER BY b.created_at DESC", null);

            Log.d(TAG, "Found " + billsCursor.getCount() + " bills in database");

            // Process each bill
            if (billsCursor.moveToFirst()) {
                do {
                    long billId = billsCursor.getLong(billsCursor.getColumnIndexOrThrow("bill_id"));
                    int itemCount = billsCursor.getInt(billsCursor.getColumnIndexOrThrow("item_count"));

                    // Lấy thông tin về voucher
                    int discountPercentage = 0;
                    int discountColumnIndex = billsCursor.getColumnIndex("discount_percentage");
                    if (discountColumnIndex != -1) {
                        discountPercentage = billsCursor.getInt(discountColumnIndex);
                    }

                    Log.d(TAG, "Processing bill ID: " + billId + " with " + itemCount + " items" +
                            ", discount: " + discountPercentage + "%");

                    BillDetails bill = new BillDetails();
                    bill.setBillId(billId);
                    bill.setHasMultipleItems(itemCount > 1);
                    bill.setDiscountPercentage(discountPercentage); // Thiết lập discount percentage

                    // Store the timestamp with correction
                    long timestamp = getTimestampFromCursor(billsCursor, "created_at");
                    bill.setTimestamp(timestamp);

                    // Set formatted bill ID for display
                    bill.setFormattedBillId(formatBillId(billId));

                    billsMap.put(billId, bill);
                } while (billsCursor.moveToNext());
            }
            billsCursor.close();

            // Now for each bill, get the details of all items
            for (Map.Entry<Long, BillDetails> entry : billsMap.entrySet()) {
                long billId = entry.getKey();
                BillDetails bill = entry.getValue();

                Log.d(TAG, "Getting items for bill ID: " + billId);

                Cursor detailsCursor = db.rawQuery(
                        "SELECT bd.*, p.phone_name " +
                                "FROM bill_details bd " +
                                "INNER JOIN phones p ON bd.phone_id = p.id " +
                                "WHERE bd.bill_id = ?",
                        new String[]{String.valueOf(billId)});

                Log.d(TAG, "Found " + detailsCursor.getCount() + " items for bill ID: " + billId);

                if (detailsCursor.moveToFirst()) {
                    int totalQuantity = 0;

                    do {
                        int detailId = detailsCursor.getInt(detailsCursor.getColumnIndexOrThrow("id"));
                        String phoneId = detailsCursor.getString(detailsCursor.getColumnIndexOrThrow("phone_id"));
                        String phoneName = detailsCursor.getString(detailsCursor.getColumnIndexOrThrow("phone_name"));
                        int quantity = detailsCursor.getInt(detailsCursor.getColumnIndexOrThrow("quantity"));
                        double unitPrice = detailsCursor.getDouble(detailsCursor.getColumnIndexOrThrow("unit_price"));

                        Log.d(TAG, "Item: " + phoneName + ", Quantity: " + quantity + ", Price: " + unitPrice);

                        // For the first item, set the basic details of the bill
                        if (detailsCursor.isFirst()) {
                            bill.setId(detailId);
                            bill.setPhoneId(phoneId);
                            bill.setUnitPrice(unitPrice);
                        }

                        // Add this item to the bill's items list
                        bill.addItem(new BillDetails.BillItem(phoneId, phoneName, quantity, unitPrice));

                        // Keep track of total quantity
                        totalQuantity += quantity;
                    } while (detailsCursor.moveToNext());

                    // Set the total quantity for the bill
                    bill.setQuantity(totalQuantity);
                    Log.d(TAG, "Total quantity for bill ID " + billId + ": " + totalQuantity);

                    // Log thông tin về discount để debug
                    if (bill.hasDiscount()) {
                        Log.d(TAG, "Bill " + billId + " has discount: " + bill.getFormattedDiscount() +
                                ", Original: " + bill.formatPrice(bill.calculateSubtotal()) +
                                ", After discount: " + bill.formatPrice(bill.getTotalAmount()));
                    }
                }
                detailsCursor.close();

                // Kiểm tra thêm thông tin discount từ bảng bills
                if (!bill.hasDiscount()) {
                    try {
                        Cursor discountCursor = db.rawQuery(
                                "SELECT discount_percentage FROM bills WHERE id = ? LIMIT 1",
                                new String[]{String.valueOf(billId)});

                        if (discountCursor.moveToFirst()) {
                            int discountColumnIndex = discountCursor.getColumnIndex("discount_percentage");
                            if (discountColumnIndex != -1) {
                                int discountPercentage = discountCursor.getInt(discountColumnIndex);
                                if (discountPercentage > 0) {
                                    bill.setDiscountPercentage(discountPercentage);
                                    Log.d(TAG, "Found additional discount info for bill " + billId +
                                            ": " + discountPercentage + "%");
                                }
                            }
                        }
                        discountCursor.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Error getting additional discount info: " + e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting bills: " + e.getMessage(), e);
        }

        // Convert the map to a list
        List<BillDetails> result = new ArrayList<>(billsMap.values());
        Log.d(TAG, "Returning " + result.size() + " bills");
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupRecyclerView(); // Refresh list when fragment resumes
    }
}