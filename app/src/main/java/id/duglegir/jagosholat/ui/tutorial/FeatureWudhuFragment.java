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

public class FeatureWudhuFragment extends Fragment {

    private FragmentFeatureTutorImageBinding binding;

    private String imageResId[] = {"wudhu_0","wudhu_1","wudhu_2","wudhu_3","wudhu_4","wudhu_5","wudhu_6","wudhu_7","wudhu_8","wudhu_9"};
    private int imageRes, i=0;

    public FeatureWudhuFragment() {

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
        if (!(i==0)) {
            i--;
            setImage(i);
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

        binding.photoView.setImageResource(R.drawable.wudhu_0);
        binding.photoView.setScaleType(ImageView.ScaleType.FIT_XY);

        binding.btnImageNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextImage();
            }
        });
        binding.btnImagePrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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