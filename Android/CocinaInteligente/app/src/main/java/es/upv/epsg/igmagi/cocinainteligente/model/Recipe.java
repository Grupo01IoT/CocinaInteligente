package es.upv.epsg.igmagi.cocinainteligente.model;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Recipe {

    public String uid;
    public String name;
    public String description;
    public Timestamp creationDate;
    public String picture;
    public String tipo;
    public ArrayList<String> steps;
    public HashMap<String,Long> ratings;
    public String user;
    public int duration;

    public Recipe(String uid, String name, String description, int duracion) {
        this.uid = uid;
        this.name = name;
        this.description = description;
        this.picture ="";
        this.steps = new ArrayList<String>();
        this.ratings =new HashMap<String,Long>();
        this.user = "Paco";
        this.duration = duracion;
    }

    public Recipe(String uid, String name, String description, String picture,
                  HashMap<String,Long> rating, String user, int duracion) {
        this.uid = uid;
        this.name = name;
        this.description = description;
        this.picture = picture;
        this.ratings = rating;
        this.user = user;
        this.duration = duracion;
    }

    public Recipe(String uid, String name, String description, Timestamp creationDate, String picture,
                  ArrayList<String> steps, HashMap<String,Long> rating, String user, int duracion) {
        this.uid = uid;
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
        this.picture = picture;
        this.steps = steps;
        this.ratings = (rating==null?new HashMap<String,Long>():rating);
        this.user = user;
        this.duration = duracion;
    }

    public Recipe() {
        /*this.name = "RECETA EJEMPLO";
        this.description = "descripcion de la receta ejemplo";
        this.image = "@drawable/btnluzon";
        this.duration = 50;
        this.ratings = new ArrayList<Integer>();
         */
    }

    public Recipe(String id) {
        this.uid = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public ArrayList<String> getSteps() {
        return steps;
    }

    public void setSteps(ArrayList<String> steps) {
        this.steps = steps;
    }


    public float getRatingValue() {
        //float r = (float) 33.22;
        float r = 0;
        if (ratings == null) return r;
        if(ratings.size() > 0){
            for (Long i: this.ratings.values()) {
                r+=i;
            }
            r = r/ratings.size();
        }
        return r;
    }

    public float getRatingValue(String uid) {
        float r = 0;
        if (ratings == null) return r;
        if (ratings.containsKey(uid)) return ratings.get(uid);
        else return getRatingValue();
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public HashMap<String,Long> getRatings() {
        return ratings;
    }

    public void setRatings(HashMap<String,Long> ratings) {
        this.ratings = ratings;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFormattedDuration(){
        int h;
        int min;
        String fin = "";
        if(this.duration > 59){
            h = this.duration/60;
            min = this.duration%60;

            fin = String.valueOf(h)+"h "+String.valueOf(min)+"min";
        }else{
            fin = String.valueOf(this.duration)+"min";
        }
        return fin;
    }

    public int getNumberOfRatings(){
        //return rating.size();
        return 23;
    }

    public String getFormattedNumberOfRatings() {
        //return ratings.size();
        return String.valueOf("(" + ratings.size()) + ")";
        //return "(23)";
    }

    public String data(){
        //return rating.size();
        //return String.valueOf(rating.size())+" ratings.";
        return (this.name + this.description + this.user);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean equals = ((String)((Recipe)obj).getUid()).equals((String)this.getUid());
        return equals;
    }

}
