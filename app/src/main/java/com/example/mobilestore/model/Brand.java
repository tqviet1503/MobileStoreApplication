package com.example.mobilestore.model;

public class Brand {
    private String name;
    private int logoResource;
    private int phoneCount;

    public Brand(String name, int logoResource, int phoneCount) {
        this.name = name;
        this.logoResource = logoResource;
        this.phoneCount = phoneCount;
    }

    // Getters
    public String getName() { return name; }
    public int getLogoResource() { return logoResource; }
    public int getPhoneCount() { return phoneCount; }
}