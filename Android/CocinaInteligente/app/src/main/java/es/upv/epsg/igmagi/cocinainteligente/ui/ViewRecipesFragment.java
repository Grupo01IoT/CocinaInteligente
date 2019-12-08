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
import android.widget.ImageView;

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_view_recipes, container, false);
        setUpRecycleViewByFirestore();

        return root;
    }

    private void setUpRecycleViewByFirestore() {
        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recyclerRecipeList);
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
    }

    private Recipe getRecipeFromDoc(DocumentChange dc){
        return new Recipe(
                dc.getDocument().getId(),
                (String) dc.getDocument().get("name"),
                (String) dc.getDocument().get("description"),
                (Timestamp) dc.getDocument().get("creationDate"),
                (String) dc.getDocument().get("picture"),
                (ArrayList<String>) dc.getDocument().get("steps"),
                (HashMap<String, Long>) dc.getDocument().get("ratings"),
                (String) dc.getDocument().get("user"),
                Integer.parseInt(dc.getDocument().get("duration")+"")
        );
    }

}


