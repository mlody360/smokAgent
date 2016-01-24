package environment;

import gui.*;

import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;


import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.MessageTemplate;



public class EnvironmentAgent extends Agent{
	
	private static String fileName = "Grid.serialized";
	
	public static GridGui gridGui = null;
	public static Grid grid = null;
	
	protected void setup() {
		/* deserialisation of the grid */
		deSerializeGrid(); 
		if (grid == null) { grid =  new Grid();}
		/* resets the objects inside the grid after they changed in a previous game, except the qTables */
		grid.setGridCellContent();
		gridGui = new GridGui(grid);
		/* Draws the  grid Gui based on the grid (Matrix) state */
		gridGui.showGui();
		registerEnvironment();
		this.addBehaviour(new EnvironmentAgentBehaviour());	
	}//end setup
	
	
	public static class EnvironmentAgentBehaviour extends CyclicBehaviour {
		
		private AID theHunterSender;
		private MessageTemplate mt;
		public static Cell hunterCell;
		
		public void action() {
			mt = MessageTemplate.MatchAll();
			// Receiving: receives a message from hunter
			WumpusACLMessage receivedMessageFromHunter = (WumpusACLMessage)myAgent.receive(mt);
			
			if (receivedMessageFromHunter != null) {
				theHunterSender = receivedMessageFromHunter.getSender();
				WumpusACLMessage environmentReply;
				try { 
					// Sending:  sends to the hunter his current cell 
					if (receivedMessageFromHunter.getPerformative() == WumpusACLMessage.REQUEST_POSITION) {
						environmentReply = new WumpusACLMessage(WumpusACLMessage.INFORM_POSITION);
						environmentReply.addReceiver(theHunterSender);
						environmentReply.setContentObject( (Serializable) new Perception(grid.getHunterCell())) ;
						myAgent.send(environmentReply);
					}
					// Sending : executes the action and sends the result cell and status	
					if (receivedMessageFromHunter.getPerformative() == WumpusACLMessage.REQUEST_ACTION) {
						environmentReply = new WumpusACLMessage(WumpusACLMessage.CONFIRM_ACTION);
						environmentReply.addReceiver(theHunterSender);
						int action = (Integer) receivedMessageFromHunter.getContentObject();
						environmentReply.setContentObject(executeAction(action));
						myAgent.send(environmentReply);
					}
					// Receiving: updates the qTable
					if (receivedMessageFromHunter.getPerformative() == WumpusACLMessage.UPDATE_TABLE) {
						Cell cellToUpdate = (Cell) receivedMessageFromHunter.getContentObject();
						Cell c = grid.getCell(cellToUpdate.getX(), cellToUpdate.getY());
						c.qTable = cellToUpdate.qTable;
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
			else {
				block();
			}
			serializeGrid();	
			
		}// End of action()
	
		public void informPosition() {
			
		}
		
		/**
		 * Returns a perception that contains the executed action.
		 * Executes the action sent by hunter.
		 */
		public Perception executeAction(int action) {
			Perception perceptionResult = null;
			int x = grid.getHunterPosX();
			int y = grid.getHunterPosY();
			
			switch(action) {			
				case Cell.MOVE_UP:     perceptionResult = moveHunter(x - 1, y);  break;	
				case Cell.MOVE_DOWN:   perceptionResult = moveHunter(x + 1, y);  break;								
				case Cell.MOVE_LEFT:   perceptionResult = moveHunter(x, y - 1);  break;		
				case Cell.MOVE_RIGHT:  perceptionResult = moveHunter(x, y + 1);  break;
				case Cell.SHOOT_UP:    perceptionResult = shootWumpus(x - 1, y); break;
				case Cell.SHOOT_DOWN:  perceptionResult = shootWumpus(x + 1, y); break;	
				case Cell.SHOOT_LEFT:  perceptionResult = shootWumpus(x, y - 1); break;	
				case Cell.SHOOT_RIGHT: perceptionResult = shootWumpus(x, y + 1); break;			
			}
			try { Thread.sleep(500); } catch(Exception e) { } /* Delaying hunter's movement */	
			/* adds the hunter's current cell to perceptionResult (contains the state of his action = unavailable/success....) */
			perceptionResult.setCell(grid.getHunterCell());
			System.out.println(perceptionResult.toString());
			return perceptionResult;
			
		}// End of executeAction()
		
		
		/**
		 * Returns the result (status) of hunter's move
		 * refreshes the grid's Cell and the CellGui after an action
		 */
		public static Perception moveHunter(int newX, int newY) {	
			
			if ( (newX < 0) || (newX > grid.nLines - 1) || (newY < 0) || (newY > grid.nLines -1) ){
				return new Perception(Perception.UNAVAILABLE);
			}	
			Cell oldCell = grid.getHunterCell();
			/* delete hunter from cell ( grid's cell) */
			oldCell.hunter = false; 
			/* hunter = true in new cell */
			grid.setHunterPosition(newX, newY);
			
			/* Delete hunter from the current cellGui */
			gridGui.getCellGui(oldCell.getX(), oldCell.getY()).updateCellGui();
			/* Display hunter in the new cellGui */
			gridGui.getCellGui(newX, newY).updateCellGui();
			
			Cell newCell = grid.getHunterCell();			
			if (newCell.wumpus || newCell.pit) {
				gridGui.getCellGui(newX, newY).hunterIsKilled = true;
				return new Perception(Perception.KILLED);
			}
			if (newCell.gold) {
				gridGui.getCellGui(newX, newY).hunterWins = true;
				return new Perception(Perception.WIN);
			}
			return new Perception(Perception.SUCCESS);
		}
		
		
		/**
		 * Returns the result (status) of hunter's shoot
		 * refreshes the grid's Cell and the CellGui after an action
		 */
		public static Perception shootWumpus(int newX, int newY) {
			
			Perception perceptionResult = null;
			
			if ( (newX < 0) || (newX > grid.nLines - 1) || (newY < 0) || (newY > grid.nLines -1) ){
				return new Perception(Perception.UNAVAILABLE);
			}
			
			if ( !grid.getCell(newX, newY).wumpus) { 
				perceptionResult = new Perception (Perception.UNAVAILABLE);
			}
			else { 
				perceptionResult = new Perception(Perception.WUMPUS_KILLED);
				grid.getCell(newX, newY).wumpus = false;
 				gridGui.getCellGui(newX, newY).wumpusIsKilled = true;
				gridGui.getCellGui(newX, newY).updateCellGui();
				disableStench(newX, newY);
			}
			return perceptionResult;
		}
		
		
		/* Disables the stenches when Wumpus' killed so that the hunter won't shoot any more */
		public static void disableStench(int newX, int newY) {
			if (newX - 1 > 0) { 
				grid.getCell(newX - 1, newY).stench = false;
				gridGui.getCellGui(newX - 1, newY).disableStench = true;
				gridGui.getCellGui(newX - 1, newY).updateCellGui();
			}
			if (newY - 1 > 0) {
				grid.getCell(newX, newY - 1).stench = false;
				gridGui.getCellGui(newX, newY - 1).disableStench = true;
				gridGui.getCellGui(newX, newY - 1).updateCellGui();
			}
			if (newX + 1 < grid.nLines - 1) {
				grid.getCell(newX + 1, newY).stench = false;
				gridGui.getCellGui(newX + 1, newY).disableStench = true;
				gridGui.getCellGui(newX + 1, newY).updateCellGui();
			}
			if (newY + 1 < grid.nLines - 1) { 
				grid.getCell(newX, newY + 1).stench = false;
				gridGui.getCellGui(newX, newY + 1).disableStench = true;
				gridGui.getCellGui(newX, newY + 1).updateCellGui();
			}
		}
				
		//****** Serialization method ******
		/* Writes the current grid object to a file, called in EnvironmentAgentBehaviour action() method */
		private void serializeGrid() {
			FileOutputStream fos = null;
			ObjectOutputStream out = null;
			try {
				fos = new FileOutputStream (fileName);
				out = new ObjectOutputStream (fos);
				out.writeObject(grid);
				out.close();
			}
			catch (IOException ioe) { }
		}
		/*---- End  of Serialization method----*/	

	} // End of inner class environmentAgentBehaviour

	
	//******deSerialization method******
	/* reads the former grid object saved in a file, called in EnvironmentAgent setup() method */
	private void deSerializeGrid() {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(fileName);
			in  = new ObjectInputStream(fis); 
			grid = (Grid) in.readObject();
			in.close();
		}
		catch (IOException ioe) { }
		catch (ClassNotFoundException e) { }
	}
	/*----End of deSerialization method----*/

	 //Registers environmentAgent in the yellow pages 
	private void registerEnvironment() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(this.getLocalName());
		sd.setType("Environment");
		dfd.addServices(sd);
		
		try {
			DFService.register(this, dfd);
		}
		catch(FIPAException fe) {
			fe.printStackTrace();
		}
		
	}
	
	public void takeDown() {
		try {
			DFService.deregister(this);
		} 
		catch (FIPAException fe) { }
	}	
	
} // End of class EnvironmentAgent