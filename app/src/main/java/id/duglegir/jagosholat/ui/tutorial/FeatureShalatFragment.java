package id.duglegir.jagosholat.ui.tutorial;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import id.duglegir.jagosholat.R;
import id.duglegir.jagosholat.databinding.FragmentFeatureTutorImageBinding;


public class FeatureShalatFragment extends Fragment {

    private FragmentFeatureTutorImageBinding binding;


    private String imageResId[] = {"sholat_0","sholat_1","sholat_2","sholat_3","sholat_4","sholat_5","sholat_6","sholat_7","sholat_8","sholat_9","sholat_10"};
    private int imageRes, i=0;


    public FeatureShalatFragment() {

    }

    public void setImage(int x){


        imageRes = getResources().getIdentifier(imageResId[x],"drawable", requireContext().getPackageName());
        binding.photoView.setImageResource(imageRes);
    }

    public void nextImage(){
        if (!(i == imageResId.length-1)) {
            i++;
            setImage(i);
        }
    }

    public void previousImage(){
        if (!(i == 0)) {
            i--;
            setImage(i);

            binding.btnImageNext.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentFeatureTutorImageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);




        binding.photoView.setImageResource(R.drawable.sholat_0);
        binding.photoView.setScaleType(ImageView.ScaleType.FIT_XY);

        binding.btnImageNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextImage();
            }
        });
        binding.btnImagePrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousImage();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}