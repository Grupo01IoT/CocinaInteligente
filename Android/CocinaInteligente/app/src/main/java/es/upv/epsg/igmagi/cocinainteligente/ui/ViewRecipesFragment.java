package es.upv.epsg.igmagi.cocinainteligente.ui;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.net.URI;

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.adapter.RecipeHolder;
import es.upv.epsg.igmagi.cocinainteligente.model.Recipe;
import es.upv.epsg.igmagi.cocinainteligente.utils.DownloadImageTask;

public class ViewRecipesFragment extends Fragment {

    public View root;
    //RecyclerView recyclerView;
    private FirebaseFirestore mBD = FirebaseFirestore.getInstance();
    private CollectionReference recipesCollection = mBD.collection("recipes");

    private FirestoreRecyclerAdapter<Recipe, RecipeHolder> adapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_view_recipes, container, false);


        /*recyclerView = root.findViewById(R.id.recyclerRecipeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

         */
        setUpRecycleView();

        // Inflate the layout for this fragment
        return root;
    }

    private void setUpRecycleView() {
        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recyclerRecipeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);


        Query query = recipesCollection.orderBy("name", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Recipe> options = new FirestoreRecyclerOptions.Builder<Recipe>()
                .setQuery(query, Recipe.class)
                .build();

        Log.d("RECIPESFRAGMENT", "SetUpRecycleView()");

        adapter = new FirestoreRecyclerAdapter<Recipe, RecipeHolder>(options) {

            View view;

            @Override
            protected void onBindViewHolder(@NonNull RecipeHolder holder, int position, @NonNull Recipe productModel) {
                Log.d("RECIPESFRAGMENT", "Llamamos a SetRecipeName()");
                //Log.d("RECIPESFRAGMENT", "Recipe name - )"+productModel.getName());
                ImageView image = view.findViewById(R.id.foto);
                new DownloadImageTask(image, getResources()).execute(Uri.parse(productModel.getPicture()));

                holder.setRecipe(productModel.getName(), productModel.getFormattedDuration(),
                        productModel.getFormattedNumberOfRatings(), productModel.getRatingValue(), image);
            }

            @NonNull
            @Override
            public RecipeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.elemento_receta, parent, false);
                return new RecipeHolder(view);
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                super.onError(e);
                Log.d("RECIPESFRAGMENT", "ERROR - " + e.getCode());
            }
        };
//        Log.d("RECIPESFRAGMENT", "adapter - " + adapter.getItem(0).getName() + " - " + adapter.getItemCount());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("RECIPESFRAGMENT", "OnStart()");
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}


