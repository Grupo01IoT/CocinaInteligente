package es.upv.epsg.igmagi.cocinainteligente.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import es.upv.epsg.igmagi.cocinainteligente.ui.AboutFragment;
import es.upv.epsg.igmagi.cocinainteligente.ui.AboutRecipeFragment;
import es.upv.epsg.igmagi.cocinainteligente.ui.ProfileFragment;
import es.upv.epsg.igmagi.cocinainteligente.ui.RecipeFragment;

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
                return new RecipeFragment();
            case 1:
                return new AboutRecipeFragment();
            default:
                return null;
        }
    }
}