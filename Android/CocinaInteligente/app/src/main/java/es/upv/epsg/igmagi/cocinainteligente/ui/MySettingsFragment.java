package es.upv.epsg.igmagi.cocinainteligente.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.WindowManager;

import es.upv.epsg.igmagi.cocinainteligente.R;

public class MySettingsFragment extends PreferenceFragment {

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
    public MySettingsFragment(){

    }

    @Override
    public boolean onPreferenceTreeClick (PreferenceScreen preferenceScreen,
                                          Preference preference) {

        // Initiating Dialog's layout when any sub PreferenceScreen clicked
        if(preference.getClass() == PreferenceScreen.class) {
            // Retrieving the opened Dialog
            Dialog dialog = ((PreferenceScreen) preference).getDialog();
            if(dialog == null) return false;

            initDialogLayout(dialog);   // Initiate the dialog's layout
        }
        return true;
    }

    private void initDialogLayout(Dialog dialog) {
        View fragmentView = getView();

        // Get absolute coordinates of the PreferenceFragment
        int fragmentViewLocation [] = new int[2];
        fragmentView.getLocationOnScreen(fragmentViewLocation);

        // Set new dimension and position attributes of the dialog
        WindowManager.LayoutParams wlp = dialog.getWindow().getAttributes();
        wlp.x       = fragmentViewLocation[0]; // 0 for x
        wlp.y       = fragmentViewLocation[1]; // 1 for y
        wlp.width   = fragmentView.getWidth();
        wlp.height  = fragmentView.getHeight();

        dialog.getWindow().setAttributes(wlp);

        // Set flag so that you can still interact with objects outside the dialog
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
    }
}