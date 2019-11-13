package es.upv.epsg.igmagi.cocinainteligente.model;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import javax.annotation.Nullable;

public class User {
    public String uid;
    public String name;
    public String email;
    public String image;
    public long fidelity;
    public Date joinDate;
    public ArrayList<String> recipes;
    public ArrayList<String> favouriteReceipts;
    public ArrayList<String> devices;

    public User() {
    }

    public User(String uid, String name, String email, String image, long fidelity, Date join, ArrayList<String> recipes, ArrayList<String> favouriteRecipes, ArrayList<String> devices) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.image = image;
        this.fidelity = fidelity;
        this.joinDate = join;
        this.recipes = recipes;
        this.favouriteReceipts = favouriteRecipes;
        this.devices = devices;
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

    public String getImage() {
        return name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getFidelity() {
        return fidelity;
    }

    public void setFidelity(int fidelity) {
        this.fidelity = fidelity;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public ArrayList<String> getRecipes() {
        return recipes;
    }

    public void setRecipes(ArrayList<String> recipes) {
        this.recipes = recipes;
    }

    public ArrayList<String> getFavouriteReceipts() {
        return favouriteReceipts;
    }

    public void setFavouriteReceipts(ArrayList<String> favouriteReceipts) {
        this.favouriteReceipts = favouriteReceipts;
    }

    public ArrayList<String> getDevices() {
        return devices;
    }

    public void setDevices(ArrayList<String> devices) {
        this.devices = devices;
    }

    public void update(DocumentSnapshot documentSnapshot) {
        this.uid = documentSnapshot.getId();
        this.name = documentSnapshot.getString("name");
        this.email = documentSnapshot.getString("email");
        this.fidelity = documentSnapshot.getLong("fidelity");
        this.joinDate = documentSnapshot.getDate("joinDate");
        this.recipes = (ArrayList<String>) documentSnapshot.get("recipes");
        this.favouriteReceipts = (ArrayList<String>) documentSnapshot.get("favouriteReceips");
        this.devices = (ArrayList<String>) documentSnapshot.get("devices");
    }
}


