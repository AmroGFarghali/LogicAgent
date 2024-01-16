package code;

import java.util.ArrayList;

public class State {
    public int neoX;
    public int neoY;
    public int neoDamage;
    public ArrayList<Hostage> allHostages;
    public ArrayList<Pill> allPills;
    public ArrayList<Agent> allAgents;
    public int kills;
    public int C;

    
    public State(int neoX, int neoY,int C, int neoDamage,int kills,ArrayList<Hostage>allHostages,ArrayList<Pill>allPills,ArrayList<Agent>allAgents)
    {

        this.neoX= neoX;
        this.neoY= neoY;
        this.kills = kills;
        this.neoDamage = neoDamage;
        this.allHostages= allHostages;
        this.allPills= allPills;
        this.allAgents= allAgents;
        this.C = C;
    }

    public int getNoCarried() {
    	int carried = 0;
    	for(int i=0; i<allHostages.size(); i++) {
    		if(allHostages.get(i).state.equals("Carried"))
    			carried++;
    	}
    	return carried;
    }
    
    public boolean inGoal() {
    	int dead = 0;
    	int saved = 0;
    	for(int i=0; i<allHostages.size(); i++) {
    		//System.out.println("lol: "+allHostages.get(i).state);
    		if(allHostages.get(i).state.equals("Dead")) {
    			
    			dead++;
    		//	System.out.println("dead x: "+neoX+", y: "+neoY);
    		}
    		else if(allHostages.get(i).state.equals("Saved")) {

    			//System.out.println("saved x: "+neoX+", y: "+neoY);
    			saved++;
    		}
    	}
    	//System.out.println("dead: "+dead+", saved: "+saved+", total: "+allHostages.size());
    	
    	if((dead+saved) == allHostages.size()) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }

}
