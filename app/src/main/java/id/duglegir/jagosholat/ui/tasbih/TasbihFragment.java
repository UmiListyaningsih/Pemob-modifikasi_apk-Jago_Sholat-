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

    private static final String PREFS_NAME = "TasbihPrefs";
    private static final String KEY_COUNT = "tasbihCount";
    private SharedPreferences sharedPreferences;

    public TasbihFragment() {

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

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        vibrator = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);

        loadCount();
        updateCountText();

        binding.layoutTasbihMain.setOnClickListener(v -> {
            count++;
            updateCountText();
            vibrate();
        });

        binding.btnTasbihReset.setOnClickListener(v -> {
            count = 0;
            updateCountText();
            vibrate();
        });
    }

    private void vibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {

                vibrator.vibrate(50);
            }
        }
    }

    private void updateCountText() {
        binding.tvTasbihCount.setText(String.valueOf(count));
    }

    private void loadCount() {
        count = sharedPreferences.getInt(KEY_COUNT, 0); // Default ke 0
    }

    private void saveCount() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_COUNT, count);
        editor.apply();
    }

    @Override
    public void onPause() {
        super.onPause();

        saveCount();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}