package com.example.android.inventoryapp.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import java.text.DecimalFormat;

/**
 * Subclass of {@link CursorAdapter} to populate the product list on the display
 */

public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Decimal format to be displayed on screen
     */
    DecimalFormat mDecimalFormat;

    /**
     * Default constructor for {@link ProductCursorAdapter} class
     *
     * @param context The context of the app
     * @param cursor  The {@link Cursor} instance holding the data to display
     */
    public ProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mDecimalFormat = new DecimalFormat("0.00");
    }

    /**
     * Creates a new blank blank list item view.
     *
     * @param context The context of the app
     * @param cursor  Cursor holding the data
     * @param parent  The parent to which the view is attached to
     * @return A new blank list item view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * Populates the view with cursor data
     *
     * @param view    The view created in {@link #newView}
     * @param context The context of the app
     * @param cursor  The Cursor instance holding the data
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView nameView = (TextView) view.findViewById(R.id.name_text_view);
        TextView priceView = (TextView) view.findViewById(R.id.price_view);
        TextView stockView = (TextView) view.findViewById(R.id.stock_view);
        ImageView imageView = (ImageView) view.findViewById(R.id.item_image);

        String name = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
        double price = cursor.getDouble(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE));
        final int stock = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_STOCK));
        String imageCategory = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_CATEGORY_IMAGE));
        Uri imageUri = Uri.parse(imageCategory.trim());

        nameView.setText(name);
        // price and stock could be empty
        if (price == 0) {
            priceView.setText(context.getResources().getString(R.string.product_price_placeholder));
        } else {
            priceView.setText(mDecimalFormat.format(price));
        }
        stockView.setText(Integer.toString(stock));
        if (ProductEntry.isValidUri(imageUri)) {
            imageView.setImageURI(imageUri);
        }

        // set onClickListener
        LinearLayout mainLayout = (LinearLayout) view.findViewById(R.id.list_item_linear_layout);
        Button saleButton = (Button) view.findViewById(R.id.sale_button);
        final long productId = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productId);
                if (stock == 0) {
                    Toast.makeText(context, context.getString(R.string.update_stock_failed), Toast.LENGTH_SHORT).show();
                } else {
                    int newStock = stock - 1;
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_STOCK, newStock);
                    context.getContentResolver().update(currentProductUri, values, null, null);
                }
            }
        });
    }
}
