package es.upv.epsg.igmagi.cocinainteligente.ui.create;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CreateRecipeSingleton {

    private static CreateRecipeSingleton instance = null;
    String recipeName, recipeDescription, recipeDuration, recipeType, uID;
    File recipePhoto;
    Boolean interactive, veggie, vegan, dairy, gluten;
    HashMap<String, Long> ratings;
    ArrayList<Object> stepList;
    Date date;
    ArrayList<String> ingredientList;


    private CreateRecipeSingleton(){ }

    public static CreateRecipeSingleton getInstance(){
        if (instance == null)
            instance = new CreateRecipeSingleton();

        return instance;
    }

    public static void setInstance(CreateRecipeSingleton instance) {
        CreateRecipeSingleton.instance = instance;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getRecipeDescription() {
        return recipeDescription;
    }

    public void setRecipeDescription(String recipeDescription) {
        this.recipeDescription = recipeDescription;
    }

    public String getRecipeDuration() {
        return recipeDuration;
    }

    public void setRecipeDuration(String recipeDuration) {
        this.recipeDuration = recipeDuration;
    }

    public String getRecipeType() {
        return recipeType;
    }

    public void setRecipeType(String recipeType) {
        this.recipeType = recipeType;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public File getRecipePhoto() {
        return recipePhoto;
    }

    public void setRecipePhoto(File recipePhoto) {
        this.recipePhoto = recipePhoto;
    }

    public Boolean getInteractive() {
        return interactive;
    }

    public void setInteractive(Boolean interactive) {
        this.interactive = interactive;
    }

    public Boolean getVeggie() {
        return veggie;
    }

    public void setVeggie(Boolean veggie) {
        this.veggie = veggie;
    }

    public Boolean getVegan() {
        return vegan;
    }

    public void setVegan(Boolean vegan) {
        this.vegan = vegan;
    }

    public Boolean getDairy() {
        return dairy;
    }

    public void setDairy(Boolean dairy) {
        this.dairy = dairy;
    }

    public Boolean getGluten() {
        return gluten;
    }

    public void setGluten(Boolean gluten) {
        this.gluten = gluten;
    }

    public HashMap<String, Long> getRatings() {
        return ratings;
    }

    public void setRatings(HashMap<String, Long> ratings) {
        this.ratings = ratings;
    }

    public ArrayList<Object> getStepList() {
        return stepList;
    }

    public void setStepList(ArrayList<Object> stepList) {
        this.stepList = stepList;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ArrayList<String> getIngredientList() {
        return ingredientList;
    }

    public void setIngredientList(ArrayList<String> ingredientList) {
        this.ingredientList = ingredientList;
    }


}
