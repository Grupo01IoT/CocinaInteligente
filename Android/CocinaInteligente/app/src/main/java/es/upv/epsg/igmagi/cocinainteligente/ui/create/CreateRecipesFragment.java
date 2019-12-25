package es.upv.epsg.igmagi.cocinainteligente.ui.create;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import es.upv.epsg.igmagi.cocinainteligente.R;

import static android.app.Activity.RESULT_OK;

public class CreateRecipesFragment extends Fragment {
    private FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore mDB = FirebaseFirestore.getInstance();
    private static int RESULT_LOAD_IMAGE = 1;
    private static int RESULT_LOAD_PHOTO = 2;
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private Uri selectedImageUri;
    private Uri downloadUrl;
    private Context mContext;

    private EditText recipeName, recipeDescription, recipeDuration;
    private Button addIngredient, rmvIngredient, addStep, rmvStep;
    private Spinner recipeSp;
    private ImageView recipePhoto;
    private ViewFlipper infoRecipe, ingredients, steps;
    private CheckBox veggie, vegan, dairy, gluten, interactive;
    private Button next, prev, upload;
    private TextView progressTxt;
    private ProgressBar progressBar;

    private LinearLayout interactiveOptions;
    private LinearLayout currentIngredients;

    private boolean uploadFlag = true;

    private File file;

    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_create_recipes, container, false);

        setHasOptionsMenu(true);

        ingredients = vista.findViewById(R.id.ingredientsFlipper);
        steps = vista.findViewById(R.id.stepFlipper);
        infoRecipe = vista.findViewById(R.id.infoRecipe);

        recipeName = vista.findViewById(R.id.recipeName);
        recipeDescription = vista.findViewById(R.id.recipeDescription);
        recipeDuration = vista.findViewById(R.id.recipeDuration);

        currentIngredients = vista.findViewById(R.id.currentIngredients);

        addIngredient = vista.findViewById(R.id.addIngredient);
        addIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LayoutInflater li = LayoutInflater.from(getContext());
                View theview = li.inflate(R.layout.fragment_add_ingredients, null);
                ((TextView) theview.findViewById(R.id.addIngredientNumber)).setText(ingredients.getChildCount() + "");
                if (ingredients.getChildCount() == 1) {
                    ingredients.addView(theview);
                    ingredients.showNext();
                }
                if (ingredients.getChildCount() > 1) {
                    if (!((EditText) ingredients.getCurrentView().findViewById(R.id.addIngredientName)).getText().toString().equals("")) {
                        View preview = li.inflate(R.layout.fragment_add_ingredients, null);
                        ((EditText)preview.findViewById(R.id.addIngredientName)).setText(((EditText) ingredients.getCurrentView().findViewById(R.id.addIngredientName)).getText().toString());
                        currentIngredients.addView(preview);
                        ingredients.addView(theview);
                        ingredients.showNext();
                    }
                }
            }
        });
        rmvIngredient = vista.findViewById(R.id.rmvIngredient);
        rmvIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ingredients.getChildCount() > 1){
                    ingredients.removeViewAt(ingredients.getChildCount() - 1);
                    currentIngredients.removeViewAt(ingredients.getChildCount() - 1);
                }
            }
        });
        addStep = vista.findViewById(R.id.addStep);
        addStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(getContext());
                View theview = li.inflate(R.layout.fragment_add_steps, null);
                ((TextView) theview.findViewById(R.id.addStepNumber)).setText(steps.getChildCount() + "");
                if (steps.getChildCount() == 1) {
                    steps.addView(theview);
                    steps.showNext();
                }
                if (steps.getChildCount() > 1)
                    if (!((EditText) steps.getCurrentView().findViewById(R.id.addStepName)).getText().toString().equals("")) {
                        steps.addView(theview);
                        steps.showNext();
                    }
            }
        });
        rmvStep = vista.findViewById(R.id.rmvStep);
        rmvStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (steps.getChildCount() > 1)
                    steps.removeViewAt(steps.getChildCount() - 1);
            }
        });
        recipeSp = vista.findViewById(R.id.recipeSpinner);
        ArrayAdapter<CharSequence> spAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.create_recipe_type, android.R.layout.simple_spinner_item);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recipeSp.setAdapter(spAdapter);
        recipePhoto = vista.findViewById(R.id.recipePhoto);
        recipePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickImageDialog.build(new PickSetup().setSystemDialog(true))
                        .setOnPickResult(new IPickResult() {
                            @Override
                            public void onPickResult(PickResult r) {
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inSampleSize = 2; //decrease decoded image
                                Bitmap bitmap = null;
                                try {
                                    file = File.createTempFile("compressed", ".jpg");
                                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                                    bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(r.getUri()), null, options);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
                                    Bitmap comp = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                                    // Bitmap bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImageUri));
                                    FileOutputStream fos = new FileOutputStream(file);
                                    fos.write(out.toByteArray());
                                    fos.flush();
                                    fos.close();
                                    recipePhoto.setImageBitmap(comp);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).show(getActivity().getSupportFragmentManager());
            }
        });

        veggie = vista.findViewById(R.id.veggieCB);
        vegan = vista.findViewById(R.id.veganCB);
        dairy = vista.findViewById(R.id.dairyCB);
        gluten = vista.findViewById(R.id.glutenCB);

        progressTxt = vista.findViewById(R.id.stepWise);
        progressBar = vista.findViewById(R.id.stepProgress);

        next = vista.findViewById(R.id.nextBtn);
        prev = vista.findViewById(R.id.prevBtn);
        upload = vista.findViewById(R.id.uploadBtn);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = infoRecipe.getDisplayedChild() + 2;
                prev.setVisibility(View.VISIBLE);
                progressTxt.setText("Paso " + pos + " de 3");
                progressBar.setProgress((100 / 3) * pos);
                infoRecipe.setInAnimation(getContext(), R.anim.view_transition_in_left);
                infoRecipe.setOutAnimation(getContext(), R.anim.view_transition_out_left);
                infoRecipe.showNext();
                if (pos > 2) {
                    next.setVisibility(View.GONE);
                    upload.setVisibility(View.VISIBLE);
                    return;
                }
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = infoRecipe.getDisplayedChild() - 1;
                next.setVisibility(View.VISIBLE);
                upload.setVisibility(View.GONE);
                progressTxt.setText("Paso " + infoRecipe.getDisplayedChild() + " de 3");
                progressBar.setProgress((100 / 3) * infoRecipe.getDisplayedChild());
                infoRecipe.setInAnimation(getContext(), R.anim.view_transition_in_right);
                infoRecipe.setOutAnimation(getContext(), R.anim.view_transition_out_right);
                infoRecipe.showPrevious();
                if (pos < 1) {
                    prev.setVisibility(View.INVISIBLE);
                    return;
                }
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "WATCHOUT!", Toast.LENGTH_SHORT).show();
            }
        });

        interactive = vista.findViewById(R.id.interactiveRecipe);
        interactiveOptions = vista.findViewById(R.id.interactiveSide);

        interactive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (steps.getChildCount()==1) return;
                interactiveOptions = steps.findViewById(R.id.interactiveSide);
                if (isChecked) interactiveOptions.setVisibility(View.VISIBLE);
                else interactiveOptions.setVisibility(View.GONE);
            }
        });
        return vista;
    }

    public void guardarReceta() {
        if(file==null) {
            Toast.makeText(getContext(), "Selecciona una imagen!", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        final String pid = UUID.randomUUID().toString();

        final StorageReference ref = storageRef.child("images/" + pid);
        ref.putFile(Uri.fromFile(file))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Uploaded", Toast.LENGTH_SHORT).show();

                        Map<String, Object> datos = new HashMap<>();
                        datos.put("date", new Date());
                        datos.put("description", recipeDescription.getText().toString());
                        datos.put("duration", Integer.parseInt(recipeDuration.getText().toString()));
                        datos.put("name", recipeName.getText().toString());
                        datos.put("picture", pid);
                        datos.put("ratings", new HashMap<String, Long>());
                        ArrayList<String> stepList = new ArrayList<>();
                        for (int i = 1; i < steps.getChildCount(); i++) {
                            String step = ((EditText) steps.getChildAt(i).findViewById(R.id.addStepName)).getText().toString();
                            if (!step.equals("")) stepList.add(step);
                        }
                        datos.put("steps", stepList);
                        datos.put("tipo", recipeSp.getSelectedItem().toString());
                        HashMap<String, Boolean> extras = new HashMap<>();
                        extras.put("veggie", veggie.isChecked());
                        extras.put("vegan", vegan.isChecked());
                        extras.put("dairy", dairy.isChecked());
                        extras.put("gluten", gluten.isChecked());
                        datos.put("extra", extras);
                        ArrayList<String> ingredientList = new ArrayList<>();
                        for (int i = 1; i < ingredients.getChildCount(); i++) {
                            String ingredient = ((EditText) ingredients.getChildAt(i).findViewById(R.id.addIngredientName)).getText().toString();
                            if (!ingredient.equals("")) ingredientList.add(ingredient);
                        }
                        datos.put("ingredients", ingredientList);
                        datos.put("user", mAuth.getUid());
                        mDB.collection("recipes").add(datos).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Navigation.findNavController(getView()).navigate(R.id.action_nav_create_to_nav_home);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage("Uploaded " + (int) progress + "%");

                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            selectedImageUri = data.getData();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; //decrease decoded image
            Bitmap bitmap = null;
            try {
                file = File.createTempFile("compressed", ".jpg");
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImageUri), null, options);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
                Bitmap comp = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                // Bitmap bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImageUri));
                ImageView imageView = (ImageView) getView().findViewById(R.id.crear_imagen_imagen);//write the bytes in file
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(out.toByteArray());
                fos.flush();
                fos.close();
                imageView.setImageBitmap(comp);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload:
                guardarReceta();
                return true;
            default:
                break;
        }

        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_upload);
        item.setVisible(true);
    }

}
