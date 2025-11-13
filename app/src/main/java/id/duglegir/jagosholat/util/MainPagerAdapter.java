package id.duglegir.jagosholat.util;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import id.duglegir.jagosholat.ui.compass.Kompas2Fragment;
import id.duglegir.jagosholat.ui.main.JadwalFragment;
import id.duglegir.jagosholat.ui.main.CatatanFragment;
import id.duglegir.jagosholat.ui.tasbih.TasbihFragment;
import id.duglegir.jagosholat.ui.tutorial.FeatureFragment;
import id.duglegir.jagosholat.ui.statistic.StatistikFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0 :
                return new CatatanFragment();
            case 1 :
                return new JadwalFragment();
            case 2 :
                return new StatistikFragment();
            case 3 :
                return new Kompas2Fragment();
            case 4 :
                return new TasbihFragment();
            case 5 :
                return new FeatureFragment();
            default:
                return new Fragment();
        }
    }

    // GANTI getCount() MENJADI getItemCount()
    @Override
    public int getItemCount() {
        return 6; // Kembalikan jumlah tab Anda secara langsung
    }
}