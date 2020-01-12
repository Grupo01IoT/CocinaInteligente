package es.upv.epsg.igmagi.cocinainteligente.ui.create.firstscreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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

import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.ui.create.CreateRecipeSingleton;
import es.upv.epsg.igmagi.cocinainteligente.ui.create.CreateRecipesFragment;

public class CreateRecipesFirstScreen extends Fragment {

    private EditText recipeName;
    private EditText recipeDescription;
    private EditText recipeDuration;
    private LinearLayout llextra;
    private Spinner recipeSp;
    private ImageView recipePhoto;
    private File file;

    public EditText getRecipeName() {
        return recipeName;
    }

    public EditText getRecipeDescription() {
        return recipeDescription;
    }

    public EditText getRecipeDuration() {
        return recipeDuration;
    }

    public LinearLayout getLlextra() {
        return llextra;
    }

    public Spinner getRecipeSp() {
        return recipeSp;
    }

    public ImageView getRecipePhoto() {
        return recipePhoto;
    }

    public File getFile() {
        return file;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View vista = inflater.inflate(R.layout.fragment_create_recipes_info1, container, false);


        CreateRecipeSingleton createRecipeSingleton = CreateRecipeSingleton.getInstance();

        recipeName = vista.findViewById(R.id.recipeName);
        recipeDescription = vista.findViewById(R.id.recipeDescription);
        recipeDuration = vista.findViewById(R.id.recipeDuration);
        llextra = vista.findViewById(R.id.linearLayout8);
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

        verifyFields();

        return vista;
    }

    public void verifyFields(){
        recipeName.addTextChangedListener(new MyTextWatcher(recipeName, getContext()));
        recipeDescription.addTextChangedListener(new MyTextWatcher(recipeDescription, getContext()));
        recipeDuration.addTextChangedListener(new MyTextWatcher(llextra, getContext()));
    }

    public boolean checkFields() {

        if (!recipeDescription.getText().toString().matches("\"^(?!\\s*$).+\"")) {
            if (!recipeName.getText().toString().matches("\"^(?!\\s*$).+\"")) {
                if (recipeDuration.getText().toString().matches("[0-9]+")) {
                        return true;
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


}
