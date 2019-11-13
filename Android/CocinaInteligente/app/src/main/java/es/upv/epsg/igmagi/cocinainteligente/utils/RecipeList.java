package es.upv.epsg.igmagi.cocinainteligente.utils;

import java.util.ArrayList;
import java.util.List;

import es.upv.epsg.igmagi.cocinainteligente.model.Recipe;

public class RecipeList {
    protected List<Recipe> recipeList;

    public RecipeList(){
        recipeList = new ArrayList<Recipe>();
        recipeList.add(new Recipe());
        recipeList.add(new Recipe("QWEWRTREW", "Macarrones", "Macarrones con tomate", 45));
    }
    public RecipeList(ArrayList<Recipe> recipeList){
        this.recipeList = recipeList;
    }
    public Recipe getRecipeById(String id){

        return null;
    }
    public Recipe getRecipeByPosition(int pos){

        return this.recipeList.get(pos);
    }
    public void add(Recipe recipe){
        recipeList.add(recipe);
    }
    public void deleteByPosition(int pos){
        recipeList.remove(pos);
    }
    public int size(){
        return recipeList.size();
    }
    public void addExamples(){
        add(new Recipe());
        add(new Recipe("QWEWRTREW", "Macarrones", "Macarrones con tomate", 45));
    }
}
