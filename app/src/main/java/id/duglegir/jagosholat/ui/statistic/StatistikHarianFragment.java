package id.duglegir.jagosholat.ui.statistic;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import id.duglegir.jagosholat.util.FunctionHelper;
import id.duglegir.jagosholat.model.DataContract.DataEntry;
import id.duglegir.jagosholat.model.DataOperation;
import id.duglegir.jagosholat.R;
import id.duglegir.jagosholat.databinding.FragmentStatistikHarianBinding;

public class StatistikHarianFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{


    private static final int DATA_LOADER = 0;





    private AlertDialog.Builder dialog;
    private LayoutInflater inflater;
    private View dialogView;
    private TextView txt_waktu;



    private FunctionHelper functionHelper = new FunctionHelper();
    private DataOperation crud = new DataOperation();
    private StatistikHarianCursorAdapter mCursorAdapter;


    private FragmentStatistikHarianBinding binding;

    public StatistikHarianFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentStatistikHarianBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        binding.listViewStatistik.setEmptyView(binding.emptyViews);
        mCursorAdapter = new StatistikHarianCursorAdapter(requireContext(), null);
        binding.listViewStatistik.setAdapter(mCursorAdapter);





        binding.listViewStatistik.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                int idColoumnIndex = cursor.getColumnIndex(DataEntry._ID);
                int waktuColoumnIndex = cursor.getColumnIndex(DataEntry.COLUMN_WAKTU);
                String getArrayId = cursor.getString(idColoumnIndex);
                String getArrayWaktu = cursor.getString(waktuColoumnIndex);

                DialogForm(getArrayId, getArrayWaktu);

            }
        });



        LoaderManager.getInstance(this).initLoader(DATA_LOADER, null, this);
    }


    private void DialogForm(final String mID, String mWaktu) {



        dialog = new AlertDialog.Builder(requireActivity());
        inflater = requireActivity().getLayoutInflater(); // Dapatkan inflater dari activity
        dialogView = inflater.inflate(R.layout.content_statistik_update, null);
        txt_waktu = dialogView.findViewById(R.id.txt_waktu_update);

        dialog.setView(dialogView);
        dialog.setCancelable(true);
        txt_waktu.setText(mWaktu);



        txt_waktu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimePickerDialog mTimePickerDialog = new TimePickerDialog(requireActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        functionHelper.getFormatTimePicker(txt_waktu, hourOfDay, minute);
                    }
                }, functionHelper.getSystemJam(), functionHelper.getSystemMenit(), true);

                mTimePickerDialog.show();
            }
        });



        dialog.setPositiveButton("CATAT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String tempWaktu = txt_waktu.getText().toString();
                String selection = DataEntry._ID + " = '" + mID + "'";



                boolean isUpdated = crud.updateDataWaktu(requireContext(), tempWaktu, selection, null);
                if (isUpdated) {
                    Toast.makeText(requireContext(), "Waktu Telah Diubah", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireContext(), "Data Not Updadted", Toast.LENGTH_LONG).show();
                }

                dialog.dismiss();
            }
        });

        dialog.setNegativeButton("BATAL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    @NonNull 
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String selection = DataEntry.COLUMN_TANGGAL + " = '" + functionHelper.getDateToday() + "'";

        return new CursorLoader(requireActivity(),
                DataEntry.CONTENT_URI,
                crud.getProjection(),
                selection,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) { 
        mCursorAdapter.swapCursor(data);

        int countTable = (data != null) ? data.getCount() : 0;
        int progress = countTable * 20;

        if (binding != null) {
            binding.statProgressBar.setProgress(progress);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

        if (binding != null) {
            binding.statProgressBar.setProgress(0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; 
    }
}