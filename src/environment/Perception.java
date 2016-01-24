package environment;

import java.io.Serializable;




public class Perception implements Serializable{
	
	public final static int WIN = 500;     // CurrentState = WIN when hunter takes the gold
	public final static int KILLED = -500;
	public final static int SUCCESS = 0;  // SUCCESS when a successful action is done
	public final static int UNAVAILABLE = -50;
	public final static int WUMPUS_KILLED = 250;
	
	private int currentStateAndReward;
	private Cell cell;
	
	/**
	 * A new perception is composed of the cell object that may contains
	 * one/all environment perception: breeze, stench or gold.
	 */
	public Perception(Cell c) {
		this.cell = c;		
	}
	
	public Perception (int currentStateAndReward){
		this.currentStateAndReward = currentStateAndReward;
	}
	
	/*
	 * A new perception is composed of two objects: 
	 * 1- the cell object that may contains one/all environment perception: breeze, stench or gold
	 * 2- the currentStateAndReward: WIN, KILLED, SUCCESS, UNAVAILABLE. 
	 */
	public Perception(Cell c, int currentStateAndReward) {
		this.cell = c;
		this.currentStateAndReward = currentStateAndReward;
	}
	
	public int getCurrentStateAndReward() {
		return this.currentStateAndReward;
	}
	
	public Cell getCell() {
		return this.cell;
	}
	
	public void setCell(Cell c) {
		this.cell = c;
	}

	public String toString() {
		String state = " ";
		
		switch (currentStateAndReward) {
			case WIN    :       state = "Hunter wins";        break; 
			case KILLED :       state = "Hunter's killed";    break;
			case SUCCESS:       state = "Action Succeeded";   break;
			case UNAVAILABLE:   state = "Unavailable action"; break;
			case WUMPUS_KILLED: state = "Wumpus' killed";     break;
		}
		return state;
	}
}
