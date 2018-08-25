package com.example.android.inventoryappstage1;

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
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryappstage1.data.ProductContract.ProductEntry;

import com.example.android.inventoryappstage1.data.ProductContract;

import static com.example.android.inventoryappstage1.data.ProductProvider.LOG_TAG;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener{

    private int prodQuantity;
    private Uri mCurrentProductUri;
    private String manufacturerContact;

    private static final int EXISTING_PRODUCT_LOADER = 0;
    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate( savedInstanceState );
        setContentView( R.layout.product_detail );

        ((Button) findViewById( R.id.sell_button )).setOnClickListener( this );
        ((Button) findViewById( R.id.buy_button )).setOnClickListener( this );
        ((Button) findViewById( R.id.call_button )).setOnClickListener( this );
        ((Button) findViewById( R.id.delete_button )).setOnClickListener( this );
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //define projection that gives the columns from the table
        //for the detail view its every row
        //for the list view not every.
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIERNAME,
                ProductEntry.COLUMN_PRODUCT_PHONENR
        };
        //loader to execute content provider query ona background thread
        return new CursorLoader( this, ProductEntry.CONTENT_URI, projection, null, null, null );
    }
    @Override
    public void onLoadFinished(Loader<Cursor>loader, Cursor cursor){
        //retutn when the cursor is null
        if (cursor == null || cursor.getCount()<1){
            return;
        }
        //reading data from the first row of the cursor
        if (cursor.moveToFirst()){
            //find the right columns that are interesting
            int nameColumnIndex = cursor.getColumnIndex( ProductContract.ProductEntry.COLUMN_PRODUCT_NAME );
            int priceColumnIndex = cursor.getColumnIndex( ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE );
            int quantityColumnIndex = cursor.getColumnIndex( ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY );
            int supplierColumnIndex = cursor.getColumnIndex( ProductEntry.COLUMN_PRODUCT_SUPPLIERNAME );
            int suppphoneColumnIndex = cursor.getColumnIndex( ProductEntry.COLUMN_PRODUCT_PHONENR );
            //to get access to the phone number to use it for the call button
            manufacturerContact = cursor.getString( Integer.parseInt( cursor.getString(   suppphoneColumnIndex ) ) );
            //to be able to change the value of the quantity
            prodQuantity = Integer.parseInt (cursor.getString( quantityColumnIndex ));
            //to read the attributes from the current product
            String prodName = cursor.getString( nameColumnIndex );
            int prodPrice = cursor.getInt( priceColumnIndex );
            int prodQuantity = cursor.getInt( quantityColumnIndex );
            String prodSupname = cursor.getString( supplierColumnIndex );
            int prodsuppphone = cursor.getInt( suppphoneColumnIndex );
            //updateviews
            ((TextView) findViewById( R.id.name_pd_value )).setText( prodName );
            ((TextView) findViewById( R.id.price_pd_value )).setText( prodPrice );
            ((TextView) findViewById( R.id.quantity_pd_value )).setText( prodQuantity );
            ((TextView) findViewById( R.id.company_pd_value )).setText( prodSupname );
            ((TextView) findViewById( R.id.phone_pd_value )).setText( prodsuppphone );
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View v) {
        ContentValues values = new ContentValues(  );
        int updateProdQuantity;

        switch (v.getId()){
            //intent to make phonecall
            case R.id.call_button:
                if (!TextUtils.isEmpty( manufacturerContact )){
                    Intent intent = new Intent( Intent.ACTION_DIAL );
                    intent.setData( Uri.parse( manufacturerContact ) );
                    startActivity( intent );
                }
            break;
            //decreases quantity
            case R.id.sell_button: updateProdQuantity = prodQuantity - 1;
            if (updateProdQuantity <= 0 ){
                Toast.makeText( v.getContext(), "no products here anymore",Toast.LENGTH_SHORT ).show();
            }else {
                values.put( ProductEntry.COLUMN_PRODUCT_QUANTITY, updateProdQuantity );
                int changedProduct =v.getContext().getContentResolver().update( mCurrentProductUri,values, null, null );
                //log message, that updating the quantity failed, when it failed
                if (changedProduct == 0){
                    Log.e( LOG_TAG , "updating quantity failed" );
                }
            }
            break;
            //increases quantity
            case R.id.buy_button: updateProdQuantity = prodQuantity + 1;
            values.put( ProductEntry.COLUMN_PRODUCT_QUANTITY, updateProdQuantity );
            int changedProduct = v.getContext().getContentResolver().update( mCurrentProductUri,values,null,null );
                //log message, that updating the quantity failed, when it failed
                if (changedProduct == 0){
                    Log.e( LOG_TAG , "updating quantity failed" );
                }
                break;
            case R.id.delete_button:
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.dialog_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
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
        break;

        }
    }
    //perform deletion
    private void deleteProduct() {
        //if thr product exists
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete( mCurrentProductUri, null, null );
            if (rowsDeleted == 0) {
                Toast.makeText( this, getString( R.string.editor_delete_product_failed ),
                        Toast.LENGTH_SHORT ).show();
            } else {
                Toast.makeText( this, getString( R.string.editor_delete_product_successful ),
                        Toast.LENGTH_SHORT ).show();
            }
        }
        finish();
    }
}
