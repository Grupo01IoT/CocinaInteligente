package es.upv.epsg.igmagi.cocinainteligente.ui;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import es.upv.epsg.igmagi.cocinainteligente.LoginActivity;
import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.utils.DownloadImageTask;

public class ProfileFragment extends Fragment {
    private ImageView image;
    private TextView name, email, verified, id;
    private FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    private Uri imgLink = mAuth.getPhotoUrl();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        image = root.findViewById(R.id.photo);
        name = root.findViewById(R.id.name);
        email = root.findViewById(R.id.email);
        verified = root.findViewById(R.id.verified);
        id = root.findViewById(R.id.id);

        imgLink = (imgLink == null) ? Uri.parse("https://cdn1.iconfinder.com/data/icons/fs-icons-ubuntu-by-franksouza-/256/goa-account-msn.png") :imgLink;
        new DownloadImageTask(image).execute(imgLink);
        name.setText(mAuth.getDisplayName());
        email.setText(mAuth.getEmail());
        verified.setText("Verified: " + mAuth.isEmailVerified());
        id.setText(mAuth.getUid());

        Button editar = root.findViewById(R.id.button2);
        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog d = new Dialog(getParentFragment().getContext());
                d.setContentView(R.layout.fragment_edit_profile);
                d.setTitle("Editar perfil");
                d.show();

                Window window = d.getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ImageView i = d.findViewById(R.id.profilePict);
                final EditText n = d.findViewById(R.id.nameText);
                final EditText e = d.findViewById(R.id.emailText);
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
                                                               if(task.isSuccessful()) {
                                                                   name.setText(n.getText());
                                                                   email.setText(e.getText());
                                                                   d.cancel();
                                                               }
                                                           }
                                                       }
                                );
                    }
                });
                new DownloadImageTask(i).execute(imgLink);

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

        return root;
    }

}
