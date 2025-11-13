package id.duglegir.jagosholat.util;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import id.duglegir.jagosholat.ui.tutorial.FeatureWudhuFragment;
import id.duglegir.jagosholat.ui.tutorial.FeatureNiatFragment;
import id.duglegir.jagosholat.ui.tutorial.FeatureShalatFragment;
import id.duglegir.jagosholat.ui.tutorial.FeatureDoaFragment;
import id.duglegir.jagosholat.R;


public class FeaturePagerAdapter extends FragmentStateAdapter {

    private Context mContext;


    public FeaturePagerAdapter(Fragment fragment, Context context) {
        super(fragment);
        mContext = context;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0 :
                return new FeatureWudhuFragment();
            case 1 :
                return new FeatureNiatFragment();
            case 2 :
                return new FeatureShalatFragment();
            case 3 :
                return new FeatureDoaFragment();
            default:

                return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0 :
                return mContext.getString(R.string.btn_tutor_wudhu);
            case 1 :
                return mContext.getString(R.string.btn_niat_sholat);
            case 2 :
                return mContext.getString(R.string.btn_tutor_sholat);
            case 3 :
                return mContext.getString(R.string.btn_doa);
            default:
                return null;
        }
    }
}