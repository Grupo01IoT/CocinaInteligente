package es.upv.epsg.igmagi.cocinainteligente.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import es.upv.epsg.igmagi.cocinainteligente.R;

public class MyKitchenFragment extends Fragment {

    boolean lights = false;
    boolean extrac = false;
    ImageButton lightsbutton;
    ImageButton extractionbutton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_kitchen, container, false);
        lightsbutton = (ImageButton) root.findViewById(R.id.btnLuzOnOff);
        lightsbutton.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                updateBD("lights", !lights);
                /*
                if(lights){
                    updateBD("lights", false);
                }else{
                    updateBD("lights", true);
                }
*/
            };
        });
        extractionbutton = (ImageButton) root.findViewById(R.id.btnExtraccionOnOff);
        extractionbutton.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                updateBD("fan", !extrac);
            }
        });

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

}
