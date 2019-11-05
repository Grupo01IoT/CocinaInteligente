package es.upv.epsg.igmagi.cocinainteligente.ui.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import es.upv.epsg.igmagi.cocinainteligente.LoginActivity;
import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.utils.DownloadImageTask;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private String TAG = "HomeFragment";

    private FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    private Uri imgLink = mAuth.getPhotoUrl();

    //Esto se movera a un singleton del perfil en un futuro.
    private long recipe = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        // create ContextThemeWrapper from the original Activity Context with the custom theme
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.AppTheme);
        // clone the inflater using the ContextThemeWrapper
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        // getting the parent view for handle the fragment
        final View root = localInflater.inflate(R.layout.fragment_home, container, false);

        //Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(mAuth.getUid());
        Log.d(TAG, "Path data: " + docRef.getPath());

        // getting the include of the User details
        View includeUser = root.findViewById(R.id.includeUser);
        ImageView test = includeUser.findViewById(R.id.imageView);
        TextView name = includeUser.findViewById(R.id.textName);
        final TextView recipes = includeUser.findViewById(R.id.recipesText);
        final ProgressBar pb = includeUser.findViewById(R.id.progressBar);
        final TextView fidelity = includeUser.findViewById(R.id.fidelityText);
        recipes.setText(this.recipe + " " + getResources().getString(R.string.user_receipts));
        name.setText((mAuth.isAnonymous()) ? "Anonymous" : mAuth.getDisplayName());
        imgLink = (imgLink == null) ? Uri.parse("https://image.flaticon.com/icons/png/512/16/16480.png") : imgLink;
        new DownloadImageTask(test).execute(imgLink);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData().get("Recipes"));
                        recipe = (long) document.getData().get("Recipes");

                        recipes.setText(recipe + " " + getResources().getString(R.string.user_receipts));
                        pb.setProgress(Integer.parseInt("" +(long)document.getData().get("Fidelity")));
                        fidelity.setText((long)document.getData().get("Fidelity") + "%");
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        includeUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                FragmentManager fragmentManager2 = getFragmentManager();
                FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                ProfileFragment fragment2 = new ProfileFragment();
                fragmentTransaction2.addToBackStack("xyz");
                fragmentTransaction2.hide(HomeFragment.this);
                fragmentTransaction2.add(android.R.id.content, fragment2);
                fragmentTransaction2.commit();
                 */
            }
        });

        // getting the include of the Device details
        View includeDevice = root.findViewById(R.id.includeDevice);
        Button button = includeDevice.findViewById(R.id.pairDevice);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.isAnonymous()) {
                    Snackbar.make(root, "Log in to pair your device", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    final Dialog d = new Dialog(getParentFragment().getContext());
                    //d.setContentView(R.layout.fragment_edit_profile);
                    d.setTitle("Vincular dispositivo");
                    d.show();

                    Window window = d.getWindow();
                    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
            }
        });

        /*
        TabLayout tabLayout = (TabLayout) root.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Profile"));
        tabLayout.addTab(tabLayout.newTab().setText("About"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final ViewPager viewPager =(ViewPager)root.findViewById(R.id.view_pager);
        TabsAdapter tabsAdapter = new TabsAdapter(this.getFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(tabsAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
*/
        return root;
    }

    //This onResume handle the log in/log out functions
    @Override
    public void onResume() {
        super.onResume();

        String logout = getResources().getString(R.string.menu_logout);
        String login = getResources().getString(R.string.menu_login);
        String title = ((AppCompatActivity) getActivity()).getSupportActionBar().getTitle().toString();

        if (title.equals(logout) || title.equals(login)) {

            if (mAuth.isAnonymous()) {
                final AlertDialog.Builder d = new AlertDialog.Builder(getContext());
                d.setTitle("Are you sure?");
                d.setMessage("Signing out will remove this anonymous account, so you will not have the information anymore.");
                d.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
                                } else {
                                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                });
                d.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alertDialog = d.create();
                alertDialog.show();
            } else {
                FirebaseAuth.getInstance().signOut();
                getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        }
    }
}