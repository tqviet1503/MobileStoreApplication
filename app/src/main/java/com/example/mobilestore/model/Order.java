package com.example.mobilestore.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Order implements Serializable {
    private String productName;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private boolean hasDiscount;
    private String customerName;
    private String customerAddress;
    private String notes;
    private Date orderDate;

    public Order(String productName, int quantity, double unitPrice, double totalPrice,
                 boolean hasDiscount, String customerName, String customerAddress, String notes) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.hasDiscount = hasDiscount;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.notes = notes;
        this.orderDate = new Date();
    }

    // Getters và setters
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
                "productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                ", hasDiscount=" + hasDiscount +
                ", customerName='" + customerName + '\'' +
                ", customerAddress='" + customerAddress + '\'' +
                ", notes='" + notes + '\'' +
                ", orderDate=" + orderDate +
                '}';
    }
}