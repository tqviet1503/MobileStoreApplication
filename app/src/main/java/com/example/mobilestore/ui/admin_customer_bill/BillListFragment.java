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

public class BillListFragment extends Fragment implements BillAdapter.OnBillClickListener {
    private static final String TAG = "BillListFragment";
    private RecyclerView recyclerView;
    private BillAdapter adapter;
    private ProductRepository repository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_bill_list, container, false);
        repository = ProductRepository.getInstance(requireContext());
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

    private void showSimpleBillDetailsDialog(BillDetails bill) {
        SQLiteDatabase db = repository.db;
        // Lấy thông tin chi tiết của bill dựa trên bill.getBillId()
        Cursor cursor = db.rawQuery(
                "SELECT b.*, c.name as customer_name, c.phone as customer_phone, " +
                        "c.address as customer_address, p.phone_name " +
                        "FROM bills b " +
                        "INNER JOIN customers c ON b.customer_id = c.id " +
                        "INNER JOIN bill_details bd ON b.id = bd.bill_id " +
                        "INNER JOIN phones p ON bd.phone_id = p.id " +
                        "WHERE b.id = ? LIMIT 1",
                new String[]{String.valueOf(bill.getBillId())}
        );

        if (cursor.moveToFirst()) {
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

            // Get corrected timestamp and format it
            long dateMillis = getTimestampFromCursor(cursor, "created_at");
            String formattedDate = formatTimestamp(dateMillis);

            billIdText.setText("Bill " + formattedBillId);

            if (billDateText != null) {
                billDateText.setText("Date: " + formattedDate);
            }

            customerText.setText(String.format("Customer: %s\nPhone: %s\nAddress: %s",
                    cursor.getString(cursor.getColumnIndexOrThrow("customer_name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("customer_phone")),
                    cursor.getString(cursor.getColumnIndexOrThrow("customer_address"))
            ));
            phoneText.setText("Product: " + cursor.getString(cursor.getColumnIndexOrThrow("phone_name")));
            quantityText.setText("Quantity: " + bill.getQuantity());

            // Tính lại tổng tiền từ các mặt hàng
            double calculatedTotal = 0;
            for (BillDetails.BillItem item : bill.getItems()) {
                calculatedTotal += item.getUnitPrice() * item.getQuantity();
            }

            double dbTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));

            // Use the calculated total if it seems more accurate
            double finalTotal = (calculatedTotal > 0 && Math.abs(calculatedTotal - dbTotal) > 1000)
                    ? calculatedTotal : dbTotal;

            totalText.setText(String.format(Locale.getDefault(), "Total Price: %,.0fđ", finalTotal));

            new AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .setPositiveButton("Close", null)
                    .show();
        }
        cursor.close();
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

            Cursor cursor = null;
            try {
                // Get bill details for a single item bill
                cursor = db.rawQuery(
                        "SELECT b.*, c.name as customer_name, c.phone as customer_phone, " +
                                "c.address as customer_address, p.phone_name, " +
                                "b.created_at as bill_date, b.discount_percentage " +
                                "FROM bills b " +
                                "INNER JOIN customers c ON b.customer_id = c.id " +
                                "INNER JOIN bill_details bd ON b.id = bd.bill_id " +
                                "INNER JOIN phones p ON bd.phone_id = p.id " +
                                "WHERE b.id = ? LIMIT 1",
                        new String[]{String.valueOf(bill.getBillId())}
                );

                Log.d(TAG, "SQL query executed for bill ID: " + bill.getBillId() +
                        ", has results: " + (cursor != null && cursor.moveToFirst()));

                if (cursor != null && cursor.moveToFirst()) {
                    View dialogView = LayoutInflater.from(getContext())
                            .inflate(R.layout.bill_details_dialog, null);

                    if (dialogView == null) {
                        Log.e(TAG, "Failed to inflate layout bill_details_dialog");
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

                    // Get corrected timestamp and format it
                    long dateMillis = getTimestampFromCursor(cursor, "bill_date");
                    String formattedDate = formatTimestamp(dateMillis);

                    // Set UI fields
                    if (billIdText != null) {
                        billIdText.setText("Bill " + formattedBillId);
                    }

                    if (billDateText != null) {
                        billDateText.setText("Date: " + formattedDate);
                    }

                    if (customerText != null) {
                        customerText.setText(String.format("Customer: %s\nPhone: %s\nAddress: %s",
                                cursor.getString(cursor.getColumnIndexOrThrow("customer_name")),
                                cursor.getString(cursor.getColumnIndexOrThrow("customer_phone")),
                                cursor.getString(cursor.getColumnIndexOrThrow("customer_address"))
                        ));
                    }

                    if (phoneText != null) {
                        phoneText.setText("Product: " + cursor.getString(cursor.getColumnIndexOrThrow("phone_name")));
                    }

                    if (quantityText != null) {
                        quantityText.setText("Quantity: " + bill.getQuantity());
                    }

                    // Lấy thông tin về voucher từ cursor
                    int discountPercentage = 0;
                    int discountColumnIndex = cursor.getColumnIndex("discount_percentage");
                    if (discountColumnIndex != -1) {
                        discountPercentage = cursor.getInt(discountColumnIndex);
                        // Cập nhật discount percentage cho bill
                        bill.setDiscountPercentage(discountPercentage);
                        Log.d(TAG, "Found discount: " + discountPercentage + "% for bill ID: " + bill.getBillId());
                    } else {
                        Log.w(TAG, "Column 'discount_percentage' not found in cursor");
                    }

                    // Calculate subtotal - giá trước khi giảm giá
                    double subtotal = 0;
                    if (bill.getItems() != null) {
                        for (BillDetails.BillItem item : bill.getItems()) {
                            subtotal += item.getUnitPrice() * item.getQuantity();
                        }
                    }
                    Log.d(TAG, "Calculated subtotal: " + subtotal);

                    double dbTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));
                    Log.d(TAG, "DB total: " + dbTotal);

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
                        // Nếu không có giảm giá, sử dụng logic cũ để lấy giá
                        finalTotal = (subtotal > 0 && Math.abs(subtotal - dbTotal) > 1000)
                                ? subtotal : dbTotal;
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
                } else {
                    Log.e(TAG, "No data found for bill ID: " + bill.getBillId() + " - Showing simple dialog instead");
                    showSimpleBillDetailsDialog(bill);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in SQL query: " + e.getMessage(), e);
                showSimpleBillDetailsDialog(bill);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
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
            TextView originalPriceText = dialogView.findViewById(R.id.text_original_price); // Thêm TextView giá gốc
            TextView discountText = dialogView.findViewById(R.id.text_discount); // Thêm TextView voucher
            TextView totalAmountText = dialogView.findViewById(R.id.text_total_amount);
            RecyclerView itemsRecyclerView = dialogView.findViewById(R.id.recycler_view_bill_items);

            // Format bill ID to be shorter and more readable
            String formattedBillId = formatBillId(bill.getBillId());
            Log.d(TAG, "Displaying details for bill: " + bill.getBillId() + " (" + formattedBillId + ")");

            // Get customer details
            SQLiteDatabase db = repository.db;

            // Set customer information even if we can't get it from DB
            billIdText.setText("Bill " + formattedBillId);

            // Get timestamp from the bill object and format it
            String formattedDate = bill.getFormattedDate();
            billDateText.setText("Date: " + formattedDate);
            Log.d(TAG, "Setting date: " + formattedDate);

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

            // Try to get customer info from database
            Cursor customerCursor = null;
            try {
                customerCursor = db.rawQuery(
                        "SELECT c.name, c.phone, c.address, c.email, b.created_at, b.total_amount, " +
                                "b.discount_percentage " + // Thêm discount_percentage vào truy vấn
                                "FROM bills b " +
                                "INNER JOIN customers c ON b.customer_id = c.id " +
                                "WHERE b.id = ? LIMIT 1",
                        new String[]{String.valueOf(bill.getBillId())}
                );

                if (customerCursor.moveToFirst()) {
                    // Get customer name, phone, address from database
                    String customerName = customerCursor.getString(customerCursor.getColumnIndexOrThrow("name"));
                    String customerPhone = customerCursor.getString(customerCursor.getColumnIndexOrThrow("phone"));
                    String customerAddress = customerCursor.getString(customerCursor.getColumnIndexOrThrow("address"));

                    // Set customer information
                    customerNameText.setText("Customer: " + customerName);
                    customerContactText.setText(String.format("Phone: %s\nAddress: %s", customerPhone, customerAddress));

                    // Get total amount from database
                    double dbTotal = customerCursor.getDouble(customerCursor.getColumnIndexOrThrow("total_amount"));

                    // Lấy thông tin voucher
                    int discountPercentage = 0;
                    int discountColumnIndex = customerCursor.getColumnIndex("discount_percentage");
                    if (discountColumnIndex != -1) {
                        discountPercentage = customerCursor.getInt(discountColumnIndex);
                        bill.setDiscountPercentage(discountPercentage);

                        Log.d(TAG, "Discount percentage from DB: " + discountPercentage + "%");
                    }

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
                    double finalTotal;
                    if (bill.hasDiscount()) {
                        finalTotal = bill.getTotalAmount();
                    } else {
                        finalTotal = (subtotal > 0 && Math.abs(subtotal - dbTotal) > 1000)
                                ? subtotal : dbTotal;
                    }

                    totalAmountText.setText("Total Price: " +
                            String.format(Locale.getDefault(), "%,.0fđ", finalTotal));

                    Log.d(TAG, "DB total: " + dbTotal + ", Final total: " + finalTotal);
                } else {
                    // Fallback if customer not found
                    customerNameText.setText("Customer: Unknown");
                    customerContactText.setText("Phone: -\nAddress: -");

                    // Hiển thị thông tin cơ bản nếu không tìm thấy khách hàng
                    totalItemsText.setText("Quantity: " + totalQuantity);
                    originalPriceText.setText("Original Price: " +
                            String.format(Locale.getDefault(), "%,.0fđ", subtotal));
                    discountText.setVisibility(View.GONE);
                    totalAmountText.setText("Total Price: " +
                            String.format(Locale.getDefault(), "%,.0fđ", subtotal));

                    Log.w(TAG, "Customer not found for bill ID: " + bill.getBillId());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting customer info: " + e.getMessage(), e);
                // Fallback if error occurs
                customerNameText.setText("Customer: Error loading data");
                customerContactText.setText("Phone: -\nAddress: -");
                totalAmountText.setText(String.format(Locale.getDefault(), "Total Price: %,.0fđ", subtotal));
            } finally {
                if (customerCursor != null) {
                    customerCursor.close();
                }
            }

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
                            "INNER JOIN bill_details bd ON b.id = bd.bill_id " +
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