package es.upv.epsg.igmagi.cocinainteligente;

import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import es.upv.epsg.igmagi.cocinainteligente.utils.DownloadImageTask;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    private ImageView profilePicture;
    private TextView profileName, profileEmail;


    private FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();

    private Uri imageUrl;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                imageUrl = (mAuth.isAnonymous()) ? Uri.parse("https://image.flaticon.com/icons/png/512/16/16480.png") : mAuth.getPhotoUrl();
                profileName.setText((mAuth.isAnonymous()) ? "Not signed in" : mAuth.getDisplayName());
                profileEmail.setText((mAuth.isAnonymous()) ? "Empty email" : mAuth.getEmail());
                new DownloadImageTask(profilePicture, getResources()).execute(imageUrl);
            }
        });

        // Including layout to activity
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        //Getting objects inside the layout header
        profilePicture = navigationView.getHeaderView(0).findViewById(R.id.imageView);
        profileName = navigationView.getHeaderView(0).findViewById(R.id.textView);
        profileEmail = navigationView.getHeaderView(0).findViewById(R.id.textView2);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_view_recipes, R.id.nav_kitchen, R.id.nav_kitchen, R.id.nav_preferences, R.id.nav_about)
                .setDrawerLayout(drawer)
                .build();
        // From activity main drawer control.
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //Setting the layout items

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actionbar_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mAuth.isAnonymous()) {
            navigationView.getMenu().findItem(R.id.logout).setVisible(false);
            navigationView.getMenu().findItem(R.id.login).setVisible(true);
        } else {
            navigationView.getMenu().findItem(R.id.logout).setVisible(true);
            navigationView.getMenu().findItem(R.id.login).setVisible(false);
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }




}
