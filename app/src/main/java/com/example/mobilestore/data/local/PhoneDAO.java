package com.example.mobilestore.data.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mobilestore.data.local.AppDatabase;
import com.example.mobilestore.model.Phone;

/**
 * DAO để thao tác với bảng phones trong database
 * Data Access Object
 */
public class PhoneDAO {
    private static final String TAG = "PhoneDAO";
    private AppDatabase database;

    public PhoneDAO(AppDatabase database) {
        this.database = database;
    }

    /**
     * Lấy số lượng tồn kho của một điện thoại dựa trên ID hoặc tên
     * @param phoneIdentifier ID hoặc tên điện thoại
     * @return số lượng tồn kho, -1 nếu không tìm thấy
     */
    public int getPhoneStock(String phoneIdentifier) {
        SQLiteDatabase db = database.getReadableDatabase();
        int stock = -1;

        try {
            // Tìm kiếm theo ID hoặc tên điện thoại
            String query = "SELECT stock_quantity FROM phones WHERE id = ? OR phone_name = ?";
            String[] selectionArgs = {phoneIdentifier, phoneIdentifier};

            Cursor cursor = db.rawQuery(query, selectionArgs);

            if (cursor.moveToFirst()) {
                int stockIndex = cursor.getColumnIndex("stock_quantity");
                if (stockIndex != -1) {
                    stock = cursor.getInt(stockIndex);
                }
            }

            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting phone stock: " + e.getMessage());
        }

        return stock;
    }

    /**
     * Cập nhật số lượng tồn kho của một điện thoại
     * @param phoneIdentifier ID hoặc tên điện thoại
     * @param newQuantity số lượng mới
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    public boolean updatePhoneStock(String phoneIdentifier, int newQuantity) {
        SQLiteDatabase db = database.getWritableDatabase();
        boolean success = false;

        try {
            ContentValues values = new ContentValues();
            values.put("stock_quantity", newQuantity);

            // Cập nhật theo ID hoặc tên điện thoại
            String whereClause = "id = ? OR phone_name = ?";
            String[] whereArgs = {phoneIdentifier, phoneIdentifier};

            int rowsAffected = db.update("phones", values, whereClause, whereArgs);
            success = (rowsAffected > 0);

            Log.d(TAG, "Updated stock for " + phoneIdentifier + " to " + newQuantity +
                    ", rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error updating phone stock: " + e.getMessage());
        }

        return success;
    }

    /**
     * Giảm số lượng tồn kho sau khi đặt hàng
     * @param phoneIdentifier ID hoặc tên điện thoại
     * @param quantityToDecrease số lượng cần giảm
     * @return true nếu thành công, false nếu thất bại hoặc không đủ hàng
     */
    public boolean decreasePhoneStock(String phoneIdentifier, int quantityToDecrease) {
        // Kiểm tra số lượng tồn kho hiện tại
        int currentStock = getPhoneStock(phoneIdentifier);

        // Nếu không tìm thấy sản phẩm hoặc không đủ tồn kho
        if (currentStock < 0 || currentStock < quantityToDecrease) {
            return false;
        }

        // Tính toán số lượng mới và cập nhật
        int newStock = currentStock - quantityToDecrease;
        return updatePhoneStock(phoneIdentifier, newStock);
    }

    /**
     * Kiểm tra xem có đủ tồn kho cho đơn hàng không
     * @param phoneIdentifier ID hoặc tên điện thoại
     * @param quantityNeeded số lượng cần đặt
     * @return true nếu đủ hàng, false nếu không đủ
     */
    public boolean hasEnoughStock(String phoneIdentifier, int quantityNeeded) {
        int currentStock = getPhoneStock(phoneIdentifier);
        return currentStock >= quantityNeeded;
    }
}