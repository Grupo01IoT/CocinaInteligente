package es.upv.epsg.igmagi.cocinainteligente.model;

import android.graphics.drawable.Drawable;

import androidx.lifecycle.ViewModel;

public class FilterViewModel extends ViewModel {

    private int index = 0;
    private boolean selected = false;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index){
        this.index = index;
    }

    public boolean hasBeenSelected(){
        return selected;
    }

    public void setSelected(boolean hasBeenSelected){
        this.selected = hasBeenSelected;
    }
    /*
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

     */
}
