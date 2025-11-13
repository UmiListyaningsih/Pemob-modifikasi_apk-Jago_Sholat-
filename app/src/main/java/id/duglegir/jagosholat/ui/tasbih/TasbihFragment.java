package id.duglegir.jagosholat.ui.tasbih;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import id.duglegir.jagosholat.databinding.FragmentTasbihBinding;

public class TasbihFragment extends Fragment {

    private FragmentTasbihBinding binding;
    private Vibrator vibrator;
    private int count = 0;

    // Nama untuk file penyimpanan
    private static final String PREFS_NAME = "TasbihPrefs";
    private static final String KEY_COUNT = "tasbihCount";
    private SharedPreferences sharedPreferences;

    public TasbihFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTasbihBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inisialisasi SharedPreferences dan Vibrator
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        vibrator = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);

        // Muat hitungan terakhir
        loadCount();
        updateCountText();

        // 1. Klik di area utama untuk MENAMBAH hitungan
        binding.layoutTasbihMain.setOnClickListener(v -> {
            count++;
            updateCountText();
            vibrate();
        });

        // 2. Klik tombol reset untuk MENGATUR ULANG hitungan
        binding.btnTasbihReset.setOnClickListener(v -> {
            count = 0;
            updateCountText();
            vibrate();
        });
    }

    // Fungsi untuk menggetarkan HP
    private void vibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Untuk Android Oreo (API 26) ke atas
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                // Untuk Android di bawah Oreo
                vibrator.vibrate(50);
            }
        }
    }

    // Fungsi untuk memperbarui TextView
    private void updateCountText() {
        binding.tvTasbihCount.setText(String.valueOf(count));
    }

    // Fungsi untuk MEMUAT hitungan
    private void loadCount() {
        count = sharedPreferences.getInt(KEY_COUNT, 0); // Default ke 0
    }

    // Fungsi untuk MENYIMPAN hitungan
    private void saveCount() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_COUNT, count);
        editor.apply();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Simpan hitungan saat fragment dijeda (misal: ganti tab atau tutup aplikasi)
        saveCount();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}