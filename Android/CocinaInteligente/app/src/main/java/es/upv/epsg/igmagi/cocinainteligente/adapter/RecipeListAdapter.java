package es.upv.epsg.igmagi.cocinainteligente.adapter;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.model.Recipe;
import es.upv.epsg.igmagi.cocinainteligente.model.RecipeViewModel;
import es.upv.epsg.igmagi.cocinainteligente.model.User;
import es.upv.epsg.igmagi.cocinainteligente.model.UserViewModel;
import es.upv.epsg.igmagi.cocinainteligente.utils.RecipeList;

public class RecipeListAdapter extends RecyclerView.Adapter<RecipeListAdapter.ViewHolder> {
    //List with recipes
    protected ArrayList<Recipe> recipes;
    protected Context context;
    protected static Activity app;
    protected static View v;

    public RecipeListAdapter(ArrayList<Recipe> recipeList){
        this.recipes = recipeList;
    }

    public RecipeListAdapter(ArrayList<Recipe> recipes, Context context, Activity app, View v) {
        this.recipes = recipes;
        this.context = context;
        this.app = app;
        this.v = v;
    }

    @Override
    public RecipeListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.elemento_receta, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.customize(recipe);
    }

    @Override
    public int getItemCount() {
        if (recipes==null) return 0;
        return recipes.size();
    }

    //Creamos nuestro ViewHolder, con los tipos de elementos a modificar
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nombre, puntuaciones, id, tiempo;
        public ImageView icono, fav, veggie, vegan, dairy, gluten;
        public RatingBar rating;

        public ViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.nombre);
            puntuaciones = itemView.findViewById(R.id.valoraciones);
            rating = itemView.findViewById(R.id.valoracion);
            //id = itemView.findViewById(R.id.idNotificacion);
            tiempo = itemView.findViewById(R.id.tiempococcion);
            icono = itemView.findViewById(R.id.foto);
            fav = itemView.findViewById(R.id.favIcon);
            gluten = itemView.findViewById(R.id.glutenIcon);
            dairy = itemView.findViewById(R.id.dairyIcon);
            vegan = itemView.findViewById(R.id.veganIcon);
            veggie= itemView.findViewById(R.id.veggieIcon);
        }

        // Personalizamos un ViewHolder a partir de un lugar
        public void customize(final Recipe recipe) {
            nombre.setText(recipe.getName());
            puntuaciones.setText(recipe.getFormattedNumberOfRatings());
            //icono.setImageResource(recipe.getImage());
            //foto.setScaleType(ImageView.ScaleType.FIT_END);
            Log.d("aaAAA",recipe.getRatingValue()+" - a");
            rating.setRating(recipe.getRatingValue());
            tiempo.setText(recipe.getFormattedDuration());
            if(recipe.getExtra().get("veggie")) veggie.setVisibility(View.VISIBLE);
            if(recipe.getExtra().get("vegan")) vegan.setVisibility(View.VISIBLE);
            if(recipe.getExtra().get("dairy")) dairy.setVisibility(View.VISIBLE);
            if(recipe.getExtra().get("gluten")) gluten.setVisibility(View.VISIBLE);

            UserViewModel userModel = ViewModelProviders.of((FragmentActivity) app).get(UserViewModel.class);
            User user = userModel.getCurrentUser();
            if (user.getFavouriteReceipts().contains(recipe.getUid())) fav.setVisibility(View.VISIBLE);

            View v = itemView.findViewById(R.id.container);
            final RecipeViewModel model = ViewModelProviders.of((FragmentActivity) app).get(RecipeViewModel.class);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    model.setCurrentRecipeImage(icono.getDrawable());
                    model.setId(recipe.getUid());
                    model.setCurrentRecipe(recipe);
                    Navigation.findNavController(v).navigate(R.id.action_nav_view_recipes_to_nav_view_recipe);
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
            StorageReference ficheroRef = storageRef.child("images/" + recipe.getPicture());
            ficheroRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess
                        (FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d("Almacenamiento", "Fichero bajado");
                    icono.setImageBitmap(BitmapFactory.decodeFile(path));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("Almacenamiento", "ERROR: bajando fichero");
                }
            });
        }


    }
}
