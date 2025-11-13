package id.duglegir.jagosholat.ui.statistic;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import id.duglegir.jagosholat.ui.statistic.StatistikGrafikFragment;
import id.duglegir.jagosholat.ui.statistic.StatistikHarianFragment;
import id.duglegir.jagosholat.ui.statistic.StatistikSemuaFragment;
import id.duglegir.jagosholat.R;
import id.duglegir.jagosholat.databinding.FragmentStatistikBinding;


public class StatistikFragment extends Fragment {

    private FragmentStatistikBinding binding;

    public StatistikFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentStatistikBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);




        StatistikGrafikFragment mStatistikGrafikFragment = new StatistikGrafikFragment();
        exchangeFragment(R.id.fragStatistik, mStatistikGrafikFragment);



        binding.btnStatHarian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatistikHarianFragment mStatistikHarianFragment = new StatistikHarianFragment();
                exchangeFragment(R.id.fragStatistik, mStatistikHarianFragment);
            }
        });

        binding.btnStatGrafik.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatistikGrafikFragment mStatistikGrafikFragment = new StatistikGrafikFragment();
                exchangeFragment(R.id.fragStatistik, mStatistikGrafikFragment);
            }
        });

        binding.btnStatSemua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatistikSemuaFragment mStatistikSemuaFragment = new StatistikSemuaFragment();
                exchangeFragment(R.id.fragStatistik, mStatistikSemuaFragment);
            }
        });

    }

    public void exchangeFragment(int frameLayout, Fragment mFragment){


        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(frameLayout, mFragment);
        ft.commit();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static class MyXAxisValueFormatter extends ValueFormatter implements IAxisValueFormatter {
        private String[] mValues;
        public MyXAxisValueFormatter(String[] values) {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mValues[(int) value];
        }
    }
}