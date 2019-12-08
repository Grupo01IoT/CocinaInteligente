package es.upv.epsg.igmagi.cocinainteligente.model;

import android.graphics.drawable.Drawable;

import androidx.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {
    private User user = new User();
    private String id;
    private Drawable image;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCurrentUser(User user){
        this.user = user;
    }
    public void setCurrentRecipeImage(Drawable img){
        this.image = img;
    }
    public User getCurrentUser(){
        return this.user;
    }
    public Drawable getCurrentRecipeImage(){return this.image;}
}
