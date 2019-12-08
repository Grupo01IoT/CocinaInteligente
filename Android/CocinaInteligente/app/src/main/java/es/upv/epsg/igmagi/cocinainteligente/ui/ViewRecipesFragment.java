package es.upv.epsg.igmagi.cocinainteligente.ui;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.annotation.Nullable;

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.adapter.CommentListAdapter;
import es.upv.epsg.igmagi.cocinainteligente.adapter.RecipeHolder;
import es.upv.epsg.igmagi.cocinainteligente.adapter.RecipeListAdapter;
import es.upv.epsg.igmagi.cocinainteligente.model.Comment;
import es.upv.epsg.igmagi.cocinainteligente.model.Recipe;
import es.upv.epsg.igmagi.cocinainteligente.model.RecipeViewModel;
import es.upv.epsg.igmagi.cocinainteligente.utils.DownloadImageTask;

public class ViewRecipesFragment extends Fragment {

    public View root;
    private FirebaseFirestore mBD = FirebaseFirestore.getInstance();
    private CollectionReference recipesCollection = mBD.collection("recipes");

    private RecipeListAdapter adapter;
    ArrayList<Recipe> recipes = new ArrayList<>();
    ArrayList<Recipe> recipesFilter = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_view_recipes, container, false);
        setUpRecycleViewByFirestore();


        return root;
    }

    private void setUpRecycleViewByFirestore() {
        final RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recyclerRecipeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        Query query = recipesCollection.orderBy("name", Query.Direction.DESCENDING);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("AA", "listen:error", e);
                    return;
                }

                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    Recipe temp;
                    switch (dc.getType()) {
                        case ADDED:
                            temp = getRecipeFromDoc(dc);
                            if (!recipes.contains(temp)) {
                                recipes.add(temp);
                            }
                            break;
                        case MODIFIED:
                            Log.d("asdd", "" + recipes.indexOf(new Recipe(dc.getDocument().getId())));
                            recipes.remove(recipes.indexOf(new Recipe(dc.getDocument().getId())));
                            temp = getRecipeFromDoc(dc);
                            recipes.add(temp);
                            break;
                        case REMOVED:
                            Log.d("asdd", "" + recipes.indexOf(new Recipe(dc.getDocument().getId())));
                            recipes.remove(recipes.indexOf(new Recipe(dc.getDocument().getId())));
                            break;
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        adapter = new RecipeListAdapter(recipes, getContext(), getActivity(), getView());

        recyclerView.setAdapter(adapter);


        Spinner sp = root.findViewById(R.id.filterSpinner);
        ArrayAdapter<CharSequence> spAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.recipe_type, android.R.layout.simple_spinner_item);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(spAdapter);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Log.d("aaAAA", "TIPO : " + item);
                if (item.equals("Todas")) {

                    Log.d("aaAAA", "A  ");
                    adapter = new RecipeListAdapter(recipes, getContext(), getActivity(), getView());
                } else {
                    Log.d("aaAAA", "B  ");
                    recipesFilter.clear();
                    for (Recipe r : recipes) {
                        Log.d("aaAAA", "item tipo : " + r.getTipo());
                        if (item.equals(r.getTipo())){
                            recipesFilter.add(r);
                            Log.d("aaAAA", "item lista : " + r.getName());
                        }
                    }
                    adapter = new RecipeListAdapter(recipesFilter, getContext(), getActivity(), getView());
                }
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

    }

    private Recipe getRecipeFromDoc(DocumentChange dc) {
        return new Recipe(
                dc.getDocument().getId(),
                (String) dc.getDocument().get("name"),
                (String) dc.getDocument().get("description"),
                (Timestamp) dc.getDocument().get("creationDate"),
                (String) dc.getDocument().get("picture"),
                (ArrayList<String>) dc.getDocument().get("steps"),
                (HashMap<String, Long>) dc.getDocument().get("ratings"),
                (String) dc.getDocument().get("user"),
                (String) dc.getDocument().get("tipo"),
                Integer.parseInt(dc.getDocument().get("duration") + "")
        );
    }

}


