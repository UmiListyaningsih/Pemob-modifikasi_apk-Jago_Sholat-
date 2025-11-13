package id.duglegir.jagosholat.ui.statistic;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import id.duglegir.jagosholat.model.DataContract.DataEntry;
import id.duglegir.jagosholat.R;


public class StatistikHarianCursorAdapter extends CursorAdapter {

    public StatistikHarianCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {


        return LayoutInflater.from(context).inflate(R.layout.content_statistik_harian, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {


        TextView stat_waktu = (TextView)view.findViewById(R.id.stat_waktu);
        TextView stat_shalat = (TextView)view.findViewById(R.id.stat_shalat);
        ImageView img = (ImageView)view.findViewById(R.id.img_status);



        int waktuColumnIndex = cursor.getColumnIndex(DataEntry.COLUMN_WAKTU);
        int shalatColumnIndex = cursor.getColumnIndex(DataEntry.COLUMN_SHALAT);
        int statusColumnIndex = cursor.getColumnIndex(DataEntry.COLUMN_STATUS);



        String waktu = cursor.getString(waktuColumnIndex);
        String shalat = cursor.getString(shalatColumnIndex);
        String status = cursor.getString(statusColumnIndex);



        String resourceImageStat[] = {"ic_done_white_48px", "ic_undone_white_48px"};
        String outImage;
        if (status.equalsIgnoreCase("shalat")){
            outImage = resourceImageStat[0];
        } else {
            outImage = resourceImageStat[1];
        }
        int resIdImage = context.getResources().getIdentifier(outImage, "drawable", context.getPackageName());



        stat_shalat.setText(shalat);
        stat_waktu.setText(waktu);
        img.setImageResource(resIdImage);


    }

}
