package com.example.mobilestore.data.repository;

import com.example.mobilestore.model.Brand;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {
    //chỗ này mai mốt sẽ đổi lại thành database thật, tạm thời thì để static cho dễ nhìn
    private static ProductRepository instance;
    private final List<Brand> brands;
    private final List<OnDataChangeListener> listeners;

    public interface OnDataChangeListener {
        void onBrandAdded(Brand brand);
    }

    private ProductRepository() {
        brands = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    public static ProductRepository getInstance() {
        if (instance == null) {
            instance = new ProductRepository();
        }
        return instance;
    }

    public void addBrand(Brand brand) {
        brands.add(brand);
    }

    public List<Brand> getAllBrands() {
        return new ArrayList<>(brands);
    }

    public void addListener(OnDataChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(OnDataChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyBrandAdded(Brand brand) {
        for (OnDataChangeListener listener : listeners) {
            listener.onBrandAdded(brand);
        }
    }
}