package code;

public class Agent implements Cloneable{
    int x, y;
    String state;

    public Agent(int x, int y) {
        this.x = x;
        this.y = y;
        this.state = "Alive";
    }
    public Agent clone(){

        Agent agentCopy = null;
        try {
            agentCopy = (Agent) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return agentCopy;


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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
