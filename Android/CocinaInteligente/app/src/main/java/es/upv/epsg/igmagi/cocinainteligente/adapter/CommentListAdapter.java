package es.upv.epsg.igmagi.cocinainteligente.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.model.Recipe;
import es.upv.epsg.igmagi.cocinainteligente.utils.RecipeList;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ViewHolder> {
    //List with recipes
    protected RecipeList recipeList;

    public CommentListAdapter(RecipeList recipeList){
        this.recipeList = recipeList;
    }

    @Override
    public CommentListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.elemento_receta, parent, false);
        return new CommentListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CommentListAdapter.ViewHolder holder, int position) {
        Recipe recipe = recipeList.getRecipeByPosition(position);
        holder.customize(recipe);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    //Creamos nuestro ViewHolder, con los tipos de elementos a modificar
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView author, body, date;
        public ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.authorTxt);
            body = itemView.findViewById(R.id.bodyTxt);
            date = itemView.findViewById(R.id.dateTxt);
            image = itemView.findViewById(R.id.commentImg);
        }

        // Personalizamos un ViewHolder a partir de un lugar
        public void customize(Recipe recipe) {
            /*
            puntuaciones.setText(recipe.getFormattedNumberOfRatings());
            //icono.setImageResource(recipe.getImage());
            //foto.setScaleType(ImageView.ScaleType.FIT_END);
            rating.setRating(recipe.getRatingValue());
            tiempo.setText(recipe.getFormattedDuration());*/
        }


    }
}
