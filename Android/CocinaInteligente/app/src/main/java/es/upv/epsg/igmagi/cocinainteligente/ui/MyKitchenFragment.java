package es.upv.epsg.igmagi.cocinainteligente.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import es.upv.epsg.igmagi.cocinainteligente.R;

import static com.example.igmagi.shared.Mqtt.*;

public class MyKitchenFragment extends Fragment implements org.eclipse.paho.client.mqttv3.MqttCallback{
    private MqttClient client;
    private static final String TAG = "KitchenFragment";

    boolean lights = false;
    boolean extrac = false;
    List<Integer> temperatures;
    //int weight;
    ImageButton lightsbutton;
    ImageButton extractionbutton;
    TextView txtlightswitch, txtextracswitch, txttemp1,txttemp2,txttemp3,txttemp4, txtalert, txtweight;
    LinearLayout temp1, temp2, temp3, temp4;
    CardView cvleak;
    int triggerTemperature = 60;

    //For mqtt
    private String activeKitchen;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        temperatures = new ArrayList<Integer>();
        View root = inflater.inflate(R.layout.fragment_kitchen, container, false);

        temp1 =  root.findViewById(R.id.ltemp1);
        temp2 =  root.findViewById(R.id.ltemp2);
        temp3 =  root.findViewById(R.id.ltemp3);
        temp4 =  root.findViewById(R.id.ltemp4);

        txttemp1 = root.findViewById(R.id.t1);
        txttemp2 = root.findViewById(R.id.t2);
        txttemp3 = root.findViewById(R.id.t3);
        txttemp4 = root.findViewById(R.id.t4);

        txtalert = root.findViewById(R.id.alert);
        cvleak = root.findViewById(R.id.cardViewLeak);
        txtweight = root.findViewById(R.id.tvweight);


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
                    client.subscribe(topicRoot+weight+"/#", qos);
                    client.setCallback(MyKitchenFragment.this);
                    Log.i(TAG, "Suscrito a " + topicRoot+weight);
                } catch (MqttException e) {
                    Log.e(TAG, "Error al suscribir.", e);
                }

            }
        }).start();

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
                    lightsbutton.setImageResource(R.drawable.lighton);
                    txtlightswitch.setText("ON");
                    //mqtt.setVisibility(View.VISIBLE);

                }
                else {
                    lightsbutton.setImageResource(R.drawable.lightoff);
                    txtlightswitch.setText("OFF");
                    //mqtt.setVisibility(View.GONE);

                }
                if (extrac = documentSnapshot.getBoolean("fan")) {
                    extractionbutton.setImageResource(R.drawable.fanon);
                    txtextracswitch.setText("ON");
                }
                else {
                    extractionbutton.setImageResource(R.drawable.fanoff);
                    txtextracswitch.setText("OFF");
                }
                if (documentSnapshot.getBoolean("leak")){

                    cvleak.setVisibility(View.VISIBLE);

                }
                else {
                    cvleak.setVisibility(View.GONE);
                }

                temperatures = (List<Integer>) documentSnapshot.get("cooktop");
                Log.d("AA", temperatures.toString());
                //Log.d("AA", String.valueOf(Integer.parseInt(String.valueOf(temperatures.get(0)))));

                txttemp1.setText(String.valueOf(temperatures.get(0)+"ºC"));
                txttemp2.setText(String.valueOf(temperatures.get(1)+"ºC"));
                txttemp3.setText(String.valueOf(temperatures.get(2)+"ºC"));
                txttemp4.setText(String.valueOf(temperatures.get(3)+"ºC"));

                /*
                if(documentSnapshot.getLong("weight") < 0){

                    txtweight.setText("0gr");
                }else{

                    txtweight.setText(documentSnapshot.getLong("weight").toString()+"g");
                }

                 */

                if(Integer.parseInt(String.valueOf(temperatures.get(0))) > triggerTemperature){
                    temp1.setBackgroundResource(R.drawable.vitroon);
                    txttemp1.setTypeface(null, Typeface.BOLD);
                }else{
                    temp1.setBackgroundResource(R.drawable.vitrooff);
                    txttemp1.setTypeface(null, Typeface.NORMAL);

                }
                if(Integer.parseInt(String.valueOf(temperatures.get(1))) > triggerTemperature){
                    temp2.setBackgroundResource(R.drawable.vitroon);
                    txttemp2.setTypeface(null, Typeface.BOLD);

                }else{
                    temp2.setBackgroundResource(R.drawable.vitrooff);
                    txttemp2.setTypeface(null, Typeface.NORMAL);
                }
                if(Integer.parseInt(String.valueOf(temperatures.get(2))) > triggerTemperature){
                    temp3.setBackgroundResource(R.drawable.vitroon);
                    txttemp3.setTypeface(null, Typeface.BOLD);

                }else{
                    temp3.setBackgroundResource(R.drawable.vitrooff);
                    txttemp3.setTypeface(null, Typeface.NORMAL);

                }
                if(Integer.parseInt(String.valueOf(temperatures.get(3))) > triggerTemperature){
                    temp4.setBackgroundResource(R.drawable.vitroon);
                    txttemp4.setTypeface(null, Typeface.BOLD);

                }else{
                    temp4.setBackgroundResource(R.drawable.vitrooff);
                    txttemp4.setTypeface(null, Typeface.NORMAL);

                }


            }
        });
    }

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
        Log.d(TAG, "connectionLost: "+cause.getCause().getMessage());
    }

    @Override
    public void messageArrived(final String topic, MqttMessage message) throws Exception {

        Log.d(TAG, "messageArrived: "+topic);
        String payload = new String(message.getPayload());
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {

                // Stuff that updates the UI
                txtweight.setText(topic.substring(topic.lastIndexOf('/')+1) + " gr");


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
