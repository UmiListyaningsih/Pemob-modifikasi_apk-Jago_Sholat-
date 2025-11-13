package id.duglegir.jagosholat.ui.main;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import id.duglegir.jagosholat.util.FunctionHelper;
import id.duglegir.jagosholat.util.JadwalHelper;
import id.duglegir.jagosholat.R;
import id.duglegir.jagosholat.databinding.FragmentJadwalBinding;


public class JadwalFragment extends Fragment {

    private JadwalHelper jadwalHelper = new JadwalHelper();
    private FunctionHelper functionHelper = new FunctionHelper();


    private int countTime;


    private FragmentJadwalBinding binding;


    public JadwalFragment() {

    }

    public void CekJadwal(){



        String jadwalShalat = jadwalHelper.getMJadwalShalat();
        if (jadwalShalat == null) {

            countTime = 0;
            binding.txtViewSholat.setText("Memuat...");
            return;
        }

        if (jadwalShalat.equals("Shalat Shubuh")){
            countTime = (jadwalHelper.getJmlWaktuDzuhur() - functionHelper.getSumWaktuDetik()) * functionHelper.getDetikKeMiliDetik();
            binding.txtViewSholat.setText("Dzuhur");
        } else if (jadwalShalat.equals("Shalat Dzuhur")){
            countTime = (jadwalHelper.getJmlWaktuAshar() - functionHelper.getSumWaktuDetik()) * functionHelper.getDetikKeMiliDetik();
            binding.txtViewSholat.setText("Ashar");
        } else if (jadwalShalat.equals("Shalat Ashar")){
            countTime = (jadwalHelper.getJmlWaktuMaghrib() - functionHelper.getSumWaktuDetik()) * functionHelper.getDetikKeMiliDetik();
            binding.txtViewSholat.setText("Maghrib");
        } else if (jadwalShalat.equals("Shalat Maghrib")){
            countTime = (jadwalHelper.getJmlWaktuIsya() - functionHelper.getSumWaktuDetik()) * functionHelper.getDetikKeMiliDetik();
            binding.txtViewSholat.setText("Isya");
        } else if (jadwalShalat.equals("Shalat Isya")){
            countTime = (jadwalHelper.getJmlWaktuShubuh() - functionHelper.getSumWaktuDetik()) * functionHelper.getDetikKeMiliDetik();
            binding.txtViewSholat.setText("Shubuh");
        } else {
            countTime = (jadwalHelper.getJmlWaktuDzuhur() - functionHelper.getSumWaktuDetik()) * functionHelper.getDetikKeMiliDetik();
            binding.txtViewSholat.setText("Dzuhur");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentJadwalBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        CekJadwal();

        jadwalHelper.setTimeOnline(
                binding.txtWaktuShubuh,
                binding.txtWaktuDzuhur,
                binding.txtWaktuAshar,
                binding.txtWaktuMaghrib,
                binding.txtWaktuIsya
        );
        jadwalHelper.CoundownTime(countTime, binding.countDown);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}