package code;

public class Hostage implements Cloneable {
    int x, y, damage;
    String state; //Alive, Mutated, Carried, Dead, Saved
    Hostage(int x, int y, int damage) {
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.state = "Alive";
    }
    public Hostage clone(){

        Hostage hostageCopy = null;
        try {
            hostageCopy = (Hostage) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return hostageCopy;


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

    public int getDamage() {
        return damage;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }
    
    public void incDamage(int dmg) {
    	this.damage +=dmg;
    	if(this.damage >=100)
    		if(this.state.equals("Carried"))
    			this.state = "Dead";
    		else
    			this.state = "Mutated";
    	
    }


}
