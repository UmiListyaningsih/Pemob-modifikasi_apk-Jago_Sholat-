package id.duglegir.jagosholat.ui.tutorial;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator; // <-- IMPORT BARU

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.viewpager2.widget.ViewPager2; // <-- IMPORT BARU
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import id.duglegir.jagosholat.util.FeaturePagerAdapter;
import id.duglegir.jagosholat.R;
import id.duglegir.jagosholat.databinding.FragmentFeatureBinding;

public class FeatureFragment extends Fragment {

    private FragmentFeatureBinding binding;
    private FeaturePagerAdapter featurePagerAdapter;

    public FeatureFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentFeatureBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        featurePagerAdapter = new FeaturePagerAdapter(this, requireContext());
        binding.viewpagerFeature.setAdapter(featurePagerAdapter);



        new TabLayoutMediator(binding.tablayoutFeature, binding.viewpagerFeature,
                (tab, position) -> tab.setText(featurePagerAdapter.getPageTitle(position))
        ).attach();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}