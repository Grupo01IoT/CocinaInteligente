package es.upv.epsg.igmagi.raspberrymanagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import static com.example.igmagi.shared.Mqtt.broker;
import static com.example.igmagi.shared.Mqtt.clientId;
import static com.example.igmagi.shared.Mqtt.enUso;
import static com.example.igmagi.shared.Mqtt.qos;
import static com.example.igmagi.shared.Mqtt.topicRoot;

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
    private MqttClient client;
    private SharedPreferences prefs;
    private ArduinoUart uart;
    private FirebaseFirestore mBD = FirebaseFirestore.getInstance();
    private Boolean lights, extrac;
    private ImageButton lightsbutton, extractionbutton;
    private Boolean flag = true;
    private Button unlock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //MQTT

        try {
            Log.i("EE", "Conectando al broker " + broker);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(60);
            connOpts.setWill(topicRoot+"WillTopic", "App desconectada".getBytes(),
                    qos, false);
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            client.connect(connOpts);
            //client.connect();
        } catch (MqttException e) {
            Log.e("EE", "Error al conectar.", e);
        }




        TextView tv = findViewById(R.id.testText);
        lightsbutton = findViewById(R.id.btnLightsOnOff);
        extractionbutton = findViewById(R.id.btnExtraccionOnOff);
        unlock = findViewById(R.id.button3);
        uart = new ArduinoUart("UART0", 9600);

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
                                //ESCRIBIR
                                try {
                                    Log.i("ESCRI", "Publicando mensaje: " + "FALSE");
                                    MqttMessage message = new MqttMessage("false".getBytes());
                                    message.setQos(qos);
                                    message.setRetained(false);
                                    client.publish(topicRoot+enUso, message);
                                } catch (MqttException e) {
                                    Log.e("ESCRI", "Error al publicar.", e);
                                }
                            } else if (cadenas[0].contains("1")) {
                                Log.d("TAG", "luces on");
                                updateBD("lights", true);
                                try {
                                    Log.i("ESCRI", "Publicando mensaje: " + "TRUE");
                                    MqttMessage message = new MqttMessage("true".getBytes());
                                    message.setQos(qos);
                                    message.setRetained(false);
                                    client.publish(topicRoot+enUso, message);
                                } catch (MqttException e) {
                                    Log.e("ESCRI", "Error al publicar.", e);
                                }
                            }
                            //FAN ON OFF
                            if (cadenas[1].contains("1")) {
                                Log.d("TAG", "extractor on");
                                updateBD("fan", true);
                            } else if (cadenas[1].contains("0")) {
                                Log.d("TAG", "extractor off");
                                updateBD("fan", false);
                            }

                            //TEMPERATURES
                            Log.d("TAG", "TemperaturaMaxima: "+cadenas[2]);
                            Log.d("TAG", "Temperatura1: "+cadenas[3]);
                            Log.d("TAG", "Temperatura2: "+cadenas[4]);
                            Log.d("TAG", "Temperatura3: "+cadenas[5]);
                            Log.d("TAG", "Temperatura4: "+cadenas[6]);

                            temps.clear();

                            temps.add(Integer.parseInt(cadenas[3]));
                            temps.add(Integer.parseInt(cadenas[4]));
                            temps.add(Integer.parseInt(cadenas[5]));
                            temps.add(Integer.parseInt(cadenas[6]));

                            //Log.d("TAG_DINS ARRRAy", ""+temps.get(0));

                            updateTemperaturesBD(temps);

                            //FUGA ON OFF
                            if (cadenas[7].contains("1")) {
                                Log.d("TAG", "FUGA");
                                updateBD("leak", true);
                            } else if (cadenas[7].contains("0")) {
                                Log.d("TAG", "FUGA off");
                                updateBD("leak", false);
                            }

                            updateWeightBD(Integer.parseInt(cadenas[8]));

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

    private void updateWeightBD(int value) {
        Map<String, Object> map = new HashMap<>();
        map.put("weight", value);
        mBD.collection("devices").document("conet_kitchen").update(map);
    }

    private void updateTemperaturesBD(List<Integer> t){
        Map<String, Object> map = new HashMap<>();
        map.put("cooktop", t);
        mBD.collection("devices").document("conet_kitchen").update(map);
    }
    private void refreshView() {
        mBD.collection("devices").document("conet_kitchen").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (lights = documentSnapshot.getBoolean("lights")) {
                    lightsbutton.setImageResource(R.drawable.btnluzon);
                    if (!flag) {uart.escribir("O");
                    Log.d("ASDASDASD", "LEIDOS: " + uart.leer());}
                } else {
                    lightsbutton.setImageResource(R.drawable.btnluzoff);
                    if (!flag) {uart.escribir("C");
                    Log.d("ASDASDASD", "LEIDOS: " + uart.leer());}
                }
                if (extrac = documentSnapshot.getBoolean("fan")) {
                    extractionbutton.setImageResource(R.drawable.btnextraon);
                    if (!flag){ uart.escribir("F");
                Log.d("ASDASDASD", "LEIDOS: " + uart.leer());}
                } else {
                    extractionbutton.setImageResource(R.drawable.btnextraoff);
                    if (!flag){ uart.escribir("N");
            Log.d("ASDASDASD", "LEIDOS: " + uart.leer());}
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        uart.cerrar();
    }

}
