package es.upv.epsg.igmagi.cocinainteligente.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.model.*;

import static com.example.igmagi.shared.Mqtt.*;

public class RecipeFragment extends Fragment implements org.eclipse.paho.client.mqttv3.MqttCallback{
    private static final String TAG = "RECIPEFRAGMENT";
    TextView tvname, tvdescription, tvtiempo, tvusername;
    RatingBar rbrating;
    ImageView vegan, veggie, dairy, gluten;
    ImageButton like;
    ViewFlipper stepsContainer;
    Recipe recipe;
    User user;
    LinearLayout ingredientList, recipePhoto;
    private ListView lvIngr, lvSteps;
    private ArrayAdapter<String> ingrAdapter, stepsAdapter;
    private ArrayList<String> ingrList, stepsList;

    private FirebaseFirestore mBD = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_info_recipe, container, false);

        lvIngr = (ListView) root.findViewById(R.id.ingredientslist);
        lvSteps = (ListView) root.findViewById(R.id.stepslist);

        ingrList = new ArrayList<String>();
        stepsList = new ArrayList<String>();

        ingrAdapter = new ArrayAdapter<String>(getContext(), R.layout.listview_item, ingrList);
        stepsAdapter = new ArrayAdapter<String>(getContext(), R.layout.listview_item, stepsList);

        lvIngr.setAdapter(ingrAdapter);
        lvSteps.setAdapter(stepsAdapter);
        //setHasOptionsMenu(true);

        final RecipeViewModel model = ViewModelProviders.of(getActivity()).get(RecipeViewModel.class);
        UserViewModel userModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);

        recipe = model.getCurrentRecipe();
        user = userModel.getCurrentUser();

        //The fabPlay Button will not appear if the recipe isn't interactive or the user isn't paired
        //Check if the user has already paired the device
        SharedPreferences pref2 = getActivity().getPreferences(Context.MODE_PRIVATE);
        String deviceId = pref2.getString("device_id", "empty");

        if(deviceId.equals("empty") || !recipe.isInteractive()){
            root.findViewById(R.id.fabPlay).setVisibility(View.GONE);
        }else{
            root.findViewById(R.id.fabPlay).setVisibility(View.VISIBLE);
        }

        root.findViewById(R.id.fabPlay).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_nav_view_recipe_to_nav_interactive));
        /*
        root.findViewById(R.id.fabPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInteractiveDialog();
            }
        });*/

        tvname = root.findViewById(R.id.name);
        tvdescription = root.findViewById(R.id.description);
        tvtiempo = root.findViewById(R.id.tiempo);
        tvusername = root.findViewById(R.id.userName);

        tvname.setText(recipe.getName());
        tvdescription.setText(recipe.getDescription());
        tvtiempo.setText(recipe.getFormattedDuration());

        //SET USER NAME OF THE USER WHO CREATED THE RECIPE
        mBD.collection("users").document(recipe.getUser()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        tvusername.setText("@" + document.getString("name"));
                        model.setUserName(document.getString("name"));
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        // Inflate the layout for this fragment
        recipePhoto = root.findViewById(R.id.recipephoto);
        recipePhoto.setBackground(model.getCurrentRecipeImage());
        rbrating = root.findViewById(R.id.showRating);
        rbrating.setRating(recipe.getRatingValue());

        gluten = root.findViewById(R.id.glutenIcon);
        dairy = root.findViewById(R.id.dairyIcon);
        vegan = root.findViewById(R.id.veganIcon);
        veggie = root.findViewById(R.id.veggieIcon);

        if (recipe.getExtra().get("veggie")) veggie.setVisibility(View.VISIBLE);
        if (recipe.getExtra().get("vegan")) vegan.setVisibility(View.VISIBLE);
        if (recipe.getExtra().get("dairy")) dairy.setVisibility(View.VISIBLE);
        if (recipe.getExtra().get("gluten")) gluten.setVisibility(View.VISIBLE);

        // If the recipe is vegan, it is veggetarian
        if (recipe.getExtra().get("veggie") && recipe.getExtra().get("vegan")) {
            veggie.setVisibility(View.GONE);
            vegan.setVisibility(View.VISIBLE);
        }
        createStepsList();
        createIngredientsList();

        // -------- LIKE BUTTON -----------
        like = root.findViewById(R.id.likeButton);
        //SET INITIAL STATE OF THE LIKE BUTTON
        if (user.getFavouriteReceipts().contains(recipe.getUid())) {//Si es favorita
            like.setBackgroundResource(R.drawable.liked);
        } else {
            like.setBackgroundResource(R.drawable.like);
        }
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getFavouriteReceipts().contains(recipe.getUid())) {//Si es favorita
                    ArrayList<String> fav = (user.getFavouriteReceipts());
                    boolean res = fav.remove(recipe.getUid());
                    user.setFavouriteReceipts(fav);
                    Map<String, Object> map = new HashMap<>();
                    map.put("favouriteReceips", user.getFavouriteReceipts());
                    mBD.collection("users").document(user.getUid()).update(map);
                    like.setBackgroundResource(R.drawable.like);
                } else {//Si no es favorita

                    ArrayList<String> fav = (user.getFavouriteReceipts());
                    boolean res = fav.add(recipe.getUid());
                    user.setFavouriteReceipts(fav);

                    Map<String, Object> map = new HashMap<>();
                    map.put("favouriteReceips", user.getFavouriteReceipts());

                    mBD.collection("users").document(user.getUid()).update(map);
                    like.setBackgroundResource(R.drawable.liked);

                }
            }

        });
        return root;
    }

    private void createIngredientsList() {
        int n = 1;
        for (String s : recipe.getIngredients()) {

            ingrAdapter.add(n + ". " + s.toUpperCase());
            n++;
        }
        ViewGroup.LayoutParams params = lvIngr.getLayoutParams();
        params.height = 55 * n;
        lvIngr.setLayoutParams(params);
        lvIngr.requestLayout();
        params = lvIngr.getLayoutParams();
    }

    private void createStepsList() {
        int n = 1;
        if (!recipe.isInteractive()) {
            for (int i = 0; i < recipe.getSteps().size(); i++) {
                stepsAdapter.add((n + ". " + (String) recipe.getSteps().get(i)).toUpperCase());
                //Log.d("OOO", (String)recipe.getSteps().get(i));
                n++;
            }
            ViewGroup.LayoutParams params = lvSteps.getLayoutParams();
            params.height = 55 * n;
            lvSteps.setLayoutParams(params);
            lvSteps.requestLayout();
        } else {
            for (int i = 0; i < recipe.getSteps().size(); i++) {
                HashMap map = (HashMap) recipe.getSteps().get(i);
                stepsAdapter.add((n + ". " + map.get("step")));
                n++;
            }
            ViewGroup.LayoutParams params = lvSteps.getLayoutParams();
            params.height = 55 * n;
            lvSteps.setLayoutParams(params);
            lvSteps.requestLayout();
        }
    }

    private AlertDialog alertDialog;

    ViewFlipper steps;
    MqttClient client = null;

    private void showInteractiveDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        View layoutView = interactiveSetup();
        dialogBuilder.setView(layoutView);
        alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                try {
                    client.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private View interactiveSetup(){
        View layoutView = getLayoutInflater().inflate(R.layout.fragment_interactive_recipe_dialog, null);
        Button dialogButton = layoutView.findViewById(R.id.intStopBtn);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        steps = layoutView.findViewById(R.id.interactiveStepsFlipper);
        ArrayList<Step> stepArrayList =  recipe.getStepsToHashMap();

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

        int cont = 0;
        for (Step s: stepArrayList) {
            LayoutInflater li = LayoutInflater.from(getContext());
            final View theview = li.inflate(R.layout.fragment_interactive_steps, null);
            ((TextView)theview.findViewById(R.id.stepsInteractiveTxt)).setText("Paso "+ cont++ + " de " + stepArrayList.size() + ". " + s.getMode());
            ((TextView)theview.findViewById(R.id.informationInteractiveTxt)).setText(s.getStep());
            ((TextView)theview.findViewById(R.id.fromInteractiveTxt)).setText("De 0");
            ((TextView)theview.findViewById(R.id.toInteractiveTxt)).setText("hasta " + s.getTrigger());
                //LEER
                try {
                    client.subscribe(topicRoot+s.getMode()+"/#", qos);
                    client.setCallback(RecipeFragment.this);
                    Log.i(TAG, "Suscrito a " + topicRoot+weight);
                } catch (MqttException e) {
                    Log.e(TAG, "Error al suscribir.", e);
                }
            steps.addView(theview);
        }

        return layoutView;
    }

    private void computeValue(int value, int pos) {
        Step s = recipe.getStepsToHashMap().get(pos);
        Log.d(TAG, "VALOR: " + value + " - " + steps.getChildCount());
        ((TextView)steps.getChildAt(0).findViewById(R.id.fromInteractiveTxt)).setText("De " + value);
        if (Integer.parseInt(s.getTrigger()) == value){
            steps.showNext();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        View current = steps.getCurrentView();
        int pos = steps.getDisplayedChild();
        String val = topic.substring(topic.lastIndexOf('/')+1);

        Log.i(TAG, "LLEGó - " + "De " + val);
        Log.i(TAG, "c");

        Log.i(TAG, "pretexto - " +  ((TextView)current.findViewById(R.id.fromInteractiveTxt)).getText());
        Log.i(TAG, "posttexto - " +  ((TextView)current.findViewById(R.id.fromInteractiveTxt)).getText());

        // computeValue(val, pos);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
