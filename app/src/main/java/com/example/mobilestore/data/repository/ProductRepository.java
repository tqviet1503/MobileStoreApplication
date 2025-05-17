package com.example.mobilestore.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.mobilestore.data.local.AppDatabase;
import com.example.mobilestore.model.Brand;
import com.example.mobilestore.model.Phone;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {
    private static ProductRepository instance;
    private final AppDatabase database;
    private final List<OnDataChangeListener> listeners;
    private SQLiteDatabase db;

    public interface OnDataChangeListener {
        void onBrandAdded(Brand brand);
        void onPhoneAdded(Phone phone);
    }

    private ProductRepository(Context context) {
        database = AppDatabase.getInstance(context);
        listeners = new ArrayList<>();
        db = database.getWritableDatabase();
    }

    public static ProductRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ProductRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void addBrand(Brand brand) {
        ContentValues values = new ContentValues();
        values.put("name", brand.getName());
        values.put("logo_resource", brand.getLogoResource());
        values.put("phone_count", brand.getPhoneCount());

        long id = db.insert("brands", null, values);
        if (id != -1) {
            notifyBrandAdded(brand);
        }
    }

    public List<Brand> getAllBrands() {
        List<Brand> brands = new ArrayList<>();
        Cursor cursor = db.query("brands", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                int logoResource = cursor.getInt(cursor.getColumnIndexOrThrow("logo_resource"));
                int phoneCount = cursor.getInt(cursor.getColumnIndexOrThrow("phone_count"));

                brands.add(new Brand(name, logoResource, phoneCount));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return brands;
    }

    public void addPhone(String brandName, Phone phone) {
        db.beginTransaction();
        try {
            // Add phone
            ContentValues phoneValues = new ContentValues();
            phoneValues.put("id", phone.getId());
            phoneValues.put("model", phone.getModel());
            phoneValues.put("brand", brandName);
            phoneValues.put("price", phone.getPrice());
            phoneValues.put("battery", phone.getPerformance().getBatteryCapacity());
            phoneValues.put("phone_name", phone.getPhoneName());
            phoneValues.put("processor", phone.getPerformance().getProcessor());
            phoneValues.put("ram_gb", phone.getPerformance().getRamGB());
            phoneValues.put("storage_gb", phone.getPerformance().getStorageGB());
            phoneValues.put("stock_quantity", phone.getStockQuantity());

            long phoneId = db.insert("phones", null, phoneValues);

            // Update brand phone count
            db.execSQL("UPDATE brands SET phone_count = phone_count + 1 WHERE name = ?",
                    new String[]{brandName});

            db.setTransactionSuccessful();

            if (phoneId != -1) {
                notifyPhoneAdded(phone);
            }
        } finally {
            db.endTransaction();
        }
    }

    public List<Phone> getPhonesForBrand(String brandName) {
        List<Phone> phones = new ArrayList<>();
        Cursor cursor = db.query("phones", null,
                "brand = ?", new String[]{brandName},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Phone.PhonePerformance performance = new Phone.PhonePerformance(
                        cursor.getString(cursor.getColumnIndexOrThrow("processor")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("ram_gb")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("storage_gb")),
                        cursor.getString(cursor.getColumnIndexOrThrow("battery"))
                );

                Phone phone = new Phone(
                        cursor.getString(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("model")),
                        cursor.getString(cursor.getColumnIndexOrThrow("brand")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        performance,
                        cursor.getString(cursor.getColumnIndexOrThrow("phone_name")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("stock_quantity"))
                );
                phones.add(phone);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return phones;
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

    private void notifyPhoneAdded(Phone phone) {
        for (OnDataChangeListener listener : listeners) {
            listener.onPhoneAdded(phone);
        }
    }

    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}