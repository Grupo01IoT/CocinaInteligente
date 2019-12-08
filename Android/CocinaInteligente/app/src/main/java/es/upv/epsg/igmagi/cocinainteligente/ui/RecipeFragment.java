package es.upv.epsg.igmagi.cocinainteligente.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.model.Recipe;
import es.upv.epsg.igmagi.cocinainteligente.model.RecipeViewModel;
import es.upv.epsg.igmagi.cocinainteligente.model.User;
import es.upv.epsg.igmagi.cocinainteligente.model.UserViewModel;

public class RecipeFragment extends Fragment {
    TextView tvname, tvdescription;
    RatingBar rbrating;
    ImageView ivfoto;
    LinearLayout stepsContainer;
    Recipe recipe;
    User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_info_recipe, container, false);

        setHasOptionsMenu(true);
        RecipeViewModel model = ViewModelProviders.of(getActivity()).get(RecipeViewModel.class);
        UserViewModel userModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        recipe = model.getCurrentRecipe();
        user = userModel.getCurrentUser();

        tvname = root.findViewById(R.id.name);
        tvdescription = root.findViewById(R.id.description);
        tvname.setText(recipe.getName());
        tvdescription.setText(recipe.getDescription());
        // Inflate the layout for this fragment
        ivfoto = root.findViewById(R.id.recipephoto);
        ivfoto.setImageDrawable(model.getCurrentRecipeImage());
        rbrating = root.findViewById(R.id.rating);
        rbrating.setRating(recipe.getRatingValue(user.getUid()));
        rbrating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                HashMap<String, Long> userRat = recipe.getRatings();
                userRat.put(user.getUid(), (long)rbrating.getRating());
                recipe.setRatings(userRat);
                FirebaseFirestore.getInstance().collection("recipes").document(recipe.getUid()).update("ratings",userRat);

            }
        });
        stepsContainer = root.findViewById(R.id.stepsContainer);
        if(recipe.steps != null)
        for (int i = 0; i < recipe.getSteps().size(); i++){
            TextView ola = new TextView(getContext());
            ola.setText(i + ". " + recipe.getSteps().get(i));
            ola.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            stepsContainer.addView(ola);
        }

        return root;
    }


}
