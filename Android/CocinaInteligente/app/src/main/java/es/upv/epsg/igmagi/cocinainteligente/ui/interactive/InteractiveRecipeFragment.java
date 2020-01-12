package es.upv.epsg.igmagi.cocinainteligente.ui.interactive;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.model.Recipe;
import es.upv.epsg.igmagi.cocinainteligente.model.RecipeViewModel;
import es.upv.epsg.igmagi.cocinainteligente.model.Step;
import es.upv.epsg.igmagi.cocinainteligente.model.User;
import es.upv.epsg.igmagi.cocinainteligente.model.UserViewModel;

import static com.example.igmagi.shared.Mqtt.broker;
import static com.example.igmagi.shared.Mqtt.clientId;
import static com.example.igmagi.shared.Mqtt.listenChannel;
import static com.example.igmagi.shared.Mqtt.playingRecipe;
import static com.example.igmagi.shared.Mqtt.qos;
import static com.example.igmagi.shared.Mqtt.topicRoot;
import static com.example.igmagi.shared.Mqtt.weight;

public class InteractiveRecipeFragment extends Fragment implements org.eclipse.paho.client.mqttv3.MqttCallback {

    private MqttClient client;
    String TAG = "InteractiveRecipeFragment";

    // GENERAL STUFF MAYBE USELESS
    private FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore mDB = FirebaseFirestore.getInstance();

    //LAYOUT ELEMENTS
    View root;
    private SlideshowViewModel slideshowViewModel;
    private TextView name, stepsCounter;
    private ImageButton stop;
    private CardView stepsCV, currentStepsCV;
    User user;
    Recipe recipe;
    ViewFlipper viewFlipper;
    LinearLayout stepsContainer, currentStepsContainer;

    //VIEWMODEL
    UserViewModel userViewModel;
    RecipeViewModel recipeViewModel;

    //Testing
    private String recipeIdPlayingOnDB = "";
    private Boolean firstTimeInitialContinueViews = true;

    //DATA ELEMENTS
    ArrayList<Step> steps;
    ArrayList<Integer> stepsToDO;
    ArrayList<Integer> stepsDone;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_interactive_recipe_dialog, container, false);


        //Layout elements
        name = root.findViewById(R.id.intRecipeName);
        stepsCounter = root.findViewById(R.id.stepCounter);
        stop = root.findViewById(R.id.intStopBtn);
        stepsContainer = (LinearLayout) root.findViewById(R.id.stepsContainer);
        currentStepsContainer = (LinearLayout) root.findViewById(R.id.currentStepsContainer);
        stepsCV = (CardView) root.findViewById(R.id.stepsCV);
        currentStepsCV = (CardView) root.findViewById(R.id.currentStepsCV);

        //ViewModels
        userViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        recipeViewModel = ViewModelProviders.of(getActivity()).get(RecipeViewModel.class);
        user = userViewModel.getCurrentUser();
        recipe = recipeViewModel.getCurrentRecipe();


        steps = new ArrayList();
        stepsToDO = new ArrayList();
        stepsDone = new ArrayList();


        //Check if the user has already paired the device
        SharedPreferences pref2 = getActivity().getPreferences(Context.MODE_PRIVATE);
        final String deviceId = pref2.getString("device_id", "empty");

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Step s : steps) {
                    Log.d(TAG, "" + s.getStep());
                }

                //SET THE RECIPE TO NOT PLAYING
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setMessage(R.string.interactive_stop_alert)
                        .setCancelable(false)
                        .setPositiveButton(R.string.accept_modal, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Map<String, Object> valores = new HashMap<>();
                                valores.put("isPlaying", false);
                                valores.put("id", recipe.getUid());
                                valores.put("cook", user.getUid());

                                Map<String, Object> datos = new HashMap<>();
                                datos.put("playingRecipe", valores);

                                mDB.collection("devices").document("conet_kitchen").set(datos, SetOptions.merge());
                                Navigation.findNavController(getParentFragment().getView()).navigate(R.id.nav_view_recipes);

                                dialog.cancel();

                            }
                        })
                        .setNegativeButton(R.string.deny_modal, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.cancel();
                            }
                        });

                AlertDialog d = alert.create();
                d.setTitle(R.string.alert_modal);
                d.show();


            }
        });

        name.setText(recipe.getName());
        if (!deviceId.equals("empty")) {
            mDB.collection("devices").document(deviceId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        // Done: See if there is any recipe playing
                        if (document.exists()) {
                            Map<String, Object> mapped = document.getData();

                            //Log.d(TAG, "onComplete: "+(mapped.containsKey("playingRecipe")));

                            if (mapped.containsKey("playingRecipe")) {
                                if ((Boolean) ((Map) mapped.get("playingRecipe")).get("isPlaying")) {
                                    Log.d(TAG, "Se está playeando una receta");
                                    recipeIdPlayingOnDB = (String) ((Map) mapped.get("playingRecipe")).get("id");
                                    continueInteractiveRecipe(recipeIdPlayingOnDB);

                                } else {
                                    Log.d(TAG, "No se está playeando nada");
                                    startInteractiveRecipe();
                                }
                            } else {
                                Log.d(TAG, "No se está playeando nada");
                                startInteractiveRecipe();
                            }

                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }

                }
            });
        }


        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

    }

    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        //Reset the Steps "ID"
        //TODO: if the recipe has been stopped -> Step.numSteps = 0;
        Step.numSteps = 0;
    }

    //Functions
    //-------------    MAIN FUNCTIONS: ---------------
    private void startInteractiveRecipe() {
        Log.d(TAG, "startInteractiveSetup: SETUP");
        setupInitialSteps();
        setupInitialViews();
        updateDBRecipePlaying();
        //TODO: Notification for devices paired with the kitchen (?)


        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(steps);
            Log.d(TAG, "steps: " + (jsonString));
            getPlayingRecipeInfo();
        } catch (Exception e) {
            Log.d(TAG, "startInteractiveRecipe: lmao");
        }


    }

    private void continueInteractiveRecipe(String playingRecipeId) {
        //If the recipe that is trying to be continued is the one already playing, it will continue playing without asking

        if (!recipe.getUid().equals(playingRecipeId)) {
            //FROM DIFFERENT RECIPE, IF YES -> startInteractiveRecipe(),
            // TODO: IF NO -> SET NEW VIEWMODEL WITH RECIPE PLAYING
            modalStopCurrentStartNew();//if true -> startInteractiveRecipe(), if false -> retrieve info from current recipe
        } else {
            //SAME RECIPE -> FETCH INFO -> REFRESH
            Log.d(TAG, "startInteractiveSetup: CONTINUE WITH RECIPE");
            name.setText(recipe.getName());

            setupInitialSteps();
            setupInitialContinueViews();
            /*setupInitialSteps();
            setupInitialViews();*/

        }

        getPlayingRecipeInfo();
    }


    //-----------------   SETUP FUNCTIONS   -------------------
    private void setupInitialSteps() {
        for (int i = 0; i < recipe.getSteps().size(); i++) {
            Step s;
            HashMap map = (HashMap) recipe.getSteps().get(i);
            if (!map.get("mode").toString().contains("Manual")) {
                steps.add(new Step(map.get("mode").toString(), map.get("step").toString(), map.get("trigger").toString()));
            } else {
                steps.add(new Step(map.get("mode").toString(), map.get("step").toString()));
            }

            stepsToDO.add(steps.get(i).getPos());
        }


    }

    private void setupInitialViews() {
        // DONE: FILL SETUP LAYOUT
        //name.setText(recipe.getName());
        LayoutInflater li = LayoutInflater.from(getContext());
        //int index = 0;
        stepsCounter.setText("(0/" + steps.size() + ")");
        for (final Step s : steps) {
            View view = li.inflate(R.layout.fragment_interactive_step, null);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 0, 10, 10);
            //int newid = View.generateViewId();
            //((LinearLayout)view.findViewById(R.id.stepContainer)).setId(""+3);
            //((LinearLayout)view.findViewById(R.id.stepContainer)).setTag(index++);

            ((LinearLayout) view.findViewById(R.id.stepContainer)).setTag("step" + s.getPos());

            ((ImageButton) view.findViewById(R.id.intNextStep)).setTag(s.getPos());

            ((TextView) view.findViewById(R.id.intStepText)).setText(s.getStep());
            ((ImageButton) view.findViewById(R.id.intNextStep)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // ----------------- PROBANDO TAGS --------------------
                    nextStep(v, s.getPos(), s.getStatus(), s.getTrigger());
                    Log.d(TAG, "onClick: CLICKED ELEMENT: " + String.valueOf(v.getTag()));
                }
            });
            //view.getBackground().setTint(getResources().getColor(R.color.white));
            stepsContainer.addView(view, layoutParams);
            //stepsContainer.getForeground().setTint(getResources().getColor(R.color.darkerer));
        }
    }

    private void updateDBRecipePlaying() {
        // DONE: SETUP FIREBASE: pairedkitchen.add("playingRecipe", {isPlaying: bool, recipeId: String(ID), cook: String(ID)}) --> This will trigger the Listener
        Map<String, Object> valores = new HashMap<>();
        valores.put("isPlaying", true);
        valores.put("id", recipe.getUid());
        valores.put("cook", user.getUid());

        Map<String, Object> datos = new HashMap<>();
        datos.put("playingRecipe", valores);
        //todo: cambiar device dynamically
        mDB.collection("devices").document("conet_kitchen").set(datos, SetOptions.merge());
    }


    //-----------------   CONTINUE FUNCTIONS   -------------------
    private void modalStopCurrentStartNew() {

        //SI LA RECETA QUE SE HA INICIADO != DE LA INICIADA
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setMessage(R.string.interactive_alert_msg)
                .setCancelable(false)
                .setPositiveButton(R.string.accept_modal, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startInteractiveRecipe();
                        dialog.cancel();

                    }
                })
                .setNegativeButton(R.string.deny_modal, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setNewRecipeViewModel();
                        getPlayingRecipeInfo();
                        dialog.cancel();
                    }
                });
        AlertDialog d = alert.create();
        d.setTitle(R.string.alert_modal);
        d.show();

    }

    private void setupInitialContinueViews() {
        if (firstTimeInitialContinueViews) {
            stepsContainer.setVisibility(View.GONE);
            setupInitialViews();
            firstTimeInitialContinueViews = false;
        }
    }

    //-----------------   SHARED FUNCTIONS   ---------------------
    private void refresh() {
        /*
        CODIGO DE STATUS:
        -----------------
                                                            Manual                          Time                    Temperature
                                                            ------                         -------                  ------------
        0 -----> Not started yet (on the bottom part)       Disabled1                     Disabled1                  Disabled1
        1 -----> Suggested (on the top part)                NotCompletedTick              NotCompletedClock         NotCompletedThermometre
        2 -----> Running (Time & Temperature)                   X                         ClockTicking              ThermometreStarting
        3 -----> Finished(Completed Background)                Tick                       ClockComplete             ThermometreComplete
        4 -----> Hidden

        status, temperature
        */

        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonString = mapper.writeValueAsString(steps);
            //Log.d(TAG, "refresh: " + jsonString);
        } catch (Exception x) {
        }


        int finishedCounter = 0;
        int currentSteps = 0;

        for (Step s : steps) {
            Log.d(TAG, "refresh: " + s.getStep());
            LinearLayout curr = (LinearLayout) root.findViewWithTag("step" + s.getPos());
            ImageButton currIm = (ImageButton) root.findViewWithTag(s.getPos());

            if ((s.getMode().toLowerCase()).equals("temperatura")) {
                currIm.setImageResource(R.drawable.thermometre);
            } else if ((s.getMode().toLowerCase()).equals("tiempo")) {
                currIm.setImageResource(R.drawable.sandclock);
            } else {

            }

            if (s.getStatus() > 0) {
                //curr.getBackground().setTint(getResources().getColor(R.color.colorPrimaryDark));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(10, 0, 10, 10);
                ((LinearLayout) curr.getParent()).removeView(curr);

                currentStepsContainer.addView(curr, 0, layoutParams);


                currIm.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.whiter));
                //currIm.setImageTintList(null);
                ((MaterialCardView) currIm.getParent()).setCardBackgroundColor(getResources().getColor(R.color.whiter));
                ((MaterialCardView) currIm.getParent()).setStrokeColor(getResources().getColor(R.color.black));

            }
            if (s.getStatus() == 1) {
                Log.d(TAG, "Status 1");
                currentSteps++;

                currIm.setImageTintList(ContextCompat.getColorStateList(getContext(), R.color.black));


            }
            if (s.getStatus() == 2) {
                Log.d(TAG, "Status 2 ");
                currentSteps++;

                currIm.setImageTintList(ContextCompat.getColorStateList(getContext(), R.color.colorAccent));
                ((MaterialCardView) currIm.getParent()).setStrokeColor(getResources().getColor(R.color.colorAccent));
                currIm.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.darker));
                ((MaterialCardView) currIm.getParent()).setCardBackgroundColor(getResources().getColor(R.color.darker));

            }
            if (s.getStatus() == 3) {
                Log.d(TAG, "Status 3 ");
                finishedCounter++;

                if ((s.getMode().toLowerCase()).equals("temperatura")) {
                    currIm.setImageResource(R.drawable.tick);
                } else if ((s.getMode().toLowerCase()).equals("tiempo")) {
                    currIm.setImageResource(R.drawable.tick);
                } else {
                    currIm.setImageResource(R.drawable.tick);
                }

                currIm.setImageTintList(ContextCompat.getColorStateList(getContext(), R.color.colorPrimary));
                ((MaterialCardView) currIm.getParent()).setStrokeColor(getResources().getColor(R.color.colorPrimary));
            }

            if (s.getStatus() > 3) {
                curr.setVisibility(View.GONE);
            }
        }
        stepsCounter.setText("(" + finishedCounter + "/" + steps.size() + ")");


        if (finishedCounter == (steps.size())) {
            stepsCV.setVisibility(View.GONE);
            currentStepsCV.setVisibility(View.GONE);
            finishRecipe();
        } else {
            /*
            if(currentSteps == 0){
                currentStepsCV.setVisibility(View.GONE);
                stepsCV.setVisibility(View.VISIBLE);

            }else if(finishedCounter + currentSteps == (steps.size()+1)){
                currentStepsCV.setVisibility(View.VISIBLE);
                stepsCV.setVisibility(View.GONE);
            }else{
                stepsCV.setVisibility(View.VISIBLE);
                currentStepsCV.setVisibility(View.VISIBLE);

            }*/
        }
    }

    private void finishRecipe() {
        Log.d(TAG, "Receta finalizada");

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getContext());

        LinearLayout linearLayout = new LinearLayout(getContext());
        final RatingBar rating = new RatingBar(getContext());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rating.setLayoutParams(lp);
        rating.setNumStars(5);
        rating.setStepSize(1);

        //add ratingBar to linearLayout
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        linearLayout.addView(rating);



        popDialog.setTitle(getResources().getString(R.string.finished_recipe));
        popDialog.setCancelable(false);
        //add linearLayout to dailog
        popDialog.setView(linearLayout);


        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                System.out.println("Rated val:" + v);
            }
        });


        // Button OK
        popDialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //textView.setText(String.valueOf(rating.getProgress()));
                        mDB.collection("recipes").document(recipe.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        HashMap<String, Long> map = (HashMap<String, Long>)document.get("ratings");
                                        map.put(String.valueOf(map.size()), Long.valueOf(rating.getProgress()));
                                        Map<String, Object> data = new HashMap<>();
                                        data.put("ratings", map);
                                        mDB.collection("recipes").document(recipe.getUid()).set(data, SetOptions.merge());
                                    } else {
                                        Log.d(TAG, "No such document");
                                    }
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }
                            }
                        });
                        dialog.dismiss();
                    }

                })

                // Button Cancel
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Navigation.findNavController().navigate(R.id.nav_view_recipes);

                                dialog.cancel();
                            }
                        });

        popDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Navigation.findNavController(getParentFragment().getView()).navigate(R.id.nav_view_recipes);
            }
        });
        popDialog.create();
        popDialog.show();


    }

    private void nextStep(View v, int pos, int status, String trigger) {
        //TODO: Escribir el MQTT para los demás dispositivos (Si recibe el mismo dispositivo el nuevo layout que ha modificado él, que no se cambie
        try {
            MqttMessage message = new MqttMessage(Integer.toString(pos).getBytes());
            message.setQos(qos);
            message.setRetained(false);
            client.publish(topicRoot + listenChannel, message);
        } catch (MqttException e) {
            Log.e(TAG, "Error al publicar.", e);
        }
    }

    private void getPlayingRecipeInfo() {


        LayoutInflater li = LayoutInflater.from(getContext());

        /*
        View view = li.inflate(R.layout.fragment_interactive_step, null);
        ((TextView)view.findViewById(R.id.intStepText)).setText(s.getStep());
        stepsContainer.addView(view);

         */
        Log.d(TAG, "RETRIEVING DATA...");

        startThread();


    }

    private void updateFromApp(String s) {
        //TODO: implementar
        try {
            MqttMessage message = new MqttMessage((s + "").getBytes());
            message.setQos(qos);
            message.setRetained(false);
            client.publish(topicRoot + weight, message);
        } catch (MqttException e) {
            Log.e(TAG, "Error al publicar.", e);
        }
    }

    private void setNewRecipeViewModel() {

        mDB.collection("recipes").document(recipeIdPlayingOnDB).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    recipe = document.toObject(Recipe.class);
                    recipe.setUid(document.getId());

                    File localFile = null;
                    localFile = new File(getContext().getCacheDir().toString().concat("/" + document.getId().concat(".jpg")));
                    final String path = localFile.getAbsolutePath();
                    if (localFile.exists()) {
                        Drawable ph;
                        ph = Drawable.createFromPath(path);
                        recipeViewModel.setCurrentRecipeImage(ph);
                        recipeViewModel.setCurrentRecipe(recipe);
                        recipeViewModel.setId(recipe.getUid());
                        name.setText(recipe.getName());
                    } else {
                        Log.d("Almacenamiento", "creando fichero: " + path);
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                        StorageReference ficheroRef = storageRef.child("images/" + recipe.getPicture());
                        ficheroRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess
                                    (FileDownloadTask.TaskSnapshot taskSnapshot) {
                                try {
                                    Log.d("Almacenamiento", "Fichero bajado");
                                    Drawable ph;
                                    ph = Drawable.createFromPath(path);
                                    recipeViewModel.setCurrentRecipeImage(ph);
                                    recipeViewModel.setCurrentRecipe(recipe);
                                    recipeViewModel.setId(recipe.getUid());
                                    name.setText(recipe.getName());
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
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }


            }
        });

    }


    //------------------------------- MQTT ----------------------------------

    private void startThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                //MQTT
                try {
                    Log.i(TAG, "Conectando al broker " + broker);
                    MqttConnectOptions connOpts = new MqttConnectOptions();
                    connOpts.setCleanSession(true);
                    connOpts.setKeepAliveInterval(60);
                    connOpts.setWill(topicRoot + "WillTopic", "App desconectada".getBytes(),
                            qos, false);
                    client = new MqttClient(broker, clientId, new MemoryPersistence());
                    client.connect(connOpts);
                    //client.connect();
                } catch (MqttException e) {
                    Log.e(TAG, "Error al conectar.", e);
                }

                //LEER
                try {
                    client.subscribe(topicRoot + playingRecipe + "/#", qos);
                    client.setCallback(InteractiveRecipeFragment.this);
                    Log.i(TAG, "Suscrito a " + topicRoot + playingRecipe);
                } catch (MqttException e) {
                    Log.e(TAG, "Error al suscribir.", e);
                }

            }
        }).start();
    }


    @Override
    public void onDestroy() {

        try {
            Log.i(TAG, "Desconectado");
            //todo: que no explote
            if (client.isConnected()) {
                client.disconnect();
            }
        } catch (MqttException e) {
            Log.e(TAG, "Error al desconectar.", e);
        } catch (NullPointerException e) {
            Log.e(TAG, "No conectado.");
        }
        super.onDestroy();
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "connectionLost: " + cause.getCause().getMessage());
    }

    @Override
    public void messageArrived(final String topic, MqttMessage message) throws Exception {

        String payload = new String(message.getPayload());


        ObjectMapper mapper = new ObjectMapper();
        Step[] payloadArr = mapper.readValue(payload, Step[].class);
        //String jsonString = mapper.writeValueAsString(steps);
        //Log.d(TAG, "messageArrived: "+(jsonString));

        /*if(data.has("lamsl")){
            Log.d(TAG, "messageArrived: IT CONTAINS");
        }*/

        // 2. convert JSON array to List of objects
        steps = new ArrayList<>(Arrays.asList(payloadArr));


        String jsonString = mapper.writeValueAsString(steps);
        Log.d(TAG, "New values: " + (jsonString));
        //updateSteps(j);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Stuff that updates the UI
                setupInitialContinueViews();//ONLY ONCE
                refresh();


            }
        });
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {

            Log.i(TAG, "deliveryComplete: " + token.getMessage());
        } catch (MqttException e) {
            Log.e(TAG, "Error al deliver.", e);
        }
    }


}