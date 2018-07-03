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


import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.InventoryContract.InventoryEntry;

/**
 * Allows user to create a new product or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int EXISTING_PRODUCT_LOADER = 0;
    private Uri currentProductUri;

    private boolean productHasChanged = false;

    private EditText nameEditText;
    private EditText priceEditText;
    private EditText qtyEditText;
    private EditText supplierNameEditText;
    private EditText supplierPhoneNumberText;
    private Spinner size;

    private int mSize = 0;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentProductUri = intent.getData();

        if (currentProductUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_product));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
        }


        // Find all relevant views that we will need to read user input from
        nameEditText = (EditText) findViewById(R.id.edit_product_name);
        priceEditText = (EditText) findViewById(R.id.edit_product_price);
        qtyEditText = (EditText) findViewById(R.id.edit_product_qty);
        size = (Spinner) findViewById(R.id.spinner_size);
        supplierNameEditText = (EditText) findViewById(R.id.edit_product_supplierName);
        supplierPhoneNumberText = (EditText) findViewById(R.id.edit_product_supplier_phoneNumber);

        nameEditText.setOnTouchListener(mTouchListener);
        priceEditText.setOnTouchListener(mTouchListener);
        qtyEditText.setOnTouchListener(mTouchListener);
        size.setOnTouchListener(mTouchListener);
        supplierNameEditText.setOnTouchListener(mTouchListener);
        supplierPhoneNumberText.setOnTouchListener(mTouchListener);

        setupSpinner();

        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the size of the product.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter sizeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_size_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        sizeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        size.setAdapter(sizeSpinnerAdapter);

        // Set the integer mSelected to the constant values
        size.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.size_small))) {
                        mSize = InventoryEntry.SIZE_small_; // Small
                    } else if (selection.equals(getString(R.string.size_medium))) {
                        mSize = InventoryEntry.SIZE_medium_; // Medium
                    } else {
                        mSize = InventoryEntry.SIZE_large_; // Large
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSize = 0; // Large size
            }
        });
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (currentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //Save product to db
                SaveProduct();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!productHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    public void SaveProduct() {

        Context context = this;
        int price = 0;
        int qty = 0;
        String nameString = nameEditText.getText().toString().trim();
        String PriceString = priceEditText.getText().toString().trim();
        String QtyString = qtyEditText.getText().toString().trim();
        String supplierNameString = supplierNameEditText.getText().toString().trim();
        String supplierPhoneNumberString = supplierPhoneNumberText.getText().toString().trim();

        if (!TextUtils.isEmpty(PriceString) && !TextUtils.isEmpty(QtyString)) {

            price = Integer.parseInt(PriceString);
            qty = Integer.parseInt(QtyString);
        }


        if (currentProductUri == null &&
                TextUtils.isEmpty(nameString) || TextUtils.isEmpty(PriceString) ||
                TextUtils.isEmpty(QtyString) || TextUtils.isEmpty(supplierNameString) || TextUtils.isEmpty(supplierPhoneNumberString)) {

            Toast.makeText(this, getString(R.string.input_msg),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();

        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_Size, mSize);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QTY, qty);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME_, supplierNameString);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONENUMBER_, supplierPhoneNumberString);

        if (currentProductUri == null) {
            // Insert a new pet into the provider, returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            int rowsAffected = context.getApplicationContext().getContentResolver().update(currentProductUri, values, null, null);

            // Check if the record was updated or not
            if (rowsAffected == 0) {

                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }


    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection
        String[] projection = {InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_QTY,
                InventoryEntry.COLUMN_PRODUCT_Size,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME_,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONENUMBER_};

        if (currentProductUri != null) // edit product -->current uri is not null
            return new CursorLoader(this, currentProductUri, projection, null, null, null);
        else return null;// add product --> current uri is null

        //The loader will execute the ContentProvider's querymethod on a background thread
        //return new CursorLoader(this, currentProductUri, projection, null, null, null);

    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int qtyColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QTY);
            int sizeColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_Size);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME_);
            int supplierPhoneNumberColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONENUMBER_);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String qty = cursor.getString(qtyColumnIndex);
            int size = cursor.getInt(sizeColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierphonenumber = cursor.getString(supplierPhoneNumberColumnIndex);

            switch (size) {
                case InventoryEntry.SIZE_medium_:
                    mSize = 1;
                    break;
                case InventoryEntry.SIZE_large_:
                    mSize = 2;
                    break;
                default:
                    mSize = 0;
                    break;
            }

            nameEditText.setText(name);
            priceEditText.setText(price);
            qtyEditText.setText(qty);
            supplierNameEditText.setText(supplierName);
            supplierPhoneNumberText.setText(supplierphonenumber);
        }
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (currentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    public void callSupplier(View view) {
        String phoneNumber = supplierPhoneNumberText.getText().toString().trim();
        Intent call = new Intent(Intent.ACTION_DIAL);
        call.setData(Uri.parse("tel:" + String.valueOf(phoneNumber)));
        startActivity(call);

    }

    int qty = 0;

    public void decreaseQty(View view) {
        String QtyString = qtyEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(QtyString) && QtyString != null) {
            qty = Integer.parseInt(QtyString);
            if (qty > 0) {
                qty--;
                qtyEditText.setText(String.valueOf(qty));
            } else {
                qtyEditText.setText(0);
            }
        }

    }

    public void increaseQty(View view) {
        String qtyString = qtyEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(qtyString) && qtyString != null) {
            qty = Integer.parseInt(qtyString);
            qty++;
            qtyEditText.setText(String.valueOf(qty));
        }
    }
}

