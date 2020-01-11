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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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

    private static final String TAG = "CREATerecipes";
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
    private ViewFlipper infoRecipe;
    private CheckBox veggie, vegan, dairy, gluten, interactive;
    private FrameLayout fveggie, fvegan, fdairy, fgluten, finteractive;
    private Button next, prev, upload;
    private TextView progressTxt;
    private ProgressBar progressBar;

    private LinearLayout llprova;

    // INGREDIENTS
    public View test;


    // STEPS & INTERACTIVE RECIPE
    int past = 0;

    // OTHER STUFF AND LISTS

    private LinearLayout interactiveOptions;
    private LinearLayout currentIngredients, currentSteps;
    private File file;

    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View vista = inflater.inflate(R.layout.fragment_create_recipes, container, false);

        setHasOptionsMenu(true);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        infoRecipe = vista.findViewById(R.id.infoRecipe);

        llprova = vista.findViewById(R.id.linearLayout8);

        recipeName = vista.findViewById(R.id.recipeName);
        recipeDescription = vista.findViewById(R.id.recipeDescription);
        recipeDuration = vista.findViewById(R.id.recipeDuration);

        currentIngredients = vista.findViewById(R.id.currentIngredients);

        addIngredient = vista.findViewById(R.id.addIngredient);
        addIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LayoutInflater li = LayoutInflater.from(getContext());
                View ingredientView = li.inflate(R.layout.fragment_add_ingredients, null);
                ((TextView) ingredientView.findViewById(R.id.addIngredientNumber)).setText(currentIngredients.getChildCount() + 1 + ". ");
                if (currentIngredients.getChildCount() >= 1) {
                    String modified = ((EditText) currentIngredients.getChildAt(past).findViewById(R.id.addIngredientName)).getText().toString();
                    if (!modified.equals("")) {
                        final View preview = li.inflate(R.layout.fragment_ingredients_min, null);
                        ((TextView) preview.findViewById(R.id.ingredientNameMin)).setText(((EditText) currentIngredients.getChildAt(past).findViewById(R.id.addIngredientName)).getText().toString());
                        ((TextView) preview.findViewById(R.id.ingredientNumberMin)).setText(((TextView) currentIngredients.getChildAt(past).findViewById(R.id.addIngredientNumber)).getText().toString());
                        preview.findViewById(R.id.editIngredientBtn).setTag(past);
                        preview.findViewById(R.id.editIngredientBtn).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                View temp;
                                temp = li.inflate(R.layout.fragment_add_ingredients, null);
                                ((TextView) temp.findViewById(R.id.addIngredientNumber)).setText(((TextView) currentIngredients.getChildAt((Integer) v.getTag()).findViewById(R.id.ingredientNumberMin)).getText().toString());
                                ((EditText) temp.findViewById(R.id.addIngredientName)).setText(((TextView) currentIngredients.getChildAt((Integer) v.getTag()).findViewById(R.id.ingredientNameMin)).getText().toString());

                                currentIngredients.removeViewAt((Integer) v.getTag());
                                currentIngredients.addView(temp, (Integer) v.getTag());
                                if (((EditText) currentIngredients.getChildAt(past).findViewById(R.id.addIngredientName)).getText().toString().matches("[ ]*")) {
                                    currentIngredients.removeViewAt(past);
                                } else {
                                    temp = li.inflate(R.layout.fragment_ingredients_min, null);
                                    ((TextView) temp.findViewById(R.id.ingredientNameMin)).setText(((EditText) currentIngredients.getChildAt(past).findViewById(R.id.addIngredientName)).getText().toString());
                                    ((TextView) temp.findViewById(R.id.ingredientNumberMin)).setText(((TextView) currentIngredients.getChildAt(past).findViewById(R.id.addIngredientNumber)).getText().toString());
                                    currentIngredients.removeViewAt(past);
                                    temp.findViewById(R.id.editIngredientBtn).setTag(past);
                                    temp.findViewById(R.id.editIngredientBtn).setOnClickListener(this);
                                    currentIngredients.addView(temp, past);
                                }
                                past = (int) v.getTag();
                            }
                        });
                        preview.findViewById(R.id.rmvIngredientBtn).setTag(past);
                        preview.findViewById(R.id.rmvIngredientBtn).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                new AlertDialog.Builder(getContext())
                                        .setTitle("Delete entry")
                                        .setMessage("Are you sure you want to delete this entry?")

                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                for (int i = (int) v.getTag() + 1; i < currentIngredients.getChildCount(); i++) {
                                                    try {
                                                        TextView numero = currentIngredients.getChildAt(i).findViewById(R.id.ingredientNumberMin);
                                                        int tag = (int) currentIngredients.getChildAt(i).findViewById(R.id.editIngredientBtn).getTag();
                                                        currentIngredients.getChildAt(i).findViewById(R.id.editIngredientBtn).setTag(tag - 1);
                                                        currentIngredients.getChildAt(i).findViewById(R.id.rmvIngredientBtn).setTag(tag - 1);
                                                        numero.setText(tag + ". ");
                                                    } catch (NullPointerException ex) {
                                                        TextView numero = currentIngredients.getChildAt(i).findViewById(R.id.addIngredientNumber);
                                                        numero.setText(i + ". ");
                                                    }
                                                }
                                                currentIngredients.removeViewAt((Integer) v.getTag());
                                                past = currentIngredients.getChildCount() - 1;
                                            }
                                        })

                                        .setNegativeButton(android.R.string.no, null)
                                        .setIcon(android.R.drawable.stat_sys_warning)
                                        .show();
                            }
                        });
                        currentIngredients.removeViewAt(past);
                        currentIngredients.addView(preview, past);
                        currentIngredients.addView(ingredientView);
                        past = currentIngredients.getChildCount() - 1;

                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                    }
                } else {
                    currentIngredients.addView(ingredientView);
                    past = currentIngredients.getChildCount() - 1;
                }
            }
        });
        currentSteps = vista.findViewById(R.id.currentSteps);

        addStep = vista.findViewById(R.id.addStep);
        addStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LayoutInflater li = LayoutInflater.from(getContext());
                final View stepView = li.inflate(R.layout.fragment_add_steps, null);

                if (interactive.isChecked()) {
                    String trigger;
                    String mode = ((Spinner) currentSteps.getChildAt(currentSteps.getChildCount()-1).findViewById(R.id.interactiveSpinner)).getSelectedItem().toString();
                    if (!mode.equals("Manual")){
                        trigger = ((EditText) currentSteps.getChildAt(currentSteps.getChildCount()-1).findViewById(R.id.interactiveTrigger)).getText().toString();
                        if(trigger.equals("")) {
                            Toast.makeText(getContext(), "Please add trigger action!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    stepView.findViewById(R.id.interactiveSide).setVisibility(View.VISIBLE);

                    ((Spinner) stepView.findViewById(R.id.interactiveSpinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedItemText = (String) parent.getItemAtPosition(position);
                            if (!selectedItemText.equals("Manual")) {
                                stepView.findViewById(R.id.interactiveSideSpec).setVisibility(View.VISIBLE);
                            } else
                                stepView.findViewById(R.id.interactiveSideSpec).setVisibility(View.GONE);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                }

                ((TextView) stepView.findViewById(R.id.addStepNumber)).setText(currentSteps.getChildCount() + 1 + ". ");
                if (currentSteps.getChildCount() >= 1) {
                    String modified = ((EditText) currentSteps.getChildAt(past).findViewById(R.id.addStepName)).getText().toString();
                    if (!modified.equals("")) {
                        final View preview = li.inflate(R.layout.fragment_steps_min, null);
                        ((TextView) preview.findViewById(R.id.stepNameMin)).setText(((EditText) currentSteps.getChildAt(past).findViewById(R.id.addStepName)).getText().toString());
                        ((TextView) preview.findViewById(R.id.stepNumberMin)).setText(((TextView) currentSteps.getChildAt(past).findViewById(R.id.addStepNumber)).getText().toString());
                        preview.findViewById(R.id.editStepBtn).setTag(past);
                        preview.findViewById(R.id.editStepBtn).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                View temp;
                                temp = li.inflate(R.layout.fragment_add_steps, null);
                                ((TextView) temp.findViewById(R.id.addStepNumber)).setText(((TextView) currentSteps.getChildAt((Integer) v.getTag()).findViewById(R.id.stepNumberMin)).getText().toString());
                                ((EditText) temp.findViewById(R.id.addStepName)).setText(((TextView) currentSteps.getChildAt((Integer) v.getTag()).findViewById(R.id.stepNameMin)).getText().toString());

                                currentSteps.removeViewAt((Integer) v.getTag());
                                currentSteps.addView(temp, (Integer) v.getTag());
                                if (((EditText) currentSteps.getChildAt(past).findViewById(R.id.addStepName)).getText().toString().matches("[ ]*")) {
                                    currentSteps.removeViewAt(past);
                                } else {
                                    temp = li.inflate(R.layout.fragment_steps_min, null);
                                    ((TextView) temp.findViewById(R.id.stepNameMin)).setText(((EditText) currentSteps.getChildAt(past).findViewById(R.id.addStepName)).getText().toString());
                                    ((TextView) temp.findViewById(R.id.stepNumberMin)).setText(((TextView) currentSteps.getChildAt(past).findViewById(R.id.addStepNumber)).getText().toString());
                                    currentSteps.removeViewAt(past);
                                    temp.findViewById(R.id.editStepBtn).setTag(past);
                                    temp.findViewById(R.id.editStepBtn).setOnClickListener(this);
                                    currentSteps.addView(temp, past);
                                }
                                past = (int) v.getTag();
                            }
                        });
                        preview.findViewById(R.id.rmvStepBtn).setTag(past);
                        preview.findViewById(R.id.rmvStepBtn).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                new AlertDialog.Builder(getContext())
                                        .setTitle("Delete entry")
                                        .setMessage("Are you sure you want to delete this entry?")

                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                for (int i = (int) v.getTag() + 1; i < currentSteps.getChildCount(); i++) {
                                                    try {
                                                        TextView numero = currentSteps.getChildAt(i).findViewById(R.id.stepNumberMin);
                                                        int tag = (int) currentSteps.getChildAt(i).findViewById(R.id.editStepBtn).getTag();
                                                        currentSteps.getChildAt(i).findViewById(R.id.editStepBtn).setTag(tag - 1);
                                                        currentSteps.getChildAt(i).findViewById(R.id.rmvStepBtn).setTag(tag - 1);
                                                        numero.setText(tag + ". ");
                                                    } catch (NullPointerException ex) {
                                                        TextView numero = currentSteps.getChildAt(i).findViewById(R.id.addStepNumber);
                                                        numero.setText(i + ". ");
                                                    }
                                                }
                                                currentSteps.removeViewAt((Integer) v.getTag());
                                                past = currentSteps.getChildCount() - 1;
                                            }
                                        })

                                        .setNegativeButton(android.R.string.no, null)
                                        .setIcon(android.R.drawable.stat_sys_warning)
                                        .show();
                            }
                        });
                        currentSteps.removeViewAt(past);
                        currentSteps.addView(preview, past);
                        currentSteps.addView(stepView);
                        past = currentSteps.getChildCount() - 1;

                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                    }
                } else {
                    currentSteps.addView(stepView);
                    past = currentSteps.getChildCount() - 1;
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


        fveggie = vista.findViewById(R.id.llveggie);
        fveggie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBackgroundNew(v);
            }
        });
        fvegan = vista.findViewById(R.id.llvegan);
        fvegan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBackgroundNew(v);
            }
        });
        fdairy = vista.findViewById(R.id.lldairy);
        fdairy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBackgroundNew(v);
            }
        });
        fgluten = vista.findViewById(R.id.llgluten);
        fgluten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBackgroundNew(v);
            }
        });
        finteractive = vista.findViewById(R.id.llinteractive);
        finteractive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBackgroundNew(v);
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


        interactive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBackground(v.findViewById(R.id.interactiveRecipe));
            }
        });

        interactive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (currentSteps.getChildCount() > 1) {
                    if (interactive.isChecked()) {
                        interactive.setChecked(false);
                        Toast.makeText(getContext(), "You have steps created already", Toast.LENGTH_SHORT).show();
                    } else {
                        interactive.setChecked(true);
                        Toast.makeText(getContext(), "You have steps created already", Toast.LENGTH_SHORT).show();
                    }
                } else if (currentSteps.getChildCount() == 1) {
                    if (interactive.isChecked()) {
                        currentSteps.findViewById(R.id.interactiveSide).setVisibility(View.VISIBLE);

                        ((Spinner) currentSteps.findViewById(R.id.interactiveSpinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                String selectedItemText = (String) parent.getItemAtPosition(position);
                                if (!selectedItemText.equals("Manual")) {
                                    currentSteps.findViewById(R.id.interactiveSideSpec).setVisibility(View.VISIBLE);
                                } else
                                    currentSteps.findViewById(R.id.interactiveSideSpec).setVisibility(View.GONE);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });
                    } else {
                        currentSteps.findViewById(R.id.interactiveSide).setVisibility(View.GONE);
                    }
                }
            }
        });

        verifyFields();

        return vista;
    }

    public void verifyFields(){
        recipeName.addTextChangedListener(new MyTextWatcher(recipeName, getContext()));
        recipeDescription.addTextChangedListener(new MyTextWatcher(recipeDescription, getContext()));
        recipeDuration.addTextChangedListener(new MyTextWatcher(llprova, getContext()));
    }

    public boolean checkFields() {
        if (!recipeDescription.getText().toString().matches("\"^(?!\\s*$).+\"")) {
            if (!recipeName.getText().toString().matches("\"^(?!\\s*$).+\"")) {
                if (recipeDuration.getText().toString().matches("[0-9]+")) {
                    if (currentSteps.getChildCount() > 1 && currentIngredients.getChildCount() > 1) {
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
                        for (int i = 0; i < currentSteps.getChildCount() - 1; i++) {
                            String step = ((TextView) currentSteps.getChildAt(i).findViewById(R.id.stepNameMin)).getText().toString();
                            if (!interactive.isChecked()) {
                                if (!step.equals("")) stepList.add(step);
                            } else {
                                String trigger = "";
                                String mode = ((Spinner) currentSteps.getChildAt(i).findViewById(R.id.interactiveSpinner)).getSelectedItem().toString();
                                if (!mode.equals("manual"))
                                    trigger = ((EditText) currentSteps.getChildAt(i).findViewById(R.id.interactiveTrigger)).getText().toString();
                                HashMap<String, String> stepMap = new HashMap<>();
                                stepMap.put("step", step);
                                stepMap.put("mode", mode);
                                if (!mode.equals("Manual"))
                                    stepMap.put("trigger", trigger);
                                if (!step.equals("")) stepList.add(stepMap);
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
                        for (int i = 0; i < currentIngredients.getChildCount(); i++) {
                            String ingredient;
                            try {
                                ingredient = ((EditText) currentIngredients.getChildAt(i).findViewById(R.id.addIngredientName)).getText().toString();
                            } catch (NullPointerException ex) {
                                ingredient = ((TextView) currentIngredients.getChildAt(i).findViewById(R.id.ingredientNameMin)).getText().toString();
                            }
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
    }

    public void changeBackground(View v) {
        if (((CheckBox) v).isChecked()) {
            //v.setBackgroundResource(R.drawable.border_checkbox_selected);
            ((FrameLayout) v.getParent()).setBackgroundResource(R.drawable.border_checkbox_selected);
            // ((FrameLayout) v.getParent()).setPadding(75, 0, 0, 0);
            //((LinearLayout)v.getParent()).getChildAt(0).set));
            ((CheckBox) v).setButtonTintList(ColorStateList.valueOf(Color.BLACK));
            ((CheckBox) v).setTextColor(Color.BLACK);
        } else {
            //v.setBackgroundResource(R.drawable.border_checkbox_unselected);
            ((FrameLayout) v.getParent()).setBackgroundResource(R.drawable.border_checkbox_unselected);
            // ((FrameLayout) v.getParent()).setPadding(75, 0, 0, 0);

            ((CheckBox) v).setButtonTintList(ColorStateList.valueOf(Color.parseColor("#FFAAAAAA")));
            ((CheckBox) v).setTextColor(Color.parseColor("#FFAAAAAA"));
        }
    }

    public void changeBackgroundNew(View v) {
        if (((CheckBox) ((FrameLayout) v).getChildAt(0)).isChecked()) {
            Log.d("AA", "isCheckedWhenClicked");
            v.setBackgroundResource(R.drawable.border_checkbox_unselected);
            //v.setPadding(75, 0, 0, 0);

            //((LinearLayout)v.setBackgroundResource(R.drawable.border_checkbox_selected);
            //((LinearLayout)v.getParent()).getChildAt(0).set));
            ((CheckBox) ((FrameLayout) v).getChildAt(0)).setChecked(false);
            ((CheckBox) ((FrameLayout) v).getChildAt(0)).setButtonTintList(ColorStateList.valueOf(Color.parseColor("#FFAAAAAA")));
            ((CheckBox) ((FrameLayout) v).getChildAt(0)).setTextColor(Color.parseColor("#FFAAAAAA"));
        } else {
            Log.d("AA", "isNotCheckedWhenclicked");

            v.setBackgroundResource(R.drawable.border_checkbox_selected);
            //v.setPadding(75, 0, 0, 0);

            //((LinearLayout)v.setBackgroundResource(R.drawable.border_checkbox_selected);
            //((LinearLayout)v.getParent()).getChildAt(0).set));
            Log.d("AA", "isCheckedWhenClicked" + ((CheckBox) ((FrameLayout) v).getChildAt(0)).getText());

            ((CheckBox) ((FrameLayout) v).getChildAt(0)).setChecked(true);
            ((CheckBox) ((FrameLayout) v).getChildAt(0)).setButtonTintList(ColorStateList.valueOf(Color.BLACK));
            ((CheckBox) ((FrameLayout) v).getChildAt(0)).setTextColor(Color.BLACK);
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

class MyTextWatcher implements TextWatcher {

    private View mEditText;
    private Context mContext;
    String oldText;

    public MyTextWatcher(View editText, Context context) {
        mEditText = editText;
        mContext = context;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().matches("^(?!\\s*$).+")){
            mEditText.setBackground(ContextCompat.getDrawable(mContext, R.drawable.border_item_completed));
        } else {
            mEditText.setBackground(ContextCompat.getDrawable(mContext, R.drawable.border_checkbox_unselected));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

}
