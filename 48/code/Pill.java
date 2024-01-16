package code;

public class Pill implements Cloneable{
    int x, y;
    String state;
    public Pill(int x, int y) {
        this.x = x;
        this.y = y;
        state = "NT"; //NOT TAKEN
    }
    public Pill clone(){

        Pill pillCopy = null;
        try {
            pillCopy = (Pill) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return pillCopy;


    }
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
