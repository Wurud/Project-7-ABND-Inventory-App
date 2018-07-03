/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;


import static com.example.android.pets.InventoryContract.InventoryEntry.SIZE_small_;

/**
 * Displays list of products that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int PRODUCT_LOADER = 0;
    ProductCursorAdapter pCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the pet data
        ListView productListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        pCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(pCursorAdapter);

        //Setup an item click listener
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri currentProductUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
                intent.setData(currentProductUri);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert product data" menu option
            case R.id.action_insert_product_data:
                InsertProduct();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(InventoryContract.InventoryEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }

    private void InsertProduct() {
        ContentValues values = new ContentValues();

        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME, "Red Dress");
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE, 2000);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_Size, SIZE_small_);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QTY, 1);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME_, "Khalid");
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONENUMBER_, "05503489891");

        Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection
        String[] projection = {InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_QTY};
        //The loader will execute the ContentProvider's querymethod on a background thread
        return new CursorLoader(this, InventoryContract.InventoryEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        pCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        pCursorAdapter.swapCursor(null);
    }
}
