package es.upv.epsg.igmagi.cocinainteligente.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.ui.home.HomeFragment;

import static android.app.Activity.RESULT_OK;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class CrearRecetasFragment extends Fragment {
    private FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    private EditText nombre_receta, descripcion_receta, duracion, paso1, paso2, paso3, paso4, paso5;
    private Button boton_guardar, boton_cancelar;
    private ImageView galeria, eliminar;
    private static int RESULT_LOAD_IMAGE = 1;
    private static int RESULT_LOAD_PHOTO = 2;
    private StorageReference storageRef;
    private Uri selectedImageUri;
    private  Uri downloadUrl;
    private Context mContext;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_crear_recetas, container, false);

        mContext = getActivity();

        storageRef = FirebaseStorage.getInstance().getReference();
        selectedImageUri = Uri.parse("android.resource://es.upv.epsg.igmagi.cocinainteligente/drawable/verrecetaslite.png");
        nombre_receta = vista.findViewById(R.id.crear_nombre);
        descripcion_receta = vista.findViewById(R.id.crear_descripcion);
        duracion = vista.findViewById(R.id.crear_duracion);
        paso1 = vista.findViewById(R.id.crear_paso1);
        paso2 = vista.findViewById(R.id.crear_paso2);
        paso3 = vista.findViewById(R.id.crear_paso3);
        paso4 = vista.findViewById(R.id.crear_paso4);
        paso5 = vista.findViewById(R.id.crear_paso5);
        boton_guardar = vista.findViewById(R.id.crear_guardar);
        boton_guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarReceta(view);
            }
        });
        boton_cancelar = vista.findViewById(R.id.crear_cancelar);
        boton_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmarCancelar();
            }
        });
        galeria = vista.findViewById(R.id.galeria);
        galeria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
        eliminar = vista.findViewById(R.id.eliminar_foto);
        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView imageView = (ImageView) getView().findViewById(R.id.crear_imagen_imagen);
                imageView.setImageResource(R.drawable.verrecetaslite);
                selectedImageUri = Uri.parse("android.resource://es.upv.epsg.igmagi.cocinainteligente/drawable/verrecetaslite.png");
            }
        });
        return vista;
    }

    public void guardarReceta(View vista) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //FirebaseStorage storage = FirebaseStorage.getInstance();

       /* UUID uuid = UUID.randomUUID();
        subirFichero(selectedImageUri, "recipes/" + uuid);
        StorageReference ficheroRef = storageRef.child("recipes/" + uuid);
        ficheroRef.child("recipes/" + uuid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                downloadUrl = uri;
            }
        });*/

        Map<String, Object> datos = new HashMap<>();

        datos.put("creationDate", new Date());
        datos.put("description", descripcion_receta.getText().toString());
        datos.put("duration", duracion.getText().toString());
        datos.put("name", nombre_receta.getText().toString());
        datos.put("picture", /*downloadUrl.toString()*/"https://previews.123rf.com/images/margolana/margolana1706/margolana170600029/80490007-vector-verduras-ensalada-estilo-de-dibujos-animados-ensalada-tazón-comida-fresca-saludable-en-un-plato-.jpg");
        datos.put("ratings", Arrays.asList());
        datos.put("steps", Arrays.asList(paso1.getText().toString(), paso2.getText().toString(), paso3.getText().toString(), paso4.getText().toString(), paso5.getText().toString()));
        datos.put("tipo", "Principal");
        datos.put("user", mAuth.getUid());

        db.collection("recipes").add(datos).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(mContext,
                        "Receta guardada", Toast.LENGTH_SHORT).show();
            }
        });
        //getFragmentManager().popBackStackImmediate();
        Navigation.findNavController(getView()).navigate(R.id.action_nav_create_to_nav_home);
    }

    public void confirmarCancelar() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Cancelar")
                .setMessage("Si sales ahora no se guardará la receta. ¿Estás seguro de que quieres salir?")
                .setPositiveButton("Seguir", null)
                .setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /*FragmentManager fragmentManager2 = getFragmentManager();
                        FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                        HomeFragment hf = new HomeFragment();
                        fragmentTransaction2.addToBackStack(null);
                        fragmentTransaction2.hide(CrearRecetasFragment.this);
                        fragmentTransaction2.commit();*/
                        //getFragmentManager().popBackStackImmediate();
                        Navigation.findNavController(getView()).navigate(R.id.action_nav_create_to_nav_home);
                    }
                })
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImageUri));
                ImageView imageView = (ImageView) getView().findViewById(R.id.crear_imagen_imagen);
                imageView.setImageBitmap(bitmap);
            } catch (IOException ie) {

            }

        }
    }

    /*private void subirFichero(Uri fichero, String referencia) {
        StorageReference ficheroRef = storageRef.child(referencia);
        ficheroRef.putFile(fichero).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("Almacenamiento", "Fichero subido");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("Almacenamiento", "ERROR: subiendo fichero");
            }
        });
    }*/
}
