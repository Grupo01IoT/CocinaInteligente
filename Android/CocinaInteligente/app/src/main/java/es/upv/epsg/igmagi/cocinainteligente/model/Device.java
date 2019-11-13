package es.upv.epsg.igmagi.cocinainteligente.model;

import java.util.ArrayList;
import java.util.Date;

public class Device extends Model{
    public ArrayList<Float> cooktop;
    public Boolean fan, lights, voice;
    public Date joinDate;
    public String name;
    public String id;
    public int pin;

    public ArrayList<Float> getCooktop() {
        return cooktop;
    }

    public void setCooktop(ArrayList<Float> cooktop) {
        this.cooktop = cooktop;
    }

    public Boolean getFan() {
        return fan;
    }

    public void setFan(Boolean fan) {
        this.fan = fan;
    }

    public Boolean getLights() {
        return lights;
    }

    public void setLights(Boolean lights) {
        this.lights = lights;
    }

    public Boolean getVoice() {
        return voice;
    }

    public void setVoice(Boolean voice) {
        this.voice = voice;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }

    public Device (){

    }

}
