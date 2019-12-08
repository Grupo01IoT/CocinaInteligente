package es.upv.epsg.igmagi.cocinainteligente.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.adapter.TabsAdapter;
import es.upv.epsg.igmagi.cocinainteligente.model.Recipe;
import es.upv.epsg.igmagi.cocinainteligente.model.RecipeViewModel;
import es.upv.epsg.igmagi.cocinainteligente.model.User;
import es.upv.epsg.igmagi.cocinainteligente.model.UserViewModel;

public class ViewRecipeFragment extends Fragment {
    FirebaseFirestore mBD = FirebaseFirestore.getInstance();
    Recipe recipe;
    User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_view_recipe, container, false);

        RecipeViewModel model = ViewModelProviders.of(getActivity()).get(RecipeViewModel.class);
        UserViewModel userModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        recipe = model.getCurrentRecipe();
        user = userModel.getCurrentUser();
        setHasOptionsMenu(true);

        TabLayout tabLayout = (TabLayout) root.findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Recipe"));
        tabLayout.addTab(tabLayout.newTab().setText("Comments"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final ViewPager viewPager = (ViewPager) root.findViewById(R.id.recipeContainer);
        TabsAdapter tabsAdapter = new TabsAdapter(this.getFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(tabsAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

//        ivfoto.setImageDrawable(photo.getDrawable());
        return root;
    }

    private void updateBD(String field, Boolean value) {
        Map<String, Object> map = new HashMap<>();
        map.put(field, value);
        mBD.collection("devices").document("conet_kitchen").update(map);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite:
                if (user.getFavouriteReceipts().contains(recipe.getUid())) {
                    ArrayList<String> fav = (user.getFavouriteReceipts());
                    boolean res = fav.remove(recipe.getUid());
                    user.setFavouriteReceipts(fav);
                    mBD.collection("users").document(user.getUid()).update("favouriteReceips",user.getFavouriteReceipts());
                    item.setIcon(R.drawable.baseline_favorite_border_24);
                } else {
                    ArrayList<String> fav = (user.getFavouriteReceipts());
                    boolean res = fav.add(recipe.getUid());
                    user.setFavouriteReceipts(fav);
                    mBD.collection("users").document(user.getUid()).update("favouriteReceips",user.getFavouriteReceipts());
                    item.setIcon(R.drawable.baseline_favorite_24);
                }
                return true;
            default:
                break;
        }

        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_favorite);
        item.setVisible(true);
        if (user.getFavouriteReceipts().contains(recipe.getUid())) {
            item.setIcon(R.drawable.baseline_favorite_24);
        } else {
            item.setIcon(R.drawable.baseline_favorite_border_24);
        }

    }

    /*private void refreshView() {
        mBD.collection("devices").document("conet_kitchen").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                //if (lights = documentSnapshot.getBoolean("lights")){

            }
        });
    }


     */
}
