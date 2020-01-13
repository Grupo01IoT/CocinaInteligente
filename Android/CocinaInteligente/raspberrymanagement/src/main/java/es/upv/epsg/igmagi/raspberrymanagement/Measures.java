package es.upv.epsg.igmagi.raspberrymanagement;

public class Measures {

    Boolean light, fan, leak;
    int temp1, temp2, temp3, temp4, maxTemp, weight;

    public Measures() {
    }

    public Measures(Boolean light, Boolean fan, Boolean leak, int temp1, int temp2, int temp3, int temp4, int maxTemp, int weight) {
        this.light = light;
        this.fan = fan;
        this.leak = leak;
        this.temp1 = temp1;
        this.temp2 = temp2;
        this.temp3 = temp3;
        this.temp4 = temp4;
        this.maxTemp = maxTemp;
        this.weight = weight;
    }

    public Boolean getLight() {
        return light;
    }

    public void setLight(Boolean light) {
        this.light = light;
    }

    public Boolean getFan() {
        return fan;
    }

    public void setFan(Boolean fan) {
        this.fan = fan;
    }

    public Boolean getLeak() {
        return leak;
    }

    public void setLeak(Boolean leak) {
        this.leak = leak;
    }

    public int getTemp1() {
        return temp1;
    }

    public void setTemp1(int temp1) {
        this.temp1 = temp1;
    }

    public int getTemp2() {
        return temp2;
    }

    public void setTemp2(int temp2) {
        this.temp2 = temp2;
    }

    public int getTemp3() {
        return temp3;
    }

    public void setTemp3(int temp3) {
        this.temp3 = temp3;
    }

    public int getTemp4() {
        return temp4;
    }

    public void setTemp4(int temp4) {
        this.temp4 = temp4;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(int maxTemp) {
        this.maxTemp = maxTemp;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
