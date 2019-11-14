package es.upv.epsg.igmagi.cocinainteligente.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.model.Recipe;
import es.upv.epsg.igmagi.cocinainteligente.utils.RecipeList;

public class RecipeListAdapter extends RecyclerView.Adapter<RecipeListAdapter.ViewHolder> {
    //List with recipes
    protected RecipeList recipeList;

    public RecipeListAdapter(RecipeList recipeList){
        this.recipeList = recipeList;
    }

    @Override
    public RecipeListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.elemento_receta, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Recipe recipe = recipeList.getRecipeByPosition(position);
        holder.customize(recipe);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    //Creamos nuestro ViewHolder, con los tipos de elementos a modificar
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nombre, puntuaciones, id, tiempo;
        public ImageView icono;
        public RatingBar rating;

        public ViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.nombre);
            puntuaciones = itemView.findViewById(R.id.valoraciones);
            rating = itemView.findViewById(R.id.valoracion);
            //id = itemView.findViewById(R.id.idNotificacion);
            tiempo = itemView.findViewById(R.id.tiempococcion);
            icono = itemView.findViewById(R.id.foto);
        }

        // Personalizamos un ViewHolder a partir de un lugar
        public void customize(Recipe recipe) {
            nombre.setText(recipe.getName());/*
            puntuaciones.setText(recipe.getFormattedNumberOfRatings());
            //icono.setImageResource(recipe.getPicture());
            //foto.setScaleType(ImageView.ScaleType.FIT_END);
            rating.setRating(recipe.getRatingValue());
            tiempo.setText(recipe.getFormattedDuration());*/
        }


    }
}
