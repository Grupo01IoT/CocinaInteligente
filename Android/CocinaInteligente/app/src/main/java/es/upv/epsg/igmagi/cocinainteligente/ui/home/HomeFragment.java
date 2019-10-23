package es.upv.epsg.igmagi.cocinainteligente.ui.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import es.upv.epsg.igmagi.cocinainteligente.LoginActivity;
import es.upv.epsg.igmagi.cocinainteligente.MainActivity;
import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.utils.DownloadImageTask;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private ImageView image;
    private TextView name, email, phone, id;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText("Bienvenido: " +
                        FirebaseAuth.getInstance().getCurrentUser().getEmail());
            }
        });

        image = root.findViewById(R.id.photo);
        name = root.findViewById(R.id.name);
        email = root.findViewById(R.id.email);
        phone = root.findViewById(R.id.phone);
        id = root.findViewById(R.id.id);
        new DownloadImageTask(image).execute(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());
        name.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        phone.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        id.setText(FirebaseAuth.getInstance().getCurrentUser().getUid());

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