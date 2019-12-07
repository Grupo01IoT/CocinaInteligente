package es.upv.epsg.igmagi.cocinainteligente.model;

import android.text.format.DateFormat;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;

import java.util.Calendar;
import java.util.Locale;

public class Comment {

    public String id;
    public String author;
    public String body;
    public Timestamp date;
    public String image;
    public String recipe;

    public Comment() {
    }

    public Comment(String author, String body, Timestamp creationDate, String picture,String recipe) {
        this.author = author;
        this.body = body;
        this.date = creationDate;
        this.image = picture;
        this.recipe = recipe;
    }

    public Comment(String id, String author, String body, Timestamp date, String image) {
        this.id = id;
        this.author = author;
        this.body = body;
        this.date = date;
        this.image = image;
    }

    public Comment(String id) {
        this.id = id;
    }

    public String getRecipe() {
        return recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }
    public String getFormattedDate(){
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(getDate().getSeconds());
        return DateFormat.format("dd-MM-yyyy", cal).toString();
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean equals = ((String)((Comment)obj).getId()).equals((String)this.getId());
        return equals;
    }
}
