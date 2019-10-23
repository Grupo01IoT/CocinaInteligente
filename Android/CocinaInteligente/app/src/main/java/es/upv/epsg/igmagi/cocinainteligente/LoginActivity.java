package es.upv.epsg.igmagi.cocinainteligente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import static android.widget.Toast.*;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailUI, passwordUI;
    private Button signinUI, signupUI;

    private String email, password;

    private String TAG = "LOGINACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI
        emailUI = findViewById(R.id.email);
        passwordUI = findViewById(R.id.password);
        signinUI = findViewById(R.id.signin);
        signupUI = findViewById(R.id.signup);

        signinUI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailUI.getText().toString();
                password = passwordUI.getText().toString();
                //Toast.makeText(LoginActivity.this, email + " and ********" , LENGTH_LONG).show();
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }

                            }
                        });
            }
        });

        signupUI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailUI.getText().toString();
                password = passwordUI.getText().toString();
                //Toast.makeText(LoginActivity.this, email + " and ********" , LENGTH_LONG).show();
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful() &&
                                        task.getException() instanceof FirebaseAuthUserCollisionException) {

                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Authentication failed. The user already exists",
                                            LENGTH_LONG).show();
                                    updateUI(null);
                                }
                                else if(task.isSuccessful()){
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);
                                }
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                            }
                        });
            }
        });

    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
        }
    }
}
