package es.upv.epsg.igmagi.cocinainteligente;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private String TAG = "LOGINACTIVITY";
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        login();
    }

    private FirebaseUser mAuth;
    private FirebaseFirestore db;

    private void login() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario != null) {
            if (!usuario.isEmailVerified() && !usuario.isAnonymous()) {
                usuario.sendEmailVerification();
                Toast.makeText(this, usuario.getDisplayName() + " verificia tu cuenta con el correo que te hemos enviado a: " +
                        usuario.getEmail(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Bienvenido: " +
                        usuario.getDisplayName() + " - " + usuario.getEmail(), Toast.LENGTH_LONG).show();
            }

            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Initialize global variables
            mAuth = FirebaseAuth.getInstance().getCurrentUser();
            db = FirebaseFirestore.getInstance();
            // Check if there's an user already on the database
            db.collection("users").document(mAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "Document exists!");
                        } else {
                            Log.d(TAG, "Document does not exist!");

                            // Create a new user with a first and last name
                            Map<String, Object> user = new HashMap<>();
                            user.put("Email", mAuth.getEmail());
                            user.put("Name", mAuth.getDisplayName());
                            user.put("Fidelity", 0);
                            user.put("Recipes", 0);
                            user.put("Join Date", new Date());

                            // Add a new document with a generated ID
                            db.collection("users")
                                    .document(mAuth.getUid()).set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });
                        }
                    } else {
                        Log.d(TAG, "Failed with: ", task.getException());
                    }
                }
            });
            startActivity(i);
        } else {
            startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.AnonymousBuilder().build(),
                                    new AuthUI.IdpConfig.EmailBuilder().setAllowNewAccounts(true).build(),
                                    new AuthUI.IdpConfig.GoogleBuilder().build()))
                            .setTheme(R.style.LoginTheme)
                            .setLogo(R.drawable.logo1)
                            .build()
                    , RC_SIGN_IN);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                login();
                finish();
            } else {
                IdpResponse response = IdpResponse.fromResultIntent(data);
                if (response == null) {
                    Toast.makeText(this, "Cancelado", Toast.LENGTH_LONG).show();
                    return;
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "Sin conexión a Internet",
                            Toast.LENGTH_LONG).show();
                    return;
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, "Error desconocido",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }

}

