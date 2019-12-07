package es.upv.epsg.igmagi.cocinainteligente.model;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.lifecycle.ViewModel;

public class RecipeViewModel extends ViewModel {
    private Recipe recipe = new Recipe();
    private String id;
    private Drawable image;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCurrentRecipe(Recipe recipe){
        this.recipe = recipe;
    }
    public void setCurrentRecipeImage(Drawable img){
        this.image = img;
    }
    public Recipe getCurrentRecipe(){
        return this.recipe;
    }
    public Drawable getCurrentRecipeImage(){return this.image;}
}
