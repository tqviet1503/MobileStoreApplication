package com.example.mobilestore.data.customer;

public class CustomerDetails {
    public String id;
    public String name;
    public String phone;
    public String email;
    public String address;
    public int totalOrders, loyaltyPoints;
    public double totalSpent;

    public CustomerDetails(String id, String name, String phone, String email,
                    String address, int totalOrders, double totalSpent,
                    int loyaltyPoints) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.totalOrders = totalOrders;
        this.totalSpent = totalSpent;
        this.loyaltyPoints = loyaltyPoints;
    }
}
