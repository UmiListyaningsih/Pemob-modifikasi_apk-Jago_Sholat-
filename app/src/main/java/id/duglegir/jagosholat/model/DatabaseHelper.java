package id.duglegir.jagosholat.model;



import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import id.duglegir.jagosholat.model.DataContract.DataEntry;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE = "jagosholat.db";
    private static final int DATABASE_VERSION = 1;


    private SQLiteDatabase sqLiteDatabase = getWritableDatabase();



    public DatabaseHelper(Context context) {
        super(context, DATABASE, null, DATABASE_VERSION);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String sql_create_table = "CREATE TABLE IF NOT EXISTS " + DataEntry.TABLE_NAME + " (" +
                    DataEntry._ID + " TEXT PRIMARY KEY," +
                    DataEntry.COLUMN_TANGGAL + " TEXT NOT NULL," +
                    DataEntry.COLUMN_SHALAT + " TEXT NOT NULL," +
                    DataEntry.COLUMN_WAKTU + " TEXT NOT NULL," +
                    DataEntry.COLUMN_STATUS + " TEXT NOT NULL);";
            db.execSQL(sql_create_table);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql_drop_table = "DROP TABLE IF EXISTS " + DataEntry.TABLE_NAME;
        db.execSQL(sql_drop_table);
        onCreate(db);
    }


}
