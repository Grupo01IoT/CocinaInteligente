package es.upv.epsg.igmagi.cocinainteligente.ui.home;

import android.app.Application;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.ImageView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import es.upv.epsg.igmagi.cocinainteligente.utils.DownloadImageTask;

public class HomeViewModel extends ViewModel {

    private Bitmap bitmap;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}