package es.upv.epsg.igmagi.cocinainteligente.ui.create;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.ui.create.firstscreen.CreateRecipesFirstScreen;
import es.upv.epsg.igmagi.cocinainteligente.ui.create.secondscreen.CreateRecipesSecondScreen;
import es.upv.epsg.igmagi.cocinainteligente.ui.create.thirdscreen.CreateRecipesThirdScreen;

import static android.app.Activity.RESULT_OK;

public class CreateRecipesFragment extends Fragment {

    // GENERAL STUFF MAYBE USELESS
    private FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore mDB = FirebaseFirestore.getInstance();
    private static int RESULT_LOAD_IMAGE = 1;
    private static int RESULT_LOAD_PHOTO = 2;
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private Uri selectedImageUri;

    private ViewPager infoRecipe;
    private Button next, prev, upload;
    private TextView progressTxt;
    private ProgressBar progressBar;

    private File file;
    FixedPageAdapter fixedPageAdapter;

    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View vista = inflater.inflate(R.layout.fragment_create_recipes, container, false);

        setHasOptionsMenu(true);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        fixedPageAdapter = new FixedPageAdapter(getChildFragmentManager());
        infoRecipe = vista.findViewById(R.id.infoRecipe);
        infoRecipe.setAdapter(fixedPageAdapter);
        infoRecipe.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                progressBar.setProgress((100/3) * (position+1));
                progressTxt.setText("Paso " + (position+1) + " de 3");

                if (position >= 2) {
                    next.setVisibility(View.GONE);
                    upload.setVisibility(View.VISIBLE);
                }else{
                    next.setVisibility(View.VISIBLE);
                    upload.setVisibility(View.GONE);
                }
                if (position < 1) {
                    prev.setVisibility(View.INVISIBLE);
                }else{
                    prev.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

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
                int pos = infoRecipe.getCurrentItem() + 2;
                prev.setVisibility(View.VISIBLE);
                progressTxt.setText("Paso " + pos + " de 3");
                progressBar.setProgress((100 / 3) * pos);

                infoRecipe.setCurrentItem(infoRecipe.getCurrentItem()+1, true); //getItem(-1) for previous
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
                int pos = infoRecipe.getCurrentItem() - 1;
                next.setVisibility(View.VISIBLE);
                upload.setVisibility(View.GONE);
                progressTxt.setText("Paso " + infoRecipe.getCurrentItem() + " de 3");
                progressBar.setProgress((100 / 3) * infoRecipe.getCurrentItem());
                infoRecipe.setCurrentItem(infoRecipe.getCurrentItem()-1, true); //getItem(-1) for previous
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

        return vista;
    }


    public boolean checkFields() {
        if(fixedPageAdapter.createRecipesFirstScreen.checkFields()){
            if(fixedPageAdapter.createRecipesSecondScreen.checkFields()){
                return fixedPageAdapter.createRecipesThirdScreen.checkFields();
            }
        }
        return false;
    }

    public void guardarReceta() {
        file = fixedPageAdapter.createRecipesFirstScreen.getFile();
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
                        datos.put("description", fixedPageAdapter.createRecipesFirstScreen.getRecipeDescription().getText().toString());
                        datos.put("duration", Integer.parseInt(fixedPageAdapter.createRecipesFirstScreen.getRecipeDuration().getText().toString()));
                        datos.put("name", fixedPageAdapter.createRecipesFirstScreen.getRecipeName().getText().toString());
                        datos.put("picture", pid);
                        datos.put("ratings", new HashMap<String, Long>());
                        datos.put("interactive", fixedPageAdapter.createRecipesThirdScreen.getInteractive().isChecked());
                        ArrayList<Object> stepList = new ArrayList<>();
                        LinearLayout currentSteps = fixedPageAdapter.createRecipesThirdScreen.getCurrentSteps();
                        for (int i = 0; i < currentSteps.getChildCount() - 1; i++) {
                            String step = ((TextView) currentSteps.getChildAt(i).findViewById(R.id.stepNameMin)).getText().toString();
                            if (!fixedPageAdapter.createRecipesThirdScreen.getInteractive().isChecked()) {
                                if (!step.equals("")) stepList.add(step);
                            } else {
                                HashMap<String, String> stepMap = new HashMap<>();
                                String trigger = "";
                                //solve this
                                String mode = ((TextView) currentSteps.getChildAt(i).findViewById(R.id.ModeMin)).getText().toString();
                                if (!mode.equals("Manual")){
                                    trigger = ((TextView) currentSteps.getChildAt(i).findViewById(R.id.TriggerMin)).getText().toString();
                                    stepMap.put("mode", mode.substring(0, mode.indexOf(":")-1));
                                } else {
                                    stepMap.put("mode", mode);
                                }
                                stepMap.put("step", step);
                                if (!mode.equals("Manual"))
                                    stepMap.put("trigger", trigger.substring(0, trigger.indexOf(" ")-1));
                                if (!step.equals("")) stepList.add(stepMap);
                            }
                        }
                        datos.put("steps", stepList);
                        datos.put("tipo", fixedPageAdapter.createRecipesFirstScreen.getRecipeSp().getSelectedItem().toString());
                        //SUBIR INGREDIENTS
                        HashMap<String, Boolean> extras = new HashMap<>();
                        extras.put("veggie", fixedPageAdapter.createRecipesSecondScreen.getVeggie().isChecked());
                        extras.put("vegan",  fixedPageAdapter.createRecipesSecondScreen.getVegan().isChecked());
                        extras.put("dairy",  fixedPageAdapter.createRecipesSecondScreen.getDairy().isChecked());
                        extras.put("gluten",  fixedPageAdapter.createRecipesSecondScreen.getGluten().isChecked());
                        datos.put("extra", extras);
                        ArrayList<String> ingredientList = new ArrayList<>();
                        LinearLayout currentIngredients = fixedPageAdapter.createRecipesSecondScreen.getCurrentIngredients();
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
                ((ImageView)infoRecipe.getFocusedChild().findViewById(R.id.recipePhoto)).setImageBitmap(comp);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    class FixedPageAdapter extends FragmentPagerAdapter {

        CreateRecipesFirstScreen createRecipesFirstScreen = new CreateRecipesFirstScreen();
        CreateRecipesSecondScreen createRecipesSecondScreen = new CreateRecipesSecondScreen();
        CreateRecipesThirdScreen createRecipesThirdScreen = new CreateRecipesThirdScreen();

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            //super.destroyItem(container, position, object);
        }

        public FixedPageAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        public Fragment getItemAt(int position) {
            switch (position){
                case 0: return createRecipesFirstScreen;
                case 1: return createRecipesSecondScreen;
                case 2: return createRecipesThirdScreen;
                default: return null;
            }
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0: return createRecipesFirstScreen;
                case 1: return createRecipesSecondScreen;
                case 2: return createRecipesThirdScreen;
                default: return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }



    }



}
