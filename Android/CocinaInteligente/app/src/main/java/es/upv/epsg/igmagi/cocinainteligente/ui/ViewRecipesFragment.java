package es.upv.epsg.igmagi.cocinainteligente.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.adapter.RecipeListAdapter;
import es.upv.epsg.igmagi.cocinainteligente.model.Recipe;
import es.upv.epsg.igmagi.cocinainteligente.utils.RecipeList;

public class ViewRecipesFragment extends Fragment {
    //private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Create a reference to the  collection
    //CollectionReference citiesRef = db.collection("cities");

    //TODO: Collect data from the db
    View root;
    RecyclerView recyclerView;

    public RecipeList recipeList = new RecipeList();
    public RecipeListAdapter adapter = new RecipeListAdapter(recipeList);


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_view_recipes, container, false);

        recyclerView = root.findViewById(R.id.recyclerRecipeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        ArrayList<Integer> x = new ArrayList<Integer>();
        x.add(2);
        x.add(4);
        x.add(7);
        x.add(10);
        x.add(2);
        recipeList.add(new Recipe("AAA", "Pechugas con patatas", "las mejores pechugas con patatas ever.","foto",x,"manolo",129));
        // Inflate the layout for this fragment
        return root;
    }

}
