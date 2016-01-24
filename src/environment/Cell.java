package environment;

import java.io.Serializable;



public class Cell implements Serializable {
	
	public final static int MOVE_UP     = 0;
	public final static int MOVE_DOWN   = 1;
	public final static int MOVE_LEFT   = 2;
	public final static int MOVE_RIGHT  = 3;
	public final static int SHOOT_UP    = 4;
	public final static int SHOOT_DOWN  = 5;
	public final static int SHOOT_LEFT  = 6;
	public final static int SHOOT_RIGHT = 7;
	
	public boolean hunter = false;
	public boolean wumpus = false;
	public boolean pit    = false;
	public boolean breeze = false;
	public boolean stench = false;
	public boolean gold   = false;
	
	
	/* first column for Q values, 2nd column for number of times an action a (in state s) is visited
	   8 lines represent the 8 possible actions mentionned above ( MOVE_UP,....) and used as indexes
	   for qTable. qTable[MOVE_UP][0] = Q-Value, qTable[MOVE_UP][1] = number of visits */
	public int [][] qTable = new int [8][2];
	private int x, y;
	
	public Cell (int x, int y) {
		this.x = x;
		this.y = y;
		/* Initializes qTable to zero */
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 2; j++) {
				qTable[i][j] = 0;
			}
		}
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public String toString() {
		String cellContent = "";
		
		if (hunter) cellContent += " hunter,";
		if (wumpus) cellContent += " wumpus,";
		if (pit)    cellContent += " pit,";
		if (breeze) cellContent += " breeze,";
		if (stench) cellContent += " stench,";
		if (gold)   cellContent += " gold,";
		
		return "Cell (" + x + ", " + y + ") contains: " + cellContent + " its QTable is: " + printQTable();
	}
	
	public String printQTable() {
		String content ="[";
		for (int i = 0; i < qTable.length - 1; i++) {
			content += " (" + qTable[i][0] + "," + qTable[i][1] + "), ";
		}
		content += " (" + qTable[qTable.length -1][0] + "," + qTable[qTable.length - 1][1] + ") ]";
		return content;
	}
	
	public String printAction(int value) {
		String action = " ";
		
		switch (value) {
			case MOVE_UP: 	  action = "Move up";     break;
			case MOVE_DOWN:   action = "Move Down";	  break;
			case MOVE_LEFT:   action = "Move left";   break;
			case MOVE_RIGHT:  action = "Move right";  break;
			case SHOOT_UP:    action = "Shoot up";    break;
			case SHOOT_DOWN:  action = "Shoot Down";  break;
			case SHOOT_LEFT:  action = "Shoot left";  break;
			case SHOOT_RIGHT: action = "Shoot right"; break;	
		}
		return action;
	}

	public void resetBooleanObjects() {
		hunter = false;
		wumpus = false;
		pit    = false;
		breeze = false;
		stench = false;
		gold   = false;
	}
	
}