package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import static com.example.android.inventoryapp.data.ProductContract.CONTENT_AUTHORITY;
import static com.example.android.inventoryapp.data.ProductContract.PATH_PRODUCTS;

/**
 * {@link ContentProvider} for the inventory app
 */
public class ProductProvider extends ContentProvider {
    /**
     * Static integer code that represents the Uri for all products
     */
    private static final int PRODUCTS = 100;
    /**
     * Static integer code that represents the Uri for a single product
     */
    private static final int PRODUCT_ID = 101;
    /**
     * {@link UriMatcher} instance
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    /**
     * Regular expression that represents an _ID path
     */
    private static final String PRODUCT_ID_PLACEHOLDER = "/#";

    // Add integer code to corresponding Uri
    static {
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PRODUCTS + PRODUCT_ID_PLACEHOLDER, PRODUCT_ID);
    }

    /**
     * {@link ProductDbHelper} instance
     */
    private ProductDbHelper mDbHelper;

    /**
     * Instantiates a new {@link ProductDbHelper} object.
     *
     * @return True if object was created
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    /**
     * Creates a {@link Cursor} instance holding results for the given Uri
     *
     * @param uri           The Uri to query
     * @param projection    The columns to include in the Cursor
     * @param selection     Selection of particular rows
     * @param selectionArgs Arguments for selection
     * @param sortOrder     How to sort the results
     * @return A Cursor instance holding the query results
     */
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCTS:
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     *
     * @param uri The content Uri
     * @return MIME type of data
     */
    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Inserts new data into the provider.
     *
     * @param uri    The content Uri
     * @param values The content values
     * @return The new Uri for the inserted data
     */
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Inserts a product into the database and checks the provided content values
     *
     * @param uri    The content uri
     * @param values The content to insert
     * @return The new Uri for the inserted product
     */
    private Uri insertProduct(Uri uri, ContentValues values) {
        // product name, provider name, and provider email must not be zero
        String productName = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (productName == null) {
            throw new IllegalArgumentException("Product requires a name");
        }
        String supplierName = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
        if (supplierName == null) {
            throw new IllegalArgumentException("Product requires a supplier name");
        }
        String supplierEmail = values.getAsString(ProductEntry.COLUMN_PRODUCT_EMAIL);
        if (supplierEmail == null) {
            throw new IllegalArgumentException("Product supplier requires an email address");
        }
        String categoryImage = values.getAsString(ProductEntry.COLUMN_CATEGORY_IMAGE);
        Uri imageUri = Uri.parse(categoryImage);
        if (!ProductEntry.isValidUri(imageUri)) {
            throw new IllegalArgumentException("Product requires a category image");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(ProductEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e("ProductProvider", "Failed to insert row for " + uri);
            return null;
        }
        Uri newUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(newUri, null);
        return newUri;
    }

    /**
     * Deletes a SINGLE product from the database
     *
     * @param uri           Uri of the product to delete
     * @param selection     Selection of rows, will be overwritten
     * @param selectionArgs Arguments for selection, will be overwritten
     * @return The number of changed rows, typically 1 for successful deletion
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT_ID:
                SQLiteDatabase database = mDbHelper.getWritableDatabase();
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                int num_rows = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                if (num_rows != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return num_rows;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    /**
     * Updates the content values of a product. The presence of keys is tested in
     * {@link #updateProduct}. Currently, only a SINGLE product can be updated at a time.
     *
     * @param uri           Uri of the product to update
     * @param values        The updated content values
     * @param selection     Selection of rows to update
     * @param selectionArgs Arguments for selection
     * @return Number of updated rows
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Does the database update for {@link #update} and ensures the validity of keys and values.
     *
     * @param uri           The product Uri
     * @param values        New content values
     * @param selection     The product selection
     * @param selectionArgs Arguments for selection
     * @return Number of updated rows
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String productName = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (productName == null) {
                throw new IllegalArgumentException("Product must have a name");
            }
        }
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
            Double productPrice = values.getAsDouble(ProductEntry.COLUMN_PRODUCT_PRICE);
            if (productPrice < 0 && productPrice != null) {
                throw new IllegalArgumentException("Product price cannot be negative");
            }
        }
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_STOCK)) {
            Integer productStock = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_STOCK);
            if (productStock < 0 && productStock != null) {
                throw new IllegalArgumentException("Product stock cannot be negative");
            }
        }
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_SUPPLIER)) {
            String supplierName = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
            if (supplierName == null) {
                throw new IllegalArgumentException("Product must have a supplier");
            }
        }
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_EMAIL)) {
            String supplierEmail = values.getAsString(ProductEntry.COLUMN_PRODUCT_EMAIL);
            if (!ProductEntry.isValidEmail(supplierEmail)) {
                throw new IllegalArgumentException("Invalid supplier email address");
            }
        }
        if (values.containsKey(ProductEntry.COLUMN_CATEGORY_IMAGE)) {
            String categoryImage = values.getAsString(ProductEntry.COLUMN_CATEGORY_IMAGE);
            Uri imageUri = Uri.parse(categoryImage.trim());
            if (!ProductEntry.isValidUri(imageUri)) {
                throw new IllegalArgumentException("Invalid category image");
            }
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int num_rows = database.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);
        if (num_rows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return num_rows;
    }
}
