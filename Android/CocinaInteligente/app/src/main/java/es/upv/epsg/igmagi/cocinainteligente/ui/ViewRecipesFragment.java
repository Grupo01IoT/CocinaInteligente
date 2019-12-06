package es.upv.epsg.igmagi.cocinainteligente.ui;

import android.graphics.BitmapFactory;
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
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.adapter.RecipeHolder;
import es.upv.epsg.igmagi.cocinainteligente.model.Recipe;
import es.upv.epsg.igmagi.cocinainteligente.model.RecipeViewModel;
import es.upv.epsg.igmagi.cocinainteligente.utils.DownloadImageTask;

public class ViewRecipesFragment extends Fragment {

    public View root;
    private FirebaseFirestore mBD = FirebaseFirestore.getInstance();
    private CollectionReference recipesCollection = mBD.collection("recipes");

    private FirestoreRecyclerAdapter<Recipe, RecipeHolder> adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_view_recipes, container, false);
        setUpRecycleViewByFirestore();

        // Inflate the layout for this fragment
        return root;
    }

    private void setUpRecycleViewByFirestore() {
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
            protected void onBindViewHolder(@NonNull RecipeHolder holder, int position, @NonNull final Recipe productModel) {
                Log.d("RECIPESFRAGMENT", "Llamamos a SetRecipeName()");
                Log.d("RECIPESFRAGMENT", "Recipe name - " + productModel.getName());
                View v = view.findViewById(R.id.container);
                final RecipeViewModel model = ViewModelProviders.of(getActivity()).get(RecipeViewModel.class);


                final ImageView image = view.findViewById(R.id.foto);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("RecipeFragment", productModel.data());
                        Log.d("RecipeFragment", productModel.getPicture());

                        model.setCurrentRecipeImage(image.getDrawable());
                        model.setCurrentRecipe(productModel);

                        Navigation.findNavController(getView()).navigate(R.id.action_nav_view_recipes_to_nav_view_recipe);

                    }
                });
                File localFile = null;
                try {
                    localFile = File.createTempFile("image", ".jpg");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final String path = localFile.getAbsolutePath();
                Log.d("Almacenamiento", "creando fichero: " + path);
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference ficheroRef = storageRef.child("images/" + productModel.getPicture());
                ficheroRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess
                            (FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.d("Almacenamiento", "Fichero bajado");
                        image.setImageBitmap(BitmapFactory.decodeFile(path));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("Almacenamiento", "ERROR: bajando fichero");
                    }
                });

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


