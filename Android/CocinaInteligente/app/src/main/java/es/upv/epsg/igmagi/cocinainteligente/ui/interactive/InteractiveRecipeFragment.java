package es.upv.epsg.igmagi.cocinainteligente.ui.interactive;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import es.upv.epsg.igmagi.cocinainteligente.R;

public class InteractiveRecipeFragment extends Fragment {

    private SlideshowViewModel slideshowViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_interactive_recipe_dialog, container, false);
        return root;
    }
}