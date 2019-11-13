package es.upv.epsg.igmagi.raspberrymanagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private String TAG = "LOGINACTIVITY";
    private static final int RC_SIGN_IN = 123;
    private FirebaseFirestore db;
    private EditText idInput, nameInput, pinInput;
    private Button login;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // FirebaseApp.initializeApp(this);
        prefs =
                getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        if (prefs.contains("id")) {
            Intent i = new Intent(getApplication(), MainActivity.class);
            startActivity(i);
        }

        idInput = findViewById(R.id.idInput);
        pinInput = findViewById(R.id.pinInput);
        nameInput = findViewById(R.id.nameInput);
        login = findViewById(R.id.button);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pinInput.getText().toString().equals("") && !nameInput.getText().toString().equals("")) login();
                else Toast.makeText(getApplicationContext(), "Llena los campos!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void login() {
        db = FirebaseFirestore.getInstance();

        idInput.setText(nameInput.toString().replace(" ", "_"));
        // Check if there's a device already on the database
        final Map<String, Object> device = new HashMap<>();
        device.put("name", nameInput.getText().toString());
        device.put("pin", pinInput.getText().toString());
        device.put("joinDate", new Date());
        device.put("lights", false);
        device.put("fan", false);
        device.put("cooktop", false);
        device.put("voice", false);

        db.collection("devices").document(idInput.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Toast.makeText(getApplication(), "ID already exists!", Toast.LENGTH_LONG).show();
                    } else {
                        // Add a new document with a generated ID
                        db.collection("devices").document(idInput.getText().toString())
                                .set(device)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void documentReference) {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");

                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("id", idInput.getText().toString());
                                        editor.putString("name", nameInput.getText().toString());
                                        editor.commit();
                                        Intent i = new Intent(getApplication(), MainActivity.class);
                                        startActivity(i);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });
                    }
                }
            }
        });

    }

}

