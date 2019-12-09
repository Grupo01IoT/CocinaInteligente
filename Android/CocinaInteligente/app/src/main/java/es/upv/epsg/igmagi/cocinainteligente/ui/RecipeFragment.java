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
import android.widget.ViewFlipper;

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
    ImageView ivfoto,vegan, veggie, dairy, gluten;
    ViewFlipper stepsContainer;
    Recipe recipe;
    User user;
    LinearLayout ingredientList;

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

        ingredientList = root.findViewById(R.id.ingredientList);
        for (String s: recipe.getIngredients()) {
            TextView t = new TextView(getContext());
            t.setText("- " + s);
            ingredientList.addView(t);
        }

        gluten = root.findViewById(R.id.glutenIcon2);
        dairy = root.findViewById(R.id.dairyIcon2);
        vegan = root.findViewById(R.id.veganIcon2);
        veggie= root.findViewById(R.id.veggieIcon2);
        if(recipe.getExtra().get("veggie")) veggie.setVisibility(View.VISIBLE);
        if(recipe.getExtra().get("vegan")) vegan.setVisibility(View.VISIBLE);
        if(recipe.getExtra().get("dairy")) dairy.setVisibility(View.VISIBLE);
        if(recipe.getExtra().get("gluten")) gluten.setVisibility(View.VISIBLE);


        stepsContainer = root.findViewById(R.id.stepFlipper);
        if(recipe.steps != null){
            for (int i = 0; i < recipe.getSteps().size(); i++){
                LayoutInflater li = LayoutInflater.from(getContext());
                View theview = li.inflate(R.layout.fragment_steps, null);
                ((TextView) theview.findViewById(R.id.stepNumber)).setText((i+1)+".");
                ((TextView) theview.findViewById(R.id.stepInfo)).setText(recipe.getSteps().get(i));
                stepsContainer.addView(theview);
            }
            stepsContainer.setAutoStart(true);
        }


        return root;
    }


}
