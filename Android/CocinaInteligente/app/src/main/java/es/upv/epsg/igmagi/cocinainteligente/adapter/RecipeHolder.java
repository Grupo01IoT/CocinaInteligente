package es.upv.epsg.igmagi.cocinainteligente.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.model.Recipe;
import es.upv.epsg.igmagi.cocinainteligente.utils.DownloadImageTask;

public class RecipeHolder extends RecyclerView.ViewHolder {
    public View view;
    public ImageView icono;
    public RatingBar rating;

    public RecipeHolder(@NonNull View itemView) {
        super(itemView);
        Log.d("RECIPESFRAGMENT", "Holas, soy el constructor");
        view = itemView;
            /*puntuaciones = itemView.findViewById(R.id.puntuaciones);
            rating = itemView.findViewById(R.id.valoracion);
            //id = itemView.findViewById(R.id.idNotificacion);
            tiempo = itemView.findViewById(R.id.tiempococcion);
            icono = itemView.findViewById(R.id.foto);*/

    }

    public void setRecipe(String recipeName, String recipeDuration, String recipeRatings, float recipeRating, ImageView image) {
        TextView nombre = view.findViewById(R.id.nombre);
        Log.d("RECIPESFRAGMENT", "SetRecipeName - " + recipeName);
        nombre.setText(recipeName);

        TextView duration = view.findViewById(R.id.tiempococcion);
        //Log.d("RECIPESFRAGMENT", "SetRecipeName - " + recipeName);
        duration.setText(recipeDuration);

        TextView ratings = view.findViewById(R.id.valoraciones);
        //Log.d("RECIPESFRAGMENT", "SetRecipeName - " + recipeName);
        ratings.setText(recipeRatings);

        RatingBar rating = view.findViewById(R.id.valoracion);
        //Log.d("RECIPESFRAGMENT", "SetRecipeName - " + recipeName);
        rating.setRating(recipeRating);

        ImageView foto = view.findViewById(R.id.foto);
        //Log.d("RECIPESFRAGMENT", "SetRecipeName - " + recipeName);
        foto = image;
    }


}

