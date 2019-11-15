package es.upv.epsg.igmagi.cocinainteligente.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import es.upv.epsg.igmagi.cocinainteligente.LoginActivity;
import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.model.Device;
import es.upv.epsg.igmagi.cocinainteligente.model.User;
import es.upv.epsg.igmagi.cocinainteligente.utils.DownloadImageTask;

public class ProfileFragment extends Fragment {
    private ImageView image;
    private TextView name, email, verified, fidelity, recipes;
    private FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    private Uri imgLink = mAuth.getPhotoUrl();
    private ProgressBar pb;

    User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        image = root.findViewById(R.id.photo);
        name = root.findViewById(R.id.name);
        email = root.findViewById(R.id.email);
        verified = root.findViewById(R.id.verified);
        fidelity = root.findViewById(R.id.fidelityText2);
        recipes = root.findViewById(R.id.recipesText2);
        pb = root.findViewById(R.id.progressBar2);

        verified.setText("Verified: " + mAuth.isEmailVerified());

        Button editar = root.findViewById(R.id.button2);
        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog d = new Dialog(getContext());
                d.setContentView(R.layout.fragment_edit_profile);
                d.setTitle("Editar perfil");
                d.show();

                Window window = d.getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ImageView i = d.findViewById(R.id.profilePict);
                final EditText n = d.findViewById(R.id.nameField);
                final EditText e = d.findViewById(R.id.emailField);
                Button b = d.findViewById(R.id.saveButton);
                n.setText(name.getText());
                e.setText(email.getText());

                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(n.getText().toString()).build();
                        mAuth.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                           @Override
                                                           public void onComplete(@NonNull Task<Void> task) {
                                                               if (task.isSuccessful()) {
                                                                   name.setText(n.getText());
                                                                   email.setText(e.getText());
                                                                   d.cancel();
                                                               }
                                                           }
                                                       }
                                );
                    }
                });
                new DownloadImageTask(i,getResources()).execute(imgLink);
            }
        });

        Button cerrar = root.findViewById(R.id.button);
        cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getParentFragment().getContext(), LoginActivity.class));
            }
        });

        update();

        return root;
    }


    FirebaseFirestore db;
    private void update() {
        db = FirebaseFirestore.getInstance();
        db.collection("users").document(mAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> documentSnapshotTask) {
                DocumentSnapshot documentSnapshot = documentSnapshotTask.getResult();
                if(documentSnapshotTask.isSuccessful()){
                    user = new User(documentSnapshot.getId(), documentSnapshot.getString("name"),
                            documentSnapshot.getString("email"), documentSnapshot.getString("image"),
                            documentSnapshot.getLong("fidelity"), documentSnapshot.getDate("joinDate"),
                            (ArrayList<String>) documentSnapshot.get("recipes"),
                            (ArrayList<String>) documentSnapshot.get("favouriteRecipes"),
                            (ArrayList<String>) documentSnapshot.get("devices"));
                    refreshUser();
                }
            }
        });
    }

    private void refreshUser() {
        name.setText((mAuth.isAnonymous()) ? "Anonymous" : user.getName());
        email.setText((mAuth.isAnonymous()) ? "Anonymous" : user.getEmail());
        imgLink = (imgLink == null) ? Uri.parse("https://image.flaticon.com/icons/png/512/16/16480.png") : imgLink;
        new DownloadImageTask(image, getResources()).execute(imgLink);
        recipes.setText(user.getRecipes().size() + " " + getResources().getString(R.string.user_receipts));
        pb.setProgress(Integer.parseInt("" +user.getFidelity()));
        fidelity.setText(user.getFidelity() + "%");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


}
