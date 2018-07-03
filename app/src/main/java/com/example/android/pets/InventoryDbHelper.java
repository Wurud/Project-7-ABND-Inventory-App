package com.example.android.pets;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.pets.InventoryContract.InventoryEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Inventory.db";
    public static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public InventoryDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_Product = "CREATE TABLE " + InventoryEntry.TABLE_NAME + "(" +
                InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                InventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL," +
                InventoryEntry.COLUMN_PRODUCT_PRICE + " TEXT NOT NULL," +
                InventoryEntry.COLUMN_PRODUCT_Size + " TEXT NOT NULL," +
                InventoryEntry.COLUMN_PRODUCT_QTY + " TEXT NOT NULL," +
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME_ + " TEXT NOT NULL," +
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONENUMBER_ + " TEXT NOT NULL );";
        db.execSQL(SQL_CREATE_Product);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
