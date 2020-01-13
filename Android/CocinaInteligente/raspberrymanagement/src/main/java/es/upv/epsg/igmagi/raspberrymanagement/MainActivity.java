package es.upv.epsg.igmagi.raspberrymanagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.w3c.dom.Text;

import java.io.File;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import static com.example.igmagi.shared.Mqtt.broker;
import static com.example.igmagi.shared.Mqtt.clientId;
import static com.example.igmagi.shared.Mqtt.listenChannel;
import static com.example.igmagi.shared.Mqtt.playingRecipe;
import static com.example.igmagi.shared.Mqtt.presence;
import static com.example.igmagi.shared.Mqtt.qos;
import static com.example.igmagi.shared.Mqtt.serverId;
import static com.example.igmagi.shared.Mqtt.topicRoot;
import static com.example.igmagi.shared.Mqtt.weight;

/**
 * Skeleton o   f an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MQTT";
    private MqttClient client;
    private SharedPreferences prefs;
    private ArduinoUart uart;
    private FirebaseFirestore mBD = FirebaseFirestore.getInstance();
    private Boolean lights, extrac;
    private TextView tvweight;
    private ImageButton lightsbutton, extractionbutton;
    private Boolean flag = true;
    private Button unlock;
    private WebView webView;
    private Recipe topRecipe;
    private Measures measures = new Measures();

    private String idRecipe = "";

    private ImageView fogon1, fogon2, fogon3, fogon4;
    private boolean isPlaying = false;
    private Recipe recipe;
    private RecipeSingleton recipeSingleton = RecipeSingleton.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = findViewById(R.id.testText);
        lightsbutton = findViewById(R.id.btnLightsOnOff);
        extractionbutton = findViewById(R.id.btnExtraccionOnOff);
        unlock = findViewById(R.id.button3);
        uart = new ArduinoUart("UART0", 9600);
        tvweight = findViewById(R.id.tvweight);
        fogon1 = findViewById(R.id.fogon1);
        fogon2 = findViewById(R.id.fogon2);
        fogon3 = findViewById(R.id.fogon3);
        fogon4 = findViewById(R.id.fogon4);

        unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uart.escribir("J");
                flag = true;
                uart.flush();
            }
        });

        lightsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = false;
                updateBD("lights", !lights);
            }
        });

        extractionbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = false;
                updateBD("fan", !extrac);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                //MQTT
                try {
                    Log.i(TAG, "Conectando al broker " + broker);
                    MqttConnectOptions connOpts = new MqttConnectOptions();
                    connOpts.setCleanSession(true);
                    connOpts.setKeepAliveInterval(60);
                    connOpts.setWill(topicRoot + "WillTopic", "Rasp desconectada".getBytes(),
                            qos, false);
                    client = new MqttClient(broker, UUID.randomUUID().toString(), new MemoryPersistence());
                    client.connect(connOpts);

                    //client.connect();
                } catch (MqttException e) {
                    Log.e(TAG, "Error al conectar.", e);
                }

                //LEER
                try {
                    client.subscribe(topicRoot + listenChannel + "/#", qos);
                    client.setCallback(new MqttCallback() {
                        Boolean flag = true;
                        @Override
                        public void connectionLost(Throwable cause) {
                            Log.d(TAG, "connectionLost: SAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD" + cause);
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            String payload = new String(message.getPayload());
                            Step.numSteps = 0;
                            Log.d(TAG, "messageArrived: " + payload);
                            if (payload.contains("peticion")) {
                                Log.d(TAG, "messageArrived: a");
                                try {
                                    RecipeSingleton recipe = RecipeSingleton.getInstance();
                                    ArrayList<Object> steps= new ArrayList<>();
                                    for (int i = 0; i < recipe.getSteps().size(); i++) {
                                        Step s;
                                        HashMap map = (HashMap) recipe.getSteps().get(i);
                                        if (!map.get("mode").toString().contains("Manual")) {
                                            steps.add(new Step(map.get("mode").toString(), map.get("step").toString(), map.get("trigger").toString()));
                                        } else {
                                            steps.add(new Step(map.get("mode").toString(), map.get("step").toString()));
                                        }
                                    }
                                    ObjectMapper mapper = new ObjectMapper();
                                    String jsonString = mapper.writeValueAsString(steps);
                                    Log.d(TAG, jsonString);
                                    // String jsonString = "[{\"mode\":\"Temperatura\",\"pos\":0,\"status\":3,\"step\":\"paso 1\",\"trigger\":null},{\"mode\":\"Tiempo\",\"pos\":1,\"status\":3,\"step\":\"paso 2\",\"trigger\":\"60\"},{\"mode\":\"Temperatura\",\"pos\":2,\"status\":3,\"step\":\"paso 3\",\"trigger\":\"50\"},{\"mode\":\"Manual\",\"pos\":3,\"status\":3,\"step\":\"paso 4\",\"trigger\":null},{\"mode\":\"Tiempo\",\"pos\":4,\"status\":3,\"step\":\"paso 5\",\"trigger\":\"50\"},{\"mode\":\"Tiempo\",\"pos\":5,\"status\":3,\"step\":\"paso 5\",\"trigger\":\"50\"}]";
                                    MqttMessage m = new MqttMessage(jsonString.getBytes());
                                    m.setQos(qos);
                                    m.setRetained(false);
                                    client.publish(topicRoot + playingRecipe, m);

                                    //Log.d(TAG, "refresh: " + jsonString);
                                } catch (Exception x) {
                                }
                            }else {
                                Log.d(TAG, "messageArrived: b");
                                int pos = Integer.parseInt(payload);
                                Log.d(TAG, "messageArrived: b");
                                RecipeSingleton recipe = RecipeSingleton.getInstance();
                                ArrayList<Object> steps= new ArrayList<>();
                                for (int i = 0; i < recipe.getSteps().size(); i++) {
                                    Step s;
                                    HashMap map = (HashMap) recipe.getSteps().get(i);
                                    if (!map.get("mode").toString().contains("Manual")) {
                                        steps.add(new Step(map.get("mode").toString(), map.get("step").toString(), map.get("trigger").toString()));
                                    } else {
                                        steps.add(new Step(map.get("mode").toString(), map.get("step").toString()));
                                    }
                                }
                                ((Step)steps.get(pos)).setStatus(((Step)steps.get(pos)).getStatus()+1);
                                recipe.setSteps(steps);
                                Log.d(TAG, "messageArrived: b");
                                ObjectMapper mapper = new ObjectMapper();
                                Log.d(TAG, "messageArrived: b");
                                String jsonString = mapper.writeValueAsString(steps);
                                Log.d(TAG, "messageArrived: b");
                                Log.d(TAG, jsonString);
                                // String jsonString = "[{\"mode\":\"Temperatura\",\"pos\":0,\"status\":3,\"step\":\"paso 1\",\"trigger\":null},{\"mode\":\"Tiempo\",\"pos\":1,\"status\":3,\"step\":\"paso 2\",\"trigger\":\"60\"},{\"mode\":\"Temperatura\",\"pos\":2,\"status\":3,\"step\":\"paso 3\",\"trigger\":\"50\"},{\"mode\":\"Manual\",\"pos\":3,\"status\":3,\"step\":\"paso 4\",\"trigger\":null},{\"mode\":\"Tiempo\",\"pos\":4,\"status\":3,\"step\":\"paso 5\",\"trigger\":\"50\"},{\"mode\":\"Tiempo\",\"pos\":5,\"status\":3,\"step\":\"paso 5\",\"trigger\":\"50\"}]";
                                MqttMessage m = new MqttMessage(jsonString.getBytes());
                                m.setQos(qos);
                                m.setRetained(false);
                                client.publish(topicRoot + playingRecipe, m);

                            }
                        }
/*

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            String payload = new String(message.getPayload());

                            Toast.makeText(getApplication(), "topic : " + payload, Toast.LENGTH_SHORT).show();
                            final ArrayList<Step> steps = new ArrayList<>();
                            if (payload.contains("peticion")) {

                                if (isPlaying) {
                                    mBD.collection("recipes").document(idRecipe).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                recipe = document.toObject(Recipe.class);
                                                recipe.setUid(document.getId());
                                                recipeViewModel.setCurrentRecipe(recipe);
                                                recipeViewModel.setId(recipe.getUid());

                                                for (int i = 0; i < recipe.getSteps().size(); i++) {
                                                    Step s;
                                                    HashMap map = (HashMap) recipe.getSteps().get(i);
                                                    if (!map.get("mode").toString().contains("Manual")) {
                                                        steps.add(new Step(map.get("mode").toString(), map.get("step").toString(), map.get("trigger").toString()));
                                                    } else {
                                                        steps.add(new Step(map.get("mode").toString(), map.get("step").toString()));
                                                    }
                                                }
                                                ObjectMapper mapper = new ObjectMapper();
                                                try {
                                                    String jsonString = mapper.writeValueAsString(steps);
                                                    MqttMessage m = new MqttMessage(jsonString.getBytes());
                                                    m.setQos(qos);
                                                    m.setRetained(false);
                                                    client.publish(topicRoot + playingRecipe, m);

                                                    //Log.d(TAG, "refresh: " + jsonString);
                                                } catch (Exception x) {
                                                }

                                                isPlaying = false;

                                            } else {
                                                Log.d(TAG, "Error getting documents: ", task.getException());
                                            }


                                        }
                                    });
                                }
                            } else {
                                Log.d(TAG, "MERDAPUTA");
                                ObjectMapper mapper = new ObjectMapper();
                                try {
                                    String jsonString = mapper.writeValueAsString(steps);
                                    MqttMessage m = new MqttMessage(jsonString.getBytes());
                                    m.setQos(qos);
                                    m.setRetained(false);
                                    client.publish(topicRoot + playingRecipe, m);

                                    //Log.d(TAG, "refresh: " + jsonString);
                                } catch (Exception x) {
                                }

                            }
                        }

 */

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {
                            Log.d(TAG, "Completed bro ");
                        }
                    });
                } catch (Exception r) {
                }

            }

        }).start();
        /*try {
            client.subscribe(topicRoot + listenChannel + "/#", qos);
            Toast.makeText(getApplication(), "Suscrito: " + topicRoot + listenChannel + "/#", Toast.LENGTH_SHORT).show();
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Toast.makeText(getApplication(), "CAUSA: " + cause, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "connectionLost: SAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD"+ cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload = new String(message.getPayload());

                    Toast.makeText(getApplication(), "topic : " + payload, Toast.LENGTH_SHORT).show();
                    final ArrayList<Step> steps = new ArrayList<>();
                    if (payload.contains("peticion")) {

                        if (true) {
                            mBD.collection("recipes").document(idRecipe).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        recipe = document.toObject(Recipe.class);
                                        recipe.setUid(document.getId());
                                        recipeViewModel.setCurrentRecipe(recipe);
                                        recipeViewModel.setId(recipe.getUid());

                                        for (int i = 0; i < recipe.getSteps().size(); i++) {
                                            Step s;
                                            HashMap map = (HashMap) recipe.getSteps().get(i);
                                            if (!map.get("mode").toString().contains("Manual")) {
                                                steps.add(new Step(map.get("mode").toString(), map.get("step").toString(), map.get("trigger").toString()));
                                            } else {
                                                steps.add(new Step(map.get("mode").toString(), map.get("step").toString()));
                                            }
                                        }
                                        ObjectMapper mapper = new ObjectMapper();
                                        try {
                                            String jsonString = "[{fdsh:sfd}]";
                                            MqttMessage m = new MqttMessage(jsonString.getBytes());
                                            m.setQos(qos);
                                            m.setRetained(false);
                                            client.publish(topicRoot + playingRecipe, m);

                                            //Log.d(TAG, "refresh: " + jsonString);
                                        } catch (Exception x) {
                                        }

                                        isPlaying = false;

                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }


                                }
                            });
                        }
                    } else {
                        Log.d(TAG, "MERDAPUTA");
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            String jsonString = mapper.writeValueAsString(steps);
                            MqttMessage m = new MqttMessage(jsonString.getBytes());
                            m.setQos(qos);
                            m.setRetained(false);
                            client.publish(topicRoot + playingRecipe, m);

                            //Log.d(TAG, "refresh: " + jsonString);
                        } catch (Exception x) {
                        }

                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Toast.makeText(getApplication(), "token: " + token, Toast.LENGTH_SHORT).show();
                    try {
                        Log.d(TAG, "Completed bro " + token.getMessage().toString());
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            });
            Log.i(TAG, "Suscrito a " + topicRoot + weight);
        } catch (MqttException e) {
            Log.e(TAG, "Error al suscribir.", e);
        }


/*

        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    List<Integer> temps = new ArrayList<Integer>();
                    try {
                        if (flag) {
                            String cadena = uart.leer();
                            TimeUnit.SECONDS.sleep(1);
                            //cadena = cadena + ",0";
                            //cadena.concat(",0");
                            Log.d("TAG_1", cadena);
                            String[] cadenas = cadena.split(",");
                            //LIGHTS + PRESENCE
                            if (cadenas[0].contains("0")) {
                                Log.d("TAG", "luces off");
                                updateBD("lights", false);
                                measures.setLight(false);
                            } else if (cadenas[0].contains("1")) {
                                Log.d("TAG", "luces on");
                                updateBD("lights", true);
                                measures.setLight(true);
                            }
                            //FAN ON OFF
                            if (cadenas[1].contains("1")) {
                                Log.d("TAG", "extractor on");
                                updateBD("fan", true);
                                measures.setFan(true);
                            } else if (cadenas[1].contains("0")) {
                                Log.d("TAG", "extractor off");
                                updateBD("fan", false);
                                measures.setLight(false);
                            }

                            //TEMPERATURES
                            Log.d("TAG", "TemperaturaMaxima: " + cadenas[2]);
                            Log.d("TAG", "Temperatura1: " + cadenas[3]);
                            Log.d("TAG", "Temperatura2: " + cadenas[4]);
                            Log.d("TAG", "Temperatura3: " + cadenas[5]);
                            Log.d("TAG", "Temperatura4: " + cadenas[6]);

                            temps.clear();

                            temps.add(Integer.parseInt(cadenas[3]));
                            temps.add(Integer.parseInt(cadenas[4]));
                            temps.add(Integer.parseInt(cadenas[5]));
                            temps.add(Integer.parseInt(cadenas[6]));

                            //Log.d("TAG_DINS ARRRAy", ""+temps.get(0));

                            updateTemperaturesBD(temps);

                            measures.setMaxTemp(Integer.parseInt(cadenas[2]));
                            measures.setTemp1(Integer.parseInt(cadenas[3]));
                            measures.setTemp1(Integer.parseInt(cadenas[4]));
                            measures.setTemp1(Integer.parseInt(cadenas[5]));
                            measures.setTemp1(Integer.parseInt(cadenas[6]));

                            //FUGA ON OFF
                            if (cadenas[7].contains("1")) {
                                Log.d("TAG", "FUGA");
                                updateBD("leak", true);
                                measures.setLeak(true);
                            } else if (cadenas[7].contains("0")) {
                                Log.d("TAG", "FUGA off");
                                updateBD("leak", false);
                                measures.setLeak(false);
                            }

                            updateWeight(Integer.parseInt(cadenas[8]));

                            measures.setWeight(Integer.parseInt(cadenas[8]));

                        }
                        //String numero= (cadenas[2].split(":"))[1].replace("\"", "0");
                        //Log.d("TAG",numero);
                        //Float temperatura = numero;
                        //Log.d("TAG", "TEMPERATURA: " + temperatura);
                        //if (temperatura < 60) {
                        //   Log.d("TAG", "extractor off " + temperatura);
                        //} else {
                        //    Log.d("TAG", "extractor on " + temperatura);
                        //}
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


 */
        prefs =

                getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        tv.setText("Welcome " + prefs.getString("name", "null"));

        Button btn = findViewById(R.id.button2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getApplicationContext().getSharedPreferences("Preferences", 0).edit().clear().apply();
                startActivity(new Intent(getApplication(), LoginActivity.class));
            }
        });

        refreshView();

    }

    private void updateBD(String field, Boolean value) {
        Map<String, Object> map = new HashMap<>();
        map.put(field, value);
        mBD.collection("devices").document("conet_kitchen").update(map);
    }

    private void updateWeight(int value) {
        try {
            MqttMessage message = new MqttMessage((value + "").getBytes());
            message.setQos(qos);
            message.setRetained(false);
            client.publish(topicRoot + listenChannel, message);
        } catch (MqttException e) {
            Log.e(TAG, "Error al publicar.", e);
        }
        Snackbar.make(getCurrentFocus(), "Publicando en MQTT", Snackbar.LENGTH_LONG);
        //Map<String, Object> map = new HashMap<>();
        //map.put("weight", value);
        //mBD.collection("devices").document("conet_kitchen").update(map);
    }

    private void updateTemperaturesBD(List<Integer> t) {
        Map<String, Object> map = new HashMap<>();
        map.put("cooktop", t);
        mBD.collection("devices").document("conet_kitchen").update(map);
    }

    private void refreshView() {
        mBD.collection("recipes").whereEqualTo("name", "Pechuga").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        topRecipe = document.toObject(Recipe.class);
                        topRecipe.setUid(document.getId());
                        ((TextView) findViewById(R.id.name)).setText(topRecipe.getName().toUpperCase());
                        /*
                        File localFile = null;
                        localFile = new File(getApplication().getCacheDir().toString().concat("/" + document.getId().concat(".jpg")));
                        final String path = localFile.getAbsolutePath();
                        if (localFile.exists()) {
                            ((TextView) findViewById(R.id.topRecipe)).setBackground(Drawable.createFromPath(path));
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
                                        ((TextView) findViewById(R.id.topRecipe)).setBackground(Drawable.createFromPath(path));
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

                         */
                        ((RatingBar) findViewById(R.id.topRating)).setRating(topRecipe.getRatingValue());
                        ((TextView) findViewById(R.id.topDuration)).setText(topRecipe.getFormattedDuration());
                        if (topRecipe.getExtra().get("veggie"))
                            ((ImageView) findViewById(R.id.topVeggieIcon)).setVisibility(View.VISIBLE);
                        if (topRecipe.getExtra().get("vegan"))
                            ((ImageView) findViewById(R.id.topVeganIcon)).setVisibility(View.VISIBLE);
                        if (topRecipe.getExtra().get("dairy"))
                            ((ImageView) findViewById(R.id.topDairyIcon)).setVisibility(View.VISIBLE);
                        if (topRecipe.getExtra().get("gluten"))
                            ((ImageView) findViewById(R.id.topGlutenIcon)).setVisibility(View.VISIBLE);

                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }

            }
        });

        mBD.collection("devices").document("conet_kitchen").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Map<String, Object> map = (Map<String, Object>) documentSnapshot.get("playingRecipe");
                isPlaying = (Boolean) map.get("isPlaying");

                idRecipe = (String) map.get("id");
                mBD.collection("recipes").document(idRecipe).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            recipe = document.toObject(Recipe.class);
                            recipe.setUid(document.getId());
                            recipeSingleton.setUpEverything(recipe);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                    }
                });

                tvweight.setText(documentSnapshot.getLong("weight") + " gr.");
                ArrayList<Long> cooktop = (ArrayList<Long>) documentSnapshot.get("cooktop");
                if (cooktop.get(0) >= 60) fogon1.setImageResource(R.drawable.vitroon);
                else fogon1.setImageResource(R.drawable.vitrooff);
                if (cooktop.get(1) >= 60) fogon2.setImageResource(R.drawable.vitroon);
                else fogon2.setImageResource(R.drawable.vitrooff);
                if (cooktop.get(2) >= 60) fogon3.setImageResource(R.drawable.vitroon);
                else fogon3.setImageResource(R.drawable.vitrooff);
                if (cooktop.get(3) >= 60) fogon4.setImageResource(R.drawable.vitroon);
                else fogon4.setImageResource(R.drawable.vitrooff);
                if (lights = documentSnapshot.getBoolean("lights")) {
                    lightsbutton.setImageResource(R.drawable.btnluzon);
                    if (!flag) {
                        uart.escribir("O");
                        Log.d("ASDASDASD", "LEIDOS: " + uart.leer());
                    }
                } else {
                    lightsbutton.setImageResource(R.drawable.btnluzoff);
                    if (!flag) {
                        uart.escribir("C");
                        Log.d("ASDASDASD", "LEIDOS: " + uart.leer());
                    }
                }
                if (extrac = documentSnapshot.getBoolean("fan")) {
                    extractionbutton.setImageResource(R.drawable.btnextraon);
                    if (!flag) {
                        uart.escribir("F");
                        Log.d("ASDASDASD", "LEIDOS: " + uart.leer());
                    }
                } else {
                    extractionbutton.setImageResource(R.drawable.btnextraoff);
                    if (!flag) {
                        uart.escribir("N");
                        Log.d("ASDASDASD", "LEIDOS: " + uart.leer());
                    }
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        try {
            Log.i(TAG, "Desconectado");
            if (client.isConnected()) client.disconnect();
        } catch (MqttException e) {
            Log.e(TAG, "Error al desconectar.", e);
        }
        super.onDestroy();
        uart.cerrar();
    }

}
