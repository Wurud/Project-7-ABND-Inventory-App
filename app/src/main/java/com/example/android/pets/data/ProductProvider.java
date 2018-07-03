package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.pets.InventoryContract;
import com.example.android.pets.InventoryDbHelper;

import static com.example.android.pets.InventoryContract.CONTENT_AUTHORITY;
import static com.example.android.pets.InventoryContract.PATH_PRODUCTS;

public class ProductProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    // Initialize the provider and the database helper object.
    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    /**
     * URI matcher code for the content URI for the pets table
     */
    private static final int PRODUCTS = 5;

    /**
     * URI matcher code for the content URI for a single pet in the pets table
     */
    private static final int PRODUCT_ID = 60;


    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor = null;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /*
     * Insert a product into the database with the given content values. Return the new content URI
     */
    private Uri insertProduct(Uri uri, ContentValues values) {

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Check that the name is not null
        String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product name is required");
        }

        // Check that the price is not null
        Integer price = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Product price is required and more than 0");
        }

        // Check that the qty is not null
        Integer qty = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QTY);
        if (qty == null || qty < 0) {
            throw new IllegalArgumentException("Product quantity is required and should be more than 0");
        }

        // Check that the size is not null
        Integer size = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_Size);
        if (size == null || !InventoryContract.InventoryEntry.isValidSize(size)) {
            throw new IllegalArgumentException("Product size is required");
        }

        // Check that the supplierName is not null
        String supplierName = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME_);
        if (supplierName == null) {
            throw new IllegalArgumentException("Product supplier name is required");
        }

        // Check that the supplierPhoneNumber is not null
        String supplierPhoneNumber = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME_);
        if (supplierPhoneNumber == null) {
            throw new IllegalArgumentException("Product supplier phone number is required");
        }

        // Insert the new product with the given values
        long id = database.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //Notify all listeners that the data has changed for the product content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_Size)) {
            Integer size = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_Size);
            if (size == null || !InventoryContract.InventoryEntry.isValidSize(size)) {
                throw new IllegalArgumentException("Product requires valid size");
            }
        }

        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE)) {
            Integer price = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
            if (price == null || price <= 0) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }

        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME_)) {
            String supplierName = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME_);
            if (supplierName == null) {
                throw new IllegalArgumentException("Product requires a supplier name");
            }
        }

        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONENUMBER_)) {
            String supplierPhoneNumber = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONENUMBER_);
            if (supplierPhoneNumber == null) {
                throw new IllegalArgumentException("Product requires a supplier name");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(InventoryContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }


    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
