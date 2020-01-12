package es.upv.epsg.igmagi.cocinainteligente.ui.create.thirdscreen;

import android.app.AlertDialog;
import android.app.assist.AssistStructure;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import es.upv.epsg.igmagi.cocinainteligente.R;

public class CreateRecipesThirdScreen extends Fragment {

    private LinearLayout currentSteps;
    private Button addStep;
    private CheckBox interactive;
    private int past;
    private FrameLayout finteractive;

    private LinearLayout interactiveOptions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View vista = inflater.inflate(R.layout.fragment_create_recipes_info3, container, false);

        currentSteps = vista.findViewById(R.id.currentSteps);

        addStep = vista.findViewById(R.id.addStep);
        addStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LayoutInflater li = LayoutInflater.from(getContext());
                final View stepView = li.inflate(R.layout.fragment_add_steps, null);

                if (interactive.isChecked()) {
                    String trigger;
                    if (currentSteps.getChildCount() > 1) {
                        String mode = ((Spinner) currentSteps.getChildAt(currentSteps.getChildCount() - 1).findViewById(R.id.interactiveSpinner)).getSelectedItem().toString();
                        if (!mode.equals("Manual")) {
                            trigger = ((EditText) currentSteps.getChildAt(currentSteps.getChildCount() - 1).findViewById(R.id.interactiveTrigger)).getText().toString();
                            if (trigger.equals("")) {
                                Toast.makeText(getContext(), "Please add trigger action!", Toast.LENGTH_SHORT).show();
                                return;
                            }
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
                        if (interactive.isChecked()){
                            preview.findViewById(R.id.interactiveSideMin).setVisibility(View.VISIBLE);
                            String text = ((Spinner) currentSteps.getChildAt(past).findViewById(R.id.interactiveSpinner)).getSelectedItem().toString();
                            if (!text.equals("Manual")) {
                                text = text.concat(": ");
                                if (text.equals("Tiempo: "))
                                    ((TextView) preview.findViewById(R.id.TriggerMin)).setText(((EditText) currentSteps.getChildAt(past).findViewById(R.id.interactiveTrigger)).getText().toString() + " segundos");
                                else
                                    ((TextView) preview.findViewById(R.id.TriggerMin)).setText(((EditText) currentSteps.getChildAt(past).findViewById(R.id.interactiveTrigger)).getText().toString() + " ºC");
                            }
                            ((TextView) preview.findViewById(R.id.ModeMin)).setText(text);
                        } else {
                            preview.findViewById(R.id.interactiveSideMin).setVisibility(View.GONE);
                        }
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


        interactive = vista.findViewById(R.id.interactiveRecipe);
        interactiveOptions = vista.findViewById(R.id.interactiveSide);
        finteractive = vista.findViewById(R.id.llinteractive);
        finteractive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBackgroundNew(v);
            }
        });


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

    public LinearLayout getCurrentSteps() {
        return currentSteps;
    }

    public Button getAddStep() {
        return addStep;
    }

    public CheckBox getInteractive() {
        return interactive;
    }

    public int getPast() {
        return past;
    }

    public FrameLayout getFinteractive() {
        return finteractive;
    }

    public LinearLayout getInteractiveOptions() {
        return interactiveOptions;
    }

    public boolean checkFields() {
        return (currentSteps.getChildCount() > 1);
    }
}