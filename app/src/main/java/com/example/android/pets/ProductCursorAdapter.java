package com.example.android.pets;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import static android.widget.Toast.makeText;


public class ProductCursorAdapter extends CursorAdapter {

    private Uri currentProductUri;
    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     * correct row.
     */

    int qty = 0;
    TextView qtyTextView;
    Button saleButton;
    String productQty;

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        //Finf individual views we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        qtyTextView = (TextView) view.findViewById(R.id.qty);

        final int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
        int qtyColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QTY);

        String productName = cursor.getString(nameColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        productQty = cursor.getString(qtyColumnIndex);

        nameTextView.setText(productName);
        priceTextView.setText(productPrice);
        qtyTextView.setText(productQty);

        saleButton = (Button) view.findViewById(R.id.sale_button);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int qty = 0;

                int idColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);
                String productId = cursor.getString(idColumnIndex);

                int qtyColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QTY);
                String productqty = cursor.getString(qtyColumnIndex);

                if (!TextUtils.isEmpty(productqty) && productqty != null) {
                    qty = Integer.parseInt(productqty);
                    if (qty > 0) {
                        qty--;
                        ContentValues values = new ContentValues();
                        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QTY, qty);
                        Uri mCurrentProductUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, Long.parseLong(productId));
                        int rowsAffected = context.getContentResolver().update(mCurrentProductUri, values, null, null);

                        if (rowsAffected == 0) {
                            //Failed
                        } else {
                           //Success
                            qtyTextView.setText(String.valueOf(qty));
                        }


                    } else {
                        qtyTextView.setText(String.valueOf(0));
                    }
                }
            }
        });

    }
}