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
    private int grandTotal = 0;
    private final int GOAL = 33;

    private static final String PREFS_NAME = "TasbihPrefs";
    private static final String KEY_COUNT = "tasbihCount";
    private static final String KEY_GRAND_TOTAL = "tasbihGrandTotal";
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

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        vibrator = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);

        loadCount();
        updateUI();

        binding.circularProgressBar.setMax(GOAL);
        binding.tvTasbihGoalText.setText("/ " + GOAL);

        binding.layoutTasbihMain.setOnClickListener(v -> {
            count++;

            if (count > GOAL) {
                vibrateLong();
                grandTotal += count - 1;
                count = 0;
            } else {
                vibrateShort();
            }
            updateUI();
            animateCountText();
        });

        binding.btnTasbihResetCount.setOnClickListener(v -> {
            count = 0;
            updateUI();
            vibrateShort();
        });

        binding.btnTasbihLog.setOnClickListener(v -> {
            if (count > 0) {
                grandTotal += count;
                count = 0;
                updateUI();
                vibrateLong();
            }
        });

        binding.btnTasbihResetGrandTotal.setOnClickListener(v -> {
            count = 0;
            grandTotal = 0;
            updateUI();
            vibrateLong();
        });
    }

    private void vibrateShort() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(50);
            }
        }
    }

    private void vibrateLong() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(200);
            }
        }
    }

    private void updateUI() {
        binding.tvTasbihCount.setText(String.valueOf(count));
        binding.tvTasbihGrandTotal.setText(String.valueOf(grandTotal));
        binding.circularProgressBar.setProgress(count); // Update progress bar
    }

    private void animateCountText() {
        binding.tvTasbihCount.animate().scaleX(1.05f).scaleY(1.05f).setDuration(50).withEndAction(() ->
                binding.tvTasbihCount.animate().scaleX(1.0f).scaleY(1.0f).setDuration(50).start()
        ).start();
    }

    private void loadCount() {
        count = sharedPreferences.getInt(KEY_COUNT, 0);
        grandTotal = sharedPreferences.getInt(KEY_GRAND_TOTAL, 0);
    }

    private void saveCount() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_COUNT, count);
        editor.putInt(KEY_GRAND_TOTAL, grandTotal);
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