package es.upv.epsg.igmagi.cocinainteligente.ui.create;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
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

    // GENERAL STUFF MAYBE USELESS
    private FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore mDB = FirebaseFirestore.getInstance();
    private static int RESULT_LOAD_IMAGE = 1;
    private static int RESULT_LOAD_PHOTO = 2;
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private Uri selectedImageUri;
    private Uri downloadUrl;
    private Context mContext;

    // RECIPE

    private EditText recipeName, recipeDescription, recipeDuration;
    private Button addIngredient, rmvIngredient, addStep, rmvStep;
    private Spinner recipeSp;
    private ImageView recipePhoto;
    private ViewFlipper infoRecipe, ingredients, steps;
    private CheckBox veggie, vegan, dairy, gluten, interactive;
    private Button next, prev, upload;
    private TextView progressTxt;
    private ProgressBar progressBar;

    // INGREDIENTS


    // STEPS & INTERACTIVE RECIPE


    // OTHER STUFF AND LISTS

    private LinearLayout interactiveOptions;
    private LinearLayout currentIngredients, currentSteps;
    private File file;

    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View vista = inflater.inflate(R.layout.fragment_create_recipes, container, false);

        setHasOptionsMenu(true);

        CustomGestureDetector customGestureDetector = new CustomGestureDetector();
        final GestureDetector mGestureDetector = new GestureDetector(getActivity(), customGestureDetector);

        ingredients = vista.findViewById(R.id.ingredientsFlipper);
        steps = vista.findViewById(R.id.stepFlipper);
        infoRecipe = vista.findViewById(R.id.infoRecipe);

        recipeName = vista.findViewById(R.id.recipeName);
        recipeDescription = vista.findViewById(R.id.recipeDescription);
        recipeDuration = vista.findViewById(R.id.recipeDuration);

        currentIngredients = vista.findViewById(R.id.currentIngredients);
        vista.findViewById(R.id.lastIngredient).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ingredients.setDisplayedChild(ingredients.getChildCount() - 1);
            }
        });

        addIngredient = vista.findViewById(R.id.addIngredient);
        addIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(getContext());
                View theview = li.inflate(R.layout.fragment_add_ingredients, null);
                ((TextView) theview.findViewById(R.id.addIngredientNumber)).setText(ingredients.getChildCount()+1 + ". ");
                if (ingredients.getChildCount() >= 1) {
                    if (((EditText) ingredients.getCurrentView().findViewById(R.id.addIngredientName)).getText().toString().matches("[A-Za-z]+ ?|[A-Z] ?|[a-z] ?")) {
                        vista.findViewById(R.id.lastIngredient).setVisibility(View.VISIBLE);
                        vista.findViewById(R.id.rmvIngredient).setVisibility(View.VISIBLE);
                        final View preview = li.inflate(R.layout.fragment_ingredients_min, null);
                        ((TextView) preview.findViewById(R.id.ingredientNameMin)).setText(((EditText) ingredients.getCurrentView().findViewById(R.id.addIngredientName)).getText().toString());
                        ((TextView) preview.findViewById(R.id.ingredientNumberMin)).setText(((TextView) ingredients.getCurrentView().findViewById(R.id.addIngredientNumber)).getText().toString());
                        preview.setTag(currentIngredients.getChildCount());
                        preview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ingredients.setDisplayedChild(((Integer) preview.getTag()) + 1);
                            }
                        });
                        if (ingredients.getDisplayedChild() != 0 && currentIngredients.getChildCount() > 0)
                            if (ingredients.getDisplayedChild() - 1 < currentIngredients.getChildCount()) {
                                int pos = ingredients.getDisplayedChild() - 1;
                                currentIngredients.removeViewAt(pos);
                                preview.setTag(pos);
                                currentIngredients.addView(preview);
                                ingredients.setDisplayedChild(ingredients.getChildCount() - 1);
                                return;
                            }
                        currentIngredients.addView(preview);
                        ingredients.addView(theview);
                        ingredients.setDisplayedChild(ingredients.getChildCount() - 1);
                    }
                }
            }
        });
        rmvIngredient = vista.findViewById(R.id.rmvIngredient);
        rmvIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ingredients.getChildCount() > 1) {
                    ingredients.removeViewAt(ingredients.getChildCount() - 1);
                    if (ingredients.getChildCount() - 1 == currentIngredients.getChildCount() && currentIngredients.getChildCount() != 0)
                        currentIngredients.removeViewAt(currentIngredients.getChildCount() - 1);
                } else {
                    vista.findViewById(R.id.lastIngredient).setVisibility(View.INVISIBLE);
                }
            }
        });


        currentSteps = vista.findViewById(R.id.currentSteps);
        vista.findViewById(R.id.lastStep).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                steps.setDisplayedChild(steps.getChildCount() - 1);
            }
        });

        addStep = vista.findViewById(R.id.addStep);
        addStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(getContext());
                View theview = li.inflate(R.layout.fragment_add_steps, null);
                if (interactive.isChecked()) {
                    if (steps.getDisplayedChild() > 0 && !(((EditText) steps.getCurrentView().findViewById(R.id.interactiveTrigger)).getText().toString().matches("[0-9]+")))
                        return;
                    theview.findViewById(R.id.interactiveSide).setVisibility(View.VISIBLE);
                }
                ((TextView) theview.findViewById(R.id.addStepNumber)).setText(steps.getChildCount() + "");
                if (steps.getChildCount() == 1) {
                    vista.findViewById(R.id.lastStep).setVisibility(View.VISIBLE);
                    steps.addView(theview);
                    steps.setDisplayedChild(steps.getChildCount() - 1);
                }
                if (steps.getChildCount() > 1) {
                    if (!((EditText) steps.getCurrentView().findViewById(R.id.addStepName)).getText().toString().equals("")) {
                        final View preview = li.inflate(R.layout.fragment_steps_min, null);
                        ((TextView) preview.findViewById(R.id.stepNameMin)).setText(((EditText) steps.getCurrentView().findViewById(R.id.addStepName)).getText().toString());
                        ((TextView) preview.findViewById(R.id.stepNumberMin)).setText(((TextView) steps.getCurrentView().findViewById(R.id.addStepNumber)).getText().toString());
                        preview.setTag(currentSteps.getChildCount());
                        preview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                steps.setDisplayedChild(((Integer) preview.getTag()) + 1);
                            }
                        });
                        if (steps.getDisplayedChild() != 0 && currentSteps.getChildCount() > 0)
                            if (steps.getDisplayedChild() - 1 < currentSteps.getChildCount()) {
                                int pos = steps.getDisplayedChild() - 1;
                                currentSteps.removeViewAt(pos);
                                preview.setTag(pos);
                                currentSteps.addView(preview);
                                steps.setDisplayedChild(steps.getChildCount() - 1);
                                return;
                            }
                        currentSteps.addView(preview);
                        steps.addView(theview);
                        steps.setDisplayedChild(steps.getChildCount() - 1);
                    }
                }
            }
        });
        rmvStep = vista.findViewById(R.id.rmvStep);
        rmvStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (steps.getChildCount() > 1) {
                    steps.removeViewAt(steps.getChildCount() - 1);
                    if (steps.getChildCount() - 1 == currentSteps.getChildCount() && currentSteps.getChildCount() != 0)
                        currentSteps.removeViewAt(currentSteps.getChildCount() - 1);
                } else {
                    vista.findViewById(R.id.lastStep).setVisibility(View.INVISIBLE);
                }
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
        veggie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBackground(v);
            }
        });
        vegan = vista.findViewById(R.id.veganCB);
        vegan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBackground(v);
            }
        });
        dairy = vista.findViewById(R.id.dairyCB);
        dairy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBackground(v);
            }
        });
        gluten = vista.findViewById(R.id.glutenCB);
        gluten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBackground(v);
            }
        });

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
                guardarReceta();
            }
        });

        interactive = vista.findViewById(R.id.interactiveRecipe);
        interactiveOptions = vista.findViewById(R.id.interactiveSide);

        interactive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (steps.getChildCount() == 1) return;
                for (int i = 1; i < steps.getChildCount(); i++) {
                    interactiveOptions = steps.getChildAt(i).findViewById(R.id.interactiveSide);
                    if (isChecked) interactiveOptions.setVisibility(View.VISIBLE);
                    else interactiveOptions.setVisibility(View.GONE);

                }
            }
        });
        return vista;
    }

    public boolean checkFields() {
        if (recipeDescription.getText().toString().matches("[A-Za-z]+ ?|[A-Z] ?|[a-z] ?")) {
            if (recipeName.getText().toString().matches("[A-Za-z]+ ?|[A-Z] ?|[a-z] ?")) {
                if (recipeDuration.getText().toString().matches("[0-9]+")) {
                    if (steps.getChildCount() > 1 && ingredients.getChildCount() > 1) {
                        //for(int i = 1; i<ingredients.getChildCount(); i++){
                        //    Log.d("CREATERECIPE",((EditText)ingredients.getChildAt(i).findViewById(R.id.interactiveSide).findViewById(R.id.interactiveTrigger)).getText().toString());
                        //    if (!(((EditText)ingredients.getChildAt(i).findViewById(R.id.interactiveSide).findViewById(R.id.interactiveTrigger)).getText().toString().matches("[0-9]+")) && interactive.isChecked()){
                        //        Toast.makeText(getContext(),"Check steps' trigger", Toast.LENGTH_SHORT).show();
                        //        return false;
                        //    }
                        //}
                        return true;
                    } else {
                        Toast.makeText(getContext(), "Check recipe's ingredients and steps!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Check recipe's duration!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Check recipe's name!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Check recipe's description!", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void guardarReceta() {
        if (file == null) {
            Toast.makeText(getContext(), "Selecciona una imagen!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!checkFields()) {
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
                        datos.put("interactive", interactive.isChecked());
                        ArrayList<Object> stepList = new ArrayList<>();
                        for (int i = 1; i < steps.getChildCount(); i++) {
                            String step = ((EditText) steps.getChildAt(i).findViewById(R.id.addStepName)).getText().toString();
                            if (!interactive.isChecked()) {
                                if (!step.equals("")) stepList.add(step);
                            } else {
                                String trigger = ((EditText) steps.getChildAt(i).findViewById(R.id.interactiveTrigger)).getText().toString();
                                String mode = ((Spinner) steps.getChildAt(i).findViewById(R.id.interactiveSpinner)).getSelectedItem().toString();
                                HashMap<String, String> stepMap = new HashMap<>();
                                stepMap.put("step", step);
                                stepMap.put("mode", mode);
                                stepMap.put("trigger", trigger);
                                stepList.add(stepMap);
                            }
                        }
                        datos.put("steps", stepList);
                        datos.put("tipo", recipeSp.getSelectedItem().toString());
                        //SUBIR INGREDIENTS
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

    public void changeBackground(View v) {
        if (((CheckBox) v).isChecked()) {
            v.setBackgroundResource(R.drawable.border_checkbox_selected);
            ((CheckBox) v).setButtonTintList(ColorStateList.valueOf(Color.BLACK));
            ((CheckBox) v).setTextColor(Color.BLACK);
        } else {
            v.setBackgroundResource(R.drawable.border_checkbox_unselected);
            ((CheckBox) v).setButtonTintList(ColorStateList.valueOf(Color.parseColor("#FFAAAAAA")));
            ((CheckBox) v).setTextColor(Color.parseColor("#FFAAAAAA"));
        }
    }

    class CustomGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            // Swipe left (next)
            if (e1.getX() > e2.getX()) {
                infoRecipe.showNext();
            }

            // Swipe right (previous)
            if (e1.getX() < e2.getX()) {
                infoRecipe.showPrevious();
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}
