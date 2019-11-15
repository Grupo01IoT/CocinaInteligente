package es.upv.epsg.igmagi.raspberrymanagement;

import android.graphics.ColorSpace;

import java.util.ArrayList;
import java.util.Date;

public class Device {
    public String luces, temperatura, presencia;

    public String getLuces() {
        return luces;
    }

    public void setLuces(String luces) {
        this.luces = luces;
    }

    public String getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(String temperatura) {
        this.temperatura = temperatura;
    }

    public String getPresencia() {
        return presencia;
    }

    public void setPresencia(String presencia) {
        this.presencia = presencia;
    }

    public Device(){

    }

}
