package com.example.android.pets;

import android.content.ContentResolver;
import android.provider.BaseColumns;
import android.net.Uri;

public final class InventoryContract {
    private InventoryContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.android.pets";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCTS = "product";


    public static final class InventoryEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String TABLE_NAME = "product";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "name";
        public static final String COLUMN_PRODUCT_PRICE = "price";
        public static final String COLUMN_PRODUCT_Size = "size";
        public static final String COLUMN_PRODUCT_QTY = "qty";
        public static final String COLUMN_PRODUCT_SUPPLIER_NAME_ = "supplierName";
        public static final String COLUMN_PRODUCT_SUPPLIER_PHONENUMBER_ = "supplierPhoneNumber";

        public static final int SIZE_small_ = 0;
        public static final int SIZE_medium_ = 1;
        public static final int SIZE_large_ = 2;

        public static boolean isValidSize(Integer size) {
            if (size == SIZE_small_ || size == SIZE_medium_ || size == SIZE_large_) {
                return true;
            }
            return false;
        }
    }
}
