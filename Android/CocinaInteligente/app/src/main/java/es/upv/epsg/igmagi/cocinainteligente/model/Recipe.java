package es.upv.epsg.igmagi.cocinainteligente.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Recipe {

    private String uid;
    private String name;
    private String desciption;
    private Date creationDate;
    private String picture;
    private ArrayList<String> steps;
    private ArrayList<Integer> rating;
    private String user;
    private int duration;

    public Recipe(String uid, String name, String desciption, int duracion) {
        this.uid = uid;
        this.name = name;
        this.desciption = desciption;
        this.picture ="";
        this.steps = new ArrayList<String>();
        this.rating = new ArrayList<Integer>();
        this.user = "Paco";
        this.duration = duracion;
    }

    public Recipe(String uid, String name, String desciption, String picture,
                  ArrayList<Integer> rating, String user, int duracion) {
        this.uid = uid;
        this.name = name;
        this.desciption = desciption;
        this.picture = picture;
        this.rating = rating;
        this.user = user;
        this.duration = duracion;
    }

    public Recipe(String uid, String name, String desciption, Date creationDate, String picture,
                  ArrayList<String> steps, ArrayList<Integer> rating, String user, int duracion) {
        this.uid = uid;
        this.name = name;
        this.desciption = desciption;
        this.creationDate = creationDate;
        this.picture = picture;
        this.steps = steps;
        this.rating = rating;
        this.user = user;
        this.duration = duracion;
    }

    public Recipe() {
        this.name = "RECETA EJEMPLO";
        this.desciption = "descripcion de la receta ejemplo";
        this.picture = "@drawable/btnluzon";
        this.duration = 50;
        this.rating = new ArrayList<Integer>();

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

    public String getDesciption() {
        return desciption;
    }

    public void setDesciption(String desciption) {
        this.desciption = desciption;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
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

    public ArrayList<Integer> getRating() {
        return rating;
    }
    public float getRatingValue() {
        //float r = (float) 33.22;
        float r = 0;
        if(rating.size() > 0){

            for (int i: this.rating) {
                r+=i;
            }
            r = r/rating.size();
        }
        return r;
    }
    public void setRating(ArrayList<Integer> rating) {
        this.rating = rating;
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
    public String getFormattedNumberOfRatings(){
        //return rating.size();
        //return String.valueOf(rating.size())+" ratings.";
        return "(23)";

    }

}
