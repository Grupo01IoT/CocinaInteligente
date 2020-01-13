package es.upv.epsg.igmagi.raspberrymanagement;

public class Step {
    public static int numSteps = 0;
    private String mode;
    private String step;
    private String trigger;
    private int pos;
    private int status;


    public Step(){}

    public Step(String mode, String step, String trigger) {
        this.mode = mode;
        this.step = step;
        this.trigger = trigger;
        this.pos = numSteps;
        this.numSteps++;
        this.status = 0;
    }

    public Step(String mode, String step) {
        this.mode = mode;
        this.step = step;
        this.pos = numSteps;
        this.numSteps++;
        this.status = 0;
    }

    public Step(String mode, String step, String trigger, int pos, int status) {
        this.mode = mode;
        this.step = step;
        this.trigger = trigger;
        this.pos = pos;
        this.status = status;
    }
    
    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


}
