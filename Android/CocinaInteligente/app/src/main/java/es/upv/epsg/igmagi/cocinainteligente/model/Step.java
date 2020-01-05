package es.upv.epsg.igmagi.cocinainteligente.model;

public class Step {
    public static int numSteps = 0;
    private String mode;
    private String step;
    private String trigger;
    private int pos;


    public Step(String mode, String step, String trigger) {
        this.mode = mode;
        this.step = step;
        this.trigger = trigger;
        pos = numSteps;
        numSteps++;
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
}
