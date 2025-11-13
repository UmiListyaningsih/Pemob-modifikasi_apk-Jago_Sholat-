package id.duglegir.jagosholat.ui.statistic;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import id.duglegir.jagosholat.model.DataContract.DataEntry;
import id.duglegir.jagosholat.R;


public class StatistikSemuaCursorAdapter extends CursorAdapter {

    public StatistikSemuaCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {


        return LayoutInflater.from(context).inflate(R.layout.content_statistik_semua, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {


        TextView stat_tanggal = view.findViewById(R.id.txt_stat_semua_tanggal);
        TextView stat_waktu = view.findViewById(R.id.txt_stat_semua_waktu);
        TextView stat_shalat = view.findViewById(R.id.txt_stat_semua_shalat);



        int waktuColumnIndex = cursor.getColumnIndex(DataEntry.COLUMN_WAKTU);
        int shalatColumnIndex = cursor.getColumnIndex(DataEntry.COLUMN_SHALAT);
        int tanggalColumnIndex = cursor.getColumnIndex(DataEntry.COLUMN_TANGGAL);



        String waktu = cursor.getString(waktuColumnIndex);
        String shalat = cursor.getString(shalatColumnIndex);
        String tanggal = cursor.getString(tanggalColumnIndex);



        String subStringShalat = shalat.substring(7, shalat.length());
        String upperCaseShalat = subStringShalat.toUpperCase();



        stat_shalat.setText(upperCaseShalat);
        stat_waktu.setText(waktu);
        stat_tanggal.setText(tanggal);

    }


}
