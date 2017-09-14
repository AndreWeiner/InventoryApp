package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API contract for the inventory app
 */
public final class ProductContract {
    // static public class attributes
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCTS = "products";

    // empty dummy Constructor
    private ProductContract() {
    }

    /**
     * Defines constant values for the products database table.
     * Each row in the table represents a single product.
     */
    public static final class ProductEntry implements BaseColumns {
        /**
         * The content Uri
         */
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

        /**
         * Name of database table for products
         */
        public final static String TABLE_NAME = "products";

        /**
         * Unique ID number for the product
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the product.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME = "name";

        /**
         * Price of the product in Euro.
         * <p>
         * Type: REAL
         */
        public final static String COLUMN_PRODUCT_PRICE = "price";

        /**
         * Quantity of the product in stock
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_STOCK = "stock";

        /**
         * Name of the product supplier.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_SUPPLIER = "supplier_name";

        /**
         * Email address of the product supplier.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_EMAIL = "supplier_email";

        /**
         * Category image of the product
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_CATEGORY_IMAGE = "image";

        /**
         * Checks an Email address for validity. It checks in particular if:
         * - contains exactly one @ symbol
         * - @ is not first character
         * - @ symbol is at least four indices away from the end
         * - contains at least one . symbol after @ symbol
         * - the last . symbol is at least three indices away from the end
         *
         * @param emailAddress The email address to check
         * @return True if valid email address
         */
        public static boolean isValidEmail(String emailAddress) {
            if (!emailAddress.isEmpty()) {
                int emailLength = emailAddress.length();
                int indexAt = emailAddress.indexOf('@');
                int lastIndexAt = emailAddress.lastIndexOf('@');
                int lastIndexDot = emailAddress.lastIndexOf('.');
                return indexAt > 0 && indexAt == lastIndexAt && lastIndexAt < emailLength - 4 &&
                        lastIndexDot > 0 && lastIndexDot < emailLength - 2;
            } else {
                return false;
            }
        }

        public static boolean isValidUri(Uri uri) {
            // TODO Built more sophisticated Uri check in case the image was deleted
            if (uri == null) {
                return false;
            }
            return true;
        }
    }
}
