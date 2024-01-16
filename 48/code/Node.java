package code;

public class Node implements Comparable<Node> {
    State currentState; //THIS WILL BE NEOS STATE + GRID ARRAY SNAPSHOT + NEXT ACTION?
    // This will be neos position + who hes carrying + dead hostages + alive + and saved aleady that way they are all unique then tchedck 3nd kol
    // node fel hash set law neos position is the same + all of the above+ if so then dont queue this node since its a state done before
    //THIS PREVENTS REPEATED STATES AND INF LOOPS
    Node parent;
    Operators operator;
    int depth;
    int cost;
    
    
    public Node(State currentState, Operators operator, Node parent, int depth, int cost) {
        this.currentState = currentState;
        this.operator = operator;
        this.parent = parent;
        this.depth = depth;

        this.cost = cost;

    }

    @Override
    public int compareTo(Node node) {
        if(this.cost > node.cost) {
            return 1;
        } else if (this.cost < node.cost) {
            return -1;
        } else {
            return 0;
        }
    }
}
