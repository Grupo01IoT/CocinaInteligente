package es.upv.epsg.igmagi.cocinainteligente.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.model.Recipe;
import es.upv.epsg.igmagi.cocinainteligente.model.RecipeViewModel;

public class ViewRecipeFragment extends Fragment {
    TextView tvname, tvdescription;
    RatingBar rbrating;
    ImageView ivfoto;
    LinearLayout stepList;

    FirebaseFirestore mBD = FirebaseFirestore.getInstance();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_view_recipe, container, false);

        RecipeViewModel model = ViewModelProviders.of(getActivity()).get(RecipeViewModel.class);
        Recipe recipe = model.getCurrentRecipe();
        Log.d("AA", recipe.data());

        tvname = root.findViewById(R.id.name);
        tvdescription = root.findViewById(R.id.description);
        tvname.setText(recipe.getName());
        tvdescription.setText(recipe.getDescription());
        // Inflate the layout for this fragment
        ivfoto = root.findViewById(R.id.recipephoto);
        ivfoto.setImageDrawable(model.getCurrentRecipeImage());
        rbrating = root.findViewById(R.id.rating);
        rbrating.setRating(recipe.getRatingValue());
//        ivfoto.setImageDrawable(photo.getDrawable());
        return root;
    }
    private void updateBD(String field, Boolean value) {
        Map<String, Object> map = new HashMap<>();
        map.put(field, value);
        mBD.collection("devices").document("conet_kitchen").update(map);
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
