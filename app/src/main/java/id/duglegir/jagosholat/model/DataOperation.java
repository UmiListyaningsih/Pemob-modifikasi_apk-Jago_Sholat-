package id.duglegir.jagosholat.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import id.duglegir.jagosholat.model.DataContract.DataEntry;



public class DataOperation {

    public DataOperation() {
    }



    private String projection[] = {DataEntry._ID,
            DataEntry.COLUMN_TANGGAL,
            DataEntry.COLUMN_SHALAT,
            DataEntry.COLUMN_WAKTU,
            DataEntry.COLUMN_STATUS
    };


    private long result; // result ini untuk mendapatkan boolean dari insert data (true/false)

    public boolean insertData(Context context, String id, String tanggal, String shalat, String waktu, String status) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DataEntry._ID, id);
        contentValues.put(DataEntry.COLUMN_TANGGAL, tanggal);
        contentValues.put(DataEntry.COLUMN_SHALAT, shalat);
        contentValues.put(DataEntry.COLUMN_WAKTU, waktu);
        contentValues.put(DataEntry.COLUMN_STATUS, status);

        Uri resultUri = context.getContentResolver().insert(DataEntry.CONTENT_URI,contentValues);

        if (resultUri == null) {
            return false;
        } else {
            return true;
        }
    }



    public boolean updateDataWaktu(Context context, String waktu, String selcetion, String[] selectionArgs) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DataEntry.COLUMN_WAKTU,waktu);
        int resultUri = context.getContentResolver().update(DataEntry.CONTENT_URI, contentValues, selcetion, selectionArgs);

        if (resultUri == 0) {
            return false;
        } else {
            return true;
        }
    }


    public Cursor getSemuaData(Context context){
        Cursor res = context.getContentResolver().query(
                DataEntry.CONTENT_URI,
                projection,
                null,
                null,
                DataEntry.COLUMN_TANGGAL);
        return res;
    }


    public Cursor getDataTanggal(Context context, String tanggal){
        String selection = DataContract.DataEntry.COLUMN_TANGGAL + " = '" + tanggal + "'";
        Cursor res = context.getContentResolver().query(
                DataEntry.CONTENT_URI,
                projection,
                selection,
                null,
                null);
        return res;
    }


    public Cursor getDataTanggalJenis(Context context, String tanggal, String shalat) {
        String selection = DataContract.DataEntry.COLUMN_TANGGAL + " = '" + tanggal +
                "' AND " + DataContract.DataEntry.COLUMN_SHALAT + " = '" + shalat + "'";
        Cursor res = context.getContentResolver().query(
                DataEntry.CONTENT_URI,
                projection,
                selection,
                null,
                null);
        return res;
    }


    public Cursor getSemuaTanggal(Context context){
        String projectionTanggal[] = {"DISTINCT " + DataEntry.COLUMN_TANGGAL};
        Cursor res = context.getContentResolver().query(
            DataEntry.CONTENT_URI,
            projectionTanggal,
            null,
            null,
            null);
        return res;
    }

    public String[] getProjection() {
        return projection;
    }
}
