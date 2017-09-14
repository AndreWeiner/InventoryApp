package com.example.android.inventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import java.text.DecimalFormat;

import static com.example.android.inventoryapp.data.ProductContract.ProductEntry.isValidUri;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * static ID for CursorLoader
     */
    private static final int LOADER_ID = 0;
    /**
     * Image content type
     */
    private static final String IMAGE_CONTENT = "image/*";
    /**
     * Indicates intent that sender is waiting for a response
     */
    private static final int SELECT_PICTURE = 1;
    /**
     * Decimal format to be displayed on screen
     */
    DecimalFormat mDecimalFormat;
    /**
     * EditText field for the product name
     */
    private EditText mNameEditText;
    /**
     * EditText field for the product price
     */
    private EditText mPriceEditText;
    /**
     * EditText field for the product stock
     */
    private EditText mStockEditText;
    /**
     * EditText field for the supplier name
     */
    private EditText mSupplierEditText;
    /**
     * EditText field for the supplier email address
     */
    private EditText mEmailEditText;
    /**
     * EditText field to modify the stock
     */
    private EditText mModifyEditText;
    /**
     * EditText field for the quantity of a new order
     */
    private EditText mOrderEditText;
    /**
     * Remove Button
     */
    private Button mRemoveButton;
    /**
     * Save Button
     */
    private Button mSaveButton;
    /**
     * Add stock Button
     */
    private Button mAddStockButton;
    /**
     * Delete stock Button
     */
    private Button mDelStockButton;
    /**
     * Send email to supplier Button
     */
    private Button mEmailButton;
    /**
     * Button to select image
     */
    private Button mImageButton;
    /**
     * Product image view
     */
    private ImageView mImageView;
    /**
     * Product image Uri
     */
    private Uri mImageUri;
    /**
     * Uri of the current product if present
     */
    private Uri mProductUri;
    /**
     * Boolean to indicate if the data was changed
     */
    private boolean mProductHasChanged = false;
    /**
     * OnTouchListener to update mProductHasChanged variable
     */
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // decimal format
        mDecimalFormat = new DecimalFormat("0.00");

        // set EditText variables
        mNameEditText = (EditText) findViewById(R.id.product_name_field);
        mPriceEditText = (EditText) findViewById(R.id.product_price_field);
        mStockEditText = (EditText) findViewById(R.id.product_stock_field);
        mSupplierEditText = (EditText) findViewById(R.id.product_supplier_field);
        mEmailEditText = (EditText) findViewById(R.id.product_supplier_email_field);
        mRemoveButton = (Button) findViewById(R.id.remove_product);
        mSaveButton = (Button) findViewById(R.id.save_product);
        mImageButton = (Button) findViewById(R.id.select_image_button);
        mImageView = (ImageView) findViewById(R.id.product_image);

        // set onTouchListener
        mNameEditText.setOnTouchListener(mOnTouchListener);
        mPriceEditText.setOnTouchListener(mOnTouchListener);
        mStockEditText.setOnTouchListener(mOnTouchListener);
        mSupplierEditText.setOnTouchListener(mOnTouchListener);
        mEmailEditText.setOnTouchListener(mOnTouchListener);
        mImageButton.setOnTouchListener(mOnTouchListener);

        // check if a new product is created or an old product is modified
        Intent intent = getIntent();
        mProductUri = intent.getData();
        if (mProductUri == null) {
            setTitle(getResources().getString(R.string.new_product_name));
            // make remove, modify, and order section invisible for a new product
            mRemoveButton.setVisibility(View.GONE);
            TextView modifyHeader = (TextView) findViewById(R.id.modify_stock_header);
            LinearLayout modifyLayout = (LinearLayout) findViewById(R.id.modify_stock_layout);
            TextView supplierHeader = (TextView) findViewById(R.id.supplier_support_header);
            LinearLayout supplierLayout = (LinearLayout) findViewById(R.id.supplier_support_layout);
            modifyHeader.setVisibility(View.GONE);
            modifyLayout.setVisibility(View.GONE);
            supplierHeader.setVisibility(View.GONE);
            supplierLayout.setVisibility(View.GONE);
        } else {
            setTitle(getResources().getString(R.string.product_details));
            getLoaderManager().initLoader(LOADER_ID, null, this);
            mAddStockButton = (Button) findViewById(R.id.modify_stock_add_button);
            mDelStockButton = (Button) findViewById(R.id.modify_stock_del_button);
            mEmailButton = (Button) findViewById(R.id.order_button);
            mModifyEditText = (EditText) findViewById(R.id.modify_stock_count);
            mOrderEditText = (EditText) findViewById(R.id.order_count);

            mRemoveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog();
                }
            });

            mAddStockButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean tryToAdd = addToStock();
                    if (tryToAdd) {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.update_stock_successful), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.update_stock_failed), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            mDelStockButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean tryToDel = delFromStock();
                    if (tryToDel) {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.update_stock_successful), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.update_stock_failed), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            mEmailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendEmailToSupplier();
                }
            });

        }

        mImageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            mImageUri = data.getData();
            if (mImageUri != null && isValidUri(mImageUri)) {
                mImageView.setImageURI(mImageUri);
            }
        }
    }

    /**
     * Lets the user select an image from the android gallery app and
     * displays it in the DetailsActivity
     */
    private void selectImage() {

        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.setType(IMAGE_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent,
                getString(R.string.select_category_image)), SELECT_PICTURE);

    }

    /**
     * Try to increase the current stock
     *
     * @return True if successful
     */
    private boolean addToStock() {
        // get the current stock and the requested change of stock
        int currentStock = Integer.parseInt(mStockEditText.getText().toString().trim());
        String addition = mModifyEditText.getText().toString().trim();
        if (TextUtils.isEmpty(addition)) {
            return false;
        }
        int requestedAddition = Integer.parseInt(addition);
        int tempCount = currentStock + requestedAddition;
        if (tempCount < 0 || tempCount > 10E6) {
            return false;
        }
        mStockEditText.setText(Integer.toString(tempCount));
        mProductHasChanged = true;
        return true;
    }

    /**
     * Try to decrease the current stock
     *
     * @return True if successful
     */
    private boolean delFromStock() {
        // get the current stock and the requested change of stock
        int currentStock = Integer.parseInt(mStockEditText.getText().toString().trim());
        String subtraction = mModifyEditText.getText().toString().trim();
        if (TextUtils.isEmpty(subtraction)) {
            return false;
        }
        int requestedSubtraction = Integer.parseInt(subtraction);
        int tempCount = currentStock - requestedSubtraction;
        if (tempCount < 0 || tempCount > 10E6) {
            return false;
        }
        mStockEditText.setText(Integer.toString(tempCount));
        mProductHasChanged = true;
        return true;
    }

    /**
     * Creates and starts a SENDTO action intent to order the product from the supplier
     */
    private void sendEmailToSupplier() {
        // TODO Add more check if Strings have been temporally modified
        String mailAddress = mEmailEditText.getText().toString().trim();
        String amount = mOrderEditText.getText().toString().trim();
        String name = mNameEditText.getText().toString().trim();
        if (ProductEntry.isValidEmail(mailAddress)) {
            String subject = getString(R.string.subject, amount, name);
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    getString(R.string.mail_to), mailAddress, null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            startActivity(Intent.createChooser(emailIntent, getString(R.string.sending_mail)));
        } else {
            Toast.makeText(this, getString(R.string.invalid_email_msg), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    /**
     * Saves the new or updated product
     */
    private void saveProduct() {
        // read from EditText fields and remove trailing white space
        String name = mNameEditText.getText().toString().trim();
        String price = mPriceEditText.getText().toString().trim();
        String stock = mStockEditText.getText().toString().trim();
        String supplier = mSupplierEditText.getText().toString().trim();
        String email = mEmailEditText.getText().toString().trim();

        // check input for validity
        if (TextUtils.isEmpty(name) && mProductUri == null) {
            Toast.makeText(this, getString(R.string.invalid_name_msg), Toast.LENGTH_SHORT).show();
            return;
        }

        double priceNum = 0.0;
        if (!TextUtils.isEmpty(price)) {
            // replace comma with dot
            priceNum = Double.parseDouble(price.replace(',', '.'));
            if (priceNum < 0) {
                Toast.makeText(this, getString(R.string.invalid_price_msg), Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(this, getString(R.string.invalid_price_msg), Toast.LENGTH_SHORT).show();
            return;
        }

        int stockNum = 0;
        if (!TextUtils.isEmpty(stock)) {
            stockNum = Integer.parseInt(stock);
            if (stockNum < 0 || stockNum > 10E6) {
                Toast.makeText(this, getString(R.string.invalid_stock_msg), Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(this, getString(R.string.invalid_stock_msg), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(supplier)) {
            Toast.makeText(this, getString(R.string.invalid_supplier_msg), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!ProductEntry.isValidEmail(email)) {
            Toast.makeText(this, getString(R.string.invalid_email_msg), Toast.LENGTH_SHORT).show();
            return;
        }

        String imageUriString;
        if (ProductEntry.isValidUri(mImageUri)) {
            imageUriString = mImageUri.toString().trim();
        } else {
            Toast.makeText(this, getString(R.string.select_category_image), Toast.LENGTH_SHORT).show();
            return;
        }

        // create values with table columns
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, name);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceNum);
        values.put(ProductEntry.COLUMN_PRODUCT_STOCK, stockNum);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplier);
        values.put(ProductEntry.COLUMN_PRODUCT_EMAIL, email);
        values.put(ProductEntry.COLUMN_CATEGORY_IMAGE, imageUriString);

        if (mProductUri == null) { // perform insert
            mProductUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (mProductUri == null) {
                Toast.makeText(this, getString(R.string.insert_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.insert_product_successful), Toast.LENGTH_SHORT).show();
                finish();
            }
        } else { // perform update
            int numRows = getContentResolver().update(mProductUri, values, null, null);
            if (numRows == 0) {
                Toast.makeText(this, getString(R.string.update_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.update_product_successful), Toast.LENGTH_SHORT).show();
                finish();
            }

        }

    }

    /**
     * Displays a warning dialog when the back button on the phone is pressed and
     * modifications have not been saved
     */
    @Override
    public void onBackPressed() {

        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Displays a warning dialog if the menu back button is pressed while there are
     * unsaved modifications
     *
     * @param item The menu back item
     * @return True
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!mProductHasChanged) {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                        }
                    };

            showUnsavedChangesDialog(discardButtonClickListener);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_STOCK,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductEntry.COLUMN_PRODUCT_EMAIL,
                ProductEntry.COLUMN_CATEGORY_IMAGE};

        return new CursorLoader(this, mProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {

        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
            Double price = cursor.getDouble(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE));
            String stock = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_STOCK));
            String supplier = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER));
            String email = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_EMAIL));
            String image = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_CATEGORY_IMAGE));
            Uri imageUri = Uri.parse(image.trim());

            // format price for screen
            String priceFormatted = mDecimalFormat.format(price);

            mNameEditText.setText(name);
            mPriceEditText.setText(priceFormatted);
            mStockEditText.setText(stock);
            mSupplierEditText.setText(supplier);
            mEmailEditText.setText(email);
            mImageUri = imageUri;
            if(ProductEntry.isValidUri(mImageUri)) {
                mImageView.setImageURI(mImageUri);
            } else {
                Toast.makeText(getApplicationContext(), "Product image was deleted", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Deletes the product from the database
     */
    private void deleteProduct() {
        int rowsDeleted = getContentResolver().delete(mProductUri, null, null);
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
    }
}