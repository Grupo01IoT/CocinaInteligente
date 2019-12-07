package es.upv.epsg.igmagi.cocinainteligente.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.model.Recipe;
import es.upv.epsg.igmagi.cocinainteligente.model.RecipeViewModel;

public class RecipeFragment extends Fragment {
    TextView tvname, tvdescription;
    RatingBar rbrating;
    ImageView ivfoto;
    LinearLayout stepsContainer;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_info_recipe, container, false);

        RecipeViewModel model = ViewModelProviders.of(getActivity()).get(RecipeViewModel.class);
        Recipe recipe = model.getCurrentRecipe();
        tvname = root.findViewById(R.id.name);
        tvdescription = root.findViewById(R.id.description);
        tvname.setText(recipe.getName());
        tvdescription.setText(recipe.getDescription());
        // Inflate the layout for this fragment
        ivfoto = root.findViewById(R.id.recipephoto);
        ivfoto.setImageDrawable(model.getCurrentRecipeImage());
        rbrating = root.findViewById(R.id.rating);
        rbrating.setRating(recipe.getRatingValue());
        stepsContainer = root.findViewById(R.id.stepsContainer);
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
