package id.duglegir.jagosholat.ui.main;

import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Random;

import id.duglegir.jagosholat.util.FunctionHelper;
import id.duglegir.jagosholat.util.JadwalHelper;
import id.duglegir.jagosholat.model.DataOperation;
import id.duglegir.jagosholat.R;
import id.duglegir.jagosholat.databinding.FragmentCatatanBinding;


public class CatatanFragment extends Fragment {


    private FunctionHelper functionHelper = new FunctionHelper();
    private JadwalHelper jadwalHelper = new JadwalHelper();
    private DataOperation crud = new DataOperation();



    private String cekid;
    private final String bukanWaktuSholat = "Belum Masuk Waktu Sholat";
    private String[] mHadistArab = {"hadis_arab_0","hadis_arab_1","hadis_arab_2","hadis_arab_3","hadis_arab_4","hadis_arab_5"};
    private String[] mHadistText = {"hadis_text_0","hadis_text_1","hadis_text_2","hadis_text_3","hadis_text_4","hadis_text_5"};



    private FragmentCatatanBinding binding;


    private String isi_tanggal, isi_sholat, isi_waktu, isi_status;
    private String id_ibadah;

    public CatatanFragment() {

    }

    public boolean isEmptyRowTable() {
        Cursor res = null;
        try {

            res = crud.getDataTanggalJenis(requireContext(), isi_tanggal, isi_sholat);
            int cek = res.getCount();
            return cek == 0;
        } finally {

            if (res != null) {
                res.close();
            }
        }
    }


    public String cekDataSudahAda() {
        Cursor res = crud.getDataTanggalJenis(requireContext(), isi_tanggal, isi_sholat);
        try{
            while (res.moveToNext()) {
                cekid = res.getString(2);
            }
        } finally {

            if (res != null) {
                res.close();
            }
        }
        return cekid;
    }


    public void insertDataToDatabase() {
        boolean isInserted = crud.insertData(requireContext(), id_ibadah, isi_tanggal, isi_sholat, isi_waktu, isi_status);
        if (isInserted) {
            Toast.makeText(requireContext(), "Alhamdullilah " + isi_sholat, Toast.LENGTH_LONG).show();
            binding.btnSimpan.setVisibility(View.GONE); // Gunakan binding
        } else {
            Toast.makeText(requireContext(), "Data Not Inserted", Toast.LENGTH_LONG).show();
        }
    }


    public void addData(String mShalat) {
        try {
            if (isEmptyRowTable()) {
                if (mShalat.equals(bukanWaktuSholat)) {
                    Toast.makeText(requireContext(), bukanWaktuSholat, Toast.LENGTH_LONG).show();
                } else {
                    insertDataToDatabase();
                }
            } else {
                if (cekDataSudahAda().equals(mShalat)) {
                    Toast.makeText(requireContext(), "Data Sudah Tercatat", Toast.LENGTH_LONG).show();
                } else if (mShalat.equals(bukanWaktuSholat)) {
                    Toast.makeText(requireContext(), bukanWaktuSholat, Toast.LENGTH_LONG).show();
                } else {
                    insertDataToDatabase();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void tampilanButtonSimpan(String mShalat){

        if (mShalat.equalsIgnoreCase(bukanWaktuSholat)){
            binding.btnSimpan.setVisibility(View.GONE);
        } else {
            if (isEmptyRowTable()){
                binding.btnSimpan.setVisibility(View.VISIBLE);
            } else {
                if (!cekDataSudahAda().equals(mShalat)){
                    binding.btnSimpan.setVisibility(View.VISIBLE);
                } else {
                    binding.btnSimpan.setVisibility(View.GONE);
                }
            }
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCatatanBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        functionHelper.getSystemTime();
        functionHelper.getSystemRealTime();
        functionHelper.getSumRealTime();

        jadwalHelper.setJadwalShalat(binding.txtSholat);

        binding.txtTanggal.setText(functionHelper.getDateToday());


        isi_sholat = binding.txtSholat.getText().toString();
        isi_waktu = functionHelper.getOutputStringTime();
        isi_tanggal = functionHelper.getDateToday();
        isi_status = "Shalat";


        Random randomInt = new Random();
        int maxRandom = mHadistArab.length - 1;
        int minRandom = 0;
        int getIndexArrayHadis = randomInt.nextInt(maxRandom - minRandom + 1) + minRandom;

        int mResIdHadistArab = getResources().getIdentifier(mHadistArab[getIndexArrayHadis],"string", requireContext().getPackageName());
        int mResIdHadistText = getResources().getIdentifier(mHadistText[getIndexArrayHadis],"string", requireContext().getPackageName());

        binding.txtHadistArab.setText(mResIdHadistArab);
        binding.txtHadistText.setText(mResIdHadistText);


        id_ibadah = "IDS" + functionHelper.getRandomChar();

        tampilanButtonSimpan(isi_sholat);

        binding.btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addData(isi_sholat);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}