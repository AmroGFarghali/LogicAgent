package code;

import java.util.ArrayList;

public class Neo {
    int x, y, damage, capacity, kills;
    ArrayList<Hostage> aliveHostages, savedHostages, mutatedHostages, killedHostages, carriedHostages;

    public Neo(int x, int y, int capacity, int damage,ArrayList<Hostage> carriedHostages, ArrayList<Hostage> aliveHostages, ArrayList<Hostage> savedHostages, ArrayList<Hostage> mutatedHostages, ArrayList<Hostage>killedHostages ) {
        this.x = x;
        this.y = y;
        this.capacity = capacity;
        this.kills = 0;
        this.damage = damage;
        this.carriedHostages = carriedHostages;
        this.aliveHostages = aliveHostages;
        this.savedHostages = savedHostages;
        this.mutatedHostages = mutatedHostages;
        this.killedHostages = killedHostages;
    }

    public int getDamage() {
        return this.damage;
    }

    public void incDamage(int value) {
        this.damage +=value;
			/*if(this.damage == 100 && this.damage >100) {
				this.dead = true;
			}*/
    }
////////////// remove this later

    public int getX() {
        return x;
    }

    public void setX(int x) {
        //System.out.println("CHANGED THE X OF NEO");
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        //System.out.println("CHANGED THE Y OF NEO");
        this.y = y;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int c) {
        this.capacity = c;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }
    ///also an operator
    public void carryHostage(Hostage hostage) {
        carriedHostages.add(hostage);
    }
    ////bardo this since its an operator
    public void dropHostages() {
        for(int i=0;i<carriedHostages.size(); i++) {
            savedHostages.add(carriedHostages.remove(i));
        }
    }
    public ArrayList<Hostage> getHostages() {
        return aliveHostages;
    }
    public void setAllHostages(ArrayList<Hostage> allHostages) {
        this.aliveHostages = allHostages;
    }

}
