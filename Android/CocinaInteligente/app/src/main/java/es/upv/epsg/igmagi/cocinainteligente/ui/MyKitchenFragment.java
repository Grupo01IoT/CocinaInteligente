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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import es.upv.epsg.igmagi.cocinainteligente.R;

public class MyKitchenFragment extends Fragment {

    boolean lights = false;
    boolean extrac = false;
    List<Integer> temperatures;
    ImageButton lightsbutton;
    ImageButton extractionbutton;
    TextView txtlightswitch, txtextracswitch, txttemp1,txttemp2,txttemp3,txttemp4;
    ImageButton temp1, temp2, temp3, temp4;
    int triggerTemperature = 60;

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
                }
                else {
                    lightsbutton.setImageResource(R.drawable.btnluzoff);
                    txtlightswitch.setText("OFF");
                }
                if (extrac = documentSnapshot.getBoolean("fan")) {
                    extractionbutton.setImageResource(R.drawable.btnextraon);
                    txtextracswitch.setText("ON");
                }
                else {
                    extractionbutton.setImageResource(R.drawable.btnextraoff);
                    txtextracswitch.setText("OFF");
                }
                temperatures = (List<Integer>) documentSnapshot.get("cooktop");
                Log.d("AA", temperatures.toString());
                //Log.d("AA", String.valueOf(Integer.parseInt(String.valueOf(temperatures.get(0)))));

                txttemp1.setText(String.valueOf(temperatures.get(0)+"ºC"));
                txttemp2.setText(String.valueOf(temperatures.get(1)+"ºC"));
                txttemp3.setText(String.valueOf(temperatures.get(2)+"ºC"));
                txttemp4.setText(String.valueOf(temperatures.get(3)+"ºC"));

                if(Integer.parseInt(String.valueOf(temperatures.get(0))) > triggerTemperature){
                    //TODO: Cambiar imagen & updatear valor
                    temp1.setImageResource(R.drawable.vitroon);
                    txttemp1.setTypeface(null, Typeface.BOLD);
                }else{
                    //TODO: Restaurar imagen &
                    temp1.setImageResource(R.drawable.vitrooff);
                    txttemp1.setTypeface(null, Typeface.NORMAL);

                }
                if(Integer.parseInt(String.valueOf(temperatures.get(1))) > triggerTemperature){
                    //TODO: Cambiar imagen & updatear valor
                    temp2.setImageResource(R.drawable.vitroon);
                    txttemp2.setTypeface(null, Typeface.BOLD);

                }else{
                    //TODO: Restaurar imagen &
                    temp2.setImageResource(R.drawable.vitrooff);
                    txttemp2.setTypeface(null, Typeface.NORMAL);
                }
                if(Integer.parseInt(String.valueOf(temperatures.get(2))) > triggerTemperature){
                    //TODO: Cambiar imagen & updatear valor
                    temp3.setImageResource(R.drawable.vitroon);
                    txttemp3.setTypeface(null, Typeface.BOLD);

                }else{
                    //TODO: Restaurar imagen &
                    temp3.setImageResource(R.drawable.vitrooff);
                    txttemp3.setTypeface(null, Typeface.NORMAL);

                }
                if(Integer.parseInt(String.valueOf(temperatures.get(3))) > triggerTemperature){
                    //TODO: Cambiar imagen & updatear valor
                    temp4.setImageResource(R.drawable.vitroon);
                    txttemp4.setTypeface(null, Typeface.BOLD);

                }else{
                    //TODO: Restaurar imagen &
                    temp4.setImageResource(R.drawable.vitrooff);
                    txttemp4.setTypeface(null, Typeface.NORMAL);

                }



            }
        });
    }

}
