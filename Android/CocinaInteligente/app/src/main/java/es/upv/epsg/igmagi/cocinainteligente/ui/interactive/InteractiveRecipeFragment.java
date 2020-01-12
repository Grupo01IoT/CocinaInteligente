package es.upv.epsg.igmagi.cocinainteligente.ui.interactive;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.sql.Time;
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
import es.upv.epsg.igmagi.cocinainteligente.ui.MyKitchenFragment;

import static com.example.igmagi.shared.Mqtt.broker;
import static com.example.igmagi.shared.Mqtt.clientId;
import static com.example.igmagi.shared.Mqtt.playingRecipe;
import static com.example.igmagi.shared.Mqtt.qos;
import static com.example.igmagi.shared.Mqtt.topicRoot;
import static com.example.igmagi.shared.Mqtt.weight;
import com.google.gson.Gson;

public class InteractiveRecipeFragment extends Fragment implements org.eclipse.paho.client.mqttv3.MqttCallback{

    private MqttClient client;
    String TAG = "InteractiveRecipeFragment";

    // GENERAL STUFF MAYBE USELESS
    private FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore mDB = FirebaseFirestore.getInstance();

    //LAYOUT ELEMENTS
    View root;
    private SlideshowViewModel slideshowViewModel;
    private TextView name;
    private ImageButton stop;
    UserViewModel userViewModel;
    RecipeViewModel recipeViewModel;
    User user;
    Recipe recipe;
    ViewFlipper viewFlipper;
    LinearLayout stepsContainer;


    //DATA ELEMENTS
    ArrayList<Step> steps;
    ArrayList<Integer> stepsToDO;
    ArrayList<Integer> stepsDone;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_interactive_recipe_dialog, container, false);


        //Layout elements
        name = root.findViewById(R.id.intRecipeName);
        stop = root.findViewById(R.id.intStopBtn);
        stepsContainer = (LinearLayout)root.findViewById(R.id.stepsContainer);


        //ViewModels
        userViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        recipeViewModel = ViewModelProviders.of(getActivity()).get(RecipeViewModel.class);
        user = userViewModel.getCurrentUser();
        recipe = recipeViewModel.getCurrentRecipe();


        steps = new ArrayList();
        stepsToDO = new ArrayList();
        stepsDone = new ArrayList();

        // TODO: See if there is any recipe playing
        //Check if the user has already paired the device
        SharedPreferences pref2 = getActivity().getPreferences(Context.MODE_PRIVATE);
        String deviceId = pref2.getString("device_id", "empty");

        if(!deviceId.equals("empty")) {
            mDB.collection("devices").document(deviceId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> mapped = document.getData();

                            //Log.d(TAG, "onComplete: "+(mapped.containsKey("playingRecipe")));

                            if(mapped.containsKey("playingRecipe") ){
                                if((Boolean)((Map)mapped.get("playingRecipe")).get("isPlaying")){
                                    Log.d(TAG, "Se está playeando una receta");
                                    continueInteractiveRecipe((String)((Map)mapped.get("playingRecipe")).get("id"));

                                }else{
                                    Log.d(TAG, "No se está playeando nada");
                                    startInteractiveRecipe();
                                }
                            }else{
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
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();

    }
    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();

        //Reset the Steps "ID"
        //TODO: if the recipe has been stopped -> Step.numSteps = 0;
        Step.numSteps = 0;
    }

    //Functions
    //-------------    MAIN FUNCTIONS: ---------------
    private void startInteractiveRecipe(){
        Log.d(TAG, "startInteractiveSetup: SETUP");
        setupInitialSteps();
        setupInitialViews();
        updateDBRecipePlaying();
        //TODO: Notification for devices paired with the kitchen (?)

        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(steps);
            Log.d(TAG, "messageArrived: " + (jsonString));
            retrievePlayingRecipeInfo();
        }catch (Exception e){
            Log.d(TAG, "startInteractiveRecipe: lmao");
        }


    }
    private void continueInteractiveRecipe(String playingRecipeId) {
        //If the recipe that is trying to be continued is the one already playing, it will continue playing without asking

        if(!recipe.getUid().equals(playingRecipeId)){
            modalStopCurrentStartNew();//if true -> startInteractiveRecipe(), if false -> retrieve info from current recipe
        }else{
            Log.d(TAG, "startInteractiveSetup: CONTINUE WITH RECIPE");
            name.setText(recipe.getName());

        }

        retrievePlayingRecipeInfo();
    }


    //-----------------   SETUP FUNCTIONS   -------------------
    private void setupInitialSteps(){
        for (int i = 0; i < recipe.getSteps().size(); i++) {
            Step s;
            HashMap map = (HashMap) recipe.getSteps().get(i);
            if(!map.get("mode").toString().contains("Manual")){

                steps.add(new Step( map.get("mode").toString(),map.get("step").toString(), map.get("trigger").toString()));
            }else{
                steps.add(new Step( map.get("mode").toString(),map.get("step").toString()));
            }
            if(!map.get("mode").toString().equals("Manual"))
            steps.add(new Step( map.get("mode").toString(),map.get("step").toString(), map.get("trigger").toString()));
            else
                steps.add(new Step( map.get("mode").toString(),map.get("step").toString()));
            stepsToDO.add(steps.get(i).getPos());
        }
    }
    private void setupInitialViews(){
        // DONE: FILL SETUP LAYOUT
        name.setText(recipe.getName());
        LayoutInflater li = LayoutInflater.from(getContext());

        for(Step s: steps){
            View view = li.inflate(R.layout.fragment_interactive_step, null);
            ((TextView)view.findViewById(R.id.intStepText)).setText(s.getStep());
            ((LinearLayout)root.findViewById(R.id.stepsContainer)).addView(view);
        }
    }

    private void updateDBRecipePlaying(){
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
    private void modalStopCurrentStartNew(){

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

                        retrievePlayingRecipeInfo();
                        dialog.cancel();
                    }
                });
        AlertDialog d = alert.create();
        d.setTitle(R.string.alert_modal);
        d.show();

    }

    //-----------------   SHARED FUNCTIONS   ---------------------
    private void refresh(){
        LayoutInflater li = LayoutInflater.from(getContext());

        for(Step s: steps){
            View view = li.inflate(R.layout.fragment_interactive_step, null);
            ((TextView)view.findViewById(R.id.intStepText)).setText(s.getStep());
            stepsContainer.addView(view);
        }
    }

    private void retrievePlayingRecipeInfo(){

        Log.d(TAG, "RETRIEVING DATA...");
        startThread();


    }
    private void updateFromApp(String s){
        try {
            MqttMessage message = new MqttMessage((s + "").getBytes());
            message.setQos(qos);
            message.setRetained(false);
            client.publish(topicRoot + weight, message);
        } catch (MqttException e) {
            Log.e(TAG, "Error al publicar.", e);
        }
    }













    //------------------------------- MQTT ----------------------------------

    private void startThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {

                //MQTT
                try {
                    Log.i(TAG, "Conectando al broker " + broker);
                    MqttConnectOptions connOpts = new MqttConnectOptions();
                    connOpts.setCleanSession(true);
                    connOpts.setKeepAliveInterval(60);
                    connOpts.setWill(topicRoot+"WillTopic", "App desconectada".getBytes(),
                            qos, false);
                    client = new MqttClient(broker, clientId, new MemoryPersistence());
                    client.connect(connOpts);
                    //client.connect();
                } catch (MqttException e) {
                    Log.e(TAG, "Error al conectar.", e);
                }

                //LEER
                try {
                    client.subscribe(topicRoot+playingRecipe+"/#", qos);
                    client.setCallback(InteractiveRecipeFragment.this);
                    Log.i(TAG, "Suscrito a " + topicRoot+playingRecipe);
                } catch (MqttException e) {
                    Log.e(TAG, "Error al suscribir.", e);
                }

            }
        }).start();
    }


    @Override public void onDestroy() {

        try {
            Log.i(TAG, "Desconectado");
            //todo: que no explote
            if(client.isConnected()){
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
        Log.d(TAG, "connectionLost: "+cause.getCause().getMessage());
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
        Log.d(TAG, "messageArrived: "+(jsonString));

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Stuff that updates the UI

                refresh();


            }
        });
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {

            Log.i(TAG, "deliveryComplete: "+token.getMessage());
        }catch(MqttException e) {
            Log.e(TAG, "Error al deliver.", e);
        }
    }

}