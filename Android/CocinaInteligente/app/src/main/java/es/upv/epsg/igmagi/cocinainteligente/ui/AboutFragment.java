package es.upv.epsg.igmagi.cocinainteligente.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import es.upv.epsg.igmagi.cocinainteligente.R;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_about, container, false);
        Button help = root.findViewById(R.id.buttonHelp);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"cocinainteligenteiot@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Contacto con el servicio técnico");
                i.putExtra(Intent.EXTRA_TEXT   , "Escríbenos tu problema.");
                try {
                    startActivity(Intent.createChooser(i, "Enviar correo desde..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "No hay apps disponibles para enviar correos.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return root;
    }

}
