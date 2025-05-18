package com.example.mobilestore.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Order implements Serializable {
    // Add serialVersionUID to ensure compatibility across different versions
    private static final long serialVersionUID = 1L;

    private String orderId; // Added to uniquely identify orders
    private List<OrderItem> orderItems; // List of items in this order
    private double totalPrice;
    private boolean hasDiscount;
    private String customerName;
    private String customerAddress;
    private String notes;
    private Date orderDate;

    // Constructor for single product order (for backward compatibility)
    public Order(String productName, int quantity, double unitPrice, double totalPrice,
                 boolean hasDiscount, String customerName, String customerAddress, String notes) {
        this.orderId = generateOrderId();
        this.orderItems = new ArrayList<>();
        // Add the single product as an OrderItem
        this.orderItems.add(new OrderItem(productName, quantity, unitPrice));
        this.totalPrice = totalPrice;
        this.hasDiscount = hasDiscount;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.notes = notes;
        this.orderDate = new Date();
    }

    // Constructor for multiple product orders
    public Order(List<OrderItem> orderItems, double totalPrice, boolean hasDiscount,
                 String customerName, String customerAddress, String notes) {
        this.orderId = generateOrderId();
        this.orderItems = orderItems;
        this.totalPrice = totalPrice;
        this.hasDiscount = hasDiscount;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.notes = notes;
        this.orderDate = new Date();
    }

    // Generate a unique order ID based on timestamp
    private String generateOrderId() {
        return "ORD" + System.currentTimeMillis();
    }

    // For backward compatibility with single product orders
    public String getProductName() {
        if (orderItems != null && !orderItems.isEmpty()) {
            if (orderItems.size() == 1) {
                return orderItems.get(0).getProductName();
            } else {
                return "Multiple items (" + orderItems.size() + ")";
            }
        }
        return "Unknown product";
    }

    // For backward compatibility with single product orders
    public int getQuantity() {
        if (orderItems != null && !orderItems.isEmpty()) {
            if (orderItems.size() == 1) {
                return orderItems.get(0).getQuantity();
            } else {
                // Return total quantity across all items
                int totalQuantity = 0;
                for (OrderItem item : orderItems) {
                    totalQuantity += item.getQuantity();
                }
                return totalQuantity;
            }
        }
        return 0;
    }

    // For backward compatibility with single product orders
    public double getUnitPrice() {
        if (orderItems != null && !orderItems.isEmpty() && orderItems.size() == 1) {
            return orderItems.get(0).getUnitPrice();
        }
        return 0;
    }

    // New methods to work with multiple order items
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public boolean hasMultipleItems() {
        return orderItems != null && orderItems.size() > 1;
    }

    public int getItemCount() {
        return orderItems != null ? orderItems.size() : 0;
    }

    public String getOrderId() {
        return orderId;
    }

    // Existing getters
    public double getTotalPrice() {
        return totalPrice;
    }

    public boolean isHasDiscount() {
        return hasDiscount;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public String getNotes() {
        return notes;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(orderDate);
    }

    public String getFormattedPrice(double price) {
        return String.format(Locale.getDefault(), "%,.0fđ", price);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", orderItems=" + orderItems +
                ", totalPrice=" + totalPrice +
                ", hasDiscount=" + hasDiscount +
                ", customerName='" + customerName + '\'' +
                ", customerAddress='" + customerAddress + '\'' +
                ", notes='" + notes + '\'' +
                ", orderDate=" + orderDate +
                '}';
    }

    // Inner class to represent an individual order item
    public static class OrderItem implements Serializable {
        // Add serialVersionUID to ensure compatibility
        private static final long serialVersionUID = 1L;

        private String productName;
        private int quantity;
        private double unitPrice;

        public OrderItem(String productName, int quantity, double unitPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getProductName() {
            return productName;
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

        @Override
        public String toString() {
            return "OrderItem{" +
                    "productName='" + productName + '\'' +
                    ", quantity=" + quantity +
                    ", unitPrice=" + unitPrice +
                    '}';
        }
    }
}