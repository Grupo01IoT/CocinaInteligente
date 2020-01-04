package es.upv.epsg.igmagi.cocinainteligente.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.upv.epsg.igmagi.cocinainteligente.MainActivity;
import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.model.Recipe;
import es.upv.epsg.igmagi.cocinainteligente.model.RecipeViewModel;
import es.upv.epsg.igmagi.cocinainteligente.model.User;
import es.upv.epsg.igmagi.cocinainteligente.model.UserViewModel;

public class RecipeFragment extends Fragment {
    FirebaseFirestore mBD = FirebaseFirestore.getInstance();

    String TAG = "RECIPEFRAGMENT";
    TextView tvname, tvdescription, tvtiempo, tvusername;
    RatingBar rbrating;
    ImageView vegan, veggie, dairy, gluten;
    ImageButton like;
    ViewFlipper stepsContainer;
    Recipe recipe;
    User user;
    LinearLayout ingredientList, recipePhoto;
    private ListView lvIngr, lvSteps;
    private ArrayAdapter<String> ingrAdapter, stepsAdapter;
    private ArrayList<String> ingrList, stepsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_info_recipe, container, false);

        lvIngr = (ListView) root.findViewById(R.id.ingredientslist);
        lvSteps = (ListView) root.findViewById(R.id.stepslist);

        ingrList = new ArrayList<String>();
        stepsList = new ArrayList<String>();

        ingrAdapter = new ArrayAdapter<String>(getContext(), R.layout.listview_item, ingrList);
        stepsAdapter = new ArrayAdapter<String>(getContext(), R.layout.listview_item, stepsList);

        lvIngr.setAdapter(ingrAdapter);
        lvSteps.setAdapter(stepsAdapter);
        //setHasOptionsMenu(true);

        RecipeViewModel model = ViewModelProviders.of(getActivity()).get(RecipeViewModel.class);
        UserViewModel userModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);

        recipe = model.getCurrentRecipe();
        user = userModel.getCurrentUser();


        tvname = root.findViewById(R.id.name);
        tvdescription = root.findViewById(R.id.description);
        tvtiempo = root.findViewById(R.id.tiempo);
        tvusername = root.findViewById(R.id.userName);

        tvname.setText(recipe.getName());
        tvdescription.setText(recipe.getDescription());
        tvtiempo.setText(recipe.getFormattedDuration());

        //SET USER NAME OF THE USER WHO CREATED THE RECIPE
        mBD.collection("users").document(recipe.getUser()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        tvusername.setText("@" + document.getString("name"));
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });




        // Inflate the layout for this fragment
        recipePhoto = root.findViewById(R.id.recipephoto);
        recipePhoto.setBackground(model.getCurrentRecipeImage());
        rbrating = root.findViewById(R.id.showRating);
        rbrating.setRating(recipe.getRatingValue());


        /*rbrating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                HashMap<String, Long> userRat = recipe.getRatings();
                userRat.put(user.getUid(), (long)rbrating.getRating());
                recipe.setRatings(userRat);
                //FirebaseFirestore.getInstance().collection("recipes").document(recipe.getUid()).update("ratings",userRat);
            }
        });
        */
        //ingredientList = root.findViewById(R.id.ingredientList);


        gluten = root.findViewById(R.id.glutenIcon);
        dairy = root.findViewById(R.id.dairyIcon);
        vegan = root.findViewById(R.id.veganIcon);
        veggie = root.findViewById(R.id.veggieIcon);

        if (recipe.getExtra().get("veggie")) veggie.setVisibility(View.VISIBLE);
        if (recipe.getExtra().get("vegan")) vegan.setVisibility(View.VISIBLE);
        if (recipe.getExtra().get("dairy")) dairy.setVisibility(View.VISIBLE);
        if (recipe.getExtra().get("gluten")) gluten.setVisibility(View.VISIBLE);

        // If the recipe is vegan, it is veggetarian
        if (recipe.getExtra().get("veggie") && recipe.getExtra().get("vegan")) {
            veggie.setVisibility(View.GONE);
            vegan.setVisibility(View.VISIBLE);
        }


        createStepsList();
        createIngredientsList();
        /*
        stepsContainer = root.findViewById(R.id.stepFlipper);

        if(recipe.steps != null){
            for (int i = 0; i < recipe.getSteps().size(); i++){
                LayoutInflater li = LayoutInflater.from(getContext());
                View theview = li.inflate(R.layout.fragment_steps, null);
                ((TextView) theview.findViewById(R.id.stepNumber)).setText((i+1)+".");
                if (!recipe.isInteractive())((TextView) theview.findViewById(R.id.stepInfo)).setText(recipe.getSteps().get(i).toString());
                else ((TextView) theview.findViewById(R.id.stepInfo)).setText(recipe.getSteps().get(i).toString());
                stepsContainer.addView(theview);
            }
            stepsContainer.setAutoStart(true);
        }

        */

        // -------- LIKE BUTTON -----------
        like = root.findViewById(R.id.likeButton);
        //SET INITIAL STATE OF THE LIKE BUTTON
        if (user.getFavouriteReceipts().contains(recipe.getUid())) {//Si es favorita
            like.setBackgroundResource(R.drawable.liked);
        } else {
            like.setBackgroundResource(R.drawable.like);
        }
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getFavouriteReceipts().contains(recipe.getUid())) {//Si es favorita
                    ArrayList<String> fav = (user.getFavouriteReceipts());
                    boolean res = fav.remove(recipe.getUid());
                    user.setFavouriteReceipts(fav);
                    /*
                    Map<String, Object> map = new HashMap<>();
                    map.put("cooktop", t);
                    mBD.collection("devices").document("conet_kitchen").update(map);
                    */
                    Map<String, Object> map = new HashMap<>();

                    map.put("favouriteReceips", user.getFavouriteReceipts());

                    mBD.collection("users").document(user.getUid()).update(map);
                    //item.setIcon(R.drawable.baseline_favorite_border_24);
                    like.setBackgroundResource(R.drawable.like);
                } else {//Si no es favorita

                    ArrayList<String> fav = (user.getFavouriteReceipts());
                    boolean res = fav.add(recipe.getUid());
                    user.setFavouriteReceipts(fav);

                    Map<String, Object> map = new HashMap<>();
                    map.put("favouriteReceips", user.getFavouriteReceipts());


                    mBD.collection("users").document(user.getUid()).update(map);
                    //mBD.collection("users").document(user.getUid()).update("favouriteReceips",user.getFavouriteReceipts());
                    //item.setIcon(R.drawable.baseline_favorite_24);
                    like.setBackgroundResource(R.drawable.liked);

                }
            }

        });
        return root;
    }

    private void createIngredientsList() {
        int n = 1;
        for (String s : recipe.getIngredients()) {

            ingrAdapter.add(n + ". " + s.toUpperCase());
            n++;
        }
        ViewGroup.LayoutParams params = lvIngr.getLayoutParams();

        params.height = 55 * n;
        lvIngr.setLayoutParams(params);
        lvIngr.requestLayout();
        params = lvIngr.getLayoutParams();
    }

    private void createStepsList() {

        int n = 1;
        if (!recipe.isInteractive()) {
            for (int i = 0; i < recipe.getSteps().size(); i++) {
                stepsAdapter.add((n + ". " + (String) recipe.getSteps().get(i)).toUpperCase());
                //Log.d("OOO", (String)recipe.getSteps().get(i));
                n++;
            }
            //Log.d("OOO", stepsAdapter.getItem(1));


            ViewGroup.LayoutParams params = lvSteps.getLayoutParams();

            params.height = 55 * n;
            lvSteps.setLayoutParams(params);
            lvSteps.requestLayout();


        } else {
            for (int i = 0; i < recipe.getSteps().size(); i++) {
                HashMap map = (HashMap) recipe.getSteps().get(i);


                stepsAdapter.add((n + ". " + map.get("step")));
                n++;
            }
            //Log.d("OOO", stepsAdapter.getItem(1));


            ViewGroup.LayoutParams params = lvSteps.getLayoutParams();

            params.height = 55 * n;
            lvSteps.setLayoutParams(params);
            lvSteps.requestLayout();

        }


    }

    /*
    public class PasosInteractivos{

    }
    */
}
