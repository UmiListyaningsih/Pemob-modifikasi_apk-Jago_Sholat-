package id.duglegir.jagosholat.ui.tutorial;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;

import id.duglegir.jagosholat.util.JSONHelper;
import id.duglegir.jagosholat.model.NiatShalat;
import id.duglegir.jagosholat.R;

import id.duglegir.jagosholat.databinding.FragmentFeatureTutorTextBinding;


public class FeatureNiatFragment extends Fragment {



    private FragmentFeatureTutorTextBinding binding;

    public FeatureNiatFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentFeatureTutorTextBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayList<NiatShalat> arrayWords = JSONHelper.extractNiatShalat();





        NiatShalatAdapter call = new NiatShalatAdapter(requireContext(), arrayWords);
        binding.listViewFeature.setAdapter(call);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}