package p16.model;

public class Parametre {
    boolean status;
    String value;

    public Parametre(boolean status, String value) {
        this.status = status;
        this.value = value;
    }

    public Parametre() {
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
