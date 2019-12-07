package es.upv.epsg.igmagi.cocinainteligente.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.model.Comment;
import es.upv.epsg.igmagi.cocinainteligente.model.Recipe;
import es.upv.epsg.igmagi.cocinainteligente.utils.ImageUtils;
import es.upv.epsg.igmagi.cocinainteligente.utils.RecipeList;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ViewHolder> {
    //List with recipes
    protected ArrayList<Comment> comments = new ArrayList<>();
    protected static Context context;

    public CommentListAdapter(ArrayList<Comment> recipeList, Context context){
        this.comments = recipeList;
        this.context = context;
    }

    @Override
    public CommentListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_element, parent, false);
        return new CommentListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CommentListAdapter.ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.customize(comment);
    }

    @Override
    public int getItemCount() {
        if (comments==null) return 0;
        return comments.size();
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
        public void customize(Comment comment) {
            author.setText(comment.author);
            body.setText(comment.body);
            date.setText(comment.date.toDate().toString());

            if (comment.image.contains("-")) {
                File localFile = null;
                image.setVisibility(View.VISIBLE);
                try {
                    localFile = File.createTempFile("image", ".jpg");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final String path = localFile.getAbsolutePath();
                Log.d("Almacenamiento", "creando fichero: " + path);
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference ficheroRef = storageRef.child("comments/" + comment.getImage());
                ficheroRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess
                            (FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.d("Almacenamiento", "Fichero bajado");
                        File file;
                        try {
                            file = ImageUtils.getCompressed(context, path);
                            image.setImageBitmap(BitmapFactory.decodeFile(file.getPath()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("Almacenamiento", "ERROR: bajando fichero");
                    }
                });
            }
            /*
            puntuaciones.setText(recipe.getFormattedNumberOfRatings());
            //icono.setImageResource(recipe.getImage());
            //foto.setScaleType(ImageView.ScaleType.FIT_END);
            rating.setRating(recipe.getRatingValue());
            tiempo.setText(recipe.getFormattedDuration());*/
        }


    }
}
