package es.upv.epsg.igmagi.cocinainteligente.Adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import es.upv.epsg.igmagi.cocinainteligente.ui.ProfileFragment;
import es.upv.epsg.igmagi.cocinainteligente.ui.home.HomeFragment;

/**
 * Created by tutlane on 19-12-2017.
 */

public class TabsAdapter extends FragmentStatePagerAdapter {

    int mNumOfTabs;

    public TabsAdapter(FragmentManager fm, int NoofTabs){
        super(fm);
        this.mNumOfTabs = NoofTabs;
    }
    @Override
    public int getCount() {
        return mNumOfTabs;
    }
    @Override
    public Fragment getItem(int position){
        switch (position){
            case 0:
                ProfileFragment home = new ProfileFragment();
                return home;
            case 1:
                ProfileFragment about = new ProfileFragment();
                return about;
            default:
                return null;
        }
    }
}