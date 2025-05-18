package com.example.mobilestore.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mobile_store.db";
    private static final int DATABASE_VERSION = 9;

    private static AppDatabase instance;

    private AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new AppDatabase(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Profit table
        db.execSQL("CREATE TABLE profit (" +
                "total_sold INTEGER, " +
                "income INTEGER)"
        );

        // Brands table
        db.execSQL(
                "CREATE TABLE brands (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "logo_resource INTEGER," +
                        "phone_count INTEGER DEFAULT 0)"
        );

        // Phones table
        db.execSQL(
                "CREATE TABLE phones (" +
                        "id TEXT PRIMARY KEY," +
                        "model TEXT NOT NULL," +
                        "brand TEXT NOT NULL," +
                        "price REAL NOT NULL," +
                        "phone_name TEXT NOT NULL," +
                        "processor TEXT," +
                        "ram_gb INTEGER," +
                        "storage_gb INTEGER," +
                        "battery TEXT," +
                        "stock_quantity INTEGER)"
        );

        // Customers table
        db.execSQL(
                "CREATE TABLE customers (" +
                        "id TEXT," +
                        "name TEXT NOT NULL," +
                        "phone TEXT NOT NULL," +
                        "email TEXT," +
                        "address TEXT NOT NULL," +
                        "total_orders INTEGER DEFAULT 0," +
                        "total_spent REAL DEFAULT 0.0," +
                        "loyalty_points INTEGER DEFAULT 0," +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                        ")"
        );

        // Bills table with discount_percentage
        db.execSQL(
                "CREATE TABLE bills (" +
                        "id INTEGER PRIMARY KEY," +
                        "customer_id TEXT," +
                        "total_amount REAL NOT NULL," +
                        "discount_percentage INTEGER DEFAULT 0," + // discount_percentage
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY (customer_id) REFERENCES customers(id))"
        );

        // Bill details table
        db.execSQL(
                "CREATE TABLE bill_details (" +
                        "id INTEGER PRIMARY KEY," +
                        "bill_id INTEGER," +
                        "phone_id TEXT," +
                        "quantity INTEGER," +
                        "unit_price REAL," +
                        "FOREIGN KEY (bill_id) REFERENCES bills(id)," +
                        "FOREIGN KEY (phone_id) REFERENCES phones(id))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 9) {
            try {
                db.execSQL("ALTER TABLE bills ADD COLUMN discount_percentage INTEGER DEFAULT 0");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (oldVersion < 8) {
            db.execSQL("DROP TABLE IF EXISTS bill_details");
            db.execSQL("DROP TABLE IF EXISTS bills");
            db.execSQL("DROP TABLE IF EXISTS phones");
            db.execSQL("DROP TABLE IF EXISTS customers");
            db.execSQL("DROP TABLE IF EXISTS brands");
            db.execSQL("DROP TABLE IF EXISTS profit");
            // Create new tables
            onCreate(db);
        }
    }
}