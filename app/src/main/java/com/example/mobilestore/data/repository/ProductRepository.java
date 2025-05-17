package com.example.mobilestore.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mobilestore.data.local.AppDatabase;
import com.example.mobilestore.data.local.PhoneDAO;
import com.example.mobilestore.model.Brand;
import com.example.mobilestore.model.Phone;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {
//    private static final String TAG = "ProductRepository";
    private static ProductRepository instance;
    private final AppDatabase database;
    private final List<OnDataChangeListener> listeners;
    public SQLiteDatabase db;
//    private final PhoneDAO phoneDAO;

    public interface OnDataChangeListener {
        void onBrandAdded(Brand brand);
        void onPhoneAdded(Phone phone);
    }

    private ProductRepository(Context context) {
        database = AppDatabase.getInstance(context);
        listeners = new ArrayList<>();
        db = database.getWritableDatabase();
//        phoneDAO = new PhoneDAO(database);
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

//    /**
//     * Tìm điện thoại theo tên
//     * @param phoneName tên điện thoại cần tìm
//     * @return đối tượng Phone nếu tìm thấy, null nếu không tìm thấy
//     */
    public Phone findPhoneByName(String phoneName) {
        if (phoneName == null) return null;

        // Truy vấn trực tiếp từ cơ sở dữ liệu
        Cursor cursor = db.query("phones", null,
                "phone_name = ?", new String[]{phoneName},
                null, null, null);

        Phone phone = null;
        if (cursor.moveToFirst()) {
            Phone.PhonePerformance performance = new Phone.PhonePerformance(
                    cursor.getString(cursor.getColumnIndexOrThrow("processor")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("ram_gb")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("storage_gb")),
                    cursor.getString(cursor.getColumnIndexOrThrow("battery"))
            );

            phone = new Phone(
                    cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("model")),
                    cursor.getString(cursor.getColumnIndexOrThrow("brand")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    performance,
                    cursor.getString(cursor.getColumnIndexOrThrow("phone_name")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("stock_quantity"))
            );
        }
        cursor.close();

        if (phone != null) {
            return phone;
        }

        // Nếu không tìm thấy trực tiếp, thử tìm kiếm không phân biệt hoa thường
        String phoneNameLower = phoneName.toLowerCase();
        List<Brand> brands = getAllBrands();
        for (Brand brand : brands) {
            List<Phone> phones = getPhonesForBrand(brand.getName());
            for (Phone p : phones) {
                if (p.getPhoneName().toLowerCase().equals(phoneNameLower) ||
                        p.getModel().toLowerCase().equals(phoneNameLower)) {
                    return p;
                }
            }
        }

        // Tìm kiếm một phần
        for (Brand brand : brands) {
            List<Phone> phones = getPhonesForBrand(brand.getName());
            for (Phone p : phones) {
                if (p.getPhoneName().toLowerCase().contains(phoneNameLower) ||
                        phoneNameLower.contains(p.getPhoneName().toLowerCase())) {
                    return p;
                }
            }
        }

        return null;
    }
//
//    /**
//     * Kiểm tra xem có đủ tồn kho cho đơn hàng không
//     * @param phoneIdentifier ID hoặc tên điện thoại
//     * @param quantityNeeded số lượng cần đặt
//     * @return true nếu đủ hàng, false nếu không đủ
//     */
//    public boolean hasEnoughStock(String phoneIdentifier, int quantityNeeded) {
//        Log.d(TAG, "Checking stock for: " + phoneIdentifier + ", needed: " + quantityNeeded);
//
//        // Tìm điện thoại trong repository
//        Phone phone = findPhoneByName(phoneIdentifier);
//
//        // Nếu tìm thấy trong repository, kiểm tra tồn kho
//        if (phone != null) {
//            boolean hasEnough = phone.getStockQuantity() >= quantityNeeded;
//            Log.d(TAG, "Found in repository, stock: " + phone.getStockQuantity() +
//                    ", has enough: " + hasEnough);
//            return hasEnough;
//        } else {
//            // Nếu không tìm thấy trong repository, kiểm tra trong database
//            boolean hasEnough = phoneDAO.hasEnoughStock(phoneIdentifier, quantityNeeded);
//            Log.d(TAG, "Checking in database, has enough: " + hasEnough);
//            return hasEnough;
//        }
//    }
//
//    /**
//     * Lấy số lượng tồn kho hiện tại của điện thoại
//     * @param phoneIdentifier ID hoặc tên điện thoại
//     * @return số lượng tồn kho, -1 nếu không tìm thấy
//     */
//    public int getPhoneStock(String phoneIdentifier) {
//        // Tìm điện thoại trong repository
//        Phone phone = findPhoneByName(phoneIdentifier);
//
//        if (phone != null) {
//            Log.d(TAG, "Found stock in repository: " + phone.getStockQuantity());
//            return phone.getStockQuantity();
//        } else {
//            // Nếu không tìm thấy trong repository, truy vấn từ database
//            int stock = phoneDAO.getPhoneStock(phoneIdentifier);
//            Log.d(TAG, "Found stock in database: " + stock);
//            return stock;
//        }
//    }
//
//    /**
//     * Giảm số lượng tồn kho sau khi đặt hàng
//     * @param phoneIdentifier ID hoặc tên điện thoại
//     * @param quantityToDecrease số lượng cần giảm
//     * @return true nếu thành công, false nếu thất bại
//     */
//    public boolean decreaseStock(String phoneIdentifier, int quantityToDecrease) {
//        Log.d(TAG, "Decreasing stock for: " + phoneIdentifier + " by " + quantityToDecrease);
//
//        boolean success = false;
//
//        // Tìm điện thoại trong repository để cập nhật trong bộ nhớ
//        Phone phone = findPhoneByName(phoneIdentifier);
//
//        // Nếu tìm thấy trong repository, cập nhật cả trong bộ nhớ
//        if (phone != null) {
//            if (phone.getStockQuantity() >= quantityToDecrease) {
//                // Cập nhật trong bộ nhớ
//                phone.setStockQuantity(phone.getStockQuantity() - quantityToDecrease);
//
//                // Cập nhật trong database
//                success = phoneDAO.decreasePhoneStock(phone.getId(), quantityToDecrease);
//                Log.d(TAG, "Updated in memory and database, new stock: " + phone.getStockQuantity() +
//                        ", database update success: " + success);
//            } else {
//                Log.d(TAG, "Not enough stock in repository: " + phone.getStockQuantity() +
//                        " available, " + quantityToDecrease + " needed");
//                success = false;
//            }
//        } else {
//            // Nếu không tìm thấy trong repository, chỉ cập nhật database
//            success = phoneDAO.decreasePhoneStock(phoneIdentifier, quantityToDecrease);
//            Log.d(TAG, "Updated only in database, success: " + success);
//        }
//
//        return success;
//    }
}