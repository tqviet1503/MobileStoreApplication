package com.example.mobilestore.bill;
public class BillDetails {
    private int id;
    private int billId;
    private String phoneId;
    private int quantity;
    private double unitPrice;

    public BillDetails(int id, int billId, String phoneId, int quantity, double unitPrice) {
        this.id = id;
        this.billId = billId;
        this.phoneId = phoneId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Default constructor
    public BillDetails() {
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
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

    // Calculate total amount for this detail
    public double getTotalAmount() {
        return quantity * unitPrice;
    }

    @Override
    public String toString() {
        return "BillDetails{" +
                "id=" + id +
                ", billId=" + billId +
                ", phoneId='" + phoneId + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                '}';
    }
}