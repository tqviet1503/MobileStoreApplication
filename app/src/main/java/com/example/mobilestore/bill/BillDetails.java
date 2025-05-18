package com.example.mobilestore.bill;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BillDetails {
    private int id;
    private long billId;
    private String formattedBillId; // lưu ID
    private String phoneId;
    private int quantity;
    private double unitPrice;
    private List<BillItem> items; // Added field for multiple items
    private boolean hasMultipleItems; // Flag to indicate if this is a multi-item bill
    private long timestamp; // Lưu trữ timestamp của bill
    private int discountPercentage; // lưu trữ phần trăm giảm giá

    public BillDetails() {
        items = new ArrayList<>();
        discountPercentage = 0; // Mặc định không có giảm giá
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getBillId() {
        return billId;
    }

    public void setBillId(long billId) {
        this.billId = billId;
        if (formattedBillId == null) {
            formattedBillId = "#" + String.format("%05d", billId % 100000);
        }
    }

    public String getFormattedBillId() {
        if (formattedBillId == null) {
            formattedBillId = "#" + String.format("%05d", billId % 100000);
        }
        return formattedBillId;
    }

    public void setFormattedBillId(String formattedBillId) {
        this.formattedBillId = formattedBillId;
    }

    public String getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(String phoneId) {
        this.phoneId = phoneId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Getter và setter cho phần trăm giảm giá
    public int getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(int discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    // New methods for multi-item handling
    public void addItem(BillItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        if (items.size() > 1) {
            hasMultipleItems = true;
        }
    }

    public List<BillItem> getItems() {
        return items;
    }

    public boolean hasMultipleItems() {
        return hasMultipleItems;
    }

    public void setHasMultipleItems(boolean hasMultipleItems) {
        this.hasMultipleItems = hasMultipleItems;
    }

    public int getTotalItems() {
        return items != null ? items.size() : 0;
    }

    // Tính tổng số lượng thực của tất cả sản phẩm
    public int getTotalQuantity() {
        int total = 0;
        if (items != null) {
            for (BillItem item : items) {
                total += item.getQuantity();
            }
        }
        return total;
    }

    // Tính tổng giá trị đơn hàng (đã bao gồm giảm giá)
    public double getTotalAmount() {
        double total = calculateSubtotal();

        // Áp dụng giảm giá nếu có
        if (discountPercentage > 0) {
            total = total * (100 - discountPercentage) / 100.0;
        }

        return total;
    }

    // Tính tổng phụ (trước khi giảm giá)
    public double calculateSubtotal() {
        double subtotal = 0;
        if (items != null) {
            for (BillItem item : items) {
                subtotal += item.getUnitPrice() * item.getQuantity();
            }
        }
        return subtotal;
    }

    // Tính số tiền được giảm
    public double getDiscountAmount() {
        double subtotal = calculateSubtotal();
        return discountPercentage > 0 ? (subtotal * discountPercentage / 100.0) : 0;
    }

    // Kiểm tra xem có áp dụng giảm giá không
    public boolean hasDiscount() {
        return discountPercentage > 0;
    }

    // Lấy thông tin giảm giá dưới dạng chuỗi định dạng
    public String getFormattedDiscount() {
        return hasDiscount() ? "-" + discountPercentage + "%" : "0%";
    }

    // Thêm phương thức định dạng ngày
    public String getFormattedDate() {
        try {
            long timeToFormat = timestamp;
            if (timeToFormat < 946684800000L) {
                if (timeToFormat > 946684800) {
                    timeToFormat *= 1000;
                } else {
                    timeToFormat = System.currentTimeMillis();
                }
            }

            // Định dạng timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(new Date(timeToFormat));
        } catch (Exception e) {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(new Date(System.currentTimeMillis()));
        }
    }

    // Phương thức định dạng giá thành chuỗi
    public String formatPrice(double price) {
        return String.format(Locale.getDefault(), "%,.0fđ", price);
    }

    // Inner class to represent individual items in a bill
    public static class BillItem {
        private String phoneId;
        private String phoneName;
        private int quantity;
        private double unitPrice;

        public BillItem(String phoneId, String phoneName, int quantity, double unitPrice) {
            this.phoneId = phoneId;
            this.phoneName = phoneName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getPhoneId() {
            return phoneId;
        }

        public String getPhoneName() {
            return phoneName;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public double getTotalPrice() {
            return unitPrice * quantity;
        }
    }
}