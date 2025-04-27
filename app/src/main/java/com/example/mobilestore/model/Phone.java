package com.example.mobilestore.model;

public class Phone {
    private String id;
    private String model;
    private String brand;
    private String operation;
    private double price;
    private PhonePerformance performance;
    private String phoneName;
    private int stockQuantity;

    // Performance characteristics
    public static class PhonePerformance {
        private String processor;
        private int ramGB;
        private int storageGB;
        private String batteryCapacity;

        public PhonePerformance(String processor, int ramGB, int storageGB, String batteryCapacity) {
            this.processor = processor;
            this.ramGB = ramGB;
            this.storageGB = storageGB;
            this.batteryCapacity = batteryCapacity;
        }

        // Getters and setters
        public String getProcessor() { return processor; }
        public void setProcessor(String processor) { this.processor = processor; }
        public int getRamGB() { return ramGB; }
        public void setRamGB(int ramGB) { this.ramGB = ramGB; }
        public int getStorageGB() { return storageGB; }
        public void setStorageGB(int storageGB) { this.storageGB = storageGB; }
        public String getBatteryCapacity() { return batteryCapacity; }
        public void setBatteryCapacity(String batteryCapacity) { this.batteryCapacity = batteryCapacity; }
    }

    // Constructor
    public Phone(String id, String model, String brand, double price,
                 PhonePerformance performance, String phoneName, int stockQuantity) {
        this.id = id;
        this.model = model;
        this.brand = brand;
        this.price = price;
        this.performance = performance;
        this.stockQuantity = stockQuantity;
        this.phoneName = phoneName;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getPhoneName () { return phoneName; }
    public void setPhoneName (String phoneName) { this.phoneName = phoneName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public PhonePerformance getPerformance() { return performance; }
    public void setPerformance(PhonePerformance performance) { this.performance = performance; }


    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
}