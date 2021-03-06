package es.upv.epsg.igmagi.cocinainteligente.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import es.upv.epsg.igmagi.cocinainteligente.ui.home.HomeFragment;

public class DownloadImageTask extends AsyncTask<Uri, Void, Bitmap> {
    ImageView bmImage;
    Resources res;
    Boolean rounded = true;

    public DownloadImageTask(ImageView bmImage, Resources r) {
        this.bmImage = bmImage;
        res = r;
    }

    public DownloadImageTask(ImageView image, Resources r, boolean b) {
        this.bmImage = image;
        res = r;
        rounded = b;
    }

    public DownloadImageTask(Resources system) {
        res = system;
    }


    @Override
    protected Bitmap doInBackground(Uri... uris) {
        Uri urldisplay = uris[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay.toString()).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        if (rounded){
            RoundedBitmapDrawable rbd = RoundedBitmapDrawableFactory.create(res, result);
            rbd.setCircular(true);
            bmImage.setImageDrawable(rbd);
        } else {
            bmImage.setImageBitmap(result);
        }
    }
}
