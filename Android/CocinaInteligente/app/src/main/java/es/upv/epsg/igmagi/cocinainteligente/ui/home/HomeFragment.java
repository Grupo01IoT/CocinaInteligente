package es.upv.epsg.igmagi.cocinainteligente.ui.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import es.upv.epsg.igmagi.cocinainteligente.FirebaseService;
import es.upv.epsg.igmagi.cocinainteligente.LoginActivity;
import es.upv.epsg.igmagi.cocinainteligente.MainActivity;
import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.model.Device;
import es.upv.epsg.igmagi.cocinainteligente.model.Recipe;
import es.upv.epsg.igmagi.cocinainteligente.model.RecipeViewModel;
import es.upv.epsg.igmagi.cocinainteligente.model.User;
import es.upv.epsg.igmagi.cocinainteligente.model.UserViewModel;
import es.upv.epsg.igmagi.cocinainteligente.ui.ProfileFragment;
import es.upv.epsg.igmagi.cocinainteligente.utils.DownloadImageTask;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private String TAG = "HomeFragment";

    private FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    private Uri imgLink = mAuth.getPhotoUrl();
    private ViewFlipper includeDevice;
    private View includeTopRecipe;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    //Recipe
    LinearLayout photo;
    TextView name, duration;
    ImageView topVeggie, topVegan, topDairy, topGluten;
    RatingBar rating;
    Recipe topRecipe;

    //Device
    private User user;
    private ArrayList<Device> devices = new ArrayList<>();
    private TextView deviceName;
    private ImageView lights;
    private ImageView fan;
    private TextView temp1;
    private TextView temp2;
    private TextView temp3;
    private TextView temp4;

    //Notificaciones
    private boolean notification = false;
    private NotificationManager notificationManager;
    static final String CANAL_ID = "mi_canal";
    static final int NOTIFICACION_ID = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        // create ContextThemeWrapper from the original Activity Context with the custom theme
        // clone the inflater using the ContextThemeWrapper
        // getting the parent view for handle the fragment
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.AppTheme);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        final View root = localInflater.inflate(R.layout.fragment_home, container, false);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //getActivity().startService(new Intent(getContext(), FirebaseService.class));

        final ViewFlipper topFlipper = (ViewFlipper) root.findViewById(R.id.topFlipper);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                topFlipper.setInAnimation(getContext(), R.anim.view_transition_fade_in);
                topFlipper.setOutAnimation(getContext(), R.anim.view_transition_fade_out);
                topFlipper.showNext();
            }
        }, 1000);


        // getting the include of the User details
        includeTopRecipe = root.findViewById(R.id.includeTopRecipe);
        photo = includeTopRecipe.findViewById(R.id.topRecipe);
        name = includeTopRecipe.findViewById(R.id.topName);
        rating = includeTopRecipe.findViewById(R.id.topRating);
        duration = includeTopRecipe.findViewById(R.id.topDuration);
        topDairy = includeTopRecipe.findViewById(R.id.topDairyIcon);
        topVegan = includeTopRecipe.findViewById(R.id.topVeganIcon);
        topVeggie = includeTopRecipe.findViewById(R.id.topVeggieIcon);
        topGluten = includeTopRecipe.findViewById(R.id.topGlutenIcon);
        final RecipeViewModel model = ViewModelProviders.of(getActivity()).get(RecipeViewModel.class);
        includeTopRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.setCurrentRecipeImage(photo.getBackground());
                model.setCurrentRecipe(topRecipe);
                Navigation.findNavController(v).navigate(R.id.nav_view_recipe);
            }
        });

        // getting the include of the Device details
        includeDevice = ((ViewFlipper) root.findViewById(R.id.viewFlipper1));
        View nodevice = includeDevice.getChildAt(0);
        Button button = nodevice.findViewById(R.id.pairDevice);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPairingWindow();
            }
        });
        View device = includeDevice.getChildAt(1);
        deviceName = device.findViewById(R.id.includeDeviceName);
        lights = device.findViewById(R.id.includeDeviceLights);
        fan = device.findViewById(R.id.includeDeviceFan);
        temp1 = device.findViewById(R.id.includeDeviceT1);
        temp2 = device.findViewById(R.id.includeDeviceT2);
        temp3 = device.findViewById(R.id.includeDeviceT3);
        temp4 = device.findViewById(R.id.includeDeviceT4);

        if (pref.getBoolean("currencySummaryDeviceOnOff", true))
            includeDevice.setVisibility(View.VISIBLE);
        else
            includeDevice.setVisibility(View.GONE);
        update();

        return root;
    }

    private void update() {
        db.collection("recipes").whereEqualTo("name", "Brian A La Planxa").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        topRecipe = document.toObject(Recipe.class);
                        name.setText(topRecipe.getName());
                        File localFile = null;
                        localFile = new File(getContext().getCacheDir().toString().concat("/" + document.getId().concat(".jpg")));
                        final String path = localFile.getAbsolutePath();
                        if (localFile.exists()) {
                            photo.setBackground(Drawable.createFromPath(path));
                        } else {
                            Log.d("Almacenamiento", "creando fichero: " + path);
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                            StorageReference ficheroRef = storageRef.child("images/" + topRecipe.getPicture());
                            ficheroRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess
                                        (FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    try {
                                        Log.d("Almacenamiento", "Fichero bajado");
                                        photo.setBackground(Drawable.createFromPath(path));
                                    } catch (OutOfMemoryError er) {
                                        //CONTROL LA FLUSH
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Log.e("Almacenamiento", "ERROR: bajando fichero");
                                }
                            });
                        }
                        rating.setRating(topRecipe.getRatingValue());
                        duration.setText(topRecipe.getFormattedDuration());
                        if (topRecipe.getExtra().get("veggie")) topVeggie.setVisibility(View.VISIBLE);
                        if (topRecipe.getExtra().get("vegan")) topVegan.setVisibility(View.VISIBLE);
                        if (topRecipe.getExtra().get("dairy")) topDairy.setVisibility(View.VISIBLE);
                        if (topRecipe.getExtra().get("gluten")) topGluten.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
        db.collection("users").document(mAuth.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                //user = documentSnapshot.toObject(User.class);
                //user.setUid(mAuth.getUid());
                user = new User(documentSnapshot.getId(), documentSnapshot.getString("name"),
                        documentSnapshot.getString("email"), documentSnapshot.getString("image"),
                        documentSnapshot.getLong("fidelity"), documentSnapshot.getDate("joinDate"),
                        (ArrayList<String>) documentSnapshot.get("recipes"),
                        (ArrayList<String>) documentSnapshot.get("favouriteReceips"),
                        (ArrayList<String>) documentSnapshot.get("devices"));


                db.collection("devices").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                            if (user.devices.contains(snapshot.getId())) {
                                devices.clear();
                                devices.add(snapshot.toObject(Device.class));
                                refreshDevice();
                            }
                        }
                    }
                });
            }
        });
    }

    private void refreshDevice() {
        deviceName.setText(devices.get(0).getName());
        temp1.setText("Temperatura 1: " + devices.get(0).getCooktop().get(0));
        temp2.setText("Temperatura 2: " + devices.get(0).getCooktop().get(1));
        temp3.setText("Temperatura 3: " + devices.get(0).getCooktop().get(2));
        temp4.setText("Temperatura 4: " + devices.get(0).getCooktop().get(3));

        for (int i = 0; i < 4; i++) {
            if (devices.get(0).getCooktop().get(0) > 60 && notification == false) {
                notificationManager = (NotificationManager) getActivity().getSystemService(NotificationManager.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel(CANAL_ID, "Mis Notificaciones", NotificationManager.IMPORTANCE_DEFAULT);
                    notificationChannel.setDescription("Descripcion del canal");
                    notificationManager.createNotificationChannel(notificationChannel);
                }
                NotificationCompat.Builder notificacion = new NotificationCompat.Builder(getContext(), CANAL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Cocina en uso")
                        .setContentText("El fuego está encendido")
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo1))
                        .setWhen(System.currentTimeMillis() + 1000 * 60 * 60)
                        .setContentInfo("más info")
                        .setTicker("Texto en barra de estado");
                notificationManager.notify(NOTIFICACION_ID, notificacion.build());
                notification = true;
            }
        }
        if (!notification) {
            try {

                notificationManager.cancel(NOTIFICACION_ID);
            } catch (Exception e) {

            }
        }
        notification = false;

        if (devices.get(0).lights) {
            lights.setImageResource(R.drawable.btnluzon);
        } else {
            lights.setImageResource(R.drawable.btnluzoff);
        }
        if (devices.get(0).fan) {
            fan.setImageResource(R.drawable.btnextraon);
        } else {
            fan.setImageResource(R.drawable.btnextraoff);
        }
    }

    private void showPairingWindow() {
        if (mAuth.isAnonymous()) {
            Snackbar.make(getView(), "Log in to pair your device", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {
            final BottomSheetDialog d = new BottomSheetDialog(getParentFragment().getContext());
            d.setContentView(R.layout.fragment_pair);
            d.setTitle("Vincular dispositivo");

            final EditText id = d.findViewById(R.id.editText);
            final Button pair = d.findViewById(R.id.pairDevice);
            id.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.toString().equals("")) {
                        pair.setEnabled(false);
                    } else {
                        pair.setEnabled(true);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            pair.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.collection("users").document(mAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot document = task.getResult();
                            ArrayList<String> group;
                            if (user.getDevices().size() > 0) {
                                group = (ArrayList<String>) document.get("devices");
                                group.add(id.getText().toString());
                            } else {
                                group = new ArrayList<String>();
                                group.add(id.getText().toString());
                            }
                            db.collection("users").document(mAuth.getUid())
                                    .update("devices", group)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void documentReference) {
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                            includeDevice.setDisplayedChild(1);
                                            d.cancel();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });
                        }
                    });
                }
            });
            d.show();

            Window window = d.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private boolean checkDevices(DocumentSnapshot document) {
        return document.get("devices") != null;
    }

    //This onResume handle the log in/log out functions
    @Override
    public void onResume() {
        super.onResume();
        String logout = getResources().getString(R.string.menu_logout);
        String login = getResources().getString(R.string.menu_login);
        String pair = getResources().getString(R.string.menu_pair);
        String title = ((AppCompatActivity) getActivity()).getSupportActionBar().getTitle().toString();

        if (title.equals(pair)) {
            showPairingWindow();
        } else if (title.equals(logout) || title.equals(login)) {

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