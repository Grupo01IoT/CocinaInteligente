package es.upv.epsg.igmagi.cocinainteligente.model;

public class Step {
    private String mode;
    private String step;
    private String trigger;

    public Step(String mode, String step, String trigger) {
        this.mode = mode;
        this.step = step;
        this.trigger = trigger;
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
}
