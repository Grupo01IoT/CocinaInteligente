package es.upv.epsg.igmagi.cocinainteligente.ui.create.secondscreen;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import es.upv.epsg.igmagi.cocinainteligente.R;

public class CreateRecipesSecondScreen extends Fragment {

    private LinearLayout currentIngredients;
    private FrameLayout fveggie, fvegan, fdairy, fgluten;
    private Button addIngredient;
    private int past;
    private CheckBox veggie, vegan, gluten, dairy;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View vista = inflater.inflate(R.layout.fragment_create_recipes_info2, container, false);


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

        return vista;
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

    public LinearLayout getCurrentIngredients() {
        return currentIngredients;
    }

    public FrameLayout getFveggie() {
        return fveggie;
    }

    public FrameLayout getFvegan() {
        return fvegan;
    }

    public FrameLayout getFdairy() {
        return fdairy;
    }

    public FrameLayout getFgluten() {
        return fgluten;
    }

    public Button getAddIngredient() {
        return addIngredient;
    }

    public int getPast() {
        return past;
    }

    public CheckBox getVeggie() {
        return veggie;
    }

    public CheckBox getVegan() {
        return vegan;
    }

    public CheckBox getGluten() {
        return gluten;
    }

    public CheckBox getDairy() {
        return dairy;
    }

    public boolean checkFields() {
        return (currentIngredients.getChildCount() > 1);
    }
}