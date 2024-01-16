package code;

import java.util.Comparator;

public class Cell implements Comparator<Cell> {
    int x, y;
    Neo neo;
    String type;

    public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        // this.cost = cost;
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

    //__________________________//

    public Neo getNeo() {
        return this.neo;
    }

    public void setNeo(Neo neo) {
        this.neo = neo;
    }

	@Override
	public int compare(Cell o1, Cell o2) {
        if(o1.getX() == o2.getX() && o1.getY() == o2.getY()) {
            return 0;
        } else {
            return -1;
        } 
	}

    

    /*
     * public int getCost() { return cost; }
     *
     * public void setCost(int cost) { this.cost = cost; }
     */
}
