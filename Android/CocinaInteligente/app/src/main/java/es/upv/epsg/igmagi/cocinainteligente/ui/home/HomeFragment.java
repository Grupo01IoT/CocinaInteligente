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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.upv.epsg.igmagi.cocinainteligente.LoginActivity;
import es.upv.epsg.igmagi.cocinainteligente.adapter.TabsAdapter;
import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.ui.ProfileFragment;
import es.upv.epsg.igmagi.cocinainteligente.utils.DownloadImageTask;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private ImageView image;
    private TextView name, email, phone, id;

    private FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    private Uri imgLink = mAuth.getPhotoUrl();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        // create ContextThemeWrapper from the original Activity Context with the custom theme
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.AppTheme);
        // clone the inflater using the ContextThemeWrapper
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        final View root = localInflater.inflate(R.layout.fragment_home, container, false);

        View includeUser = root.findViewById(R.id.includeUser);
        ImageView test = includeUser.findViewById(R.id.imageView);
        TextView name = includeUser.findViewById(R.id.textName);
        name.setText((mAuth.isAnonymous()) ? "Anonymous" : mAuth.getDisplayName());
        imgLink = (imgLink == null) ? Uri.parse("https://image.flaticon.com/icons/png/512/16/16480.png") : imgLink;
        new DownloadImageTask(test).execute(imgLink);

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

    @Override
    public void onResume() {
        super.onResume();

        String home = getResources().getString(R.string.menu_home);

        if (!(((AppCompatActivity) getActivity()).getSupportActionBar().getTitle().toString().equals(home))) {

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