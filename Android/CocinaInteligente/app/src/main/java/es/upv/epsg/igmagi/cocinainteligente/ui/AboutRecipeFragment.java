package es.upv.epsg.igmagi.cocinainteligente.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickCancel;
import com.vansuita.pickimage.listeners.IPickResult;

import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.UUID;

import javax.annotation.Nullable;

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.adapter.CommentHolder;
import es.upv.epsg.igmagi.cocinainteligente.adapter.CommentListAdapter;
import es.upv.epsg.igmagi.cocinainteligente.model.Comment;
import es.upv.epsg.igmagi.cocinainteligente.model.Recipe;
import es.upv.epsg.igmagi.cocinainteligente.model.RecipeViewModel;
import es.upv.epsg.igmagi.cocinainteligente.utils.ImageUtils;
import es.upv.epsg.igmagi.cocinainteligente.utils.RecipeList;

public class AboutRecipeFragment extends Fragment {
    View root;
    View subview;
    private FirebaseFirestore mBD = FirebaseFirestore.getInstance();
    private CollectionReference commentsCollection = mBD.collection("comments");
    //private FirestoreRecyclerAdapter<Comment, CommentHolder> adapter;
    private CommentListAdapter adapter;
    boolean picture = false;
    String user = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    File file;
    Recipe recipe;

    ArrayList<Comment> comments = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_about_recipe, container, false);
        final RecipeViewModel model = ViewModelProviders.of(getActivity()).get(RecipeViewModel.class);
        recipe = model.getCurrentRecipe();
        setUpRecycleViewByFirestore();

        Button comment = root.findViewById(R.id.commentBtn);
        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog dialog = new BottomSheetDialog(getContext());
                subview = LayoutInflater.from(getContext()).inflate(R.layout.fragment_add_comment, null);
                ((TextView) subview.findViewById(R.id.authorTxt2)).setText(user);
                ((TextView) subview.findViewById(R.id.dateTxt2)).setText(Timestamp.now().toDate().toString());
                ((ImageButton) subview.findViewById(R.id.imageCommentBtn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PickImageDialog.build(new PickSetup().setSystemDialog(true))
                                .setOnPickResult(new IPickResult() {
                                    @Override
                                    public void onPickResult(PickResult r) {
                                        BitmapFactory.Options options=new BitmapFactory.Options();
                                        options.inSampleSize=2; //decrease decoded image
                                        Bitmap bitmap = null;
                                        try {
                                            file = File.createTempFile("compressed", ".jpg");
                                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                                            bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(r.getUri()), null, options);
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
                                            Bitmap comp = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                                            // Bitmap bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImageUri));
                                            FileOutputStream fos = new FileOutputStream(file);
                                            fos.write(out.toByteArray());
                                            fos.flush();
                                            fos.close();
                                            ((ImageButton) subview.findViewById(R.id.imageCommentBtn)).setImageBitmap(comp);
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        picture = true;
                                    }
                                }).show(getActivity().getSupportFragmentManager());
                    }
                });

                ((Button) subview.findViewById(R.id.addCommentBtn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setTitle("Uploading...");
                        progressDialog.show();

                        final String body = ((TextView) subview.findViewById(R.id.bodyTxt2)).getText().toString();
                        final Timestamp timestamp = Timestamp.now();
                        if (!body.equals("")){
                            if (picture){
                                final String pid = UUID.randomUUID().toString();
                                StorageReference ref = FirebaseStorage.getInstance().getReference().child("comments/" + pid);
                                ref.putFile(Uri.fromFile(file))
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                Map<String, Object> datos = new HashMap<>();
                                                datos.put("author", user);
                                                datos.put("body", body);
                                                datos.put("date", timestamp);
                                                datos.put("image", pid);
                                                datos.put("recipe", recipe.getUid());
                                                mBD.collection("comments").add(datos).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        Toast.makeText(getContext(),
                                                                "Comentario publicado", Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                    }
                                                });
                                                dialog.dismiss();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(getActivity(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                                        .getTotalByteCount());
                                                progressDialog.setMessage("Uploaded " + (int) progress + "%");

                                            }
                                        });
                            } else {
                                progressDialog.dismiss();
                                dialog.dismiss();
                                Map<String, Object> datos = new HashMap<>();
                                datos.put("author", user);
                                datos.put("body", body);
                                datos.put("date", timestamp);
                                datos.put("image", "null");
                                datos.put("recipe", recipe.getUid());
                                mBD.collection("comments").add(datos).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Toast.makeText(getContext(),
                                                "Comentario publicado", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(getContext(), "SE SUPOSA no QUE LA PUJA", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.setContentView(subview);

                dialog.show();
            }
        });

        return root;
    }

    public String TAG = "commentss";

    private void setUpRecycleViewByFirestore() {
        final RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recyclerCommentsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Query query = commentsCollection.orderBy("date", Query.Direction.DESCENDING).whereEqualTo("recipe",recipe.getUid());

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    Comment temp;
                    switch (dc.getType()) {
                        case ADDED:
                            temp = new Comment(dc.getDocument().getId(),(String)dc.getDocument().get("author"),(String)dc.getDocument().get("body"),new Timestamp((Date)dc.getDocument().get("date")),(String)dc.getDocument().get("image"));
                            comments.add(temp);
                            break;
                        case MODIFIED:
                            comments.remove(comments.indexOf(new Comment(dc.getDocument().getId())));
                            temp = new Comment(dc.getDocument().getId(),(String)dc.getDocument().get("author"),(String)dc.getDocument().get("body"),new Timestamp((Date)dc.getDocument().get("date")),(String)dc.getDocument().get("image"));
                            comments.add(temp);
                            break;
                        case REMOVED:
                            comments.remove(comments.indexOf(new Comment(dc.getDocument().getId())));
                            break;
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        adapter = new CommentListAdapter(comments, getContext());
        recyclerView.setAdapter(adapter);

        /*
        adapter = new FirestoreRecyclerAdapter<Comment, CommentHolder>(options) {

            View view;

            @NonNull
            @Override
            public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_element, parent, false);
                return new CommentHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CommentHolder commentHolder, int i, @NonNull Comment comment) {
                View v = view.findViewById(R.id.commentContainer);
                final RecipeViewModel model = ViewModelProviders.of(getActivity()).get(RecipeViewModel.class);

                final ImageView image = view.findViewById(R.id.commentImg);
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
                                file = ImageUtils.getCompressed(getContext(), path);
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
                commentHolder.setRecipe(comment.getAuthor(), comment.getBody(),
                        comment.getFormattedDate(), image);
            }
        };
        */
    }

}
