package es.upv.epsg.igmagi.cocinainteligente.ui.interactive;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.model.Recipe;
import es.upv.epsg.igmagi.cocinainteligente.model.RecipeViewModel;
import es.upv.epsg.igmagi.cocinainteligente.model.Step;
import es.upv.epsg.igmagi.cocinainteligente.model.User;
import es.upv.epsg.igmagi.cocinainteligente.model.UserViewModel;

public class InteractiveRecipeFragment extends Fragment {

    String TAG = "InteractiveRecipeFragment";

    // GENERAL STUFF MAYBE USELESS
    private FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore mDB = FirebaseFirestore.getInstance();

    //LAYOUT ELEMENTS
    private SlideshowViewModel slideshowViewModel;
    private TextView name;
    private ImageButton stop;
    UserViewModel userViewModel;
    RecipeViewModel recipeViewModel;
    User user;
    Recipe recipe;
    ViewFlipper viewFlipper;
    LinearLayout stepsContainer;


    //DATA ELEMENTS
    ArrayList<Step> steps;
    ArrayList<Integer> stepsToDO;
    ArrayList<Integer> stepsDone;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_interactive_recipe_dialog, container, false);

        //Layout elements
        name = root.findViewById(R.id.intRecipeName);
        stop = root.findViewById(R.id.intStopBtn);
        stepsContainer = (LinearLayout)root.findViewById(R.id.stepsContainer);


        //ViewModels
        userViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        recipeViewModel = ViewModelProviders.of(getActivity()).get(RecipeViewModel.class);
        user = userViewModel.getCurrentUser();
        recipe = recipeViewModel.getCurrentRecipe();
        name.setText(recipe.getName());

        steps = new ArrayList();
        stepsToDO = new ArrayList();
        stepsDone = new ArrayList();

        // ------------   THE SETUP WILL ONLY BE DONE IF THERE'S NO RECIPE PLAYING    -----------------

        // DONE: STEPS SETUP
        setupInitialSteps();


        // DONE: FILL SETUP LAYOUT
        // If the recipe isn't playing
        LayoutInflater li = LayoutInflater.from(getContext());

        for(Step s: steps){
            View view = li.inflate(R.layout.fragment_interactive_step, null);
            ((TextView)view.findViewById(R.id.intStepText)).setText(s.getStep());
            ((LinearLayout)root.findViewById(R.id.stepsContainer)).addView(view);
        }

        // DONE: SETUP FIREBASE: pairedkitchen.add("playingRecipe", {isPlaying: bool, recipeId: String(ID), cook: String(ID)}) --> This will trigger the Listener
        Map<String, Object> valores = new HashMap<>();
        valores.put("isPlaying", true);
        valores.put("id", recipe.getUid());
        valores.put("cook", user.getUid());

        Map<String, Object> datos = new HashMap<>();
        datos.put("playingRecipe", valores);

        mDB.collection("devices").document("conet_kitchen").set(datos, SetOptions.merge());

        //todo: cambiar device dynamically

        //TODO: Notification for devices paired with the kitchen (?)


        // If the recipe is already playing
        //TODO:


        return root;
    }
    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();

    }
    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();

        //Reset the Steps "ID"
        Step.numSteps = 0;
    }

    //Functions
    private void setupInitialSteps(){
        for (int i = 0; i < recipe.getSteps().size(); i++) {
            Step s;
            HashMap map = (HashMap) recipe.getSteps().get(i);
            if(!map.get("mode").toString().equals("Manual"))
            steps.add(new Step( map.get("mode").toString(),map.get("step").toString(), map.get("trigger").toString()));
            else
                steps.add(new Step( map.get("mode").toString(),map.get("step").toString()));
            stepsToDO.add(steps.get(i).getPos());
        }
    }

    private void refresh(){
        LayoutInflater li = LayoutInflater.from(getContext());

        for(Step s: steps){
            View view = li.inflate(R.layout.fragment_interactive_step, null);
            ((TextView)view.findViewById(R.id.intStepText)).setText(s.getStep());
            stepsContainer.addView(view);
        }
    }
}