package code;
import java.util.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Matrix {

    public static void main(String[] args) {

		/*
		 5,5;2;0,4;1,4;0,1,1,1,2,1,3,1,3,3,3,4;1,0,2,4;0,3,4,3,4,3,0,3;0,0,30,3,0,80,4,4,80
		*/
    	ArrayList<Cell> testing = new ArrayList<Cell>();
    	testing.add(new Cell(0,0));

    	
        Matrix main = new Matrix();
       // main.solve("5,5;2;3,4;1,2;0,3,1,4;2,3;4,4,0,2,0,2,4,4;2,2,91,2,4,62","BF",false);
        main.solve("5,5;4;1,1;4,1;2,4,0,4,3,2,3,0,4,2,0,1,1,3,2,1;4,0,4,4,1,0;2,0,0,2,0,2,2,0;0,0,62,4,3,45,3,3,39,2,3,40","AS2",false);
    }
    
    public static boolean arrContain(ArrayList<Cell> arr, Cell cell) {
    	for(int i=0; i<arr.size(); i++) {
    		if(arr.get(i).getX() == cell.getX() && arr.get(i).getY() == cell.getY()) {
    			return true;
    		}
    	}
    	return false;
    }

    static Neo neo;
    static ArrayList<Agent> agents;
    static ArrayList<Hostage> hostages;
    static int hostNo;
    static ArrayList<Pad> Pad;
    static ArrayList<code.Pill> Pill;
    static TelephoneBooth telephone;
    static String[][] gridArray;
    static int m = getRandomNumber(5, 15); // vertical
    static int n = getRandomNumber(5, 15); // horizontal
    static Cell[] hostageCells;
    static String mainGrid = "";
    boolean[][] vis = new boolean[m][n];
    
    static int c;

    public static Operators operationFinder(int i) {
        //north, south, east, west
        if(i==0)
            return Operators.up;
        if(i==1)
            return Operators.down;
        if(i==2)
            return Operators.right;
        if(i==3)
            return Operators.left;
        return null;
    }
    
    
    public static boolean canCarry(ArrayList<Hostage> hostages) {
    	int carried = 0;
    	for(int i=0; i<hostages.size(); i++) {
    		if(hostages.get(i).getState().equals("Carried"))
    			carried+=1;
    	}
    	return c > carried;
    }
    
    
    public static int heuristic1(Node node) {
    	//Taking into account Neos Damage and Hostage deaths
    	int dead = 0;
    	for(int i=0; i<node.currentState.allHostages.size(); i++) {
    		if(node.currentState.allHostages.get(i).getState().equals("Dead") || 
    				node.currentState.allHostages.get(i).getState().equals("Mutated")) {
    			dead++;
    		}
    	}
    	return node.currentState.neoDamage + dead;
    }
    
    
    public static int calcDistance(int a, int b, int x, int y) {
    	int distance = Math.abs(a-x) + Math.abs(b-y); // |a - x| + |b - y|
    	int result = distance;
    	for(int i=0; i<Pad.size(); i++) {
    		int padDistanceStart =  Math.abs(a-Pad.get(i).getStartX()) + Math.abs(b-Pad.get(i).getStartY());
    		int padDistanceEnd =  Math.abs(Pad.get(i).getEndX()-x) + Math.abs(Pad.get(i).getEndY()-y);
    		
    		result = Math.min(result, Math.min(padDistanceStart, padDistanceEnd));
    		//distance = //minimum of the distance and the distance using pad
    	}
    	return result;
    }

    
    public static int heuristic2(Node node) {
    	////Count of hostage deaths and agent kills
    	int dead = 0;
    	for(int i=0; i<node.currentState.allHostages.size(); i++) {
    		if(node.currentState.allHostages.get(i).getState().equals("Dead") || 
    				node.currentState.allHostages.get(i).getState().equals("Mutated")) {
    			dead++;
    		}
    	}
    	return node.currentState.kills + dead;
    }
    

    public static String AStar2(String[][] grid) {
        int[] dx = { -1, 1, 0, 0 };
        int[] dy = { 0, 0, 1, -1 };
        int nodesExpanded = 0;
        HashSet<String> hashedStates = new HashSet<String>();

        hostNo = hostages.size();
        
      //  Comparator<Node> comparator =
		
        PriorityQueue<Node> mainQueue = new PriorityQueue<Node>(1000, new Comparator<Node>() {
        	public int compare (Node n1, Node n2) {
        		
                if((heuristic2(n1)+n1.cost) > (heuristic2(n2)+n2.cost)) {
                	//System.out.println("n1: "+heuristic1(n1)+", n2: "+ heuristic1(n2));
                    return 1;
                } else if ((heuristic2(n1)+n1.cost) < (heuristic2(n2)+n2.cost)) {
                    return -1;
                } else {
                    return 0;
                }
        	}
        });
        
        State startState = new State(neo.getX(), neo.getY(),c,0,0, hostages ,Pill, agents);

        Node start = new Node(startState, null, null , 0, 0);

        mainQueue.add(start);



        while(!mainQueue.isEmpty()){
        	
           	/*for(int q=0; q<mainQueue.size(); q++) {
        		System.out.println(mainQueue.);
        	}*/
        	boolean hostageTaken = false;
            boolean pillTAKE = false;
            boolean padFly = false;
            boolean allHostagesSaved=true;
            Node currentNode = mainQueue.peek(); //el node ele hn3mlha dequeue from the front
            int x = currentNode.currentState.neoX; //tb3n lazem nghyha l private w n3melha getMETHOD bas lateron
            int y =currentNode.currentState.neoY;
            mainQueue.remove();

            //System.out.println(currentNode.cost);
            //all the checks if we are on H, pad, TB

            if (grid[x][y].charAt(0) == 'H' ) { ////////////you move first to that node and then drop/carry hostage
                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                for (Hostage h : currentNode.currentState.allHostages){
                    newHostages.add(h.clone());
                }
                for(int h=0; h<newHostages.size(); h++){
                    ///////////lazem n3mel condition law hostage mat fe edena
                    //// NOTE: DO CAPACITY LATER
                    if (newHostages.get(h).getX()==x &&newHostages.get(h).getY()==y) {//get hostage
                        ////carry the dead hostage as well i think?
                        if (newHostages.get(h).getState().equals("Alive")) {
                            // is it the next node or this node tho?
                            newHostages.get(h).setState("Carried");
                            
                            ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                            
                            for (Hostage h2 : newHostages) {
                                newHostages2.add(h2.clone());
                            }
                            int hostagesThatDied=0;
                            for(int h3=0; h3<newHostages2.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                newHostages2.get(h3).setDamage(newHostages2.get(h3).getDamage()+2);
                                if(newHostages2.get(h3).getDamage()>=100  )

                                    if(newHostages2.get(h3).getState().equals("Carried")) {
                                        newHostages2.get(h3).setState("Dead");
                                        hostagesThatDied++;
                                    }
                                    else if(newHostages2.get(h3).getState().equals("Alive")) {
                                    	newHostages2.get(h3).setState("Mutated");
                                    	hostagesThatDied++;
                                    }
                            }

                            if(canCarry(currentNode.currentState.allHostages)){
                            	Node tempNode = new Node(new State(x,y,c,currentNode.currentState.neoDamage,currentNode.currentState.kills,newHostages2, currentNode.currentState.allPills, currentNode.currentState.allAgents),Operators.carry,currentNode, currentNode.depth++,currentNode.cost+3+(hostagesThatDied*3000));	
                                mainQueue.add(tempNode);
                                hostageTaken = true;
                                nodesExpanded++;
                                break;
                            }/*else {
                            	Node tempNode = new Node(new State(x,y,c,currentNode.currentState.neoDamage,currentNode.currentState.kills,currentNode.currentState.allHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents),Oper,currentNode, currentNode.depth++);
                                mainQueue.add(tempNode);
                            }*/
                            //mainQueue.add(tempNode);
                            
                          
                            //shofo law alive
                        }
                    }
                }
            }
            if (grid[x][y].charAt(0) == 'T' ) { ////////////if there is any hostages to be dropped then add a node after every hostage is dropped, else dont add node
                boolean carriedHostages = false;
                    ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                    for (Hostage h : currentNode.currentState.allHostages) {
                        newHostages.add(h.clone());
                    }
                    for (int h = 0; h < newHostages.size(); h++) {
                        ///////////lazem n3mel condition law hostage mat fe edena
                        //// NOTE: DO CAPACITY LATER
                        if (newHostages.get(h).getState().equals("Carried")) {//get hostage
                            carriedHostages = true;
                            break;
                        }
                    }
                    if (carriedHostages) {
                        for (int h = 0; h < newHostages.size(); h++) {
                            if (newHostages.get(h).getState().equals("Carried")) {//get hostage
                                newHostages.get(h).setState("Saved");
                            }
                        }
                        int hostagesThatDied=0;	
                        ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                        for (Hostage h2 : newHostages) {
                            newHostages2.add(h2.clone());
                        }
                        for (int h3 = 0; h3 < newHostages2.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                            newHostages2.get(h3).setDamage(newHostages2.get(h3).getDamage() + 2);
                            if (newHostages2.get(h3).getDamage() >= 100)
                                if (newHostages2.get(h3).getState().equals("Carried")) {
                                    newHostages2.get(h3).setState("Dead");
                                    hostagesThatDied++;
                                }
                                else if (newHostages2.get(h3).getState().equals("Alive")) {
                                    newHostages2.get(h3).setState("Mutated");
                                    hostagesThatDied++;
                                }
                        }
                        Node tempNode = new Node(new State(x, y,c , currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages2, currentNode.currentState.allPills, currentNode.currentState.allAgents), Operators.drop, currentNode, currentNode.depth++,currentNode.cost+3+(hostagesThatDied*3000));
                        mainQueue.add(tempNode);
                        nodesExpanded++;
                    }



            }

            if(grid[x][y].charAt(0) == 'P' && grid[x][y].length()==1 )
            { /////DOES HE HAVE TO TAKE PILL IF HE IS ON IT?

                ArrayList<Pill> newPills = new ArrayList<Pill>();
                for (Pill p : currentNode.currentState.allPills){
                    newPills.add(p.clone());
                }
                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                for (Hostage h : currentNode.currentState.allHostages){
                    newHostages.add(h.clone());
                }

                for(int p=0; p<newPills.size(); p++)
                {
                    if (newPills.get(p).getX()==x &&newPills.get(p).getY()==y)
                    {
                        if(newPills.get(p).state.equals("NT"))
                        {
                            newPills.get(p).setState("T");
                            for(int h=0; h<newHostages.size();h++){

                                newHostages.get(h).setDamage(newHostages.get(h).getDamage()-20);
                                if(newHostages.get(h).getDamage()<0) {
                                	  newHostages.get(h).setDamage(0);
                                }
                            }
                                ////////CLONE THE OLD CLONED ONE AFTER SETTING DAMAGE
                           

                            Node tempNode = new Node(new State(x,y,c,(currentNode.currentState.neoDamage)-20,currentNode.currentState.kills,newHostages, newPills, currentNode.currentState.allAgents),Operators.takePill,currentNode, currentNode.depth++,currentNode.cost+3);
                            mainQueue.add(tempNode);
                            nodesExpanded++;
                            pillTAKE=true;


                            break;
                        }
                        else if(newPills.get(p).state.equals("T"))
                        {

                        }
                    }
                }
            }


            if(grid[x][y].charAt(0) == 'P' && grid[x][y].length()!=1 ){

                for (int j = 0; j < Pad.size(); j++) {
                    Pad pad = Pad.get(j);

                    if (pad.getStartX() == x && pad.getStartY() == y) {

                        String stateToBeHashed = makeHashedState(currentNode, 100, Operators.fly);
                        if(hashedStates.contains(stateToBeHashed)) {
                            break;
                        }
                        else {
                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                newHostages.add(h2.clone());
                            }
                            int hostagesThatDied=0;
                            for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                if (newHostages.get(h3).getDamage() >= 100)
                                    if (newHostages.get(h3).getState().equals("Carried")) {
                                        newHostages.get(h3).setState("Dead");
                                        hostagesThatDied++;}
                                    else if (newHostages.get(h3).getState().equals("Alive")) {
                                        newHostages.get(h3).setState("Mutated");
                                        hostagesThatDied++;
                                    }
                            }
                            if(!(currentNode.operator == Operators.fly)) {

                                Node tempNode = new Node(new State(pad.getEndX(), pad.getEndY(),c, currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), Operators.fly, currentNode, currentNode.depth++,currentNode.cost+3+hostagesThatDied*3000);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                                hashedStates.add(stateToBeHashed);

                                padFly=true;
                                break;
                            }
                            else{
                                break;
                            }
                        }
                    }
                }

            }


            for(int h=0; h<currentNode.currentState.allHostages.size(); h++){
                ///////////lazem n3mel condition law hostage mat fe edena
                if (currentNode.currentState.allHostages.get(h).state.equals("Alive")
                    ||currentNode.currentState.allHostages.get(h).state.equals("Carried") ||currentNode.currentState.allHostages.get(h).state.equals("Mutated") ) {
                             allHostagesSaved = false;

                    break;
                }
            }
            if(allHostagesSaved) {
                //out.println("Solution reached at depth" + currentNode.depth);
                //System.out.println(finalPrint(currentNode));
            }
        if(currentNode.currentState.neoDamage<100 && !hostageTaken ){

            if(!allHostagesSaved) { ///el hostages are not all saved yet

                /////////////////ELE T7T KOLO WAS JUST 2 LINES WITHOUT HASHING
                for(int i=0; i<4; i++) {   //this checks all neighbors by taking the array by up down right and left in this order


                    int col = x + dx[i];
                    int row = y + dy[i];

                    if (col < 0 || row < 0 || col>=m || row>=n) {

                    } else {


                        if (grid[col][row].charAt(0) == 'H' )  /// NOTE
                        { ////////////NOTE:MOVE TO THE NODE IF ITS NOT MUTATED, KILL IF MUTATED BY IF CONDITION THEN MOVE NEXT ITER


                                boolean hostageMutated = false;  ////////////tb3n brdo lazem n2tel ay hostage in the vicinity or agent if we do a kill action
                                for(int h=0; h<currentNode.currentState.allHostages.size(); h++) {
                                    ///////////lazem n3mel condition law hostage mat fe edena
                                    //// NOTE: DO CAPACITY LATER
                                    if (currentNode.currentState.allHostages.get(h).getX() == col && currentNode.currentState.allHostages.get(h).getY() == row) {//get hostage
                                        ////carry the dead hostage as well i think?
                                        if (currentNode.currentState.allHostages.get(h).getState().equals("Mutated")) {

                                            //KILL IF MUTATED
                                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                                newHostages.add(h2.clone());
                                            }
                                           ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                          
                                            int hostageKills = 0;
                                            int agentKills = 0;
                                            //newAgents.get(a).setState("Dead");
                                         
                                            newHostages.get(h).setState("Dead");
                                            hostageKills++;
                                            ArrayList<Agent> newAgents = new ArrayList<Agent>();
                                            for (Agent a : currentNode.currentState.allAgents){
                                                newAgents.add(a.clone());
                                            }

                                            int[] dx2 = { -1, 1, 0, 0 };
                                            int[] dy2= { 0, 0, 1, -1 };
                                            ///////////KILL ALL NEIGHBORING AGENTS IF THERE IS


                                            for(int k=0; k<4;k++) {
                                                        //KILLING ALL AGENTS IS DONE 5ALAS
                                                int col2 = x + dx2[k];
                                                int row2 = y + dy2[k];
                                                if (col2 < 0 || row2 < 0 || col2>=m || row2>=n) {

                                                } else {
                                                if (grid[col2][row2].charAt(0) == 'H') {
                                                    for (int a2 = 0; a2 < newHostages.size(); a2++) {
                                                        if (newHostages.get(a2).getX() == col2 && newHostages.get(a2).getY() == row2) {

                                                            if (newHostages.get(a2).state.equals("Mutated")) {
                                                            	newHostages.get(a2).setState("Dead");
                                                                hostageKills++;

                                                            }
                                                        }
                                                        }
                                                    }
                                                
                                                if (grid[col2][row2].charAt(0) == 'A') {
                                                    for (int a2 = 0; a2 < newAgents.size(); a2++) {
                                                        if (newAgents.get(a2).getX() == col2 && newAgents.get(a2).getY() == row2) {

                                                            if (newAgents.get(a2).state.equals("Alive")) {
                                                                newAgents.get(a2).setState("Dead");
                                                                agentKills++;

                                                            }
                                                        }
                                                        }
                                                    }
                                                
                                                
                                                }

                                            }		
                                            	
                                            ///INCUR DMG
                                            ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                                            for (Hostage h4 : newHostages) {
                                                newHostages2.add(h4.clone());
                                            }
                                            int hostagesThatDied=0;
                                            for(int h5=0; h5<newHostages2.size(); h5++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                                newHostages2.get(h5).setDamage(newHostages2.get(h5).getDamage()+2);
                                                if(newHostages2.get(h5).getDamage()>=100)
                                                    if(newHostages2.get(h5).getState().equals("Carried")) {
                                                        newHostages2.get(h5).setState("Dead");
                                                    hostagesThatDied++;
                                                    }
                                                    else if(newHostages2.get(h5).getState().equals("Alive")) {
                                                        newHostages2.get(h5).setState("Mutated");
                                                        hostagesThatDied++;
                                                        
                                                    }
                                            }
                                            Node tempNode = new Node(new State(x, y,c , currentNode.currentState.neoDamage+20, currentNode.currentState.kills+agentKills+hostageKills, newHostages2, currentNode.currentState.allPills, currentNode.currentState.allAgents), Operators.kill, currentNode, currentNode.depth++,currentNode.cost+(3000 * agentKills)+(3*hostageKills) +(3000*hostagesThatDied));

                                            mainQueue.add(tempNode);

                                            nodesExpanded++;
                                            hostageMutated = true;
                                            break;

                                        }


                                    }
                                }
                                    /////////GO HERE IF HOSTAGE ISNT MUTATED
                                if(!hostageMutated)
                                {
                                    String stateToBeHashed = makeHashedState(currentNode, i,null);
                                    if(hashedStates.contains(stateToBeHashed)){
                                    }
                                    else {
                                        ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                        for (Hostage h2 : currentNode.currentState.allHostages) {
                                            newHostages.add(h2.clone());
                                        }
                                        int hostagesThatDied=0;
                                        for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                            newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                            if (newHostages.get(h3).getDamage() >= 100)
                                                if (newHostages.get(h3).getState().equals("Carried")) {
                                                    newHostages.get(h3).setState("Dead");
                                                     hostagesThatDied++;
                                                }
                                                else if (newHostages.get(h3).getState().equals("Alive")) {
                                                    newHostages.get(h3).setState("Mutated");
                                                    hostagesThatDied++;
                                                }
                                        }
                                        boolean isHeGoingToTurn = false;
                                        for(int h4 = 0; h4 < newHostages.size(); h4++) {
                                     	   if (newHostages.get(h4).getX() == col && newHostages.get(h4).getY() == row) //get hostage
                                               ////carry the dead hostage as well i think?
                                                if (newHostages.get(h4).getDamage()>=98 && newHostages.get(h4).getState().equals("Alive"))
                                             	   isHeGoingToTurn = true;
                                        }
                                        if(!isHeGoingToTurn) {

                                        Node tempNode = new Node(new State(col, row, c, currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,currentNode.cost+3+hostagesThatDied*3000);
                                        hashedStates.add(stateToBeHashed);
                                        mainQueue.add(tempNode);
                                        nodesExpanded++;
                                        }
                                    }
                                }
                            }





                        else if(grid[col][row].charAt(0) == 'T')
                        {
                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){

                            }

                            else {  ///////////////hostages might have to be dropped in the next iteration
                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                int hostagesThatDied=0;
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried")) {
                                            newHostages.get(h3).setState("Dead");
                                             hostagesThatDied++;
                                        }
                                        else if(newHostages.get(h3).getState().equals("Alive")) {
                                            newHostages.get(h3).setState("Mutated");
                                             hostagesThatDied++;
                                        }
                                }



                                Node tempNode = new Node(new State(col, row, c, currentNode.currentState.neoDamage, currentNode.currentState.kills,newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,currentNode.cost+3 +(hostagesThatDied*3000));
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }

                        else if(grid[col][row].charAt(0) == 'A' ){  ///////////////////NOTE NOTE NOTELAW FEE AGENT GNBY KAMAN GHEER EL NEIGHBOR DA THEN KILL THEM ALL
                            ArrayList<Agent> newAgents = new ArrayList<Agent>();
                            for (Agent a : currentNode.currentState.allAgents){
                                newAgents.add(a.clone());
                            }

                            for(int a=0; a<newAgents.size(); a++)
                            {

                                if (newAgents.get(a).getX()==col &&newAgents.get(a).getY()==row)
                                {

                                    if(newAgents.get(a).state.equals("Alive"))
                                    {

                                        ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                        for (Hostage h2 : currentNode.currentState.allHostages) {
                                            newHostages.add(h2.clone());
                                        }
                                       
                                        int agentKills = 0;
                                        int hostageKills = 0;
                                        newAgents.get(a).setState("Dead");
                                        agentKills++;
                                        int[] dx2 = { -1, 1, 0, 0 };
                                        int[] dy2= { 0, 0, 1, -1 };
                                        ///////////KILL ALL NEIGHBORING AGENTS IF THERE IS


                                        for(int k=0; k<4;k++) {
                                                    //KILLING ALL AGENTS IS DONE 5ALAS
                                            int col2 = x + dx2[k];
                                            int row2 = y + dy2[k];
                                            if (col2 < 0 || row2 < 0 || col2>=m || row2>=n) {

                                            } else {
                                            if (grid[col2][row2].charAt(0) == 'A') {
                                                for (int a2 = 0; a2 < newAgents.size(); a2++) {
                                                    if (newAgents.get(a2).getX() == col2 && newAgents.get(a2).getY() == row2) {

                                                        if (newAgents.get(a2).state.equals("Alive")) {
                                                            newAgents.get(a2).setState("Dead");
                                                            agentKills++;

                                                        }
                                                    }
                                                    }
                                                }
                                            if (grid[col2][row2].charAt(0) == 'H') {
                                                for (int a2 = 0; a2 < newHostages.size(); a2++) {
                                                    if (newHostages.get(a2).getX() == col2 && newHostages.get(a2).getY() == row2) {

                                                        if (newHostages.get(a2).state.equals("Mutated")) {
                                                        	newHostages.get(a2).setState("Dead");
                                                            hostageKills++;

                                                        }
                                                    }
                                                    }
                                        }
                                            }

                                        }
                                        int hostagesThatDied=0;
                                        for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                            newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                            if(newHostages.get(h3).getDamage()>=100)
                                                if(newHostages.get(h3).getState().equals("Carried")) {
                                                    newHostages.get(h3).setState("Dead");
                                                    hostagesThatDied++;
                                                }
                                                else if(newHostages.get(h3).getState().equals("Alive")) {
                                                    newHostages.get(h3).setState("Mutated");
                                                    hostagesThatDied++;
                                                }
                                        }
                                        Node tempNode = new Node(new State(x,y,c,(currentNode.currentState.neoDamage)+20,currentNode.currentState.kills+agentKills+hostageKills,newHostages, currentNode.currentState.allPills, newAgents),Operators.kill,currentNode, currentNode.depth++,currentNode.cost+(3000*agentKills) +(3*hostageKills) +3000*hostagesThatDied);

                                        mainQueue.add(tempNode);
                                        nodesExpanded++;
                                        break;
                                    }
                                    else if(newAgents.get(a).state.equals("Dead"))
                                    {
                                        String stateToBeHashed = makeHashedState(currentNode, i,null);
                                        if(hashedStates.contains(stateToBeHashed)){
                                        }
                                        else {
                                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                                newHostages.add(h2.clone());
                                            }
                                            int hostagesThatDied=0;
                                            for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                               
                                                newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                                if (newHostages.get(h3).getDamage() >= 100)
                                                    if (newHostages.get(h3).getState().equals("Carried")) {
                                                        newHostages.get(h3).setState("Dead");
                                                		 hostagesThatDied++;}
                                                    else if (newHostages.get(h3).getState().equals("Alive")) {
                                                        newHostages.get(h3).setState("Mutated");
                                                		 hostagesThatDied++;}

                                            }

                                            Node tempNode = new Node(new State(col, row,c, (currentNode.currentState.neoDamage), currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, newAgents), operationFinder(i), currentNode, currentNode.depth++,currentNode.cost+3+hostagesThatDied*3000);
                                            mainQueue.add(tempNode);
                                            hashedStates.add(stateToBeHashed);
                                            nodesExpanded++;
                                        }
                                    }
                                }
                            }
                        }
                        else if (grid[col][row].charAt(0) == 'P')
                        {


                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){
                            }
                            else {

                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                int hostagesThatDied=0;
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried")) {
                                            newHostages.get(h3).setState("Dead");
                                    	 hostagesThatDied++;}
                                        else if(newHostages.get(h3).getState().equals("Alive")) {
                                            newHostages.get(h3).setState("Mutated");
                                    		 hostagesThatDied++;}

                                }

                                Node tempNode = new Node(new State(col, row,c, (currentNode.currentState.neoDamage),currentNode.currentState.kills, newHostages,currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,currentNode.cost+3+hostagesThatDied*3000);
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }
                        else if(grid[col][row].charAt(0) == '-'   ){ //GO TO PILL / EMPTY /PAD
                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){
                                }
                            else {

                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                int hostagesThatDied=0;
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried")) {
                                            newHostages.get(h3).setState("Dead");
                                            hostagesThatDied++;}
                                        else if(newHostages.get(h3).getState().equals("Alive")) {
                                            newHostages.get(h3).setState("Mutated");
                                            hostagesThatDied++;
                                        }
                                }

                                Node tempNode = new Node(new State(col, row, c, (currentNode.currentState.neoDamage),currentNode.currentState.kills, newHostages,currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,currentNode.cost+3+3000*hostagesThatDied);
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }

                    }


                }

            }
            else {
            	//System.out.println(c);
                int deadHostages = 0;
                for (int h = 0; h < currentNode.currentState.allHostages.size(); h++) {
                    if (currentNode.currentState.allHostages.get(h).getState().equals("Dead"))
                         deadHostages++;

                }
                if(currentNode.currentState.neoX == telephone.x &&currentNode.currentState.neoY == telephone.y) {
                    System.out.println("Solution reached at depth" + currentNode.depth);
                    
                    String output = (finalPrint(currentNode) + ";" +deadHostages  + ";" +  currentNode.currentState.kills+ ";" + nodesExpanded);
                    System.out.println(output);
                    return output;
                }


            }

        }

        }
        
        return "No Solution";
    } 
   

    public static String AStar1(String[][] grid) {
        int[] dx = { -1, 1, 0, 0 };
        int[] dy = { 0, 0, 1, -1 };
        int nodesExpanded = 0;
        HashSet<String> hashedStates = new HashSet<String>();

        hostNo = hostages.size();
        
      //  Comparator<Node> comparator =
		
        PriorityQueue<Node> mainQueue = new PriorityQueue<Node>(1000, new Comparator<Node>() {
        	public int compare (Node n1, Node n2) {
        		
                if((heuristic1(n1)+n1.cost) > (heuristic1(n2)+n2.cost)) {
                	//System.out.println("n1: "+heuristic1(n1)+", n2: "+ heuristic1(n2));
                    return 1;
                } else if ((heuristic1(n1)+n1.cost) < (heuristic1(n2)+n2.cost)) {
                    return -1;
                } else {
                    return 0;
                }
        	}
        });
        
        State startState = new State(neo.getX(), neo.getY(),c,0,0, hostages ,Pill, agents);

        Node start = new Node(startState, null, null , 0, 0);

        mainQueue.add(start);



        while(!mainQueue.isEmpty()){
        	
           	/*for(int q=0; q<mainQueue.size(); q++) {
        		System.out.println(mainQueue.);
        	}*/
        	boolean hostageTaken = false;
            boolean pillTAKE = false;
            boolean padFly = false;
            boolean allHostagesSaved=true;
            Node currentNode = mainQueue.peek(); //el node ele hn3mlha dequeue from the front
            int x = currentNode.currentState.neoX; //tb3n lazem nghyha l private w n3melha getMETHOD bas lateron
            int y =currentNode.currentState.neoY;
            mainQueue.remove();

            //System.out.println(currentNode.cost);
            //all the checks if we are on H, pad, TB

            if (grid[x][y].charAt(0) == 'H' ) { ////////////you move first to that node and then drop/carry hostage
                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                for (Hostage h : currentNode.currentState.allHostages){
                    newHostages.add(h.clone());
                }
                for(int h=0; h<newHostages.size(); h++){
                    ///////////lazem n3mel condition law hostage mat fe edena
                    //// NOTE: DO CAPACITY LATER
                    if (newHostages.get(h).getX()==x &&newHostages.get(h).getY()==y) {//get hostage
                        ////carry the dead hostage as well i think?
                        if (newHostages.get(h).getState().equals("Alive")) {
                            // is it the next node or this node tho?
                            newHostages.get(h).setState("Carried");
                            
                            ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                            
                            for (Hostage h2 : newHostages) {
                                newHostages2.add(h2.clone());
                            }
                            int hostagesThatDied=0;
                            for(int h3=0; h3<newHostages2.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                newHostages2.get(h3).setDamage(newHostages2.get(h3).getDamage()+2);
                                if(newHostages2.get(h3).getDamage()>=100  )

                                    if(newHostages2.get(h3).getState().equals("Carried")) {
                                        newHostages2.get(h3).setState("Dead");
                                        hostagesThatDied++;
                                    }
                                    else if(newHostages2.get(h3).getState().equals("Alive")) {
                                    	newHostages2.get(h3).setState("Mutated");
                                    	hostagesThatDied++;
                                    }
                            }

                            if(canCarry(currentNode.currentState.allHostages)){
                            	Node tempNode = new Node(new State(x,y,c,currentNode.currentState.neoDamage,currentNode.currentState.kills,newHostages2, currentNode.currentState.allPills, currentNode.currentState.allAgents),Operators.carry,currentNode, currentNode.depth++,currentNode.cost+3+(hostagesThatDied*3000));	
                                mainQueue.add(tempNode);
                                hostageTaken = true;
                                nodesExpanded++;
                                break;
                            }/*else {
                            	Node tempNode = new Node(new State(x,y,c,currentNode.currentState.neoDamage,currentNode.currentState.kills,currentNode.currentState.allHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents),Oper,currentNode, currentNode.depth++);
                                mainQueue.add(tempNode);
                            }*/
                            //mainQueue.add(tempNode);
                            
                          
                            //shofo law alive
                        }
                    }
                }
            }
            if (grid[x][y].charAt(0) == 'T' ) { ////////////if there is any hostages to be dropped then add a node after every hostage is dropped, else dont add node
                boolean carriedHostages = false;
                    ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                    for (Hostage h : currentNode.currentState.allHostages) {
                        newHostages.add(h.clone());
                    }
                    for (int h = 0; h < newHostages.size(); h++) {
                        ///////////lazem n3mel condition law hostage mat fe edena
                        //// NOTE: DO CAPACITY LATER
                        if (newHostages.get(h).getState().equals("Carried")) {//get hostage
                            carriedHostages = true;
                            break;
                        }
                    }
                    if (carriedHostages) {
                        for (int h = 0; h < newHostages.size(); h++) {
                            if (newHostages.get(h).getState().equals("Carried")) {//get hostage
                                newHostages.get(h).setState("Saved");
                            }
                        }
                        int hostagesThatDied=0;	
                        ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                        for (Hostage h2 : newHostages) {
                            newHostages2.add(h2.clone());
                        }
                        for (int h3 = 0; h3 < newHostages2.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                            newHostages2.get(h3).setDamage(newHostages2.get(h3).getDamage() + 2);
                            if (newHostages2.get(h3).getDamage() >= 100)
                                if (newHostages2.get(h3).getState().equals("Carried")) {
                                    newHostages2.get(h3).setState("Dead");
                                    hostagesThatDied++;
                                }
                                else if (newHostages2.get(h3).getState().equals("Alive")) {
                                    newHostages2.get(h3).setState("Mutated");
                                    hostagesThatDied++;
                                }
                        }
                        Node tempNode = new Node(new State(x, y,c , currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages2, currentNode.currentState.allPills, currentNode.currentState.allAgents), Operators.drop, currentNode, currentNode.depth++,currentNode.cost+3+(hostagesThatDied*3000));
                        mainQueue.add(tempNode);
                        nodesExpanded++;
                    }



            }

            if(grid[x][y].charAt(0) == 'P' && grid[x][y].length()==1 )
            { /////DOES HE HAVE TO TAKE PILL IF HE IS ON IT?

                ArrayList<Pill> newPills = new ArrayList<Pill>();
                for (Pill p : currentNode.currentState.allPills){
                    newPills.add(p.clone());
                }
                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                for (Hostage h : currentNode.currentState.allHostages){
                    newHostages.add(h.clone());
                }

                for(int p=0; p<newPills.size(); p++)
                {
                    if (newPills.get(p).getX()==x &&newPills.get(p).getY()==y)
                    {
                        if(newPills.get(p).state.equals("NT"))
                        {
                            newPills.get(p).setState("T");
                            for(int h=0; h<newHostages.size();h++){

                                newHostages.get(h).setDamage(newHostages.get(h).getDamage()-20);
                                if(newHostages.get(h).getDamage()<0) {
                                	  newHostages.get(h).setDamage(0);
                                }
                            }
                                ////////CLONE THE OLD CLONED ONE AFTER SETTING DAMAGE
                           

                            Node tempNode = new Node(new State(x,y,c,(currentNode.currentState.neoDamage)-20,currentNode.currentState.kills,newHostages, newPills, currentNode.currentState.allAgents),Operators.takePill,currentNode, currentNode.depth++,currentNode.cost+3);
                            mainQueue.add(tempNode);
                            nodesExpanded++;
                            pillTAKE=true;


                            break;
                        }
                        else if(newPills.get(p).state.equals("T"))
                        {

                        }
                    }
                }
            }


            if(grid[x][y].charAt(0) == 'P' && grid[x][y].length()!=1 ){

                for (int j = 0; j < Pad.size(); j++) {
                    Pad pad = Pad.get(j);

                    if (pad.getStartX() == x && pad.getStartY() == y) {

                        String stateToBeHashed = makeHashedState(currentNode, 100, Operators.fly);
                        if(hashedStates.contains(stateToBeHashed)) {
                            break;
                        }
                        else {
                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                newHostages.add(h2.clone());
                            }
                            int hostagesThatDied=0;
                            for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                if (newHostages.get(h3).getDamage() >= 100)
                                    if (newHostages.get(h3).getState().equals("Carried")) {
                                        newHostages.get(h3).setState("Dead");
                                        hostagesThatDied++;}
                                    else if (newHostages.get(h3).getState().equals("Alive")) {
                                        newHostages.get(h3).setState("Mutated");
                                        hostagesThatDied++;
                                    }
                            }
                            if(!(currentNode.operator == Operators.fly)) {

                                Node tempNode = new Node(new State(pad.getEndX(), pad.getEndY(),c, currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), Operators.fly, currentNode, currentNode.depth++,currentNode.cost+3+hostagesThatDied*3000);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                                hashedStates.add(stateToBeHashed);

                                padFly=true;
                                break;
                            }
                            else{
                                break;
                            }
                        }
                    }
                }

            }


            for(int h=0; h<currentNode.currentState.allHostages.size(); h++){
                ///////////lazem n3mel condition law hostage mat fe edena
                if (currentNode.currentState.allHostages.get(h).state.equals("Alive")
                    ||currentNode.currentState.allHostages.get(h).state.equals("Carried") ||currentNode.currentState.allHostages.get(h).state.equals("Mutated") ) {
                             allHostagesSaved = false;

                    break;
                }
            }
            if(allHostagesSaved) {
                //out.println("Solution reached at depth" + currentNode.depth);
                //System.out.println(finalPrint(currentNode));
            }
        if(currentNode.currentState.neoDamage<100 && !hostageTaken ){

            if(!allHostagesSaved) { ///el hostages are not all saved yet

                /////////////////ELE T7T KOLO WAS JUST 2 LINES WITHOUT HASHING
                for(int i=0; i<4; i++) {   //this checks all neighbors by taking the array by up down right and left in this order


                    int col = x + dx[i];
                    int row = y + dy[i];

                    if (col < 0 || row < 0 || col>=m || row>=n) {

                    } else {


                        if (grid[col][row].charAt(0) == 'H' )  /// NOTE
                        { ////////////NOTE:MOVE TO THE NODE IF ITS NOT MUTATED, KILL IF MUTATED BY IF CONDITION THEN MOVE NEXT ITER


                                boolean hostageMutated = false;  ////////////tb3n brdo lazem n2tel ay hostage in the vicinity or agent if we do a kill action
                                for(int h=0; h<currentNode.currentState.allHostages.size(); h++) {
                                    ///////////lazem n3mel condition law hostage mat fe edena
                                    //// NOTE: DO CAPACITY LATER
                                    if (currentNode.currentState.allHostages.get(h).getX() == col && currentNode.currentState.allHostages.get(h).getY() == row) {//get hostage
                                        ////carry the dead hostage as well i think?
                                        if (currentNode.currentState.allHostages.get(h).getState().equals("Mutated")) {

                                            //KILL IF MUTATED
                                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                                newHostages.add(h2.clone());
                                            }
                                           ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                          
                                            int hostageKills = 0;
                                            int agentKills = 0;
                                            //newAgents.get(a).setState("Dead");
                                         
                                            newHostages.get(h).setState("Dead");
                                            hostageKills++;
                                            ArrayList<Agent> newAgents = new ArrayList<Agent>();
                                            for (Agent a : currentNode.currentState.allAgents){
                                                newAgents.add(a.clone());
                                            }

                                            int[] dx2 = { -1, 1, 0, 0 };
                                            int[] dy2= { 0, 0, 1, -1 };
                                            ///////////KILL ALL NEIGHBORING AGENTS IF THERE IS


                                            for(int k=0; k<4;k++) {
                                                        //KILLING ALL AGENTS IS DONE 5ALAS
                                                int col2 = x + dx2[k];
                                                int row2 = y + dy2[k];
                                                if (col2 < 0 || row2 < 0 || col2>=m || row2>=n) {

                                                } else {
                                                if (grid[col2][row2].charAt(0) == 'H') {
                                                    for (int a2 = 0; a2 < newHostages.size(); a2++) {
                                                        if (newHostages.get(a2).getX() == col2 && newHostages.get(a2).getY() == row2) {

                                                            if (newHostages.get(a2).state.equals("Mutated")) {
                                                            	newHostages.get(a2).setState("Dead");
                                                                hostageKills++;

                                                            }
                                                        }
                                                        }
                                                    }
                                                
                                                if (grid[col2][row2].charAt(0) == 'A') {
                                                    for (int a2 = 0; a2 < newAgents.size(); a2++) {
                                                        if (newAgents.get(a2).getX() == col2 && newAgents.get(a2).getY() == row2) {

                                                            if (newAgents.get(a2).state.equals("Alive")) {
                                                                newAgents.get(a2).setState("Dead");
                                                                agentKills++;

                                                            }
                                                        }
                                                        }
                                                    }
                                                
                                                
                                                }

                                            }		
                                            	
                                            ///INCUR DMG
                                            ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                                            for (Hostage h4 : newHostages) {
                                                newHostages2.add(h4.clone());
                                            }
                                            int hostagesThatDied=0;
                                            for(int h5=0; h5<newHostages2.size(); h5++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                                newHostages2.get(h5).setDamage(newHostages2.get(h5).getDamage()+2);
                                                if(newHostages2.get(h5).getDamage()>=100)
                                                    if(newHostages2.get(h5).getState().equals("Carried")) {
                                                        newHostages2.get(h5).setState("Dead");
                                                    hostagesThatDied++;
                                                    }
                                                    else if(newHostages2.get(h5).getState().equals("Alive")) {
                                                        newHostages2.get(h5).setState("Mutated");
                                                        hostagesThatDied++;
                                                        
                                                    }
                                            }
                                            Node tempNode = new Node(new State(x, y,c , currentNode.currentState.neoDamage+20, currentNode.currentState.kills+agentKills+hostageKills, newHostages2, currentNode.currentState.allPills, currentNode.currentState.allAgents), Operators.kill, currentNode, currentNode.depth++,currentNode.cost+(3000 * agentKills)+(3*hostageKills) +(3000*hostagesThatDied));

                                            mainQueue.add(tempNode);

                                            nodesExpanded++;
                                            hostageMutated = true;
                                            break;

                                        }


                                    }
                                }
                                    /////////GO HERE IF HOSTAGE ISNT MUTATED
                                if(!hostageMutated)
                                {
                                    String stateToBeHashed = makeHashedState(currentNode, i,null);
                                    if(hashedStates.contains(stateToBeHashed)){
                                    }
                                    else {
                                        ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                        for (Hostage h2 : currentNode.currentState.allHostages) {
                                            newHostages.add(h2.clone());
                                        }
                                        int hostagesThatDied=0;
                                        for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                            newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                            if (newHostages.get(h3).getDamage() >= 100)
                                                if (newHostages.get(h3).getState().equals("Carried")) {
                                                    newHostages.get(h3).setState("Dead");
                                                     hostagesThatDied++;
                                                }
                                                else if (newHostages.get(h3).getState().equals("Alive")) {
                                                    newHostages.get(h3).setState("Mutated");
                                                    hostagesThatDied++;
                                                }
                                        }
                                        boolean isHeGoingToTurn = false;
                                        for(int h4 = 0; h4 < newHostages.size(); h4++) {
                                     	   if (newHostages.get(h4).getX() == col && newHostages.get(h4).getY() == row) //get hostage
                                               ////carry the dead hostage as well i think?
                                                if (newHostages.get(h4).getDamage()>=98 && newHostages.get(h4).getState().equals("Alive"))
                                             	   isHeGoingToTurn = true;
                                        }
                                        if(!isHeGoingToTurn) {

                                        Node tempNode = new Node(new State(col, row, c, currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,currentNode.cost+3+hostagesThatDied*3000);
                                        hashedStates.add(stateToBeHashed);
                                        mainQueue.add(tempNode);
                                        nodesExpanded++;
                                        }
                                    }
                                }
                            }





                        else if(grid[col][row].charAt(0) == 'T')
                        {
                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){

                            }

                            else {  ///////////////hostages might have to be dropped in the next iteration
                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                int hostagesThatDied=0;
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried")) {
                                            newHostages.get(h3).setState("Dead");
                                             hostagesThatDied++;
                                        }
                                        else if(newHostages.get(h3).getState().equals("Alive")) {
                                            newHostages.get(h3).setState("Mutated");
                                             hostagesThatDied++;
                                        }
                                }



                                Node tempNode = new Node(new State(col, row, c, currentNode.currentState.neoDamage, currentNode.currentState.kills,newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,currentNode.cost+3 +(hostagesThatDied*3000));
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }

                        else if(grid[col][row].charAt(0) == 'A' ){  ///////////////////NOTE NOTE NOTELAW FEE AGENT GNBY KAMAN GHEER EL NEIGHBOR DA THEN KILL THEM ALL
                            ArrayList<Agent> newAgents = new ArrayList<Agent>();
                            for (Agent a : currentNode.currentState.allAgents){
                                newAgents.add(a.clone());
                            }

                            for(int a=0; a<newAgents.size(); a++)
                            {

                                if (newAgents.get(a).getX()==col &&newAgents.get(a).getY()==row)
                                {

                                    if(newAgents.get(a).state.equals("Alive"))
                                    {

                                        ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                        for (Hostage h2 : currentNode.currentState.allHostages) {
                                            newHostages.add(h2.clone());
                                        }
                                       
                                        int agentKills = 0;
                                        int hostageKills = 0;
                                        newAgents.get(a).setState("Dead");
                                        agentKills++;
                                        int[] dx2 = { -1, 1, 0, 0 };
                                        int[] dy2= { 0, 0, 1, -1 };
                                        ///////////KILL ALL NEIGHBORING AGENTS IF THERE IS


                                        for(int k=0; k<4;k++) {
                                                    //KILLING ALL AGENTS IS DONE 5ALAS
                                            int col2 = x + dx2[k];
                                            int row2 = y + dy2[k];
                                            if (col2 < 0 || row2 < 0 || col2>=m || row2>=n) {

                                            } else {
                                            if (grid[col2][row2].charAt(0) == 'A') {
                                                for (int a2 = 0; a2 < newAgents.size(); a2++) {
                                                    if (newAgents.get(a2).getX() == col2 && newAgents.get(a2).getY() == row2) {

                                                        if (newAgents.get(a2).state.equals("Alive")) {
                                                            newAgents.get(a2).setState("Dead");
                                                            agentKills++;

                                                        }
                                                    }
                                                    }
                                                }
                                            if (grid[col2][row2].charAt(0) == 'H') {
                                                for (int a2 = 0; a2 < newHostages.size(); a2++) {
                                                    if (newHostages.get(a2).getX() == col2 && newHostages.get(a2).getY() == row2) {

                                                        if (newHostages.get(a2).state.equals("Mutated")) {
                                                        	newHostages.get(a2).setState("Dead");
                                                            hostageKills++;

                                                        }
                                                    }
                                                    }
                                        }
                                            }

                                        }
                                        int hostagesThatDied=0;
                                        for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                            newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                            if(newHostages.get(h3).getDamage()>=100)
                                                if(newHostages.get(h3).getState().equals("Carried")) {
                                                    newHostages.get(h3).setState("Dead");
                                                    hostagesThatDied++;
                                                }
                                                else if(newHostages.get(h3).getState().equals("Alive")) {
                                                    newHostages.get(h3).setState("Mutated");
                                                    hostagesThatDied++;
                                                }
                                        }
                                        Node tempNode = new Node(new State(x,y,c,(currentNode.currentState.neoDamage)+20,currentNode.currentState.kills+agentKills+hostageKills,newHostages, currentNode.currentState.allPills, newAgents),Operators.kill,currentNode, currentNode.depth++,currentNode.cost+(3000*agentKills) +(3*hostageKills) +3000*hostagesThatDied);

                                        mainQueue.add(tempNode);
                                        nodesExpanded++;
                                        break;
                                    }
                                    else if(newAgents.get(a).state.equals("Dead"))
                                    {
                                        String stateToBeHashed = makeHashedState(currentNode, i,null);
                                        if(hashedStates.contains(stateToBeHashed)){
                                        }
                                        else {
                                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                                newHostages.add(h2.clone());
                                            }
                                            int hostagesThatDied=0;
                                            for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                               
                                                newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                                if (newHostages.get(h3).getDamage() >= 100)
                                                    if (newHostages.get(h3).getState().equals("Carried")) {
                                                        newHostages.get(h3).setState("Dead");
                                                		 hostagesThatDied++;}
                                                    else if (newHostages.get(h3).getState().equals("Alive")) {
                                                        newHostages.get(h3).setState("Mutated");
                                                		 hostagesThatDied++;}

                                            }

                                            Node tempNode = new Node(new State(col, row,c, (currentNode.currentState.neoDamage), currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, newAgents), operationFinder(i), currentNode, currentNode.depth++,currentNode.cost+3+hostagesThatDied*3000);
                                            mainQueue.add(tempNode);
                                            hashedStates.add(stateToBeHashed);
                                            nodesExpanded++;
                                        }
                                    }
                                }
                            }
                        }
                        else if (grid[col][row].charAt(0) == 'P')
                        {


                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){
                            }
                            else {

                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                int hostagesThatDied=0;
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried")) {
                                            newHostages.get(h3).setState("Dead");
                                    	 hostagesThatDied++;}
                                        else if(newHostages.get(h3).getState().equals("Alive")) {
                                            newHostages.get(h3).setState("Mutated");
                                    		 hostagesThatDied++;}

                                }

                                Node tempNode = new Node(new State(col, row,c, (currentNode.currentState.neoDamage),currentNode.currentState.kills, newHostages,currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,currentNode.cost+3+hostagesThatDied*3000);
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }
                        else if(grid[col][row].charAt(0) == '-'   ){ //GO TO PILL / EMPTY /PAD
                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){
                                }
                            else {

                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                int hostagesThatDied=0;
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried")) {
                                            newHostages.get(h3).setState("Dead");
                                            hostagesThatDied++;}
                                        else if(newHostages.get(h3).getState().equals("Alive")) {
                                            newHostages.get(h3).setState("Mutated");
                                            hostagesThatDied++;
                                        }
                                }

                                Node tempNode = new Node(new State(col, row, c, (currentNode.currentState.neoDamage),currentNode.currentState.kills, newHostages,currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,currentNode.cost+3+3000*hostagesThatDied);
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }

                    }


                }

            }
            else {
            	//System.out.println(c);
                int deadHostages = 0;
                for (int h = 0; h < currentNode.currentState.allHostages.size(); h++) {
                    if (currentNode.currentState.allHostages.get(h).getState().equals("Dead"))
                         deadHostages++;

                }
                if(currentNode.currentState.neoX == telephone.x &&currentNode.currentState.neoY == telephone.y) {
                    System.out.println("Solution reached at depth" + currentNode.depth);
                    
                    String output = (finalPrint(currentNode) + ";" +deadHostages  + ";" +  currentNode.currentState.kills+ ";" + nodesExpanded);
                    System.out.println(output);
                    return output;
                }


            }

        }

        }
        
        return "No Solution";
    } 
    
    
    public static String Greedy2(String[][] grid) {
    	
        int[] dx = { -1, 1, 0, 0 };
        int[] dy = { 0, 0, 1, -1 };
        
        int nodesExpanded = 0;
        HashSet<String> hashedStates = new HashSet<String>();

        hostNo = hostages.size();

      //  heuristic1
        PriorityQueue<Node> mainQueue = new PriorityQueue<Node>(10000, new Comparator<Node>() {
        	public int compare (Node n1, Node n2) {
        		
                if(heuristic2(n1) > heuristic2(n2)) {
                	//System.out.println("n1: "+heuristic1(n1)+", n2: "+ heuristic1(n2));
                    return 1;
                } else if ( heuristic2(n1) < heuristic2(n2)) {
                    return -1;
                } else {
                    return 0;
                }
        	}
        });
        State startState = new State(neo.getX(), neo.getY(),c,0,0, hostages ,Pill, agents);

        Node start = new Node(startState, null, null , 0, 0);

        mainQueue.add(start);


        while(!mainQueue.isEmpty()){
        	
        	/*for(int q=0; q<mainQueue.size(); q++) {
        		System.out.println(mainQueue.);
        	}*/
            boolean pillTAKE = false;
            boolean padFly = false;
            boolean allHostagesSaved=true;
            boolean hostageTaken= false;
            Node currentNode = mainQueue.peek(); //el node ele hn3mlha dequeue from the front
            int x = currentNode.currentState.neoX; //tb3n lazem nghyha l private w n3melha getMETHOD bas lateron
            int y =currentNode.currentState.neoY;
            mainQueue.remove();

            //all the checks if we are on H, pad, TB


            if (grid[x][y].charAt(0) == 'H' ) { ////////////you move first to that node and then drop/carry hostage
                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                for (Hostage h : currentNode.currentState.allHostages){
                    newHostages.add(h.clone());
                }
                for(int h=0; h<newHostages.size(); h++){
                    ///////////lazem n3mel condition law hostage mat fe edena
                    //// NOTE: DO CAPACITY LATER
                    if (newHostages.get(h).getX()==x &&newHostages.get(h).getY()==y) {//get hostage
                        ////carry the dead hostage as well i think?
                        if (newHostages.get(h).getState().equals("Alive")) {
                            // is it the next node or this node tho?
                            newHostages.get(h).setState("Carried");
                            ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                            for (Hostage h2 : newHostages) {
                                newHostages2.add(h2.clone());
                            }
                            for(int h3=0; h3<newHostages2.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                newHostages2.get(h3).setDamage(newHostages2.get(h3).getDamage()+2);
                                if(newHostages2.get(h3).getDamage()>=100  )

                                    if(newHostages2.get(h3).getState().equals("Carried"))
                                        newHostages2.get(h3).setState("Dead");
                                    else if(newHostages2.get(h3).getState().equals("Alive"))
                                    newHostages2.get(h3).setState("Mutated");

                            }

                            if(canCarry(currentNode.currentState.allHostages)){
                            	Node tempNode = new Node(new State(x,y,c,currentNode.currentState.neoDamage,currentNode.currentState.kills,newHostages2, currentNode.currentState.allPills, currentNode.currentState.allAgents),Operators.carry,currentNode, currentNode.depth++,0);	
                                mainQueue.add(tempNode);
                                hostageTaken = true;
                                nodesExpanded++;
                                break;
                            }/*else {
                            	Node tempNode = new Node(new State(x,y,c,currentNode.currentState.neoDamage,currentNode.currentState.kills,currentNode.currentState.allHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents),Oper,currentNode, currentNode.depth++);
                                mainQueue.add(tempNode);
                            }*/
                            //mainQueue.add(tempNode);
                           
                            //shofo law alive
                        }

                    }
                }

            }
            if (grid[x][y].charAt(0) == 'T' ) { ////////////if there is any hostages to be dropped then add a node after every hostage is dropped, else dont add node
                boolean carriedHostages = false;
                    ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                    for (Hostage h : currentNode.currentState.allHostages) {

                        newHostages.add(h.clone());
                    }
                    for (int h = 0; h < newHostages.size(); h++) {
                        ///////////lazem n3mel condition law hostage mat fe edena
                        //// NOTE: DO CAPACITY LATER
                        if (newHostages.get(h).getState().equals("Carried")) {//get hostage
                            carriedHostages = true;
                            break;
                        }
                    }
                    if (carriedHostages) {
                        for (int h = 0; h < newHostages.size(); h++) {
                            if (newHostages.get(h).getState().equals("Carried")) {//get hostage
                                newHostages.get(h).setState("Saved");


                            }

                        }

                        ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                        for (Hostage h2 : newHostages) {
                            newHostages2.add(h2.clone());
                        }
                        for (int h3 = 0; h3 < newHostages2.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                            newHostages2.get(h3).setDamage(newHostages2.get(h3).getDamage() + 2);
                            if (newHostages2.get(h3).getDamage() >= 100)
                                if (newHostages2.get(h3).getState().equals("Carried"))
                                    newHostages2.get(h3).setState("Dead");
                                else if (newHostages2.get(h3).getState().equals("Alive"))
                                    newHostages2.get(h3).setState("Mutated");

                        }
                        Node tempNode = new Node(new State(x, y,c , currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages2, currentNode.currentState.allPills, currentNode.currentState.allAgents), Operators.drop, currentNode, currentNode.depth++,0);
                        mainQueue.add(tempNode);
                        nodesExpanded++;
                    }



            }

            if(grid[x][y].charAt(0) == 'P' && grid[x][y].length()==1 )
            { /////DOES HE HAVE TO TAKE PILL IF HE IS ON IT?

                ArrayList<Pill> newPills = new ArrayList<Pill>();
                for (Pill p : currentNode.currentState.allPills){
                    newPills.add(p.clone());
                }
                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                for (Hostage h : currentNode.currentState.allHostages){
                    newHostages.add(h.clone());
                }

                for(int p=0; p<newPills.size(); p++)
                {
                    if (newPills.get(p).getX()==x &&newPills.get(p).getY()==y)
                    {
                        if(newPills.get(p).state.equals("NT"))
                        {
                            newPills.get(p).setState("T");
                            for(int h=0; h<newHostages.size();h++){

                                newHostages.get(h).setDamage(newHostages.get(h).getDamage()-20);
                                if(newHostages.get(h).getDamage()<0) {
                              	  newHostages.get(h).setDamage(0);
                              }

                            }
                                ////////CLONE THE OLD CLONED ONE AFTER SETTING DAMAGE
                            

                            Node tempNode = new Node(new State(x,y,c,(currentNode.currentState.neoDamage)-20,currentNode.currentState.kills,newHostages, newPills, currentNode.currentState.allAgents),Operators.takePill,currentNode, currentNode.depth++,0);
                            mainQueue.add(tempNode);
                            nodesExpanded++;
                            pillTAKE=true;


                            break;
                        }
                        else if(newPills.get(p).state.equals("T"))
                        {

                        }
                    }
                }
            }


            if(grid[x][y].charAt(0) == 'P' && grid[x][y].length()!=1 ){

                for (int j = 0; j < Pad.size(); j++) {
                    Pad pad = Pad.get(j);

                    if (pad.getStartX() == x && pad.getStartY() == y) {

                        String stateToBeHashed = makeHashedState(currentNode, 100, Operators.fly);
                        if(hashedStates.contains(stateToBeHashed)) {
                            break;
                        }
                        else {
                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                newHostages.add(h2.clone());
                            }
                            for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                if (newHostages.get(h3).getDamage() >= 100)
                                    if (newHostages.get(h3).getState().equals("Carried"))
                                        newHostages.get(h3).setState("Dead");
                                    else if (newHostages.get(h3).getState().equals("Alive"))
                                        newHostages.get(h3).setState("Mutated");

                            }
                            if(!(currentNode.operator == Operators.fly)) {

                                Node tempNode = new Node(new State(pad.getEndX(), pad.getEndY(),c, currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), Operators.fly, currentNode, currentNode.depth++,0);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                                hashedStates.add(stateToBeHashed);

                                padFly=true;
                                break;
                            }
                            else{
                                break;
                            }
                        }
                    }
                }

            }


            for(int h=0; h<currentNode.currentState.allHostages.size(); h++){
                ///////////lazem n3mel condition law hostage mat fe edena
                if (currentNode.currentState.allHostages.get(h).state.equals("Alive")
                    ||currentNode.currentState.allHostages.get(h).state.equals("Carried") ||currentNode.currentState.allHostages.get(h).state.equals("Mutated") ) {
                             allHostagesSaved = false;

                    break;
                }
            }
            if(allHostagesSaved) {
                //out.println("Solution reached at depth" + currentNode.depth);
                //System.out.println(finalPrint(currentNode));
            }
        if(currentNode.currentState.neoDamage<100 && !hostageTaken){

            if(!allHostagesSaved) { ///el hostages are not all saved yet

                /////////////////ELE T7T KOLO WAS JUST 2 LINES WITHOUT HASHING
                for(int i=0; i<4; i++) {   //this checks all neighbors by taking the array by up down right and left in this order



                    int col = x + dx[i];
                    int row = y + dy[i];

                    if (col < 0 || row < 0 || col>=m || row>=n) {

                    } else {


                        if (grid[col][row].charAt(0) == 'H' )  /// NOTE
                        { ////////////NOTE:MOVE TO THE NODE IF ITS NOT MUTATED, KILL IF MUTATED BY IF CONDITION THEN MOVE NEXT ITER


                                boolean hostageMutated = false;  ////////////tb3n brdo lazem n2tel ay hostage in the vicinity or agent if we do a kill action
                                for(int h=0; h<currentNode.currentState.allHostages.size(); h++) {
                                    ///////////lazem n3mel condition law hostage mat fe edena
                                    //// NOTE: DO CAPACITY LATER
                                    if (currentNode.currentState.allHostages.get(h).getX() == col && currentNode.currentState.allHostages.get(h).getY() == row) {//get hostage
                                        ////carry the dead hostage as well i think?
                                        if (currentNode.currentState.allHostages.get(h).getState().equals("Mutated")) {

                                            //KILL IF MUTATED
                                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                                newHostages.add(h2.clone());
                                            }
                                           ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                            newHostages.get(h).setState("Dead");
                                            int kills = 0;
                                            //newAgents.get(a).setState("Dead");
                                            kills++;
                                            ArrayList<Agent> newAgents = new ArrayList<Agent>();
                                            for (Agent a : currentNode.currentState.allAgents){
                                                newAgents.add(a.clone());
                                            }

                                            int[] dx2 = { -1, 1, 0, 0 };
                                            int[] dy2= { 0, 0, 1, -1 };
                                            ///////////KILL ALL NEIGHBORING AGENTS IF THERE IS


                                            for(int k=0; k<4;k++) {
                                                        //KILLING ALL AGENTS IS DONE 5ALAS
                                                int col2 = x + dx2[k];
                                                int row2 = y + dy2[k];
                                                if (col2 < 0 || row2 < 0 || col2>=m || row2>=n) {

                                                } else {
                                                if (grid[col2][row2].charAt(0) == 'H') {
                                                    for (int a2 = 0; a2 < newHostages.size(); a2++) {
                                                        if (newHostages.get(a2).getX() == col2 && newHostages.get(a2).getY() == row2) {

                                                            if (newHostages.get(a2).state.equals("Mutated")) {
                                                            	newHostages.get(a2).setState("Dead");
                                                                kills++;

                                                            }
                                                        }
                                                        }
                                                    }
                                                
                                                if (grid[col2][row2].charAt(0) == 'A') {
                                                    for (int a2 = 0; a2 < newAgents.size(); a2++) {
                                                        if (newAgents.get(a2).getX() == col2 && newAgents.get(a2).getY() == row2) {

                                                            if (newAgents.get(a2).state.equals("Alive")) {
                                                                newAgents.get(a2).setState("Dead");
                                                                kills++;

                                                            }
                                                        }
                                                        }
                                                    }
                                                
                                                
                                                
                                                
                                                
                                                
                                                
                                                }

                                            }		
                                            ///INCUR DMG
                                            ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                                            for (Hostage h4 : newHostages) {
                                                newHostages2.add(h4.clone());
                                            }
                                            for(int h5=0; h5<newHostages2.size(); h5++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                                newHostages2.get(h5).setDamage(newHostages2.get(h5).getDamage()+2);
                                                if(newHostages2.get(h5).getDamage()>=100)
                                                    if(newHostages2.get(h5).getState().equals("Carried"))
                                                        newHostages2.get(h5).setState("Dead");
                                                    else if(newHostages2.get(h5).getState().equals("Alive"))
                                                        newHostages2.get(h5).setState("Mutated");

                                            }
                                            Node tempNode = new Node(new State(x, y,c , currentNode.currentState.neoDamage+20, currentNode.currentState.kills+kills, newHostages2, currentNode.currentState.allPills, currentNode.currentState.allAgents), Operators.kill, currentNode, currentNode.depth++,0);

                                            mainQueue.add(tempNode);

                                            nodesExpanded++;
                                            hostageMutated = true;
                                            break;

                                        }


                                    }
                                }
                                    /////////GO HERE IF HOSTAGE ISNT MUTATED
                                if(!hostageMutated)
                                {
                                    String stateToBeHashed = makeHashedState(currentNode, i,null);
                                    if(hashedStates.contains(stateToBeHashed)){
                                    }
                                    else {
                                        ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                        for (Hostage h2 : currentNode.currentState.allHostages) {
                                            newHostages.add(h2.clone());
                                        }
                                        for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                            newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                            if (newHostages.get(h3).getDamage() >= 100)
                                                if (newHostages.get(h3).getState().equals("Carried"))
                                                    newHostages.get(h3).setState("Dead");
                                                else if (newHostages.get(h3).getState().equals("Alive"))
                                                    newHostages.get(h3).setState("Mutated");

                                        }
                                       boolean isHeGoingToTurn = false;
                                       for(int h4 = 0; h4 < newHostages.size(); h4++) {
                                    	   if (newHostages.get(h4).getX() == col && newHostages.get(h4).getY() == row) //get hostage
                                              ////carry the dead hostage as well i think?
                                               if (newHostages.get(h4).getDamage()>=98 && newHostages.get(h4).getState().equals("Alive"))
                                            	   isHeGoingToTurn = true;
                                       }
                                       if(!isHeGoingToTurn) {
                                        Node tempNode = new Node(new State(col, row, c, currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                        hashedStates.add(stateToBeHashed);
                                        mainQueue.add(tempNode);
                                        nodesExpanded++;
                                       }
                                      
                                    	   
                                    }
                                }
                            }





                        else if(grid[col][row].charAt(0) == 'T')
                        {
                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){

                            }

                            else {  ///////////////hostages might have to be dropped in the next iteration
                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried"))
                                            newHostages.get(h3).setState("Dead");
                                        else if(newHostages.get(h3).getState().equals("Alive"))
                                            newHostages.get(h3).setState("Mutated");

                                }



                                Node tempNode = new Node(new State(col, row, c, currentNode.currentState.neoDamage, currentNode.currentState.kills,newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }

                        else if(grid[col][row].charAt(0) == 'A' ){  ///////////////////NOTE NOTE NOTELAW FEE AGENT GNBY KAMAN GHEER EL NEIGHBOR DA THEN KILL THEM ALL
                            ArrayList<Agent> newAgents = new ArrayList<Agent>();
                            for (Agent a : currentNode.currentState.allAgents){
                                newAgents.add(a.clone());
                            }

                            for(int a=0; a<newAgents.size(); a++)
                            {

                                if (newAgents.get(a).getX()==col &&newAgents.get(a).getY()==row)
                                {

                                    if(newAgents.get(a).state.equals("Alive"))
                                    {

                                        ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                        for (Hostage h2 : currentNode.currentState.allHostages) {
                                            newHostages.add(h2.clone());
                                        }
                                        
                                        int kills = 0;
                                        newAgents.get(a).setState("Dead");
                                        kills++;
                                        int[] dx2 = { -1, 1, 0, 0 };
                                        int[] dy2= { 0, 0, 1, -1 };
                                        ///////////KILL ALL NEIGHBORING AGENTS IF THERE IS


                                        for(int k=0; k<4;k++) {
                                                    //KILLING ALL AGENTS IS DONE 5ALAS
                                            int col2 = x + dx2[k];
                                            int row2 = y + dy2[k];
                                            if (col2 < 0 || row2 < 0 || col2>=m || row2>=n) {

                                            } else {
                                            if (grid[col2][row2].charAt(0) == 'A') {
                                                for (int a2 = 0; a2 < newAgents.size(); a2++) {
                                                    if (newAgents.get(a2).getX() == col2 && newAgents.get(a2).getY() == row2) {

                                                        if (newAgents.get(a2).state.equals("Alive")) {
                                                            newAgents.get(a2).setState("Dead");
                                                            kills++;

                                                        }
                                                    }
                                                    }
                                                }
                                            if (grid[col2][row2].charAt(0) == 'H') {
                                                for (int a2 = 0; a2 < newHostages.size(); a2++) {
                                                    if (newHostages.get(a2).getX() == col2 && newHostages.get(a2).getY() == row2) {

                                                        if (newHostages.get(a2).state.equals("Mutated")) {
                                                        	newHostages.get(a2).setState("Dead");
                                                            kills++;

                                                        }
                                                    }
                                                    }
                                        }
                                            }

                                        }

                                        for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                            newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                            if(newHostages.get(h3).getDamage()>=100)
                                                if(newHostages.get(h3).getState().equals("Carried"))
                                                    newHostages.get(h3).setState("Dead");
                                                else if(newHostages.get(h3).getState().equals("Alive"))
                                                    newHostages.get(h3).setState("Mutated");

                                        }
                                        Node tempNode = new Node(new State(x,y,c,(currentNode.currentState.neoDamage)+20,currentNode.currentState.kills+kills,newHostages, currentNode.currentState.allPills, newAgents),Operators.kill,currentNode, currentNode.depth++,0);

                                        mainQueue.add(tempNode);
                                        nodesExpanded++;
                                        break;
                                    }
                                    else if(newAgents.get(a).state.equals("Dead"))
                                    {
                                        String stateToBeHashed = makeHashedState(currentNode, i,null);
                                        if(hashedStates.contains(stateToBeHashed)){
                                        }
                                        else {
                                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                                newHostages.add(h2.clone());
                                            }
                                            for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                                newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                                if (newHostages.get(h3).getDamage() >= 100)
                                                    if (newHostages.get(h3).getState().equals("Carried"))
                                                        newHostages.get(h3).setState("Dead");
                                                    else if (newHostages.get(h3).getState().equals("Alive"))
                                                        newHostages.get(h3).setState("Mutated");

                                            }

                                            Node tempNode = new Node(new State(col, row,c, (currentNode.currentState.neoDamage), currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, newAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                            mainQueue.add(tempNode);
                                            hashedStates.add(stateToBeHashed);
                                            nodesExpanded++;
                                        }
                                    }
                                }
                            }
                        }
                        else if (grid[col][row].charAt(0) == 'P')
                        {


                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){
                            }
                            else {

                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried"))
                                            newHostages.get(h3).setState("Dead");
                                        else if(newHostages.get(h3).getState().equals("Alive"))
                                            newHostages.get(h3).setState("Mutated");

                                }

                                Node tempNode = new Node(new State(col, row,c, (currentNode.currentState.neoDamage),currentNode.currentState.kills, newHostages,currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }
                        else if(grid[col][row].charAt(0) == '-'   ){ //GO TO PILL / EMPTY /PAD
                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){
                                }
                            else {

                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried"))
                                            newHostages.get(h3).setState("Dead");
                                        else if(newHostages.get(h3).getState().equals("Alive"))
                                            newHostages.get(h3).setState("Mutated");

                                }

                                Node tempNode = new Node(new State(col, row, c, (currentNode.currentState.neoDamage),currentNode.currentState.kills, newHostages,currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }

                    }


                }

            }
            else {
            	//System.out.println(c);
                int deadHostages = 0;
                for (int h = 0; h < currentNode.currentState.allHostages.size(); h++) {
                    if (currentNode.currentState.allHostages.get(h).getState().equals("Dead"))
                         deadHostages++;

                }
                if(currentNode.currentState.neoX == telephone.x &&currentNode.currentState.neoY == telephone.y) {
                    System.out.println("Solution reached at depth" + currentNode.depth);
                    String output = (finalPrint(currentNode) + ";" +deadHostages  + ";" +  currentNode.currentState.kills+ ";" + nodesExpanded);
                    System.out.println(output);
                    return output;
                }


            }

        }

        }
        
        return "No Solution";
    }
   
    
    public static String Greedy1(String[][] grid) {
    	
        int[] dx = { -1, 1, 0, 0 };
        int[] dy = { 0, 0, 1, -1 };
        
        int nodesExpanded = 0;
        HashSet<String> hashedStates = new HashSet<String>();

        hostNo = hostages.size();

      //  heuristic1
        PriorityQueue<Node> mainQueue = new PriorityQueue<Node>(10000, new Comparator<Node>() {
        	public int compare (Node n1, Node n2) {
        		
                if(heuristic1(n1) > heuristic1(n2)) {
                	//System.out.println("n1: "+heuristic1(n1)+", n2: "+ heuristic1(n2));
                    return 1;
                } else if ( heuristic1(n1) < heuristic1(n2)) {
                    return -1;
                } else {
                    return 0;
                }
        	}
        });
        State startState = new State(neo.getX(), neo.getY(),c,0,0, hostages ,Pill, agents);

        Node start = new Node(startState, null, null , 0, 0);

        mainQueue.add(start);


        while(!mainQueue.isEmpty()){
        	
        	/*for(int q=0; q<mainQueue.size(); q++) {
        		System.out.println(mainQueue.);
        	}*/
            boolean pillTAKE = false;
            boolean padFly = false;
            boolean allHostagesSaved=true;
            boolean hostageTaken= false;
            Node currentNode = mainQueue.peek(); //el node ele hn3mlha dequeue from the front
            int x = currentNode.currentState.neoX; //tb3n lazem nghyha l private w n3melha getMETHOD bas lateron
            int y =currentNode.currentState.neoY;
            mainQueue.remove();

            //all the checks if we are on H, pad, TB


            if (grid[x][y].charAt(0) == 'H' ) { ////////////you move first to that node and then drop/carry hostage
                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                for (Hostage h : currentNode.currentState.allHostages){
                    newHostages.add(h.clone());
                }
                for(int h=0; h<newHostages.size(); h++){
                    ///////////lazem n3mel condition law hostage mat fe edena
                    //// NOTE: DO CAPACITY LATER
                    if (newHostages.get(h).getX()==x &&newHostages.get(h).getY()==y) {//get hostage
                        ////carry the dead hostage as well i think?
                        if (newHostages.get(h).getState().equals("Alive")) {
                            // is it the next node or this node tho?
                            newHostages.get(h).setState("Carried");
                            ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                            for (Hostage h2 : newHostages) {
                                newHostages2.add(h2.clone());
                            }
                            for(int h3=0; h3<newHostages2.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                newHostages2.get(h3).setDamage(newHostages2.get(h3).getDamage()+2);
                                if(newHostages2.get(h3).getDamage()>=100  )

                                    if(newHostages2.get(h3).getState().equals("Carried"))
                                        newHostages2.get(h3).setState("Dead");
                                    else if(newHostages2.get(h3).getState().equals("Alive"))
                                    newHostages2.get(h3).setState("Mutated");

                            }

                            if(canCarry(currentNode.currentState.allHostages)){
                            	Node tempNode = new Node(new State(x,y,c,currentNode.currentState.neoDamage,currentNode.currentState.kills,newHostages2, currentNode.currentState.allPills, currentNode.currentState.allAgents),Operators.carry,currentNode, currentNode.depth++,0);	
                                mainQueue.add(tempNode);
                                hostageTaken = true;
                            }/*else {
                            	Node tempNode = new Node(new State(x,y,c,currentNode.currentState.neoDamage,currentNode.currentState.kills,currentNode.currentState.allHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents),Oper,currentNode, currentNode.depth++);
                                mainQueue.add(tempNode);
                            }*/
                            //mainQueue.add(tempNode);
                            nodesExpanded++;
                            break;
                            //shofo law alive
                        }

                    }
                }

            }
            if (grid[x][y].charAt(0) == 'T' ) { ////////////if there is any hostages to be dropped then add a node after every hostage is dropped, else dont add node
                boolean carriedHostages = false;
                    ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                    for (Hostage h : currentNode.currentState.allHostages) {

                        newHostages.add(h.clone());
                    }
                    for (int h = 0; h < newHostages.size(); h++) {
                        ///////////lazem n3mel condition law hostage mat fe edena
                        //// NOTE: DO CAPACITY LATER
                        if (newHostages.get(h).getState().equals("Carried")) {//get hostage
                            carriedHostages = true;
                            break;
                        }
                    }
                    if (carriedHostages) {
                        for (int h = 0; h < newHostages.size(); h++) {
                            if (newHostages.get(h).getState().equals("Carried")) {//get hostage
                                newHostages.get(h).setState("Saved");


                            }

                        }

                        ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                        for (Hostage h2 : newHostages) {
                            newHostages2.add(h2.clone());
                        }
                        for (int h3 = 0; h3 < newHostages2.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                            newHostages2.get(h3).setDamage(newHostages2.get(h3).getDamage() + 2);
                            if (newHostages2.get(h3).getDamage() >= 100)
                                if (newHostages2.get(h3).getState().equals("Carried"))
                                    newHostages2.get(h3).setState("Dead");
                                else if (newHostages2.get(h3).getState().equals("Alive"))
                                    newHostages2.get(h3).setState("Mutated");

                        }
                        Node tempNode = new Node(new State(x, y,c , currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages2, currentNode.currentState.allPills, currentNode.currentState.allAgents), Operators.drop, currentNode, currentNode.depth++,0);
                        mainQueue.add(tempNode);
                        nodesExpanded++;
                    }



            }

            if(grid[x][y].charAt(0) == 'P' && grid[x][y].length()==1 )
            { /////DOES HE HAVE TO TAKE PILL IF HE IS ON IT?

                ArrayList<Pill> newPills = new ArrayList<Pill>();
                for (Pill p : currentNode.currentState.allPills){
                    newPills.add(p.clone());
                }
                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                for (Hostage h : currentNode.currentState.allHostages){
                    newHostages.add(h.clone());
                }

                for(int p=0; p<newPills.size(); p++)
                {
                    if (newPills.get(p).getX()==x &&newPills.get(p).getY()==y)
                    {
                        if(newPills.get(p).state.equals("NT"))
                        {
                            newPills.get(p).setState("T");
                            for(int h=0; h<newHostages.size();h++){

                                newHostages.get(h).setDamage(newHostages.get(h).getDamage()-20);
                                if(newHostages.get(h).getDamage()<0) {
                              	  newHostages.get(h).setDamage(0);
                              }

                            }
                                ////////CLONE THE OLD CLONED ONE AFTER SETTING DAMAGE
                            

                            Node tempNode = new Node(new State(x,y,c,(currentNode.currentState.neoDamage)-20,currentNode.currentState.kills,newHostages, newPills, currentNode.currentState.allAgents),Operators.takePill,currentNode, currentNode.depth++,0);
                            mainQueue.add(tempNode);
                            nodesExpanded++;
                            pillTAKE=true;


                            break;
                        }
                        else if(newPills.get(p).state.equals("T"))
                        {

                        }
                    }
                }
            }


            if(grid[x][y].charAt(0) == 'P' && grid[x][y].length()!=1 ){

                for (int j = 0; j < Pad.size(); j++) {
                    Pad pad = Pad.get(j);

                    if (pad.getStartX() == x && pad.getStartY() == y) {

                        String stateToBeHashed = makeHashedState(currentNode, 100, Operators.fly);
                        if(hashedStates.contains(stateToBeHashed)) {
                            break;
                        }
                        else {
                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                newHostages.add(h2.clone());
                            }
                            for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                if (newHostages.get(h3).getDamage() >= 100)
                                    if (newHostages.get(h3).getState().equals("Carried"))
                                        newHostages.get(h3).setState("Dead");
                                    else if (newHostages.get(h3).getState().equals("Alive"))
                                        newHostages.get(h3).setState("Mutated");

                            }
                            if(!(currentNode.operator == Operators.fly)) {

                                Node tempNode = new Node(new State(pad.getEndX(), pad.getEndY(),c, currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), Operators.fly, currentNode, currentNode.depth++,0);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                                hashedStates.add(stateToBeHashed);

                                padFly=true;
                                break;
                            }
                            else{
                                break;
                            }
                        }
                    }
                }

            }


            for(int h=0; h<currentNode.currentState.allHostages.size(); h++){
                ///////////lazem n3mel condition law hostage mat fe edena
                if (currentNode.currentState.allHostages.get(h).state.equals("Alive")
                    ||currentNode.currentState.allHostages.get(h).state.equals("Carried") ||currentNode.currentState.allHostages.get(h).state.equals("Mutated") ) {
                             allHostagesSaved = false;

                    break;
                }
            }
            if(allHostagesSaved) {
                //out.println("Solution reached at depth" + currentNode.depth);
                //System.out.println(finalPrint(currentNode));
            }
        if(currentNode.currentState.neoDamage<100 && !hostageTaken){

            if(!allHostagesSaved) { ///el hostages are not all saved yet

                /////////////////ELE T7T KOLO WAS JUST 2 LINES WITHOUT HASHING
                for(int i=0; i<4; i++) {   //this checks all neighbors by taking the array by up down right and left in this order



                    int col = x + dx[i];
                    int row = y + dy[i];

                    if (col < 0 || row < 0 || col>=m || row>=n) {

                    } else {


                        if (grid[col][row].charAt(0) == 'H' )  /// NOTE
                        { ////////////NOTE:MOVE TO THE NODE IF ITS NOT MUTATED, KILL IF MUTATED BY IF CONDITION THEN MOVE NEXT ITER


                                boolean hostageMutated = false;  ////////////tb3n brdo lazem n2tel ay hostage in the vicinity or agent if we do a kill action
                                for(int h=0; h<currentNode.currentState.allHostages.size(); h++) {
                                    ///////////lazem n3mel condition law hostage mat fe edena
                                    //// NOTE: DO CAPACITY LATER
                                    if (currentNode.currentState.allHostages.get(h).getX() == col && currentNode.currentState.allHostages.get(h).getY() == row) {//get hostage
                                        ////carry the dead hostage as well i think?
                                        if (currentNode.currentState.allHostages.get(h).getState().equals("Mutated")) {

                                            //KILL IF MUTATED
                                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                                newHostages.add(h2.clone());
                                            }
                                           ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                            newHostages.get(h).setState("Dead");
                                            int kills = 0;
                                            //newAgents.get(a).setState("Dead");
                                            kills++;
                                            ArrayList<Agent> newAgents = new ArrayList<Agent>();
                                            for (Agent a : currentNode.currentState.allAgents){
                                                newAgents.add(a.clone());
                                            }

                                            int[] dx2 = { -1, 1, 0, 0 };
                                            int[] dy2= { 0, 0, 1, -1 };
                                            ///////////KILL ALL NEIGHBORING AGENTS IF THERE IS


                                            for(int k=0; k<4;k++) {
                                                        //KILLING ALL AGENTS IS DONE 5ALAS
                                                int col2 = x + dx2[k];
                                                int row2 = y + dy2[k];
                                                if (col2 < 0 || row2 < 0 || col2>=m || row2>=n) {

                                                } else {
                                                if (grid[col2][row2].charAt(0) == 'H') {
                                                    for (int a2 = 0; a2 < newHostages.size(); a2++) {
                                                        if (newHostages.get(a2).getX() == col2 && newHostages.get(a2).getY() == row2) {

                                                            if (newHostages.get(a2).state.equals("Mutated")) {
                                                            	newHostages.get(a2).setState("Dead");
                                                                kills++;

                                                            }
                                                        }
                                                        }
                                                    }
                                                
                                                if (grid[col2][row2].charAt(0) == 'A') {
                                                    for (int a2 = 0; a2 < newAgents.size(); a2++) {
                                                        if (newAgents.get(a2).getX() == col2 && newAgents.get(a2).getY() == row2) {

                                                            if (newAgents.get(a2).state.equals("Alive")) {
                                                                newAgents.get(a2).setState("Dead");
                                                                kills++;

                                                            }
                                                        }
                                                        }
                                                    }
                                                
                                                
                                                
                                                
                                                
                                                
                                                
                                                }

                                            }		
                                            ///INCUR DMG
                                            ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                                            for (Hostage h4 : newHostages) {
                                                newHostages2.add(h4.clone());
                                            }
                                            for(int h5=0; h5<newHostages2.size(); h5++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                                newHostages2.get(h5).setDamage(newHostages2.get(h5).getDamage()+2);
                                                if(newHostages2.get(h5).getDamage()>=100)
                                                    if(newHostages2.get(h5).getState().equals("Carried"))
                                                        newHostages2.get(h5).setState("Dead");
                                                    else if(newHostages2.get(h5).getState().equals("Alive"))
                                                        newHostages2.get(h5).setState("Mutated");

                                            }
                                            Node tempNode = new Node(new State(x, y,c , currentNode.currentState.neoDamage+20, currentNode.currentState.kills+kills, newHostages2, currentNode.currentState.allPills, newAgents), Operators.kill, currentNode, currentNode.depth++,0);

                                            mainQueue.add(tempNode);

                                            nodesExpanded++;
                                            hostageMutated = true;
                                            break;

                                        }


                                    }
                                }
                                    /////////GO HERE IF HOSTAGE ISNT MUTATED
                                if(!hostageMutated)
                                {
                                    String stateToBeHashed = makeHashedState(currentNode, i,null);
                                    if(hashedStates.contains(stateToBeHashed)){
                                    }
                                    else {
                                        ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                        for (Hostage h2 : currentNode.currentState.allHostages) {
                                            newHostages.add(h2.clone());
                                        }
                                        for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                            newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                            if (newHostages.get(h3).getDamage() >= 100)
                                                if (newHostages.get(h3).getState().equals("Carried"))
                                                    newHostages.get(h3).setState("Dead");
                                                else if (newHostages.get(h3).getState().equals("Alive"))
                                                    newHostages.get(h3).setState("Mutated");

                                        }
                                       boolean isHeGoingToTurn = false;
                                       for(int h4 = 0; h4 < newHostages.size(); h4++) {
                                    	   if (newHostages.get(h4).getX() == col && newHostages.get(h4).getY() == row) //get hostage
                                              ////carry the dead hostage as well i think?
                                               if (newHostages.get(h4).getDamage()>=98 && newHostages.get(h4).getState().equals("Alive"))
                                            	   isHeGoingToTurn = true;
                                       }
                                       if(!isHeGoingToTurn) {
                                        Node tempNode = new Node(new State(col, row, c, currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                        hashedStates.add(stateToBeHashed);
                                        mainQueue.add(tempNode);
                                        nodesExpanded++;
                                       }
                                      
                                    	   
                                    }
                                }
                            }





                        else if(grid[col][row].charAt(0) == 'T')
                        {
                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){

                            }

                            else {  ///////////////hostages might have to be dropped in the next iteration
                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried"))
                                            newHostages.get(h3).setState("Dead");
                                        else if(newHostages.get(h3).getState().equals("Alive"))
                                            newHostages.get(h3).setState("Mutated");

                                }



                                Node tempNode = new Node(new State(col, row, c, currentNode.currentState.neoDamage, currentNode.currentState.kills,newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }

                        else if(grid[col][row].charAt(0) == 'A' ){  ///////////////////NOTE NOTE NOTELAW FEE AGENT GNBY KAMAN GHEER EL NEIGHBOR DA THEN KILL THEM ALL
                            ArrayList<Agent> newAgents = new ArrayList<Agent>();
                            for (Agent a : currentNode.currentState.allAgents){
                                newAgents.add(a.clone());
                            }

                            for(int a=0; a<newAgents.size(); a++)
                            {

                                if (newAgents.get(a).getX()==col &&newAgents.get(a).getY()==row)
                                {

                                    if(newAgents.get(a).state.equals("Alive"))
                                    {

                                        ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                        for (Hostage h2 : currentNode.currentState.allHostages) {
                                            newHostages.add(h2.clone());
                                        }
                                        
                                        int kills = 0;
                                        newAgents.get(a).setState("Dead");
                                        kills++;
                                        int[] dx2 = { -1, 1, 0, 0 };
                                        int[] dy2= { 0, 0, 1, -1 };
                                        ///////////KILL ALL NEIGHBORING AGENTS IF THERE IS


                                        for(int k=0; k<4;k++) {
                                                    //KILLING ALL AGENTS IS DONE 5ALAS
                                            int col2 = x + dx2[k];
                                            int row2 = y + dy2[k];
                                            if (col2 < 0 || row2 < 0 || col2>=m || row2>=n) {

                                            } else {
                                            if (grid[col2][row2].charAt(0) == 'A') {
                                                for (int a2 = 0; a2 < newAgents.size(); a2++) {
                                                    if (newAgents.get(a2).getX() == col2 && newAgents.get(a2).getY() == row2) {

                                                        if (newAgents.get(a2).state.equals("Alive")) {
                                                            newAgents.get(a2).setState("Dead");
                                                            kills++;

                                                        }
                                                    }
                                                    }
                                                }
                                            if (grid[col2][row2].charAt(0) == 'H') {
                                                for (int a2 = 0; a2 < newHostages.size(); a2++) {
                                                    if (newHostages.get(a2).getX() == col2 && newHostages.get(a2).getY() == row2) {

                                                        if (newHostages.get(a2).state.equals("Mutated")) {
                                                        	newHostages.get(a2).setState("Dead");
                                                            kills++;

                                                        }
                                                    }
                                                    }
                                        }
                                            }

                                        }

                                        for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                            newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                            if(newHostages.get(h3).getDamage()>=100)
                                                if(newHostages.get(h3).getState().equals("Carried"))
                                                    newHostages.get(h3).setState("Dead");
                                                else if(newHostages.get(h3).getState().equals("Alive"))
                                                    newHostages.get(h3).setState("Mutated");

                                        }
                                        Node tempNode = new Node(new State(x,y,c,(currentNode.currentState.neoDamage)+20,currentNode.currentState.kills+kills,newHostages, currentNode.currentState.allPills, newAgents),Operators.kill,currentNode, currentNode.depth++,0);

                                        mainQueue.add(tempNode);
                                        nodesExpanded++;
                                        break;
                                    }
                                    else if(newAgents.get(a).state.equals("Dead"))
                                    {
                                        String stateToBeHashed = makeHashedState(currentNode, i,null);
                                        if(hashedStates.contains(stateToBeHashed)){
                                        }
                                        else {
                                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                                newHostages.add(h2.clone());
                                            }
                                            for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                                newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                                if (newHostages.get(h3).getDamage() >= 100)
                                                    if (newHostages.get(h3).getState().equals("Carried"))
                                                        newHostages.get(h3).setState("Dead");
                                                    else if (newHostages.get(h3).getState().equals("Alive"))
                                                        newHostages.get(h3).setState("Mutated");

                                            }

                                            Node tempNode = new Node(new State(col, row,c, (currentNode.currentState.neoDamage), currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, newAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                            mainQueue.add(tempNode);
                                            hashedStates.add(stateToBeHashed);
                                            nodesExpanded++;
                                        }
                                    }
                                }
                            }
                        }
                        else if (grid[col][row].charAt(0) == 'P')
                        {


                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){
                            }
                            else {

                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried"))
                                            newHostages.get(h3).setState("Dead");
                                        else if(newHostages.get(h3).getState().equals("Alive"))
                                            newHostages.get(h3).setState("Mutated");

                                }

                                Node tempNode = new Node(new State(col, row,c, (currentNode.currentState.neoDamage),currentNode.currentState.kills, newHostages,currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }
                        else if(grid[col][row].charAt(0) == '-'   ){ //GO TO PILL / EMPTY /PAD
                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){
                                }
                            else {

                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried"))
                                            newHostages.get(h3).setState("Dead");
                                        else if(newHostages.get(h3).getState().equals("Alive"))
                                            newHostages.get(h3).setState("Mutated");

                                }

                                Node tempNode = new Node(new State(col, row, c, (currentNode.currentState.neoDamage),currentNode.currentState.kills, newHostages,currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }

                    }


                }

            }
            else {
            	//System.out.println(c);
                int deadHostages = 0;
                for (int h = 0; h < currentNode.currentState.allHostages.size(); h++) {
                    if (currentNode.currentState.allHostages.get(h).getState().equals("Dead"))
                         deadHostages++;

                }
                if(currentNode.currentState.neoX == telephone.x &&currentNode.currentState.neoY == telephone.y) {
                    System.out.println("Solution reached at depth" + currentNode.depth);
                    String output = (finalPrint(currentNode) + ";" +deadHostages  + ";" +  currentNode.currentState.kills+ ";" + nodesExpanded);
                    System.out.println(output);
                    return output;
                }


            }

        }

        }
        
        return "No Solution";
    }   
    
    
    public static String IterativeDPS(String[][] grid) {
        int[] dx = { -1, 1, 0, 0 };
        int[] dy = { 0, 0, 1, -1 };
        int nodesExpanded = 0;
        HashSet<String> hashedStates = new HashSet<String>();

        hostNo = hostages.size();
        HashSet<State> set=new HashSet<State>();
        Stack<Node> mainQueue = new Stack<Node>(); //actually a stack not a queue :p but we do that because the names are already set to mainQueue from the copied code
        State startState = new State(neo.getX(), neo.getY(),c,0,0, hostages ,Pill, agents);
        //////////////lazem ncheck anhy hostage eta5ed since 2 neos could have saved 2 different hostages and be in the same state
        /// which could be done by comparing hostage X and Y


        Node start = new Node(startState, null, null , 1,0);

        mainQueue.add(start);

        int level = 10;
        //if a node exceeds this level ^ don't put it in the stack
        //else, put it in the stack
        //make 2 variables
        //


        while(true) {

        	
	        while(!mainQueue.isEmpty()){
	        	
	            boolean pillTAKE = false;
	            boolean padFly = false;
	            boolean allHostagesSaved=true;
	            boolean hostageTaken = false;
	            Node currentNode = mainQueue.peek(); //el node ele hn3mlha dequeue from the front
	            int x = currentNode.currentState.neoX; //tb3n lazem nghyha l private w n3melha getMETHOD bas lateron
	            int y =currentNode.currentState.neoY;
	            mainQueue.pop();
	
	            //all the checks if we are on H, pad, TB
	
	
	       ///     if()
	
	            if (grid[x][y].charAt(0) == 'H' ) { ////////////you move first to that node and then drop/carry hostage
	                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
	                for (Hostage h : currentNode.currentState.allHostages){
	                    newHostages.add(h.clone());
	                }
	                for(int h=0; h<newHostages.size(); h++){
	                    ///////////lazem n3mel condition law hostage mat fe edena
	                    //// NOTE: DO CAPACITY LATER
	                    if (newHostages.get(h).getX()==x &&newHostages.get(h).getY()==y) {//get hostage
	                        ////carry the dead hostage as well i think?
	                    	
	                    	//System.out.println(newHostages.get(h).getState());
	                        if (newHostages.get(h).getState().equals("Alive")) {
	                            // is it the next node or this node tho?
	                            newHostages.get(h).setState("Carried");
	                            ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
	                            for (Hostage h2 : newHostages) {
	                                newHostages2.add(h2.clone());
	                            }
	                            for(int h3=0; h3<newHostages2.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
	                                newHostages2.get(h3).setDamage(newHostages2.get(h3).getDamage()+2);
	                                if(newHostages2.get(h3).getDamage()>=100  )
	
	                                    if(newHostages2.get(h3).getState().equals("Carried"))
	                                        newHostages2.get(h3).setState("Dead");
	                                    else if(newHostages2.get(h3).getState().equals("Alive"))
	                                    	newHostages2.get(h3).setState("Mutated");
	
	                            }
	
	                            if(canCarry(currentNode.currentState.allHostages)){
	                            	Node tempNode = new Node(new State(x,y,c,currentNode.currentState.neoDamage,currentNode.currentState.kills,newHostages2, currentNode.currentState.allPills, currentNode.currentState.allAgents),Operators.carry,currentNode, currentNode.depth+1,0);	
	                                if(tempNode.depth <= level) { //add the chid node to the stack if and only if it doesn't exceed the current level
	                                	mainQueue.add(tempNode);
	                                	nodesExpanded++;
	                                	hostageTaken= true;  ////////////NOTE: IDK HOW THIS WORKS SO IT MAYBE WONT WORK
	                                }
	                            }/*else {
	                            	Node tempNode = new Node(new State(x,y,c,currentNode.currentState.neoDamage,currentNode.currentState.kills,currentNode.currentState.allHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents),Oper,currentNode, currentNode.depth++);
	                                mainQueue.add(tempNode);
	                            }*/
	                            //mainQueue.add(tempNode);
	                            break;
	                            //shofo law alive
	                        }
	
	                    }
	                }
	
	            }
	            if (grid[x][y].charAt(0) == 'T' ) { ////////////if there is any hostages to be dropped then add a node after every hostage is dropped, else dont add node
	                boolean carriedHostages = false;
	                    ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
	                    for (Hostage h : currentNode.currentState.allHostages) {
	
	                        newHostages.add(h.clone());
	                    }
	                    for (int h = 0; h < newHostages.size(); h++) {
	                        ///////////lazem n3mel condition law hostage mat fe edena
	                        //// NOTE: DO CAPACITY LATER
	                        if (newHostages.get(h).getState().equals("Carried")) {//get hostage
	                            carriedHostages = true;
	                            break;
	                        }
	                    }
	                    if (carriedHostages) {
	                        for (int h = 0; h < newHostages.size(); h++) {
	                            if (newHostages.get(h).getState().equals("Carried")) {//get hostage
	                                newHostages.get(h).setState("Saved");
	                            }
	                        }
	
	                        ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
	                        for (Hostage h2 : newHostages) {
	                            newHostages2.add(h2.clone());
	                        }
	                        for (int h3 = 0; h3 < newHostages2.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
	                            newHostages2.get(h3).setDamage(newHostages2.get(h3).getDamage() + 2);
	                            if (newHostages2.get(h3).getDamage() >= 100)
	                                if (newHostages2.get(h3).getState().equals("Carried"))
	                                    newHostages2.get(h3).setState("Dead");
	                                else if (newHostages2.get(h3).getState().equals("Alive"))
	                                    newHostages2.get(h3).setState("Mutated");
	
	                        }
	                        Node tempNode = new Node(new State(x, y,c , currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages2, currentNode.currentState.allPills, currentNode.currentState.allAgents), Operators.drop, currentNode, currentNode.depth+1,0);
                            
	                        if(tempNode.depth <= level) { //add the child node to the stack if and only if it doesn't exceed the current level
                            	mainQueue.add(tempNode);
                            	nodesExpanded++;
	                        }
	                    }
	            }
	
	            if(grid[x][y].charAt(0) == 'P' && grid[x][y].length()==1 )
	            { /////DOES HE HAVE TO TAKE PILL IF HE IS ON IT?
	
	                ArrayList<Pill> newPills = new ArrayList<Pill>();
	                for (Pill p : currentNode.currentState.allPills){
	                    newPills.add(p.clone());
	                }
	                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
	                for (Hostage h : currentNode.currentState.allHostages){
	                    newHostages.add(h.clone());
	                }
	
	                for(int p=0; p<newPills.size(); p++)
	                {
	
	                    if (newPills.get(p).getX()==x &&newPills.get(p).getY()==y)
	                    {
	
	
	                        if(newPills.get(p).state.equals("NT"))
	                        {
	                            newPills.get(p).setState("T");
	                            for(int h=0; h<newHostages.size();h++){
	
	                                newHostages.get(h).setDamage(newHostages.get(h).getDamage()-20);
	
	                            }
	                                ////////CLONE THE OLD CLONED ONE AFTER SETTING DAMAGE
	                            /*ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
	                            for (Hostage h2 : newHostages) {
	                                newHostages2.add(h2.clone());
	                            }
	                            for(int h3=0; h3<newHostages2.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
	                                newHostages2.get(h3).setDamage(newHostages2.get(h3).getDamage()+2);
	                                if(newHostages2.get(h3).getDamage()>=100)
	                                    if(newHostages2.get(h3).getState().equals("Carried"))
	                                        newHostages2.get(h3).setState("Dead");
	                                    else if(newHostages2.get(h3).getState().equals("Alive"))
	                                        newHostages2.get(h3).setState("Mutated");
	
	                            }*/
	
	                            Node tempNode = new Node(new State(x,y,c,(currentNode.currentState.neoDamage)-20,currentNode.currentState.kills,newHostages, newPills, currentNode.currentState.allAgents),Operators.takePill,currentNode, currentNode.depth+1,0);

		                        if(tempNode.depth <= level) { //add the child node to the stack if and only if it doesn't exceed the current level
	                            	mainQueue.add(tempNode);
	                            	nodesExpanded++;
		                        }
	                            pillTAKE=true;
	
	
	                            break;
	                        }
	                        else if(newPills.get(p).state.equals("T"))
	                        {
	
	                        }
	                    }
	                }
	            }
	
	
	            if(grid[x][y].charAt(0) == 'P' && grid[x][y].length()!=1 ){
	
	                for (int j = 0; j < Pad.size(); j++) {
	                    Pad pad = Pad.get(j);
	
	                    if (pad.getStartX() == x && pad.getStartY() == y) {
	
	                        String stateToBeHashed = makeHashedState(currentNode, 100, Operators.fly);
	                        if(hashedStates.contains(stateToBeHashed)) {
	                            break;
	                        }
	                        else {
	                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
	                            for (Hostage h2 : currentNode.currentState.allHostages) {
	                                newHostages.add(h2.clone());
	                            }
	                            for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
	                                newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
	                                if (newHostages.get(h3).getDamage() >= 100)
	                                    if (newHostages.get(h3).getState().equals("Carried"))
	                                        newHostages.get(h3).setState("Dead");
	                                    else if (newHostages.get(h3).getState().equals("Alive"))
	                                        newHostages.get(h3).setState("Mutated");
	
	                            }
	                            if(!(currentNode.operator == Operators.fly)) {
	
	                                Node tempNode = new Node(new State(pad.getEndX(), pad.getEndY(),c, currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), Operators.fly, currentNode, currentNode.depth+1,0);

	    	                        if(tempNode.depth <= level) { //add the child node to the stack if and only if it doesn't exceed the current level
	                                	mainQueue.add(tempNode);
	                                	nodesExpanded++;
	                                	hashedStates.add(stateToBeHashed);
	    	                        }
	
	                                padFly=true;
	                                break;
	                            }
	                            else{
	                                break;
	                            }
	                        }
	                    }
	                }
	
	            }
	
	
	            for(int h=0; h<currentNode.currentState.allHostages.size(); h++){
	                ///////////lazem n3mel condition law hostage mat fe edena
	                if (currentNode.currentState.allHostages.get(h).state.equals("Alive")
	                    ||currentNode.currentState.allHostages.get(h).state.equals("Carried") ||currentNode.currentState.allHostages.get(h).state.equals("Mutated") ) {
	                             allHostagesSaved = false;
	
	                    break;
	                }
	            }
	            if(allHostagesSaved) {
	                //out.println("Solution reached at depth" + currentNode.depth);
	                //System.out.println(finalPrint(currentNode));
	            }
	        if(currentNode.currentState.neoDamage<100 && !hostageTaken){
	
	            if(!allHostagesSaved) { ///el hostages are not all saved yet
	
	                /////////////////ELE T7T KOLO WAS JUST 2 LINES WITHOUT HASHING
	                for(int i=0; i<4; i++) {   //this checks all neighbors by taking the array by up down right and left in this order
	
	                	
	                    int col = x + dx[i];
	                    int row = y + dy[i];
	
	                    if (col < 0 || row < 0 || col>=m || row>=n) {
	
	                    } else {
	
	
	                        if (grid[col][row].charAt(0) == 'H' )  /// NOTE
	                        { ////////////NOTE:MOVE TO THE NODE IF ITS NOT MUTATED, KILL IF MUTATED BY IF CONDITION THEN MOVE NEXT ITER
	
	
	                                boolean hostageMutated = false;  ////////////tb3n brdo lazem n2tel ay hostage in the vicinity or agent if we do a kill action
	                                for(int h=0; h<currentNode.currentState.allHostages.size(); h++) {
	                                    ///////////lazem n3mel condition law hostage mat fe edena
	                                    //// NOTE: DO CAPACITY LATER
	                                    if (currentNode.currentState.allHostages.get(h).getX() == col && currentNode.currentState.allHostages.get(h).getY() == row) {//get hostage
	                                        ////carry the dead hostage as well i think?
	                                        if (currentNode.currentState.allHostages.get(h).getState().equals("Mutated")) {
	
	                                            //KILL IF MUTATED
	                                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
	                                            for (Hostage h2 : currentNode.currentState.allHostages) {
	                                                newHostages.add(h2.clone());
	                                            }
	                                           ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
	                                            newHostages.get(h).setState("Dead");
	                                            int kills = 0;
	                                            //newAgents.get(a).setState("Dead");
	                                            kills++;
	                                            ArrayList<Agent> newAgents = new ArrayList<Agent>();
	                                            for (Agent a : currentNode.currentState.allAgents){
	                                                newAgents.add(a.clone());
	                                            }

	                                            int[] dx2 = { -1, 1, 0, 0 };
	                                            int[] dy2= { 0, 0, 1, -1 };
	                                            ///////////KILL ALL NEIGHBORING AGENTS IF THERE IS


	                                            for(int k=0; k<4;k++) {
	                                                        //KILLING ALL AGENTS IS DONE 5ALAS
	                                                int col2 = x + dx2[k];
	                                                int row2 = y + dy2[k];
	                                                if (col2 < 0 || row2 < 0 || col2>=m || row2>=n) {

	                                                } else {
	                                                if (grid[col2][row2].charAt(0) == 'H') {
	                                                    for (int a2 = 0; a2 < newHostages.size(); a2++) {
	                                                        if (newHostages.get(a2).getX() == col2 && newHostages.get(a2).getY() == row2) {

	                                                            if (newHostages.get(a2).state.equals("Mutated")) {
	                                                            	newHostages.get(a2).setState("Dead");
	                                                                kills++;

	                                                            }
	                                                        }
	                                                        }
	                                                    }
	                                                
	                                                if (grid[col2][row2].charAt(0) == 'A') {
	                                                    for (int a2 = 0; a2 < newAgents.size(); a2++) {
	                                                        if (newAgents.get(a2).getX() == col2 && newAgents.get(a2).getY() == row2) {

	                                                            if (newAgents.get(a2).state.equals("Alive")) {
	                                                                newAgents.get(a2).setState("Dead");
	                                                                kills++;

	                                                            }
	                                                        }
	                                                        }
	                                                    }
	                                                
	                                                
	                                                
	                                                
	                                                
	                                                
	                                                
	                                                }

	                                            }		
	                                            	


	
	                                            ///INCUR DMG
	                                            ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
	                                            for (Hostage h4 : newHostages) {
	                                                newHostages2.add(h4.clone());
	                                            }
	                                            for(int h5=0; h5<newHostages2.size(); h5++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
	                                                newHostages2.get(h5).setDamage(newHostages2.get(h5).getDamage()+2);
	                                                if(newHostages2.get(h5).getDamage()>=100)
	                                                    if(newHostages2.get(h5).getState().equals("Carried"))
	                                                        newHostages2.get(h5).setState("Dead");
	                                                    else if(newHostages2.get(h5).getState().equals("Alive"))
	                                                        newHostages2.get(h5).setState("Mutated");
	
	                                            }
	                                            
	                                            Node tempNode = new Node(new State(x, y,c , currentNode.currentState.neoDamage+20, currentNode.currentState.kills+kills, newHostages2, currentNode.currentState.allPills, newAgents), Operators.kill, currentNode, currentNode.depth+1,0);

	                	                        if(tempNode.depth <= level) { //add the child node to the stack if and only if it doesn't exceed the current level
	                	                        	hostageMutated = true;
	                                            	mainQueue.add(tempNode);
	                                            	nodesExpanded++;
	                	                        }
	
	                                            break;
	
	                                        }
	                                    }
	                                }
	                                    /////////GO HERE IF HOSTAGE ISNT MUTATED
	                                if(!hostageMutated)
	                                {
	                                    String stateToBeHashed = makeHashedState(currentNode, i,null);
	                                    if(hashedStates.contains(stateToBeHashed)){
	                                    }
	                                    else {
	                                        ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
	                                        for (Hostage h2 : currentNode.currentState.allHostages) {
	                                            newHostages.add(h2.clone());
	                                        }
	                                        for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
	                                            newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
	                                            if (newHostages.get(h3).getDamage() >= 100)
	                                                if (newHostages.get(h3).getState().equals("Carried"))
	                                                    newHostages.get(h3).setState("Dead");
	                                                else if (newHostages.get(h3).getState().equals("Alive"))
	                                                    newHostages.get(h3).setState("Mutated");
	
	                                        }
	
	                                        boolean isHeGoingToTurn = false;
	                                        for(int h4 = 0; h4 < newHostages.size(); h4++) {
	                                     	   if (newHostages.get(h4).getX() == col && newHostages.get(h4).getY() == row) //get hostage
	                                               ////carry the dead hostage as well i think?
	                                                if (newHostages.get(h4).getDamage()>=98 && newHostages.get(h4).getState().equals("Alive"))
	                                             	   isHeGoingToTurn = true;
	                                        }
	                                        if(!isHeGoingToTurn) {
	                                        Node tempNode = new Node(new State(col, row, c, currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth+1,0);

	            	                        if(tempNode.depth <= level) { //add the child node to the stack if and only if it doesn't exceed the current level
	            	                        	hashedStates.add(stateToBeHashed);
	                                        	mainQueue.add(tempNode);
	                                        	nodesExpanded++;
	            	                        }
	            	                        }
	                                    }
	                                }
	                            }
	
	
	                        else if(grid[col][row].charAt(0) == 'T')
	                        {
	                            String stateToBeHashed = makeHashedState(currentNode, i,null);
	                            if(hashedStates.contains(stateToBeHashed)){
	
	                            }
	
	                            else {  ///////////////hostages might have to be dropped in the next iteration
	                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
	                                for (Hostage h2 : currentNode.currentState.allHostages) {
	                                    newHostages.add(h2.clone());
	                                }
	                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
	                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
	                                    if(newHostages.get(h3).getDamage()>=100)
	                                        if(newHostages.get(h3).getState().equals("Carried"))
	                                            newHostages.get(h3).setState("Dead");
	                                        else if(newHostages.get(h3).getState().equals("Alive"))
	                                            newHostages.get(h3).setState("Mutated");
	
	                                }
	
	                                Node tempNode = new Node(new State(col, row, c, currentNode.currentState.neoDamage, currentNode.currentState.kills,newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth+1,0);

	    	                        if(tempNode.depth <= level) { //add the child node to the stack if and only if it doesn't exceed the current level
	    	                        	hashedStates.add(stateToBeHashed);
	                                	mainQueue.add(tempNode);
	                                	nodesExpanded++;
	    	                        }
	                            }
	                        }
	
	                        if(grid[col][row].charAt(0) == 'A' ){
	
	
	                        	ArrayList<Agent> newAgents = new ArrayList<Agent>();
	                            for (Agent a : currentNode.currentState.allAgents){
	                                newAgents.add(a.clone());
	                            }
	
	                            for(int a=0; a<newAgents.size(); a++)
	                            {
	
	                                if (newAgents.get(a).getX()==col &&newAgents.get(a).getY()==row)
	                                {
	
	                                    if(newAgents.get(a).state.equals("Alive"))
	                                    {
	
	                                        ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
	                                        for (Hostage h2 : currentNode.currentState.allHostages) {
	                                            newHostages.add(h2.clone());
	                                        }
	                                        
	                                        int kills = 0;
	                                        newAgents.get(a).setState("Dead");
	                                        kills++;
	                                        int[] dx2 = { -1, 1, 0, 0 };
	                                        int[] dy2= { 0, 0, 1, -1 };
	                                        ///////////KILL ALL NEIGHBORING AGENTS IF THERE IS
	
	
	                                        for(int k=0; k<4;k++) {
	                                                    //KILLING ALL AGENTS IS DONE 5ALAS
	                                            int col2 = x + dx2[k];
	                                            int row2 = y + dy2[k];
	                                            if (col2 < 0 || row2 < 0 || col2>=m || row2>=n) {
	
	                                            } else {
		                                            if (grid[col2][row2].charAt(0) == 'A') {
		                                            	
		                                                for (int a2 = 0; a2 < newAgents.size(); a2++) {
		                                                    if (newAgents.get(a2).getX() == col2 && newAgents.get(a2).getY() == row2) {
		
		                                                        if (newAgents.get(a2).state.equals("Alive")) {
		                                                            newAgents.get(a2).setState("Dead");
		                                                            kills++;
		
		                                                        }
		                                                    }
		                                                }
		                                            }
		                                            if (grid[col2][row2].charAt(0) == 'H') {
	                                                    for (int a2 = 0; a2 < newHostages.size(); a2++) {
	                                                        if (newHostages.get(a2).getX() == col2 && newHostages.get(a2).getY() == row2) {

	                                                            if (newHostages.get(a2).state.equals("Mutated")) {
	                                                            	newHostages.get(a2).setState("Dead");
	                                                                kills++;

	                                                            }
	                                                        }
	                                                        }
	                                            }

		                                            
		                                            
		                                            
		                                            
	                                            }
	
	                                        }
	                                        for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
	                                            newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
	                                            if(newHostages.get(h3).getDamage()>=100)
	                                                if(newHostages.get(h3).getState().equals("Carried"))
	                                                    newHostages.get(h3).setState("Dead");
	                                                else if(newHostages.get(h3).getState().equals("Alive"))
	                                                    newHostages.get(h3).setState("Mutated");
	
	                                        }
	
	                                        Node tempNode = new Node(new State(x,y,c,(currentNode.currentState.neoDamage)+20,currentNode.currentState.kills+kills,newHostages, currentNode.currentState.allPills, newAgents),Operators.kill,currentNode, currentNode.depth+1,0);

	            	                        if(tempNode.depth <= level) { //add the child node to the stack if and only if it doesn't exceed the current level
	                                        	mainQueue.add(tempNode);
	                                        	nodesExpanded++;
	            	                        }
	                                        break;
	                                    
	                                    }else if(newAgents.get(a).state.equals("Dead")){
	                                    	
	                                        String stateToBeHashed = makeHashedState(currentNode, i,null);
	                                        if(hashedStates.contains(stateToBeHashed)){
	                                        }
	                                        else {
	                                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
	                                            for (Hostage h2 : currentNode.currentState.allHostages) {
	                                                newHostages.add(h2.clone());
	                                            }
	                                            for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
	                                                newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
	                                                if (newHostages.get(h3).getDamage() >= 100)
	                                                    if (newHostages.get(h3).getState().equals("Carried"))
	                                                        newHostages.get(h3).setState("Dead");
	                                                    else if (newHostages.get(h3).getState().equals("Alive"))
	                                                        newHostages.get(h3).setState("Mutated");
	
	                                            }
	
	                                            Node tempNode = new Node(new State(col, row,c, (currentNode.currentState.neoDamage), currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, newAgents), operationFinder(i), currentNode, currentNode.depth+1,0);

	                	                        if(tempNode.depth <= level) { //add the child node to the stack if and only if it doesn't exceed the current level
	                	                        	
	                	                        	hashedStates.add(stateToBeHashed);
	                                            	mainQueue.add(tempNode);
	                                            	nodesExpanded++;
	                	                        }
	                                        }
	                                    }
	                                }
	                            }
	                        }
	                        else if (grid[col][row].charAt(0) == 'P')
	                        {
	
	
	                            String stateToBeHashed = makeHashedState(currentNode, i,null);
	                            if(hashedStates.contains(stateToBeHashed)){
	                            }
	                            else {
	
	                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
	                                for (Hostage h2 : currentNode.currentState.allHostages) {
	                                    newHostages.add(h2.clone());
	                                }
	                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
	                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
	                                    if(newHostages.get(h3).getDamage()>=100)
	                                        if(newHostages.get(h3).getState().equals("Carried"))
	                                            newHostages.get(h3).setState("Dead");
	                                        else if(newHostages.get(h3).getState().equals("Alive"))
	                                            newHostages.get(h3).setState("Mutated");
	
	                                }
	
	                                Node tempNode = new Node(new State(col, row,c, (currentNode.currentState.neoDamage),currentNode.currentState.kills, newHostages,currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth+1,0);
	                                
        	                        if(tempNode.depth <= level) { //add the child node to the stack if and only if it doesn't exceed the current level
        	                        	hashedStates.add(stateToBeHashed);
                                    	mainQueue.add(tempNode);
                                    	nodesExpanded++;
        	                        }
	                            }
	                        }
	                        else if(grid[col][row].charAt(0) == '-'   ){ //GO TO PILL / EMPTY /PAD
	                            String stateToBeHashed = makeHashedState(currentNode, i,null);
	                            if(hashedStates.contains(stateToBeHashed)){
	                                }
	                            else {
	
	                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
	                                for (Hostage h2 : currentNode.currentState.allHostages) {
	                                    newHostages.add(h2.clone());
	                                }
	                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
	                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
	                                    if(newHostages.get(h3).getDamage()>=100)
	                                        if(newHostages.get(h3).getState().equals("Carried"))
	                                            newHostages.get(h3).setState("Dead");
	                                        else if(newHostages.get(h3).getState().equals("Alive"))
	                                            newHostages.get(h3).setState("Mutated");
	
	                                }
	
	                                Node tempNode = new Node(new State(col, row, c, (currentNode.currentState.neoDamage),currentNode.currentState.kills, newHostages,currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth+1,0);
        	                        if(tempNode.depth <= level) { //add the child node to the stack if and only if it doesn't exceed the current level
        	                        	hashedStates.add(stateToBeHashed);
                                    	mainQueue.add(tempNode);
                                    	nodesExpanded++;
        	                        }
	                            }
	                        }
	
	                    }
	
	
	                }
	
	            }
	            else {
	            	//System.out.println(c);
	                int deadHostages = 0;
	                for (int h = 0; h < currentNode.currentState.allHostages.size(); h++) {
	                    if (currentNode.currentState.allHostages.get(h).getState().equals("Dead"))
	                         deadHostages++;
	
	                }
	                if(currentNode.currentState.neoX == telephone.x &&currentNode.currentState.neoY == telephone.y) {
	                    System.out.println("Solution reached at depth" + currentNode.depth);
	                    String output = (finalPrint(currentNode) + ";" +deadHostages  + ";" +  currentNode.currentState.kills+ ";" + nodesExpanded);
	                    System.out.println(output);
	                    return output;
	                }
	
	
	            }
	        }
	        level+=10;
	        }
        }
      //  return "No Solution";
    }

    
    public static String DepthFS(String[][] grid) {
        int[] dx = { -1, 1, 0, 0 };
        int[] dy = { 0, 0, 1, -1 };
        int nodesExpanded = 0;
        HashSet<String> hashedStates = new HashSet<String>();

        hostNo = hostages.size();
        HashSet<State> set=new HashSet<State>();
        Stack<Node> mainQueue = new Stack<Node>();
        State startState = new State(neo.getX(), neo.getY(),c,0,0, hostages ,Pill, agents);
        //////////////lazem ncheck anhy hostage eta5ed since 2 neos could have saved 2 different hostages and be in the same state
        /// which could be done by comparing hostage X and Y


        Node start = new Node(startState, null, null , 0,0);

        mainQueue.add(start);



        while(!mainQueue.isEmpty()){
            boolean pillTAKE = false;
            boolean padFly = false;
            boolean allHostagesSaved=true;
            boolean hostagePickedUp= false;
            Node currentNode = mainQueue.peek(); //el node ele hn3mlha dequeue from the front
            int x = currentNode.currentState.neoX; //tb3n lazem nghyha l private w n3melha getMETHOD bas lateron
            int y =currentNode.currentState.neoY;
            mainQueue.pop();

            //all the checks if we are on H, pad, TB



            if (grid[x][y].charAt(0) == 'H' ) { ////////////you move first to that node and then drop/carry hostage
                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                for (Hostage h : currentNode.currentState.allHostages){
                    newHostages.add(h.clone());
                }
                for(int h=0; h<newHostages.size(); h++){
                    ///////////lazem n3mel condition law hostage mat fe edena
                    //// NOTE: DO CAPACITY LATER
                    if (newHostages.get(h).getX()==x &&newHostages.get(h).getY()==y) {//get hostage
                        ////carry the dead hostage as well i think?
                    	
                    	//System.out.println(newHostages.get(h).getState());
                        if (newHostages.get(h).getState().equals("Alive")) {
                            // is it the next node or this node tho?
                            newHostages.get(h).setState("Carried");
                            ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                            for (Hostage h2 : newHostages) {
                                newHostages2.add(h2.clone());
                            }
                            for(int h3=0; h3<newHostages2.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                newHostages2.get(h3).setDamage(newHostages2.get(h3).getDamage()+2);
                                if(newHostages2.get(h3).getDamage()>=100  )

                                    if(newHostages2.get(h3).getState().equals("Carried"))
                                        newHostages2.get(h3).setState("Dead");
                                    else if(newHostages2.get(h3).getState().equals("Alive"))
                                    newHostages2.get(h3).setState("Mutated");

                            }

                            if(canCarry(currentNode.currentState.allHostages)){
                            	Node tempNode = new Node(new State(x,y,c,currentNode.currentState.neoDamage,currentNode.currentState.kills,newHostages2, currentNode.currentState.allPills, currentNode.currentState.allAgents),Operators.carry,currentNode, currentNode.depth++,0);	
                            	hostagePickedUp = true;
                                mainQueue.add(tempNode);
                            }/*else {
                            	Node tempNode = new Node(new State(x,y,c,currentNode.currentState.neoDamage,currentNode.currentState.kills,currentNode.currentState.allHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents),Oper,currentNode, currentNode.depth++);
                                mainQueue.add(tempNode);
                            }*/
                            //mainQueue.add(tempNode);
                            nodesExpanded++;
                            break;
                            //shofo law alive
                        }

                    }
                }

            }
            if (grid[x][y].charAt(0) == 'T' ) { ////////////if there is any hostages to be dropped then add a node after every hostage is dropped, else dont add node
                boolean carriedHostages = false;
                    ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                    for (Hostage h : currentNode.currentState.allHostages) {

                        newHostages.add(h.clone());
                    }
                    for (int h = 0; h < newHostages.size(); h++) {
                        ///////////lazem n3mel condition law hostage mat fe edena
                        //// NOTE: DO CAPACITY LATER
                        if (newHostages.get(h).getState().equals("Carried")) {//get hostage
                            carriedHostages = true;
                            break;
                        }
                    }
                    if (carriedHostages) {
                        for (int h = 0; h < newHostages.size(); h++) {
                            if (newHostages.get(h).getState().equals("Carried")) {//get hostage
                                newHostages.get(h).setState("Saved");


                            }

                        }

                        ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                        for (Hostage h2 : newHostages) {
                            newHostages2.add(h2.clone());
                        }
                        for (int h3 = 0; h3 < newHostages2.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                            newHostages2.get(h3).setDamage(newHostages2.get(h3).getDamage() + 2);
                            if (newHostages2.get(h3).getDamage() >= 100)
                                if (newHostages2.get(h3).getState().equals("Carried"))
                                    newHostages2.get(h3).setState("Dead");
                                else if (newHostages2.get(h3).getState().equals("Alive"))
                                    newHostages2.get(h3).setState("Mutated");

                        }
                        Node tempNode = new Node(new State(x, y,c , currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages2, currentNode.currentState.allPills, currentNode.currentState.allAgents), Operators.drop, currentNode, currentNode.depth++,0);
                        mainQueue.add(tempNode);
                        nodesExpanded++;
                    }

            }

            if(grid[x][y].charAt(0) == 'P' && grid[x][y].length()==1 )
            { /////DOES HE HAVE TO TAKE PILL IF HE IS ON IT?

                ArrayList<Pill> newPills = new ArrayList<Pill>();
                for (Pill p : currentNode.currentState.allPills){
                    newPills.add(p.clone());
                }
                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                for (Hostage h : currentNode.currentState.allHostages){
                    newHostages.add(h.clone());
                }

                for(int p=0; p<newPills.size(); p++)
                {

                    if (newPills.get(p).getX()==x &&newPills.get(p).getY()==y)
                    {


                        if(newPills.get(p).state.equals("NT"))
                        {
                            newPills.get(p).setState("T");
                            for(int h=0; h<newHostages.size();h++){

                                newHostages.get(h).setDamage(newHostages.get(h).getDamage()-20);

                            }
                                ////////CLONE THE OLD CLONED ONE AFTER SETTING DAMAGE
                           /* ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                            for (Hostage h2 : newHostages) {
                                newHostages2.add(h2.clone());
                            }
                            for(int h3=0; h3<newHostages2.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                newHostages2.get(h3).setDamage(newHostages2.get(h3).getDamage()+2);
                                if(newHostages2.get(h3).getDamage()>=100)
                                    if(newHostages2.get(h3).getState().equals("Carried"))
                                        newHostages2.get(h3).setState("Dead");
                                    else if(newHostages2.get(h3).getState().equals("Alive"))
                                        newHostages2.get(h3).setState("Mutated");

                            }*/

                            Node tempNode = new Node(new State(x,y,c,(currentNode.currentState.neoDamage)-20,currentNode.currentState.kills,newHostages, newPills, currentNode.currentState.allAgents),Operators.takePill,currentNode, currentNode.depth++,0);
                            mainQueue.add(tempNode);
                            nodesExpanded++;
                            pillTAKE=true;


                            break;
                        }
                        else if(newPills.get(p).state.equals("T"))
                        {

                        }
                    }
                }
            }

            if(grid[x][y].charAt(0) == 'P' && grid[x][y].length()!=1 ){

                for (int j = 0; j < Pad.size(); j++) {
                    Pad pad = Pad.get(j);

                    if (pad.getStartX() == x && pad.getStartY() == y) {

                        String stateToBeHashed = makeHashedState(currentNode, 100, Operators.fly);
                        if(hashedStates.contains(stateToBeHashed)) {
                            break;
                        }
                        else {
                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                newHostages.add(h2.clone());
                            }
                            for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                if (newHostages.get(h3).getDamage() >= 100)
                                    if (newHostages.get(h3).getState().equals("Carried"))
                                        newHostages.get(h3).setState("Dead");
                                    else if (newHostages.get(h3).getState().equals("Alive"))
                                        newHostages.get(h3).setState("Mutated");

                            }
                            if(!(currentNode.operator == Operators.fly)) {

                                Node tempNode = new Node(new State(pad.getEndX(), pad.getEndY(),c, currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), Operators.fly, currentNode, currentNode.depth++,0);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                                hashedStates.add(stateToBeHashed);

                                padFly=true;
                                break;
                            }
                            else{
                                break;
                            }
                        }
                    }
                }

            }


            for(int h=0; h<currentNode.currentState.allHostages.size(); h++){
            	//System.out.println(currentNode.currentState.allHostages.get(h).state);
                ///////////lazem n3mel condition law hostage mat fe edena
                if (currentNode.currentState.allHostages.get(h).state.equals("Alive")
                    ||currentNode.currentState.allHostages.get(h).state.equals("Carried") ||currentNode.currentState.allHostages.get(h).state.equals("Mutated") ) {
                             allHostagesSaved = false;

                    break;
                }
            }
            if(allHostagesSaved) {
                //out.println("Solution reached at depth" + currentNode.depth);
                //System.out.println(finalPrint(currentNode));
            }
        if(currentNode.currentState.neoDamage<100  &&!hostagePickedUp){

            if(!allHostagesSaved) { ///el hostages are not all saved yet

            	
                /////////////////ELE T7T KOLO WAS JUST 2 LINES WITHOUT HASHING
                for(int i=0; i<4; i++) {   //this checks all neighbors by taking the array by up down right and left in this order


                    int col = x + dx[i];
                    int row = y + dy[i];

                    if (col < 0 || row < 0 || col>=m || row>=n) {

                    } else {


                    	if (grid[col][row].charAt(0) == 'H' )  /// NOTE
                        { ////////////NOTE:MOVE TO THE NODE IF ITS NOT MUTATED, KILL IF MUTATED BY IF CONDITION THEN MOVE NEXT ITER


                                boolean hostageMutated = false;  ////////////tb3n brdo lazem n2tel ay hostage in the vicinity or agent if we do a kill action
                                for(int h=0; h<currentNode.currentState.allHostages.size(); h++) {
                                    ///////////lazem n3mel condition law hostage mat fe edena
                                    //// NOTE: DO CAPACITY LATER
                                    if (currentNode.currentState.allHostages.get(h).getX() == col && currentNode.currentState.allHostages.get(h).getY() == row) {//get hostage
                                        ////carry the dead hostage as well i think?
                                        if (currentNode.currentState.allHostages.get(h).getState().equals("Mutated")) {

                                            //KILL IF MUTATED
                                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                                newHostages.add(h2.clone());
                                            }
                                           ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                            newHostages.get(h).setState("Dead");

                                            int kills = 0;
                                            //newAgents.get(a).setState("Dead");
                                            kills++;
                                            ArrayList<Agent> newAgents = new ArrayList<Agent>();
                                            for (Agent a : currentNode.currentState.allAgents){
                                                newAgents.add(a.clone());
                                            }

                                            int[] dx2 = { -1, 1, 0, 0 };
                                            int[] dy2= { 0, 0, 1, -1 };
                                            ///////////KILL ALL NEIGHBORING AGENTS IF THERE IS


                                            for(int k=0; k<4;k++) {
                                                        //KILLING ALL AGENTS IS DONE 5ALAS
                                                int col2 = x + dx2[k];
                                                int row2 = y + dy2[k];
                                                if (col2 < 0 || row2 < 0 || col2>=m || row2>=n) {

                                                } else {
                                                if (grid[col2][row2].charAt(0) == 'H') {
                                                    for (int a2 = 0; a2 < newHostages.size(); a2++) {
                                                        if (newHostages.get(a2).getX() == col2 && newHostages.get(a2).getY() == row2) {

                                                            if (newHostages.get(a2).state.equals("Mutated")) {
                                                            	newHostages.get(a2).setState("Dead");
                                                                kills++;

                                                            }
                                                        }
                                                        }
                                                    }
                                                
                                                if (grid[col2][row2].charAt(0) == 'A') {
                                                    for (int a2 = 0; a2 < newAgents.size(); a2++) {
                                                        if (newAgents.get(a2).getX() == col2 && newAgents.get(a2).getY() == row2) {

                                                            if (newAgents.get(a2).state.equals("Alive")) {
                                                                newAgents.get(a2).setState("Dead");
                                                                kills++;

                                                            }
                                                        }
                                                        }
                                                    }
                                                
                                                }

                                            }		
                                            ///INCUR DMG
                                            ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                                            for (Hostage h4 : newHostages) {
                                                newHostages2.add(h4.clone());
                                            }
                                            for(int h5=0; h5<newHostages2.size(); h5++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                                newHostages2.get(h5).setDamage(newHostages2.get(h5).getDamage()+2);
                                                if(newHostages2.get(h5).getDamage()>=100)
                                                    if(newHostages2.get(h5).getState().equals("Carried"))
                                                        newHostages2.get(h5).setState("Dead");
                                                    else if(newHostages2.get(h5).getState().equals("Alive"))
                                                        newHostages2.get(h5).setState("Mutated");

                                            }
                                            Node tempNode = new Node(new State(x, y,c , currentNode.currentState.neoDamage+20, currentNode.currentState.kills+kills, newHostages2, currentNode.currentState.allPills, newAgents), Operators.kill, currentNode, currentNode.depth++,0);
                                            					
                                            mainQueue.add(tempNode);

                                            nodesExpanded++;
                                            hostageMutated = true;
                                            break;

                                        }


                                    }
                                }
                                    /////////GO HERE IF HOSTAGE ISNT MUTATED
                                if(!hostageMutated)
                                {
                                    String stateToBeHashed = makeHashedState(currentNode, i,null);
                                    if(hashedStates.contains(stateToBeHashed)){
                                    }
                                    else {
                                        ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                        for (Hostage h2 : currentNode.currentState.allHostages) {
                                            newHostages.add(h2.clone());
                                        }
                                        for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                            newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                            if (newHostages.get(h3).getDamage() >= 100)
                                                if (newHostages.get(h3).getState().equals("Carried"))
                                                    newHostages.get(h3).setState("Dead");
                                                else if (newHostages.get(h3).getState().equals("Alive"))
                                                    newHostages.get(h3).setState("Mutated");

                                        }

                                        boolean isHeGoingToTurn = false;
                                        for(int h4 = 0; h4 < newHostages.size(); h4++) {
                                     	   if (newHostages.get(h4).getX() == col && newHostages.get(h4).getY() == row) //get hostage
                                               ////carry the dead hostage as well i think?
                                                if (newHostages.get(h4).getDamage()>=98 && newHostages.get(h4).getState().equals("Alive"))
                                             	   isHeGoingToTurn = true;
                                        }
                                        if(!isHeGoingToTurn) {
                                        Node tempNode = new Node(new State(col, row, c, currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                        hashedStates.add(stateToBeHashed);
                                        mainQueue.add(tempNode);
                                        nodesExpanded++;
                                        }
                                    }
                                }
                            }

                    	else if(grid[col][row].charAt(0) == 'T')
                        {
                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){

                            }

                            else {  ///////////////hostages might have to be dropped in the next iteration
                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried"))
                                            newHostages.get(h3).setState("Dead");
                                        else if(newHostages.get(h3).getState().equals("Alive"))
                                            newHostages.get(h3).setState("Mutated");

                                }

                                boolean isHeGoingToTurn = false;
                                for(int h4 = 0; h4 < newHostages.size(); h4++) {
                             	   if (newHostages.get(h4).getX() == col && newHostages.get(h4).getY() == row) //get hostage
                                       ////carry the dead hostage as well i think?
                                        if (newHostages.get(h4).getDamage()>=98 && newHostages.get(h4).getState().equals("Alive"))
                                     	   isHeGoingToTurn = true;
                                }
                                if(!isHeGoingToTurn) {

                                Node tempNode = new Node(new State(col, row, c, currentNode.currentState.neoDamage, currentNode.currentState.kills,newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                                }
                            }
                        }

                        else if(grid[col][row].charAt(0) == 'A' ){


                        	ArrayList<Agent> newAgents = new ArrayList<Agent>();
                            for (Agent a : currentNode.currentState.allAgents){
                                newAgents.add(a.clone());
                            }

                            for(int a=0; a<newAgents.size(); a++)
                            {

                                if (newAgents.get(a).getX()==col &&newAgents.get(a).getY()==row)
                                {

                                    if(newAgents.get(a).state.equals("Alive"))
                                    {

                                        ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                        for (Hostage h2 : currentNode.currentState.allHostages) {
                                            newHostages.add(h2.clone());
                                        }
                              
                                        int kills = 0;
                                        newAgents.get(a).setState("Dead");
                                        kills++;
                                        int[] dx2 = { -1, 1, 0, 0 };
                                        int[] dy2= { 0, 0, 1, -1 };
                                        ///////////KILL ALL NEIGHBORING AGENTS IF THERE IS


                                        for(int k=0; k<4;k++) {
                                                    //KILLING ALL AGENTS IS DONE 5ALAS
                                            int col2 = x + dx2[k];
                                            int row2 = y + dy2[k];
                                            if (col2 < 0 || row2 < 0 || col2>=m || row2>=n) {

                                            } else {
	                                            if (grid[col2][row2].charAt(0) == 'A') {
	                                            	
	                                                for (int a2 = 0; a2 < newAgents.size(); a2++) {
	                                                    if (newAgents.get(a2).getX() == col2 && newAgents.get(a2).getY() == row2) {
	
	                                                        if (newAgents.get(a2).state.equals("Alive")) {
	                                                            newAgents.get(a2).setState("Dead");
	                                                            kills++;
	
	                                                        }
	                                                    }
	                                                }
	                                            }
	                                            if (grid[col2][row2].charAt(0) == 'H') {
                                                    for (int a2 = 0; a2 < newHostages.size(); a2++) {
                                                        if (newHostages.get(a2).getX() == col2 && newHostages.get(a2).getY() == row2) {

                                                            if (newHostages.get(a2).state.equals("Mutated")) {
                                                            	newHostages.get(a2).setState("Dead");
                                                                kills++;

                                                            }
                                                        }
                                                        }
                                            }

                                        }
                                        }
                                        for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                            newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                            if(newHostages.get(h3).getDamage()>=100)
                                                if(newHostages.get(h3).getState().equals("Carried"))
                                                    newHostages.get(h3).setState("Dead");
                                                else if(newHostages.get(h3).getState().equals("Alive"))
                                                    newHostages.get(h3).setState("Mutated");

                                        }
                                        Node tempNode = new Node(new State(x,y,c,(currentNode.currentState.neoDamage)+20,currentNode.currentState.kills+kills,newHostages, currentNode.currentState.allPills, newAgents),Operators.kill,currentNode, currentNode.depth++,0);

                                        mainQueue.add(tempNode);
                                        nodesExpanded++;
                                        break;
                                    
                                    }else if(newAgents.get(a).state.equals("Dead")){
                                    	
                                        String stateToBeHashed = makeHashedState(currentNode, i,null);
                                        if(hashedStates.contains(stateToBeHashed)){
                                        }
                                        else {
                                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                                newHostages.add(h2.clone());
                                            }
                                            for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                                newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                                if (newHostages.get(h3).getDamage() >= 100)
                                                    if (newHostages.get(h3).getState().equals("Carried"))
                                                        newHostages.get(h3).setState("Dead");
                                                    else if (newHostages.get(h3).getState().equals("Alive"))
                                                        newHostages.get(h3).setState("Mutated");

                                            }

                                            Node tempNode = new Node(new State(col, row,c, (currentNode.currentState.neoDamage), currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, newAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                            mainQueue.add(tempNode);
                                            hashedStates.add(stateToBeHashed);
                                            nodesExpanded++;
                                        }
                                    }
                                }
                            }
                        }

                        else if (grid[col][row].charAt(0) == 'P'){


                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){
                            }
                            else {

                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried"))
                                            newHostages.get(h3).setState("Dead");
                                        else if(newHostages.get(h3).getState().equals("Alive"))
                                            newHostages.get(h3).setState("Mutated");

                                }

                                Node tempNode = new Node(new State(col, row,c, (currentNode.currentState.neoDamage),currentNode.currentState.kills, newHostages,currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }
                        else if(grid[col][row].charAt(0) == '-'   ){ //GO TO PILL / EMPTY /PAD
                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){
                                }
                            else {

                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried"))
                                            newHostages.get(h3).setState("Dead");
                                        else if(newHostages.get(h3).getState().equals("Alive"))
                                            newHostages.get(h3).setState("Mutated");

                                }

                                Node tempNode = new Node(new State(col, row, c, (currentNode.currentState.neoDamage),currentNode.currentState.kills, newHostages,currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }

                    }


                }

            }
            else {
            	//System.out.println(c);
                int deadHostages = 0;
                for (int h = 0; h < currentNode.currentState.allHostages.size(); h++) {
                    if (currentNode.currentState.allHostages.get(h).getState().equals("Dead"))
                         deadHostages++;

                }
                if(currentNode.currentState.neoX == telephone.x &&currentNode.currentState.neoY == telephone.y) {
                    System.out.println("Solution reached at depth" + currentNode.depth);
                    String output = (finalPrint(currentNode) + ";" +deadHostages  + ";" +  currentNode.currentState.kills+ ";" + nodesExpanded);
                    System.out.println(output);
                    return output;
                }


            }
        }

        }

        return "No Solution";
    }

    
    public static String CostS(String[][] grid) {
        int[] dx = { -1, 1, 0, 0 };
        int[] dy = { 0, 0, 1, -1 };
        int nodesExpanded = 0;
        HashSet<String> hashedStates = new HashSet<String>();

        hostNo = hostages.size();
        
      //  Comparator<Node> comparator =
		
        PriorityQueue<Node> mainQueue = new PriorityQueue<Node>( new Comparator<Node>() {
			
			@Override
			public int compare(Node o1, Node o2) {
		        if(o1.cost > o2.cost) {
		            return 1;
		        } else if (o1.cost < o2.cost) {
		            return -1;
		        } else {
		            return 0;
		        }
			}
        });
        
        State startState = new State(neo.getX(), neo.getY(),c,0,0, hostages ,Pill, agents);

        Node start = new Node(startState, null, null , 0, 0);

        mainQueue.add(start);



        while(!mainQueue.isEmpty()){
        	
           	/*for(int q=0; q<mainQueue.size(); q++) {
        		System.out.println(mainQueue.);
        	}*/
        	boolean hostageTaken = false;
            boolean pillTAKE = false;
            boolean padFly = false;
            boolean allHostagesSaved=true;
            Node currentNode = mainQueue.peek(); //el node ele hn3mlha dequeue from the front
            int x = currentNode.currentState.neoX; //tb3n lazem nghyha l private w n3melha getMETHOD bas lateron
            int y =currentNode.currentState.neoY;
            mainQueue.remove();

            //System.out.println(currentNode.cost);
            //all the checks if we are on H, pad, TB

            if (grid[x][y].charAt(0) == 'H' ) { ////////////you move first to that node and then drop/carry hostage
                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                for (Hostage h : currentNode.currentState.allHostages){
                    newHostages.add(h.clone());
                }
                for(int h=0; h<newHostages.size(); h++){
                    ///////////lazem n3mel condition law hostage mat fe edena
                    //// NOTE: DO CAPACITY LATER
                    if (newHostages.get(h).getX()==x &&newHostages.get(h).getY()==y) {//get hostage
                        ////carry the dead hostage as well i think?
                        if (newHostages.get(h).getState().equals("Alive")) {
                            // is it the next node or this node tho?
                            newHostages.get(h).setState("Carried");
                            
                            ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                            
                            for (Hostage h2 : newHostages) {
                                newHostages2.add(h2.clone());
                            }
                            int hostagesThatDied=0;
                            for(int h3=0; h3<newHostages2.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                newHostages2.get(h3).setDamage(newHostages2.get(h3).getDamage()+2);
                                if(newHostages2.get(h3).getDamage()>=100  )

                                    if(newHostages2.get(h3).getState().equals("Carried")) {
                                        newHostages2.get(h3).setState("Dead");
                                        hostagesThatDied++;
                                    }
                                    else if(newHostages2.get(h3).getState().equals("Alive")) {
                                    	newHostages2.get(h3).setState("Mutated");
                                    	hostagesThatDied++;
                                    }
                            }

                            if(canCarry(currentNode.currentState.allHostages)){
                            	Node tempNode = new Node(new State(x,y,c,currentNode.currentState.neoDamage,currentNode.currentState.kills,newHostages2, currentNode.currentState.allPills, currentNode.currentState.allAgents),Operators.carry,currentNode, currentNode.depth++,currentNode.cost+3+(hostagesThatDied*3000));	
                                mainQueue.add(tempNode);
                                hostageTaken = true;
                                nodesExpanded++;
                                break;
                            }/*else {
                            	Node tempNode = new Node(new State(x,y,c,currentNode.currentState.neoDamage,currentNode.currentState.kills,currentNode.currentState.allHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents),Oper,currentNode, currentNode.depth++);
                                mainQueue.add(tempNode);
                            }*/
                            //mainQueue.add(tempNode);
                            
                          
                            //shofo law alive
                        }
                    }
                }
            }
            if (grid[x][y].charAt(0) == 'T' ) { ////////////if there is any hostages to be dropped then add a node after every hostage is dropped, else dont add node
                boolean carriedHostages = false;
                    ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                    for (Hostage h : currentNode.currentState.allHostages) {
                        newHostages.add(h.clone());
                    }
                    for (int h = 0; h < newHostages.size(); h++) {
                        ///////////lazem n3mel condition law hostage mat fe edena
                        //// NOTE: DO CAPACITY LATER
                        if (newHostages.get(h).getState().equals("Carried")) {//get hostage
                            carriedHostages = true;
                            break;
                        }
                    }
                    if (carriedHostages) {
                        for (int h = 0; h < newHostages.size(); h++) {
                            if (newHostages.get(h).getState().equals("Carried")) {//get hostage
                                newHostages.get(h).setState("Saved");
                            }
                        }
                        int hostagesThatDied=0;	
                        ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                        for (Hostage h2 : newHostages) {
                            newHostages2.add(h2.clone());
                        }
                        for (int h3 = 0; h3 < newHostages2.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                            newHostages2.get(h3).setDamage(newHostages2.get(h3).getDamage() + 2);
                            if (newHostages2.get(h3).getDamage() >= 100)
                                if (newHostages2.get(h3).getState().equals("Carried")) {
                                    newHostages2.get(h3).setState("Dead");
                                    hostagesThatDied++;
                                }
                                else if (newHostages2.get(h3).getState().equals("Alive")) {
                                    newHostages2.get(h3).setState("Mutated");
                                    hostagesThatDied++;
                                }
                        }
                        Node tempNode = new Node(new State(x, y,c , currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages2, currentNode.currentState.allPills, currentNode.currentState.allAgents), Operators.drop, currentNode, currentNode.depth++,currentNode.cost+3+(hostagesThatDied*3000));
                        mainQueue.add(tempNode);
                        nodesExpanded++;
                    }



            }

            if(grid[x][y].charAt(0) == 'P' && grid[x][y].length()==1 )
            { /////DOES HE HAVE TO TAKE PILL IF HE IS ON IT?

                ArrayList<Pill> newPills = new ArrayList<Pill>();
                for (Pill p : currentNode.currentState.allPills){
                    newPills.add(p.clone());
                }
                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                for (Hostage h : currentNode.currentState.allHostages){
                    newHostages.add(h.clone());
                }

                for(int p=0; p<newPills.size(); p++)
                {
                    if (newPills.get(p).getX()==x &&newPills.get(p).getY()==y)
                    {
                        if(newPills.get(p).state.equals("NT"))
                        {
                            newPills.get(p).setState("T");
                            for(int h=0; h<newHostages.size();h++){

                                newHostages.get(h).setDamage(newHostages.get(h).getDamage()-20);
                                if(newHostages.get(h).getDamage()<0) {
                                	  newHostages.get(h).setDamage(0);
                                }
                            }
                                ////////CLONE THE OLD CLONED ONE AFTER SETTING DAMAGE
                           

                            Node tempNode = new Node(new State(x,y,c,(currentNode.currentState.neoDamage)-20,currentNode.currentState.kills,newHostages, newPills, currentNode.currentState.allAgents),Operators.takePill,currentNode, currentNode.depth++,currentNode.cost+3);
                            mainQueue.add(tempNode);
                            nodesExpanded++;
                            pillTAKE=true;


                            break;
                        }
                        else if(newPills.get(p).state.equals("T"))
                        {

                        }
                    }
                }
            }


            if(grid[x][y].charAt(0) == 'P' && grid[x][y].length()!=1 ){

                for (int j = 0; j < Pad.size(); j++) {
                    Pad pad = Pad.get(j);

                    if (pad.getStartX() == x && pad.getStartY() == y) {

                        String stateToBeHashed = makeHashedState(currentNode, 100, Operators.fly);
                        if(hashedStates.contains(stateToBeHashed)) {
                            break;
                        }
                        else {
                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                newHostages.add(h2.clone());
                            }
                            int hostagesThatDied=0;
                            for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                if (newHostages.get(h3).getDamage() >= 100)
                                    if (newHostages.get(h3).getState().equals("Carried")) {
                                        newHostages.get(h3).setState("Dead");
                                        hostagesThatDied++;}
                                    else if (newHostages.get(h3).getState().equals("Alive")) {
                                        newHostages.get(h3).setState("Mutated");
                                        hostagesThatDied++;
                                    }
                            }
                            if(!(currentNode.operator == Operators.fly)) {

                                Node tempNode = new Node(new State(pad.getEndX(), pad.getEndY(),c, currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), Operators.fly, currentNode, currentNode.depth++,currentNode.cost+3+hostagesThatDied*3000);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                                hashedStates.add(stateToBeHashed);

                                padFly=true;
                                break;
                            }
                            else{
                                break;
                            }
                        }
                    }
                }

            }


            for(int h=0; h<currentNode.currentState.allHostages.size(); h++){
                ///////////lazem n3mel condition law hostage mat fe edena
                if (currentNode.currentState.allHostages.get(h).state.equals("Alive")
                    ||currentNode.currentState.allHostages.get(h).state.equals("Carried") ||currentNode.currentState.allHostages.get(h).state.equals("Mutated") ) {
                             allHostagesSaved = false;

                    break;
                }
            }
            if(allHostagesSaved) {
                //out.println("Solution reached at depth" + currentNode.depth);
                //System.out.println(finalPrint(currentNode));
            }
        if(currentNode.currentState.neoDamage<100 && !hostageTaken ){

            if(!allHostagesSaved) { ///el hostages are not all saved yet

                /////////////////ELE T7T KOLO WAS JUST 2 LINES WITHOUT HASHING
                for(int i=0; i<4; i++) {   //this checks all neighbors by taking the array by up down right and left in this order


                    int col = x + dx[i];
                    int row = y + dy[i];

                    if (col < 0 || row < 0 || col>=m || row>=n) {

                    } else {


                        if (grid[col][row].charAt(0) == 'H' )  /// NOTE
                        { ////////////NOTE:MOVE TO THE NODE IF ITS NOT MUTATED, KILL IF MUTATED BY IF CONDITION THEN MOVE NEXT ITER


                                boolean hostageMutated = false;  ////////////tb3n brdo lazem n2tel ay hostage in the vicinity or agent if we do a kill action
                                for(int h=0; h<currentNode.currentState.allHostages.size(); h++) {
                                    ///////////lazem n3mel condition law hostage mat fe edena
                                    //// NOTE: DO CAPACITY LATER
                                    if (currentNode.currentState.allHostages.get(h).getX() == col && currentNode.currentState.allHostages.get(h).getY() == row) {//get hostage
                                        ////carry the dead hostage as well i think?
                                        if (currentNode.currentState.allHostages.get(h).getState().equals("Mutated")) {

                                            //KILL IF MUTATED
                                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                                newHostages.add(h2.clone());
                                            }
                                           ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                          
                                            int hostageKills = 0;
                                            int agentKills = 0;
                                            //newAgents.get(a).setState("Dead");
                                         
                                            newHostages.get(h).setState("Dead");
                                            hostageKills++;
                                            ArrayList<Agent> newAgents = new ArrayList<Agent>();
                                            for (Agent a : currentNode.currentState.allAgents){
                                                newAgents.add(a.clone());
                                            }

                                            int[] dx2 = { -1, 1, 0, 0 };
                                            int[] dy2= { 0, 0, 1, -1 };
                                            ///////////KILL ALL NEIGHBORING AGENTS IF THERE IS


                                            for(int k=0; k<4;k++) {
                                                        //KILLING ALL AGENTS IS DONE 5ALAS
                                                int col2 = x + dx2[k];
                                                int row2 = y + dy2[k];
                                                if (col2 < 0 || row2 < 0 || col2>=m || row2>=n) {

                                                } else {
                                                if (grid[col2][row2].charAt(0) == 'H') {
                                                    for (int a2 = 0; a2 < newHostages.size(); a2++) {
                                                        if (newHostages.get(a2).getX() == col2 && newHostages.get(a2).getY() == row2) {

                                                            if (newHostages.get(a2).state.equals("Mutated")) {
                                                            	newHostages.get(a2).setState("Dead");
                                                                hostageKills++;

                                                            }
                                                        }
                                                        }
                                                    }
                                                
                                                if (grid[col2][row2].charAt(0) == 'A') {
                                                    for (int a2 = 0; a2 < newAgents.size(); a2++) {
                                                        if (newAgents.get(a2).getX() == col2 && newAgents.get(a2).getY() == row2) {

                                                            if (newAgents.get(a2).state.equals("Alive")) {
                                                                newAgents.get(a2).setState("Dead");
                                                                agentKills++;

                                                            }
                                                        }
                                                        }
                                                    }
                                                
                                                
                                                }

                                            }		
                                            	
                                            ///INCUR DMG
                                            ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                                            for (Hostage h4 : newHostages) {
                                                newHostages2.add(h4.clone());
                                            }
                                            int hostagesThatDied=0;
                                            for(int h5=0; h5<newHostages2.size(); h5++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                                newHostages2.get(h5).setDamage(newHostages2.get(h5).getDamage()+2);
                                                if(newHostages2.get(h5).getDamage()>=100)
                                                    if(newHostages2.get(h5).getState().equals("Carried")) {
                                                        newHostages2.get(h5).setState("Dead");
                                                    hostagesThatDied++;
                                                    }
                                                    else if(newHostages2.get(h5).getState().equals("Alive")) {
                                                        newHostages2.get(h5).setState("Mutated");
                                                        hostagesThatDied++;
                                                        
                                                    }
                                            }
                                            Node tempNode = new Node(new State(x, y,c , currentNode.currentState.neoDamage+20, currentNode.currentState.kills+agentKills+hostageKills, newHostages2, currentNode.currentState.allPills, newAgents), Operators.kill, currentNode, currentNode.depth++,currentNode.cost+(3000 * agentKills)+(3*hostageKills) +(3000*hostagesThatDied));

                                            mainQueue.add(tempNode);

                                            nodesExpanded++;
                                            hostageMutated = true;
                                            break;

                                        }


                                    }
                                }
                                    /////////GO HERE IF HOSTAGE ISNT MUTATED
                                if(!hostageMutated)
                                {
                                    String stateToBeHashed = makeHashedState(currentNode, i,null);
                                    if(hashedStates.contains(stateToBeHashed)){
                                    }
                                    else {
                                        ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                        for (Hostage h2 : currentNode.currentState.allHostages) {
                                            newHostages.add(h2.clone());
                                        }
                                        int hostagesThatDied=0;
                                        for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                            newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                            if (newHostages.get(h3).getDamage() >= 100)
                                                if (newHostages.get(h3).getState().equals("Carried")) {
                                                    newHostages.get(h3).setState("Dead");
                                                     hostagesThatDied++;
                                                }
                                                else if (newHostages.get(h3).getState().equals("Alive")) {
                                                    newHostages.get(h3).setState("Mutated");
                                                    hostagesThatDied++;
                                                }
                                        }
                                        boolean isHeGoingToTurn = false;
                                        for(int h4 = 0; h4 < newHostages.size(); h4++) {
                                     	   if (newHostages.get(h4).getX() == col && newHostages.get(h4).getY() == row) //get hostage
                                               ////carry the dead hostage as well i think?
                                                if (newHostages.get(h4).getDamage()>=98 && newHostages.get(h4).getState().equals("Alive"))
                                             	   isHeGoingToTurn = true;
                                        }
                                        if(!isHeGoingToTurn) {

	                                        Node tempNode = new Node(new State(col, row, c, currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,currentNode.cost+3+hostagesThatDied*3000);
	                                        hashedStates.add(stateToBeHashed);
	                                        mainQueue.add(tempNode);
	                                        nodesExpanded++;
                                        }
                                    }
                                }
                            }





                        else if(grid[col][row].charAt(0) == 'T')
                        {
                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){

                            }

                            else {  ///////////////hostages might have to be dropped in the next iteration
                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                int hostagesThatDied=0;
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried")) {
                                            newHostages.get(h3).setState("Dead");
                                             hostagesThatDied++;
                                        }
                                        else if(newHostages.get(h3).getState().equals("Alive")) {
                                            newHostages.get(h3).setState("Mutated");
                                             hostagesThatDied++;
                                        }
                                }



                                Node tempNode = new Node(new State(col, row, c, currentNode.currentState.neoDamage, currentNode.currentState.kills,newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,currentNode.cost+3 +(hostagesThatDied*3000));
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }

                        else if(grid[col][row].charAt(0) == 'A' ){  ///////////////////NOTE NOTE NOTELAW FEE AGENT GNBY KAMAN GHEER EL NEIGHBOR DA THEN KILL THEM ALL
                            ArrayList<Agent> newAgents = new ArrayList<Agent>();
                            for (Agent a : currentNode.currentState.allAgents){
                                newAgents.add(a.clone());
                            }

                            for(int a=0; a<newAgents.size(); a++)
                            {

                                if (newAgents.get(a).getX()==col &&newAgents.get(a).getY()==row)
                                {

                                    if(newAgents.get(a).state.equals("Alive"))
                                    {

                                        ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                        for (Hostage h2 : currentNode.currentState.allHostages) {
                                            newHostages.add(h2.clone());
                                        }
                                       
                                        int agentKills = 0;
                                        int hostageKills = 0;
                                        newAgents.get(a).setState("Dead");
                                        agentKills++;
                                        int[] dx2 = { -1, 1, 0, 0 };
                                        int[] dy2= { 0, 0, 1, -1 };
                                        ///////////KILL ALL NEIGHBORING AGENTS IF THERE IS


                                        for(int k=0; k<4;k++) {
                                                    //KILLING ALL AGENTS IS DONE 5ALAS
                                            int col2 = x + dx2[k];
                                            int row2 = y + dy2[k];
                                            if (col2 < 0 || row2 < 0 || col2>=m || row2>=n) {

                                            } else {
                                            if (grid[col2][row2].charAt(0) == 'A') {
                                                for (int a2 = 0; a2 < newAgents.size(); a2++) {
                                                    if (newAgents.get(a2).getX() == col2 && newAgents.get(a2).getY() == row2) {

                                                        if (newAgents.get(a2).state.equals("Alive")) {
                                                            newAgents.get(a2).setState("Dead");
                                                            agentKills++;

                                                        }
                                                    }
                                                    }
                                                }
                                            if (grid[col2][row2].charAt(0) == 'H') {
                                                for (int a2 = 0; a2 < newHostages.size(); a2++) {
                                                    if (newHostages.get(a2).getX() == col2 && newHostages.get(a2).getY() == row2) {

                                                        if (newHostages.get(a2).state.equals("Mutated")) {
                                                        	newHostages.get(a2).setState("Dead");
                                                            hostageKills++;

                                                        }
                                                    }
                                                    }
                                        }
                                            }

                                        }
                                        int hostagesThatDied=0;
                                        for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                            newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                            if(newHostages.get(h3).getDamage()>=100)
                                                if(newHostages.get(h3).getState().equals("Carried")) {
                                                    newHostages.get(h3).setState("Dead");
                                                    hostagesThatDied++;
                                                }
                                                else if(newHostages.get(h3).getState().equals("Alive")) {
                                                    newHostages.get(h3).setState("Mutated");
                                                    hostagesThatDied++;
                                                }
                                        }
                                        Node tempNode = new Node(new State(x,y,c,(currentNode.currentState.neoDamage)+20,currentNode.currentState.kills+agentKills+hostageKills,newHostages, currentNode.currentState.allPills, newAgents),Operators.kill,currentNode, currentNode.depth++,currentNode.cost+(3000*agentKills) +(3*hostageKills) +3000*hostagesThatDied);

                                        mainQueue.add(tempNode);
                                        nodesExpanded++;
                                        break;
                                    }
                                    else if(newAgents.get(a).state.equals("Dead"))
                                    {
                                        String stateToBeHashed = makeHashedState(currentNode, i,null);
                                        if(hashedStates.contains(stateToBeHashed)){
                                        }
                                        else {
                                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                                newHostages.add(h2.clone());
                                            }
                                            int hostagesThatDied=0;
                                            for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                               
                                                newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                                if (newHostages.get(h3).getDamage() >= 100)
                                                    if (newHostages.get(h3).getState().equals("Carried")) {
                                                        newHostages.get(h3).setState("Dead");
                                                		 hostagesThatDied++;}
                                                    else if (newHostages.get(h3).getState().equals("Alive")) {
                                                        newHostages.get(h3).setState("Mutated");
                                                		 hostagesThatDied++;}

                                            }

                                            Node tempNode = new Node(new State(col, row,c, (currentNode.currentState.neoDamage), currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, newAgents), operationFinder(i), currentNode, currentNode.depth++,currentNode.cost+3+hostagesThatDied*3000);
                                            mainQueue.add(tempNode);
                                            hashedStates.add(stateToBeHashed);
                                            nodesExpanded++;
                                        }
                                    }
                                }
                            }
                        }
                        else if (grid[col][row].charAt(0) == 'P')
                        {


                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){
                            }
                            else {

                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                int hostagesThatDied=0;
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried")) {
                                            newHostages.get(h3).setState("Dead");
                                    	 hostagesThatDied++;}
                                        else if(newHostages.get(h3).getState().equals("Alive")) {
                                            newHostages.get(h3).setState("Mutated");
                                    		 hostagesThatDied++;}

                                }

                                Node tempNode = new Node(new State(col, row,c, (currentNode.currentState.neoDamage),currentNode.currentState.kills, newHostages,currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,currentNode.cost+3+hostagesThatDied*3000);
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }
                        else if(grid[col][row].charAt(0) == '-'   ){ //GO TO PILL / EMPTY /PAD
                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){
                                }
                            else {

                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                int hostagesThatDied=0;
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried")) {
                                            newHostages.get(h3).setState("Dead");
                                            hostagesThatDied++;}
                                        else if(newHostages.get(h3).getState().equals("Alive")) {
                                            newHostages.get(h3).setState("Mutated");
                                            hostagesThatDied++;
                                        }
                                }

                                Node tempNode = new Node(new State(col, row, c, (currentNode.currentState.neoDamage),currentNode.currentState.kills, newHostages,currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,currentNode.cost+3+3000*hostagesThatDied);
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }

                    }


                }

            }
            else {
            	//System.out.println(c);
                int deadHostages = 0;
                for (int h = 0; h < currentNode.currentState.allHostages.size(); h++) {
                    if (currentNode.currentState.allHostages.get(h).getState().equals("Dead"))
                         deadHostages++;

                }
                if(currentNode.currentState.neoX == telephone.x &&currentNode.currentState.neoY == telephone.y) {
                    System.out.println("Solution reached at depth" + currentNode.depth);
                    
                    String output = (finalPrint(currentNode) + ";" +deadHostages  + ";" +  currentNode.currentState.kills+ ";" + nodesExpanded);
                    System.out.println(output);
                    return output;
                }


            }

        }

        }
        
        return "No Solution";
    } 
    
    
    public static String BreadthFS(String[][] grid) { // INSHALLAH 100% DONE!
        int[] dx = { -1, 1, 0, 0 };
        int[] dy = { 0, 0, 1, -1 };
        int nodesExpanded = 0;
        HashSet<String> hashedStates = new HashSet<String>();

        hostNo = hostages.size();
        HashSet<State> set=new HashSet<State>();
        Queue<Node> mainQueue = new LinkedList<Node>();
        State startState = new State(neo.getX(), neo.getY(),c,0,0, hostages ,Pill, agents);
        //////////////lazem ncheck anhy hostage eta5ed since 2 neos could have saved 2 different hostages and be in the same state
        /// which could be done by comparing hostage X and Y


        Node start = new Node(startState, null, null , 0, 0);

        mainQueue.add(start);



        while(!mainQueue.isEmpty()){
            boolean pillTAKE = false;
            boolean padFly = false;
            boolean allHostagesSaved=true;
            boolean hostageTaken = false;
            Node currentNode = mainQueue.peek(); //el node ele hn3mlha dequeue from the front
            int x = currentNode.currentState.neoX; //tb3n lazem nghyha l private w n3melha getMETHOD bas lateron
            int y =currentNode.currentState.neoY;
            mainQueue.remove();

            //all the checks if we are on H, pad, TB



            if (grid[x][y].charAt(0) == 'H' ) { ////////////you move first to that node and then drop/carry hostage
                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                for (Hostage h : currentNode.currentState.allHostages){
                    newHostages.add(h.clone());
                }
                for(int h=0; h<newHostages.size(); h++){
                    if (newHostages.get(h).getX()==x &&newHostages.get(h).getY()==y) {
                        if (newHostages.get(h).getState().equals("Alive")) {
                            newHostages.get(h).setState("Carried");
                            ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                            for (Hostage h2 : newHostages) {
                                newHostages2.add(h2.clone());
                            }
                            for(int h3=0; h3<newHostages2.size(); h3++){
                                newHostages2.get(h3).setDamage(newHostages2.get(h3).getDamage()+2);
                                if(newHostages2.get(h3).getDamage()>=100  )

                                    if(newHostages2.get(h3).getState().equals("Carried"))
                                        newHostages2.get(h3).setState("Dead");
                                    else if(newHostages2.get(h3).getState().equals("Alive"))
                                    newHostages2.get(h3).setState("Mutated");

                            }

                            if(canCarry(currentNode.currentState.allHostages)){
                            	Node tempNode = new Node(new State(x,y,c,currentNode.currentState.neoDamage,currentNode.currentState.kills,newHostages2, currentNode.currentState.allPills, currentNode.currentState.allAgents),Operators.carry,currentNode, currentNode.depth++,0);	
                                mainQueue.add(tempNode);
                                hostageTaken = true;
                            }/*else {
                            	Node tempNode = new Node(new State(x,y,c,currentNode.currentState.neoDamage,currentNode.currentState.kills,currentNode.currentState.allHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents),Oper,currentNode, currentNode.depth++);
                                mainQueue.add(tempNode);
                            }*/
                            //mainQueue.add(tempNode);
                            nodesExpanded++;
                            break;
                            //shofo law alive
                        }

                    }
                }

            }
            if (grid[x][y].charAt(0) == 'T' ) { ////////////if there is any hostages to be dropped then add a node after every hostage is dropped, else dont add node
                boolean carriedHostages = false;
                    ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                    for (Hostage h : currentNode.currentState.allHostages) {

                        newHostages.add(h.clone());
                    }
                    for (int h = 0; h < newHostages.size(); h++) {
                        ///////////lazem n3mel condition law hostage mat fe edena
                        //// NOTE: DO CAPACITY LATER
                        if (newHostages.get(h).getState().equals("Carried")) {//get hostage
                            carriedHostages = true;
                            break;
                        }
                    }
                    if (carriedHostages) {
                        for (int h = 0; h < newHostages.size(); h++) {
                            if (newHostages.get(h).getState().equals("Carried")) {//get hostage
                                newHostages.get(h).setState("Saved");


                            }

                        }

                        ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                        for (Hostage h2 : newHostages) {
                            newHostages2.add(h2.clone());
                        }
                        for (int h3 = 0; h3 < newHostages2.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                            newHostages2.get(h3).setDamage(newHostages2.get(h3).getDamage() + 2);
                            if (newHostages2.get(h3).getDamage() >= 100)
                                if (newHostages2.get(h3).getState().equals("Carried"))
                                    newHostages2.get(h3).setState("Dead");
                                else if (newHostages2.get(h3).getState().equals("Alive"))
                                    newHostages2.get(h3).setState("Mutated");

                        }
                        Node tempNode = new Node(new State(x, y,c , currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages2, currentNode.currentState.allPills, currentNode.currentState.allAgents), Operators.drop, currentNode, currentNode.depth++,0);
                        mainQueue.add(tempNode);
                        nodesExpanded++;
                    }



            }

            if(grid[x][y].charAt(0) == 'P' && grid[x][y].length()==1 )
            { /////DOES HE HAVE TO TAKE PILL IF HE IS ON IT?

                ArrayList<Pill> newPills = new ArrayList<Pill>();
                for (Pill p : currentNode.currentState.allPills){
                    newPills.add(p.clone());
                }
                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                for (Hostage h : currentNode.currentState.allHostages){
                    newHostages.add(h.clone());
                }

                for(int p=0; p<newPills.size(); p++)
                {

                    if (newPills.get(p).getX()==x &&newPills.get(p).getY()==y)
                    {


                        if(newPills.get(p).state.equals("NT"))
                        {
                            newPills.get(p).setState("T");
                            for(int h=0; h<newHostages.size();h++){

                                newHostages.get(h).setDamage(newHostages.get(h).getDamage()-20);

                            }
                            Node tempNode = new Node(new State(x,y,c,(currentNode.currentState.neoDamage)-20,currentNode.currentState.kills,newHostages, newPills, currentNode.currentState.allAgents),Operators.takePill,currentNode, currentNode.depth++,0);
                            mainQueue.add(tempNode);
                            nodesExpanded++;
                            pillTAKE=true;


                            break;
                        }
                        else if(newPills.get(p).state.equals("T"))
                        {

                        }
                    }
                }
            }


            if(grid[x][y].charAt(0) == 'P' && grid[x][y].length()!=1 ){

                for (int j = 0; j < Pad.size(); j++) {
                    Pad pad = Pad.get(j);

                    if (pad.getStartX() == x && pad.getStartY() == y) {

                        String stateToBeHashed = makeHashedState(currentNode, 100, Operators.fly);
                        if(hashedStates.contains(stateToBeHashed)) {
                            break;
                        }
                        else {
                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                newHostages.add(h2.clone());
                            }
                            for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                if (newHostages.get(h3).getDamage() >= 100)
                                    if (newHostages.get(h3).getState().equals("Carried"))
                                        newHostages.get(h3).setState("Dead");
                                    else if (newHostages.get(h3).getState().equals("Alive"))
                                        newHostages.get(h3).setState("Mutated");

                            }
                            if(!(currentNode.operator == Operators.fly)) {

                                Node tempNode = new Node(new State(pad.getEndX(), pad.getEndY(),c, currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), Operators.fly, currentNode, currentNode.depth++,0);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                                hashedStates.add(stateToBeHashed);

                                padFly=true;
                                break;
                            }
                            else{
                                break;
                            }
                        }
                    }
                }

            }


            for(int h=0; h<currentNode.currentState.allHostages.size(); h++){
                ///////////lazem n3mel condition law hostage mat fe edena
                if (currentNode.currentState.allHostages.get(h).state.equals("Alive")
                    ||currentNode.currentState.allHostages.get(h).state.equals("Carried") ||currentNode.currentState.allHostages.get(h).state.equals("Mutated") ) {
                             allHostagesSaved = false;

                    break;
                }
            }
            if(allHostagesSaved) {
                //out.println("Solution reached at depth" + currentNode.depth);
                //System.out.println(finalPrint(currentNode));
            }
        if(currentNode.currentState.neoDamage<100 && !hostageTaken ){

            if(!allHostagesSaved) { ///el hostages are not all saved yet

                /////////////////ELE T7T KOLO WAS JUST 2 LINES WITHOUT HASHING
                for(int i=0; i<4; i++) {   //this checks all neighbors by taking the array by up down right and left in this order



                    int col = x + dx[i];
                    int row = y + dy[i];

                    if (col < 0 || row < 0 || col>=m || row>=n) {

                    } else {


                    	if (grid[col][row].charAt(0) == 'H' )  /// NOTE
                        { ////////////NOTE:MOVE TO THE NODE IF ITS NOT MUTATED, KILL IF MUTATED BY IF CONDITION THEN MOVE NEXT ITER


                                boolean hostageMutated = false;  ////////////tb3n brdo lazem n2tel ay hostage in the vicinity or agent if we do a kill action
                                for(int h=0; h<currentNode.currentState.allHostages.size(); h++) {
                                    ///////////lazem n3mel condition law hostage mat fe edena
                                    //// NOTE: DO CAPACITY LATER
                                    if (currentNode.currentState.allHostages.get(h).getX() == col && currentNode.currentState.allHostages.get(h).getY() == row) {//get hostage
                                        ////carry the dead hostage as well i think?
                                        if (currentNode.currentState.allHostages.get(h).getState().equals("Mutated")) {

                                            //KILL IF MUTATED
                                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                                newHostages.add(h2.clone());
                                            }
                                           ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                            newHostages.get(h).setState("Dead");

                                            int kills = 0;
                                            //newAgents.get(a).setState("Dead");
                                            kills++;
                                            ArrayList<Agent> newAgents = new ArrayList<Agent>();
                                            for (Agent a : currentNode.currentState.allAgents){
                                                newAgents.add(a.clone());
                                            }

                                            int[] dx2 = { -1, 1, 0, 0 };
                                            int[] dy2= { 0, 0, 1, -1 };
                                            ///////////KILL ALL NEIGHBORING AGENTS IF THERE IS


                                            for(int k=0; k<4;k++) {
                                                        //KILLING ALL AGENTS IS DONE 5ALAS
                                                int col2 = x + dx2[k];
                                                int row2 = y + dy2[k];
                                                if (col2 < 0 || row2 < 0 || col2>=m || row2>=n) {

                                                } else {
                                                if (grid[col2][row2].charAt(0) == 'H') {
                                                    for (int a2 = 0; a2 < newHostages.size(); a2++) {
                                                        if (newHostages.get(a2).getX() == col2 && newHostages.get(a2).getY() == row2) {

                                                            if (newHostages.get(a2).state.equals("Mutated")) {
                                                            	newHostages.get(a2).setState("Dead");
                                                                kills++;

                                                            }
                                                        }
                                                        }
                                                    }
                                                
                                                if (grid[col2][row2].charAt(0) == 'A') {
                                                    for (int a2 = 0; a2 < newAgents.size(); a2++) {
                                                        if (newAgents.get(a2).getX() == col2 && newAgents.get(a2).getY() == row2) {

                                                            if (newAgents.get(a2).state.equals("Alive")) {
                                                                newAgents.get(a2).setState("Dead");
                                                                kills++;

                                                            }
                                                        }
                                                        }
                                                    }
                                                
                                                }

                                            }		
                                            	


                                            ///INCUR DMG
                                            ArrayList<Hostage> newHostages2 = new ArrayList<Hostage>();
                                            for (Hostage h4 : newHostages) {
                                                newHostages2.add(h4.clone());
                                            }
                                            for(int h5=0; h5<newHostages2.size(); h5++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                                newHostages2.get(h5).setDamage(newHostages2.get(h5).getDamage()+2);
                                                if(newHostages2.get(h5).getDamage()>=100)
                                                    if(newHostages2.get(h5).getState().equals("Carried"))
                                                        newHostages2.get(h5).setState("Dead");
                                                    else if(newHostages2.get(h5).getState().equals("Alive"))
                                                        newHostages2.get(h5).setState("Mutated");

                                            }
                                            Node tempNode = new Node(new State(x, y,c , currentNode.currentState.neoDamage+20, currentNode.currentState.kills+kills, newHostages2, currentNode.currentState.allPills, newAgents), Operators.kill, currentNode, currentNode.depth++,0);
                                            					
                                            mainQueue.add(tempNode);

                                            nodesExpanded++;
                                            hostageMutated = true;
                                            break;

                                        }


                                    }
                                }
                                    /////////GO HERE IF HOSTAGE ISNT MUTATED
                                if(!hostageMutated)
                                {
                                    String stateToBeHashed = makeHashedState(currentNode, i,null);
                                    if(hashedStates.contains(stateToBeHashed)){
                                    }
                                    else {
                                        ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                        for (Hostage h2 : currentNode.currentState.allHostages) {
                                            newHostages.add(h2.clone());
                                        }
                                        for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                            newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                            if (newHostages.get(h3).getDamage() >= 100)
                                                if (newHostages.get(h3).getState().equals("Carried"))
                                                    newHostages.get(h3).setState("Dead");
                                                else if (newHostages.get(h3).getState().equals("Alive"))
                                                    newHostages.get(h3).setState("Mutated");

                                        }
                                        boolean isHeGoingToTurn = false;
                                        for(int h4 = 0; h4 < newHostages.size(); h4++) {
                                     	   if (newHostages.get(h4).getX() == col && newHostages.get(h4).getY() == row) //get hostage
                                               ////carry the dead hostage as well i think?
                                                if (newHostages.get(h4).getDamage()>=98 && newHostages.get(h4).getState().equals("Alive"))
                                             	   isHeGoingToTurn = true;
                                        }
                                        if(!isHeGoingToTurn) {

                                        Node tempNode = new Node(new State(col, row, c, currentNode.currentState.neoDamage, currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                        hashedStates.add(stateToBeHashed);
                                        mainQueue.add(tempNode);
                                        nodesExpanded++;
                                        }
                                    }
                                }
                            }
                        else if(grid[col][row].charAt(0) == 'T')
                        {
                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){

                            }

                            else {  ///////////////hostages might have to be dropped in the next iteration
                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried"))
                                            newHostages.get(h3).setState("Dead");
                                        else if(newHostages.get(h3).getState().equals("Alive"))
                                            newHostages.get(h3).setState("Mutated");

                                }

                                Node tempNode = new Node(new State(col, row, c, currentNode.currentState.neoDamage, currentNode.currentState.kills,newHostages, currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }

                        else if(grid[col][row].charAt(0) == 'A' ){


                        	ArrayList<Agent> newAgents = new ArrayList<Agent>();
                            for (Agent a : currentNode.currentState.allAgents){
                                newAgents.add(a.clone());
                            }

                            for(int a=0; a<newAgents.size(); a++)
                            {

                                if (newAgents.get(a).getX()==col &&newAgents.get(a).getY()==row)
                                {

                                    if(newAgents.get(a).state.equals("Alive"))
                                    {

                                        ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                        for (Hostage h2 : currentNode.currentState.allHostages) {
                                            newHostages.add(h2.clone());
                                        }
                              
                                        int kills = 0;
                                        newAgents.get(a).setState("Dead");
                                        kills++;
                                        int[] dx2 = { -1, 1, 0, 0 };
                                        int[] dy2= { 0, 0, 1, -1 };
                                        ///////////KILL ALL NEIGHBORING AGENTS IF THERE IS


                                        for(int k=0; k<4;k++) {
                                                    //KILLING ALL AGENTS IS DONE 5ALAS
                                            int col2 = x + dx2[k];
                                            int row2 = y + dy2[k];
                                            if (col2 < 0 || row2 < 0 || col2>=m || row2>=n) {

                                            } else {
	                                            if (grid[col2][row2].charAt(0) == 'A') {
	                                            	
	                                                for (int a2 = 0; a2 < newAgents.size(); a2++) {
	                                                    if (newAgents.get(a2).getX() == col2 && newAgents.get(a2).getY() == row2) {
	
	                                                        if (newAgents.get(a2).state.equals("Alive")) {
	                                                            newAgents.get(a2).setState("Dead");
	                                                            kills++;
	
	                                                        }
	                                                    }
	                                                }
	                                            }
	                                            if (grid[col2][row2].charAt(0) == 'H') {
                                                    for (int a2 = 0; a2 < newHostages.size(); a2++) {
                                                        if (newHostages.get(a2).getX() == col2 && newHostages.get(a2).getY() == row2) {

                                                            if (newHostages.get(a2).state.equals("Mutated")) {
                                                            	newHostages.get(a2).setState("Dead");
                                                                kills++;

                                                            }
                                                        }
                                                        }
                                            }

                                        }
                                        }
                                        for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                            newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                            if(newHostages.get(h3).getDamage()>=100)
                                                if(newHostages.get(h3).getState().equals("Carried"))
                                                    newHostages.get(h3).setState("Dead");
                                                else if(newHostages.get(h3).getState().equals("Alive"))
                                                    newHostages.get(h3).setState("Mutated");

                                        }
                                        Node tempNode = new Node(new State(x,y,c,(currentNode.currentState.neoDamage)+20,currentNode.currentState.kills+kills,newHostages, currentNode.currentState.allPills, newAgents),Operators.kill,currentNode, currentNode.depth++,0);

                                        mainQueue.add(tempNode);
                                        nodesExpanded++;
                                        break;
                                    
                                    }else if(newAgents.get(a).state.equals("Dead")){
                                    	
                                        String stateToBeHashed = makeHashedState(currentNode, i,null);
                                        if(hashedStates.contains(stateToBeHashed)){
                                        }
                                        else {
                                            ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                            for (Hostage h2 : currentNode.currentState.allHostages) {
                                                newHostages.add(h2.clone());
                                            }
                                            for (int h3 = 0; h3 < newHostages.size(); h3++) { ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                                newHostages.get(h3).setDamage(newHostages.get(h3).getDamage() + 2);
                                                if (newHostages.get(h3).getDamage() >= 100)
                                                    if (newHostages.get(h3).getState().equals("Carried"))
                                                        newHostages.get(h3).setState("Dead");
                                                    else if (newHostages.get(h3).getState().equals("Alive"))
                                                        newHostages.get(h3).setState("Mutated");

                                            }

                                            Node tempNode = new Node(new State(col, row,c, (currentNode.currentState.neoDamage), currentNode.currentState.kills, newHostages, currentNode.currentState.allPills, newAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                            mainQueue.add(tempNode);
                                            hashedStates.add(stateToBeHashed);
                                            nodesExpanded++;
                                        }
                                    }
                                }
                            }
                        }
                        else if (grid[col][row].charAt(0) == 'P')
                        {


                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){
                            }
                            else {

                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried"))
                                            newHostages.get(h3).setState("Dead");
                                        else if(newHostages.get(h3).getState().equals("Alive"))
                                            newHostages.get(h3).setState("Mutated");

                                }

                                Node tempNode = new Node(new State(col, row,c, (currentNode.currentState.neoDamage),currentNode.currentState.kills, newHostages,currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }
                        else if(grid[col][row].charAt(0) == '-'   ){ //GO TO PILL / EMPTY /PAD
                            String stateToBeHashed = makeHashedState(currentNode, i,null);
                            if(hashedStates.contains(stateToBeHashed)){
                                }
                            else {

                                ArrayList<Hostage> newHostages = new ArrayList<Hostage>();
                                for (Hostage h2 : currentNode.currentState.allHostages) {
                                    newHostages.add(h2.clone());
                                }
                                for(int h3=0; h3<newHostages.size(); h3++){ ////INCUR DMG? BEFORE ADDDING A STEP? AND ALSO KILL IF ABOVE/EQUAL 100
                                    newHostages.get(h3).setDamage(newHostages.get(h3).getDamage()+2);
                                    if(newHostages.get(h3).getDamage()>=100)
                                        if(newHostages.get(h3).getState().equals("Carried"))
                                            newHostages.get(h3).setState("Dead");
                                        else if(newHostages.get(h3).getState().equals("Alive"))
                                            newHostages.get(h3).setState("Mutated");

                                }

                                Node tempNode = new Node(new State(col, row, c, (currentNode.currentState.neoDamage),currentNode.currentState.kills, newHostages,currentNode.currentState.allPills, currentNode.currentState.allAgents), operationFinder(i), currentNode, currentNode.depth++,0);
                                hashedStates.add(stateToBeHashed);
                                mainQueue.add(tempNode);
                                nodesExpanded++;
                            }
                        }

                    }
                }

            }
            else {
            	//System.out.println(c);
                int deadHostages = 0;
                for (int h = 0; h < currentNode.currentState.allHostages.size(); h++) {
                    if (currentNode.currentState.allHostages.get(h).getState().equals("Dead"))
                         deadHostages++;

                }
                if(currentNode.currentState.neoX == telephone.x &&currentNode.currentState.neoY == telephone.y) {
                	
                    System.out.println("Solution reached at depth" + currentNode.depth);
                    String output = (finalPrint(currentNode) + ";" +deadHostages  + ";" +  currentNode.currentState.kills+ ";" + nodesExpanded);
                    System.out.println(output);
                    return output;
                }
            }
        	}
     }

    return "No Solution";
    }


    public static String makeHashedState(Node node, int direction, Operators e)
    {
        String stateToBeHashed ="";

        for(int h=0; h<node.currentState.allHostages.size(); h++){
            stateToBeHashed += node.currentState.allHostages.get(h).getState() +",";
        }

        for(int h=0; h<node.currentState.allPills.size(); h++){
            stateToBeHashed +=  node.currentState.allPills.get(h).getState() +",";
        }
        for(int h=0; h<node.currentState.allAgents.size(); h++){
            stateToBeHashed += node.currentState.allAgents.get(h).getState() +",";
        }

        if(e== null)
            stateToBeHashed += operationFinder(direction) +", ";
        else
            stateToBeHashed += e +", ";
        stateToBeHashed += node.currentState.neoX + "," +node.currentState.neoY+"," ;
        stateToBeHashed += node.currentState.neoDamage;

        return stateToBeHashed;
    }
    
    
    public static String finalPrint(Node node) {
        String finalOutput = "";
        while(node.parent!=null)
        {
            if(finalOutput.isEmpty())
                finalOutput=""+ node.operator;
            else
            finalOutput =  node.operator+","+finalOutput;
                node = node.parent;
        }

            return finalOutput;
//      if(node.parent== null) {
//
//
//            return "";
//        }
//        //==else {
//        return(finalPrint(node.parent) +""+node.operator+",");
//        //+" and "+node.getParent().getState().getX()+" and y "+node.getParent().getState().getX()+
//        //}
    }


    public static String solve(String grid, String strategy, boolean visualization) {

        mainGrid = grid;
        
        String plan="";
        switch (strategy) {
            case "BF":
                plan=BreadthFS(genGrid());
                break;
                
            case "DF":
            	plan=DepthFS(genGrid());
            	break;
            	
            case "UC":
            	plan=CostS(genGrid());
            	break;
            	
            case "ID":
            	plan=IterativeDPS(genGrid());
            	break;
            	
            case "GR1":
            	plan=Greedy1(genGrid());
            	break;
            	
            case "GR2":
            	plan=Greedy2(genGrid());
            	break;
            
            case "AS1":
            	plan=AStar1(genGrid());
            	break;
            	
            case "AS2":
            	plan=AStar2(genGrid());
            	break;
            	
        }
        if(visualization)
        	visualize(grid, plan);
        return plan;
    }

    
    public static void visualize(String grid, String plan) {
 
    	System.out.println("\n");
         String[] parts = grid.split(";");

         /*
          * for(int i=0; i<parts.length; i++) { System.out.println(parts[i]); }
          */

         String MN = parts[0];
         m=Integer.parseInt(MN.split(",")[0]);
         n=Integer.parseInt(MN.split(",")[1]);
         String C = parts[1];
         
         c = Integer.parseInt(C);

         // Neo
         String NeoXY = parts[2];
         String[] NeoArray = NeoXY.split(",");
         neo = new Neo(Integer.parseInt(NeoArray[0]), Integer.parseInt(NeoArray[1]), Integer.parseInt(C), 0, new ArrayList<Hostage>(),new ArrayList<Hostage>(), null, null, null);
         // end Neo

         // telephone
         String TelephoneXY = parts[3];
         String[] TelephoneXYArray = TelephoneXY.split(",");
         telephone = new TelephoneBooth(Integer.parseInt(TelephoneXYArray[0]), Integer.parseInt(TelephoneXYArray[1]));
         // end telephone

         // Agent
         String AgentXY = parts[4];
         String[] AgentArray = AgentXY.split(",");
         agents = new ArrayList();
         for (int i = 0; i < AgentArray.length; i = i + 2) {
             Agent agent = new Agent(Integer.parseInt(AgentArray[i]), Integer.parseInt(AgentArray[i + 1]));
             agents.add(agent);
         }
         // end Agent

         // Pill
         String PillXY = parts[5];
         String[] PillArray = PillXY.split(",");
         Pill = new ArrayList();
         for (int i = 0; i < PillArray.length; i = i + 2) {
             Pill pill = new Pill(Integer.parseInt(PillArray[i]), Integer.parseInt(PillArray[i + 1]));
             Pill.add(pill);
         }
         // end Pill

         // Pad
         String PadXY = parts[6];
         String[] PadArray = PadXY.split(",");
         Pad = new ArrayList();
         for (int i = 0; i < PadArray.length; i = i + 4) {
             Pad pad = new Pad(Integer.parseInt(PadArray[i]), Integer.parseInt(PadArray[i + 1]),
                     Integer.parseInt(PadArray[i + 2]), Integer.parseInt(PadArray[i + 3]));
             Pad.add(pad);
         }
         // end Pad

         // hostages
         String HostageXY = parts[7];
         String[] hostagesArray = HostageXY.split(",");
         hostages = new ArrayList();
         hostageCells = new Cell[hostagesArray.length];

         for (int i = 0; i < hostagesArray.length; i = i + 3) {
            Hostage hostage = new Hostage(Integer.parseInt(hostagesArray[i]), Integer.parseInt(hostagesArray[i + 1]),
                     Integer.parseInt(hostagesArray[i + 2]));

             hostageCells[i] = new Cell(hostage.getX(), hostage.getY());
             hostages.add(hostage);
         }
         // end hostages


         gridArray = new String[m][n];

         for (int i = 0; i < agents.size(); i++) {
             gridArray[agents.get(i).getX()][agents.get(i).getY()] = "A";
         }
         for (int i = 0; i < Pill.size(); i++) {
             gridArray[Pill.get(i).getX()][Pill.get(i).getY()] = "P";
         }
         for (int i = 0; i < hostages.size(); i++) {
             gridArray[hostages.get(i).getX()][hostages.get(i).getY()] = "H(" + hostages.get(i).getDamage() + ")";
         }
         for (int i = 0; i < Pad.size(); i++) {
             gridArray[Pad.get(i).getStartX()][Pad.get(i).getStartY()] = "Pad(" + Pad.get(i).getEndX() + ","
                     + Pad.get(i).getEndY() + ")";
         }

         gridArray[neo.getX()][neo.getY()] = "Neo";
         gridArray[Integer.parseInt(TelephoneXYArray[0])][Integer.parseInt(TelephoneXYArray[1])] = "TB";
         for (int i = 0; i < gridArray.length; i++) {
             for (int j = 0; j < gridArray[i].length; j++) {
                 if (gridArray[i][j] == null) {
                     gridArray[i][j] = "-";
                 }
             }
         }

         printMap(gridArray);

     	String[] step = plan.split(",");
     	
    	for(int i=0; i<step.length; i++) {
    		

    		//left,fly,right,carry,left,fly,left,kill,left,left,carry,down,down,kill,right,right,up,right,right,drop;2;3;219136
  			for(int j=0; j<Pad.size(); j++) {
  				gridArray[Pad.get(j).getStartX()][Pad.get(j).getStartY()] = "Pad("+Pad.get(j).getEndX()+","+Pad.get(j).getEndY()+")";
  				gridArray[Pad.get(j).getEndX()][Pad.get(j).getEndY()] = "Pad("+Pad.get(j).getStartX()+","+Pad.get(j).getStartY()+")";
			}
    		
    		//System.out.println(step[i]);
    		if(step[i].equals("left")) {
    			
    		//	neo.setX(neo.getX()+0);
    			gridArray[neo.getX()][neo.getY()] = "-";
    			neo.setY(neo.getY()-1);
    			gridArray[neo.getX()][neo.getY()] = "Neo";

    			
    		}else if(step[i].equals("right")){

    			gridArray[neo.getX()][neo.getY()] = "-";
    			neo.setY(neo.getY()+1);
    			gridArray[neo.getX()][neo.getY()] = "Neo";
    			
    		}else if(step[i].equals("up")){

    			gridArray[neo.getX()][neo.getY()] = "-";
    			neo.setX(neo.getX()-1);
    			gridArray[neo.getX()][neo.getY()] = "Neo";
    			
    		}else if(step[i].equals("down")){

    			gridArray[neo.getX()][neo.getY()] = "-";
    			neo.setX(neo.getX()+1);
    			gridArray[neo.getX()][neo.getY()] = "Neo";
    			
    		}else if(step[i].equals("takePill")){

    			//gridArray[neo.getX()][neo.getY()] = "-";
    			gridArray[neo.getX()][neo.getY()] = "Neo";
    			
    		}else if(step[i].equals("carry")){

    			gridArray[neo.getX()][neo.getY()] = "Neo";
    			
    		}else if(step[i].equals("fly")){

    			for(int j=0; j<Pad.size(); j++) {
    				if(Pad.get(j).getStartX() == neo.getX() && Pad.get(j).getStartY() == neo.getY()) {

    	    			neo.setX(Pad.get(j).getEndX());
    	    			neo.setY(Pad.get(j).getEndY());
    	    			
    					gridArray[neo.getX()][neo.getY()] = "Neo";
    					
    					break;
    				}
    			}
    			
    		}

    		System.out.println("\n\n");
    		printMap(gridArray);
    	}
    }
    
    public static String randomGrid() {
    	String finalString = "";
		//randomGrid = m + "," + n + ";" + c + ";" + neox + "," + neoy+ ";"+TBx+","+TBy+";"+agentXY+";"+pillXY+";"+padXY+";"+hostageXY;

    	ArrayList<Cell> locations = new ArrayList<Cell>();
    	
		String randomGrid = "";
		m = getRandomNumber(5, 15);
		n = getRandomNumber(5, 15);
		int c = getRandomNumber(1, 4);

		
		int neox=getRandomNumber(0, m-1);
		int neoy=getRandomNumber(0, n-1);
		
		finalString = m + "," + n+ ";" + c + ";"+ neox + "," + neoy+ ";";
		
		
		locations.add(new Cell(neox, neoy));
		
		while(true) {
			int TBx = getRandomNumber(0,m-1);
			int TBy = getRandomNumber(0, n-1);
			
			if(!arrContain(locations,new Cell(TBx,TBy))) {
				
				Cell newCell = new Cell(TBx,TBy);
				newCell.setType("TB");
				locations.add(newCell);
				
				finalString+=TBx+","+TBy+";";
				
				break;
			}
		}
		
		///////////done l7ad hna

		int hostageNo = getRandomNumber(3, 10);
		
		
		int maxPads = ((m*n)-hostageNo-locations.size()-2);
		if((maxPads % 2) != 0) {
			if(maxPads<2) {
				maxPads = 2;
			}else {
				maxPads++;
			}
		}
		
		int padNo = getRandomNumber(2, maxPads);
		int pillNo = getRandomNumber(1, hostageNo);
		int agentNo = getRandomNumber(1, (m*n)-locations.size()-pillNo-padNo);
		
		
		for(int i=0; i<agentNo; i++) {
			while(true) {
				int agentx = getRandomNumber(0,m-1);
				int agenty = getRandomNumber(0, n-1);
				
				if(!arrContain(locations,new Cell(agentx,agenty))) {
					Cell newCell = new Cell(agentx,agenty);
					newCell.setType("A");
					locations.add(newCell);

					if(i==agentNo)
						finalString+=agentx+","+agenty+";";
					else
						finalString+=agentx+","+agenty+",";
					break;
				}
			}
		}
		
		for(int i=0; i<pillNo; i++) {
			while(true) {
				int pillx = getRandomNumber(0,m-1);
				int pilly = getRandomNumber(0, n-1);
				
				if(!arrContain(locations,new Cell(pillx,pilly))) {
					
					Cell newCell = new Cell(pillx,pilly);
					newCell.setType("P");
					locations.add(newCell);
					
					if(i==pillNo) {
						finalString+=pillx+","+pilly+";";

					}
					else
						finalString+=pillx+","+pilly+",";
					
					break;
				}
			}
		}
		
		
		
		for(int i=0; i<padNo/2; i++) {
			while(true) {
				int startx = getRandomNumber(0,m-1);
				int starty = getRandomNumber(0, n-1);
				
				if(!arrContain(locations,new Cell(startx,starty))) {
					Cell newCell = new Cell(startx,starty);
					newCell.setType("Pad("+startx+","+starty+")");
					locations.add(newCell);
					

					finalString+=startx+","+starty+",";
					
					
					break;
				}
			}
			while(true) {
				int endx = getRandomNumber(0,m-1);
				int endy = getRandomNumber(0, n-1);
				
				if(!arrContain(locations,new Cell(endx,endy))) {
					Cell newCell = new Cell(endx,endy);
					newCell.setType("Pad("+endx+","+endy+")");
					locations.add(newCell);
					if(i==padNo/2)
						finalString+=endx+","+endy+";";
					else
						finalString+=endx+","+endy+",";
					break;
				}
			}
		}

		
		
		for(int i=0; i<hostageNo; i++) {
			while(true) {
				int hostagex = getRandomNumber(0,m-1);
				int hostagey = getRandomNumber(0, n-1);
				
				if(!arrContain(locations,new Cell(hostagex,hostagey))) {
					Cell newCell = new Cell(hostagex,hostagey);
					newCell.setType("H("+ getRandomNumber(1, 99) +")");
					locations.add(newCell);
					
					if(i==hostageNo) {
						System.out.println(hostageNo + " 5araaaa");
						finalString+=hostagex+","+hostagey+";";
					}
					else
						finalString+=hostagex+","+hostagey+",";
					
					break;
				}
			}
		}
		

		
		
		
		
	
		/*
		String agentXY = "";
		for(int i=0; i<agentNo; i++) {
			if(i!=0)
				agentXY+=",";
			agentXY+=getRandomNumber(0, m-1);
			agentXY+=",";
			agentXY+=getRandomNumber(0,n-1);
		}*/



		/*
		String hostageXY = "";
		for(int i=0; i<hostageNo; i++) {
			if(i!=0)
				hostageXY+=",";
			hostageXY+=getRandomNumber(0, m-1);
			hostageXY+=",";
			hostageXY+=getRandomNumber(0, n-1);
			hostageXY+=",";
			hostageXY+=getRandomNumber(1, 99);
		}
		
		String pillXY = "";
		for(int i=0; i<pillNo; i++) {
			if(i!=0)
				pillXY+=",";
			pillXY+=getRandomNumber(0, m-1);
			pillXY+=",";
			pillXY+=getRandomNumber(0, n-1);
		}*/
		

		System.out.println();
		return randomGrid;
    }
    
    public static String[][] genGrid() {
    	
    	
        //System.out.print("Enter a string: ");

        //String grid = sc.nextLine();
        String grid = mainGrid;
        
        System.out.println(grid);
        String[] parts = grid.split(";");

        /*
         * for(int i=0; i<parts.length; i++) { System.out.println(parts[i]); }
         */

        String MN = parts[0];
        m=Integer.parseInt(MN.split(",")[0]);
        n=Integer.parseInt(MN.split(",")[1]);
        String C = parts[1];
        
        c = Integer.parseInt(C);

        // Neo
        String NeoXY = parts[2];
        String[] NeoArray = NeoXY.split(",");
        neo = new Neo(Integer.parseInt(NeoArray[0]), Integer.parseInt(NeoArray[1]), Integer.parseInt(C), 0, new ArrayList<Hostage>(),new ArrayList<Hostage>(), null, null, null);
        // end Neo

        // telephone
        String TelephoneXY = parts[3];
        String[] TelephoneXYArray = TelephoneXY.split(",");
        telephone = new TelephoneBooth(Integer.parseInt(TelephoneXYArray[0]), Integer.parseInt(TelephoneXYArray[1]));
        // end telephone

        // Agent
        String AgentXY = parts[4];
        String[] AgentArray = AgentXY.split(",");
        agents = new ArrayList();
        for (int i = 0; i < AgentArray.length; i = i + 2) {
            Agent agent = new Agent(Integer.parseInt(AgentArray[i]), Integer.parseInt(AgentArray[i + 1]));
            agents.add(agent);
        }
        // end Agent

        // Pill
        String PillXY = parts[5];
        String[] PillArray = PillXY.split(",");
        Pill = new ArrayList();
        for (int i = 0; i < PillArray.length; i = i + 2) {
            Pill pill = new Pill(Integer.parseInt(PillArray[i]), Integer.parseInt(PillArray[i + 1]));
            Pill.add(pill);
        }
        // end Pill

        // Pad
        String PadXY = parts[6];
        String[] PadArray = PadXY.split(",");
        Pad = new ArrayList();
        for (int i = 0; i < PadArray.length; i = i + 4) {
            Pad pad = new Pad(Integer.parseInt(PadArray[i]), Integer.parseInt(PadArray[i + 1]),
                    Integer.parseInt(PadArray[i + 2]), Integer.parseInt(PadArray[i + 3]));
            Pad.add(pad);
        }
        // end Pad

        // hostages
        String HostageXY = parts[7];
        String[] hostagesArray = HostageXY.split(",");
        hostages = new ArrayList();
        hostageCells = new Cell[hostagesArray.length];

        for (int i = 0; i < hostagesArray.length; i = i + 3) {
           Hostage hostage = new Hostage(Integer.parseInt(hostagesArray[i]), Integer.parseInt(hostagesArray[i + 1]),
                    Integer.parseInt(hostagesArray[i + 2]));

            hostageCells[i] = new Cell(hostage.getX(), hostage.getY());
            hostages.add(hostage);
        }
        // end hostages

        /*
         * String[][] gridArray ={{"-","-","-","-","-","-"}, {"-","-","-","-","-","-"},
         * {"-","-","-","-","-","-"}, {"-","-","-","-","-","-"},
         * {"-","-","-","-","-","-"}};
         */

        // Populate grid

        gridArray = new String[m][n];

        for (int i = 0; i < agents.size(); i++) {
            gridArray[agents.get(i).getX()][agents.get(i).getY()] = "A";
        }
        for (int i = 0; i < Pill.size(); i++) {
            gridArray[Pill.get(i).getX()][Pill.get(i).getY()] = "P";
        }
        for (int i = 0; i < hostages.size(); i++) {
            gridArray[hostages.get(i).getX()][hostages.get(i).getY()] = "H(" + hostages.get(i).getDamage() + ")";
        }
        for (int i = 0; i < Pad.size(); i++) {
            gridArray[Pad.get(i).getStartX()][Pad.get(i).getStartY()] = "Pad(" + Pad.get(i).getEndX() + ","
                    + Pad.get(i).getEndY() + ")";
        }

        gridArray[neo.getX()][neo.getY()] = "Neo";
        gridArray[Integer.parseInt(TelephoneXYArray[0])][Integer.parseInt(TelephoneXYArray[1])] = "TB";
        for (int i = 0; i < gridArray.length; i++) {
            for (int j = 0; j < gridArray[i].length; j++) {
                if (gridArray[i][j] == null) {
                    gridArray[i][j] = "-";
                }
            }
        }

        printMap(gridArray);

        neo.setAllHostages(hostages);
        return gridArray;
    }

    
    public static void printMap(String[][] matrix) {
        for (int i = 0; i < matrix.length; i++) { // this equals to the row in our matrix.
            for (int j = 0; j < matrix[i].length; j++) { // this equals to the column in each row.
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println(); // change line on console as row comes to end in the matrix.
        }
    }
   
    
    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }


    public static void printSteps(Node current) {
    	
    	
    	Node node = current;
    	while(true) {
    		node = current.parent;
    		
    		String[][] gridArray2 = new String[m][n];
    		
            gridArray2[(telephone.x)][(telephone.y)] = "TB";

            for (int i = 0; i < node.currentState.allAgents.size(); i++) {
            	if(node.currentState.allAgents.get(i).getState().equals("Alive")) {
            		gridArray2[node.currentState.allAgents.get(i).getX()][node.currentState.allAgents.get(i).getY()] = "A";
            	}
            }
            
            for (int i = 0; i < node.currentState.allPills.size(); i++) {
            	if(node.currentState.allPills.get(i).getState().equals("NT"))
            		gridArray2[node.currentState.allPills.get(i).getX()][node.currentState.allPills.get(i).getY()] = "P";
            }
            
            for (int i = 0; i < node.currentState.allHostages.size(); i++) {
            	if(node.currentState.allHostages.get(i).getState().equals("Alive")) {
            		
                	gridArray2[node.currentState.allHostages.get(i).getX()][node.currentState.allHostages.get(i).getY()] = "H(" + node.currentState.allHostages.get(i).getDamage() + ")";

            	}else if(node.currentState.allHostages.get(i).getState().equals("Mutated")) {
            		
                	gridArray2[node.currentState.allHostages.get(i).getX()][node.currentState.allHostages.get(i).getY()] = "A";

            	}
            }
            for (int i = 0; i < Pad.size(); i++) {
            	gridArray2[Pad.get(i).getStartX()][Pad.get(i).getStartY()] = "Pad(" + Pad.get(i).getEndX() + ","
                        + Pad.get(i).getEndY() + ")";
            }

            for (int i = 0; i < gridArray2.length; i++) {
                for (int j = 0; j < gridArray2[i].length; j++) {
                    if (gridArray2[i][j] == null) {
                    	gridArray2[i][j] = "-";
                    }
                }
            }

            gridArray2[node.currentState.neoX][node.currentState.neoY] = "Neo";
    		
            printMap(gridArray2);

    		if(node.parent==null)
    			break;
    	}
    }



}

