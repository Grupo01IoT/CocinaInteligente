package es.upv.epsg.igmagi.cocinainteligente.adapter;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.Timestamp;

import es.upv.epsg.igmagi.cocinainteligente.R;

public class CommentHolder extends RecyclerView.ViewHolder {
    public View view;


    public CommentHolder(@NonNull View itemView) {
        super(itemView);
        Log.d("RECIPESFRAGMENT", "Holas, soy el constructor");
        view = itemView;
            /*puntuaciones = itemView.findViewById(R.id.puntuaciones);
            rating = itemView.findViewById(R.id.valoracion);
            //id = itemView.findViewById(R.id.idNotificacion);
            tiempo = itemView.findViewById(R.id.tiempococcion);
            icono = itemView.findViewById(R.id.foto);*/

    }
    public void setRecipe(String commentAuthor, String commentBody, String commentDate, ImageView image) {
        TextView author = view.findViewById(R.id.authorTxt);
        author.setText(commentAuthor);

        TextView body = view.findViewById(R.id.bodyTxt);
        //Log.d("RECIPESFRAGMENT", "SetRecipeName - " + recipeName);
        body.setText(commentBody);


        TextView date = view.findViewById(R.id.dateTxt);
        //Log.d("RECIPESFRAGMENT", "SetRecipeName - " + recipeName);
        date.setText(commentDate);

        ImageView foto = view.findViewById(R.id.commentImg);
        //Log.d("RECIPESFRAGMENT", "SetRecipeName - " + recipeName);
        foto = image;
    }

}
