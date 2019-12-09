package es.upv.epsg.igmagi.cocinainteligente.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.eclipse.paho.client.mqttv3.MqttClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import es.upv.epsg.igmagi.cocinainteligente.R;

//public class MyKitchenFragment extends Fragment implements org.eclipse.paho.client.mqttv3.MqttCallback{
public class MyKitchenFragment extends Fragment{
    private MqttClient client;
    private static final String TAG = "KitchenFragment";

    boolean lights = false;
    boolean extrac = false;
    List<Integer> temperatures;
    //int weight;
    ImageButton lightsbutton;
    ImageButton extractionbutton;
    TextView txtlightswitch, txtextracswitch, txttemp1,txttemp2,txttemp3,txttemp4, txtalert, txtweight, txtleak;
    ImageButton temp1, temp2, temp3, temp4;
    int triggerTemperature = 60;

    //For mqtt
    private String activeKitchen;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        temperatures = new ArrayList<Integer>();
        View root = inflater.inflate(R.layout.fragment_kitchen, container, false);

        temp1 = (ImageButton) root.findViewById(R.id.temp1);
        temp2 = (ImageButton) root.findViewById(R.id.temp2);
        temp3 = (ImageButton) root.findViewById(R.id.temp3);
        temp4 = (ImageButton) root.findViewById(R.id.temp4);

        txttemp1 = root.findViewById(R.id.t1);
        txttemp2 = root.findViewById(R.id.t2);
        txttemp3 = root.findViewById(R.id.t3);
        txttemp4 = root.findViewById(R.id.t4);

        txtalert = root.findViewById(R.id.alert);

        txtweight = root.findViewById(R.id.tvweight);
        //txtleak = root.findViewById(R.id.tvleak);

        lightsbutton = (ImageButton) root.findViewById(R.id.btnLuzOnOff);
        lightsbutton.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                updateBD("lights", !lights);
            }
        });
        extractionbutton = (ImageButton) root.findViewById(R.id.btnExtraccionOnOff);
        extractionbutton.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                updateBD("fan", !extrac);
            }
        });
        txtextracswitch = (TextView) root.findViewById(R.id.tvExtraccionOnOff);
        txtlightswitch = (TextView) root.findViewById(R.id.tvLuzOnOff);

        //MQTT
        /*
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


        //ESCRIBIR
        try {
            Log.i(TAG, "Publicando mensaje: " + "hola");
            MqttMessage message = new MqttMessage("true".getBytes());
            message.setQos(qos);
            message.setRetained(false);
            client.publish(topicRoot+enUso, message);
        } catch (MqttException e) {
            Log.e(TAG, "Error al publicar.", e);
        }*/
        //CONEXION
       /* try{
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(60);
            connOpts.setWill(topicRoot+"WillTopic", "App desconectada".getBytes(),
                    qos, false);
            client.connect(connOpts);

        }catch(MqttException e) {
            Log.e(TAG, "Error al conectarESTE.", e);
        }*/
        //LEER
        /*try {
            Log.i(TAG, "Suscrito a " + topicRoot+enUso);
            client.subscribe(topicRoot+enUso, qos);
            client.setCallback(this);
        } catch (MqttException e) {
            Log.e(TAG, "Error al suscribir.", e);
        }


         */
        refreshView();

        // Inflate the layout for this fragment
        return root;
    }

    FirebaseFirestore mBD = FirebaseFirestore.getInstance();

    private void updateBD(String field, Boolean value){
        Map<String, Object> map = new HashMap<>();
        map.put(field, value);
        mBD.collection("devices").document("conet_kitchen").update(map);
    }

    private void refreshView() {
        mBD.collection("devices").document("conet_kitchen").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (lights = documentSnapshot.getBoolean("lights")){
                    lightsbutton.setImageResource(R.drawable.btnluzon);
                    txtlightswitch.setText("ON");
                    //mqtt.setVisibility(View.VISIBLE);

                }
                else {
                    lightsbutton.setImageResource(R.drawable.btnluzoff);
                    txtlightswitch.setText("OFF");
                    //mqtt.setVisibility(View.GONE);

                }
                if (extrac = documentSnapshot.getBoolean("fan")) {
                    extractionbutton.setImageResource(R.drawable.btnextraon);
                    txtextracswitch.setText("ON");
                }
                else {
                    extractionbutton.setImageResource(R.drawable.btnextraoff);
                    txtextracswitch.setText("OFF");
                }
                if (documentSnapshot.getBoolean("leak")){

                    txtalert.setVisibility(View.VISIBLE);

                }
                else {
                    txtalert.setVisibility(View.GONE);
                }

                temperatures = (List<Integer>) documentSnapshot.get("cooktop");
                Log.d("AA", temperatures.toString());
                //Log.d("AA", String.valueOf(Integer.parseInt(String.valueOf(temperatures.get(0)))));

                txttemp1.setText(String.valueOf(temperatures.get(0)+"ºC"));
                txttemp2.setText(String.valueOf(temperatures.get(1)+"ºC"));
                txttemp3.setText(String.valueOf(temperatures.get(2)+"ºC"));
                txttemp4.setText(String.valueOf(temperatures.get(3)+"ºC"));

                if(documentSnapshot.getLong("weight") < 0){
                    txtweight.setText("0g");
                }else{

                    txtweight.setText(documentSnapshot.getLong("weight").toString()+"g");
                }

                if(Integer.parseInt(String.valueOf(temperatures.get(0))) > triggerTemperature){
                    temp1.setImageResource(R.drawable.vitroon);
                    txttemp1.setTypeface(null, Typeface.BOLD);
                }else{
                    temp1.setImageResource(R.drawable.vitrooff);
                    txttemp1.setTypeface(null, Typeface.NORMAL);

                }
                if(Integer.parseInt(String.valueOf(temperatures.get(1))) > triggerTemperature){
                    temp2.setImageResource(R.drawable.vitroon);
                    txttemp2.setTypeface(null, Typeface.BOLD);

                }else{
                    temp2.setImageResource(R.drawable.vitrooff);
                    txttemp2.setTypeface(null, Typeface.NORMAL);
                }
                if(Integer.parseInt(String.valueOf(temperatures.get(2))) > triggerTemperature){
                    temp3.setImageResource(R.drawable.vitroon);
                    txttemp3.setTypeface(null, Typeface.BOLD);

                }else{
                    temp3.setImageResource(R.drawable.vitrooff);
                    txttemp3.setTypeface(null, Typeface.NORMAL);

                }
                if(Integer.parseInt(String.valueOf(temperatures.get(3))) > triggerTemperature){
                    temp4.setImageResource(R.drawable.vitroon);
                    txttemp4.setTypeface(null, Typeface.BOLD);

                }else{
                    temp4.setImageResource(R.drawable.vitrooff);
                    txttemp4.setTypeface(null, Typeface.NORMAL);

                }



            }
        });
    }
/*
    @Override public void onDestroy() {
        try {
            Log.i(TAG, "Desconectado");
            if(client.isConnected()){
                client.disconnect();
            }
        } catch (MqttException e) {
            Log.e(TAG, "Error al desconectar.", e);
        }
        super.onDestroy();
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "connectionLost: ");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        if(payload=="true"){
            mqtt.setVisibility(View.VISIBLE);
        }else{
            mqtt.setVisibility(View.GONE);
        }
        Log.d(TAG, "messageArrived:"+payload);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {

            Log.i(TAG, "deliveryComplete: "+token.getMessage());
        }catch(MqttException e) {
            Log.e(TAG, "Error al deliver.", e);
        }
    }

 */
}
