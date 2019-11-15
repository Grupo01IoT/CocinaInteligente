package es.upv.epsg.igmagi.raspberrymanagement;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

/**
 * Skeleton of an Android Things activity.
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

    private SharedPreferences prefs;
    private ArduinoUart uart;
    private FirebaseFirestore mBD;
    private Boolean lights, extrac;
    private ImageButton lightsbutton, extractionbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = findViewById(R.id.testText);
        lightsbutton = findViewById(R.id.lightsButton);
        extractionbutton = findViewById(R.id.extracButton);


        prefs =
                getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        tv.setText("Welcome "+ prefs.getString("name", "null"));

        uart = new ArduinoUart("UART0", 9600);
/*
        final Switch s = findViewById(R.id.switch1);
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (s.isChecked()){
                    uart.escribir("O");
                } else {
                    uart.escribir("C");
                }
            }
        });*/

        Button btn = findViewById(R.id.button2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getApplicationContext().getSharedPreferences("Preferences",0).edit().clear().apply();
                startActivity(new Intent(getApplication(), LoginActivity.class));
            }
        });

    }

    private void refreshView() {
        mBD.collection("devices").document("conet_kitchen").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (lights = documentSnapshot.getBoolean("lights")){
                    lightsbutton.setImageResource(R.drawable.btnluzon);
                }
                else {
                    lightsbutton.setImageResource(R.drawable.btnluzoff);
                }
                if (extrac = documentSnapshot.getBoolean("fan")) {
                    extractionbutton.setImageResource(R.drawable.btnextraon);
                }
                else {
                    extractionbutton.setImageResource(R.drawable.btnextraoff);
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
