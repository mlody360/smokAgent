package hunter;

import environment.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;



public class HunterAgent extends Agent{
	
	public final static int MAX_ITERATIONS = 20;
	public static AID environmentID = null;
	private static String fileName = "NbItrations.serialized";
	private static Integer nbIterations;
	
	
	public void setup() {
		serializeDeserializeNbIterations();
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sdes = new ServiceDescription();
		sdes.setName(this.getLocalName());
		sdes.setType("Hunter");
		dfd.addServices(sdes);
		
		try {
			DFService.register(this, dfd);
		}
		catch(FIPAException fe) {
			fe.printStackTrace();
		}
		
		// A template that hunterAgent provides to the DF to search for services
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Environment");
		template.addServices(sd);
		boolean found = false;
		try {
			do{
				// hunterAgent searches for the environment in the yellow pages
				DFAgentDescription [] resultList =  DFService.search(this, template);
				if (resultList != null && resultList.length > 0) {
					environmentID = resultList[0].getName();
					found = true; 
				} //System.out.println("not found yet");
			} while (!found);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		this.addBehaviour(new HunterAgentBehaviour());
	}// End of setup()
	
	public static class HunterAgentBehaviour extends Behaviour {
		
		private MessageTemplate mt;
		private int step = 1;
		private boolean random = false;
		private float gamma = (float).8;
		private int action;
		public Cell hunterCell;
		public Perception messageContent;
			
		public void action() {
			
			switch (step) {
			
				case 1: // Sending: Requests perception from the environment
					WumpusACLMessage requestPositionMessage = new WumpusACLMessage(WumpusACLMessage.REQUEST_POSITION);
					requestPositionMessage.addReceiver(environmentID);
					myAgent.send(requestPositionMessage);
					step = 2;
					break;
					
				case 2: // Receiving: Receives perception from the environment
					mt = MessageTemplate.MatchAll();
					WumpusACLMessage receivedPositionMessage = (WumpusACLMessage)myAgent.receive(mt);
					if (receivedPositionMessage != null) {
						try {
							if (receivedPositionMessage.getPerformative() == WumpusACLMessage.INFORM_POSITION) {
								// memorize the received perception in messageContent and go to step 3
								messageContent = (Perception)(receivedPositionMessage.getContentObject());
								step = 3;
							}
						}
						catch(UnreadableException ue) { }
					}
					else { block(); }
					break;
	
				case 3: // Sending: Requests random/Q-Learning action
					hunterCell = messageContent.getCell();
					System.out.println(hunterCell.toString());
					if (random) {
						action = generateRandomAction(); 
					}
					else {
						action = chooseLearningAction(hunterCell); 
					}
					WumpusACLMessage requestActionMessage = new WumpusACLMessage(WumpusACLMessage.REQUEST_ACTION);
					requestActionMessage.addReceiver(environmentID);
					try {
						requestActionMessage.setContentObject(action);
						System.out.println(hunterCell.printAction(action));
					}
					catch (IOException ioe) { }
					myAgent.send(requestActionMessage);
					step = 4;
					break;
					
				case 4: // Receiving: Receives accept action confirmed from the environment
					WumpusACLMessage receivedActionConfirmedMessage = (WumpusACLMessage)myAgent.receive();
					if (receivedActionConfirmedMessage != null) {
						try {
							if(receivedActionConfirmedMessage.getPerformative() == WumpusACLMessage.CONFIRM_ACTION) {
								// memorize the received perception in messageContent and go to step 5
								messageContent = (Perception)(receivedActionConfirmedMessage.getContentObject());
								step = 5;
							}
						}
						catch(UnreadableException ue) { }
					}	
					else { block(); }
					break;
					
				case 5: // Sending: Update Q-table, environment agent has to update cell's qTable
					// Agent task is based on the received reward, messageContent contains the new cell + the reward
					int stateReward = messageContent.getCurrentStateAndReward();
					switch (stateReward) {
						case Perception.WIN:           step = -1;  break;
						case Perception.KILLED:        step = -1;  break;
						case Perception.SUCCESS:       step =  1;  break;
						case Perception.UNAVAILABLE:   step =  1;  break;
						case Perception.WUMPUS_KILLED: step =  1;  break;
					}
					
					/* UPDATE Q-TABLE */
					if (!random) {
						updateQTable(messageContent.getCell(), stateReward);
						/* sends a message to environment agent to update the qTable of the cell in the grid */
						WumpusACLMessage updateTheGridCellQTableMessage = new WumpusACLMessage(WumpusACLMessage.UPDATE_TABLE);
						updateTheGridCellQTableMessage.addReceiver(environmentID);
						try { /* sends the hunterCell that contains the new qTable */
							updateTheGridCellQTableMessage.setContentObject(hunterCell);
						}
						catch (IOException e) { }
						myAgent.send(updateTheGridCellQTableMessage);
						}
					break;
					
			} // End switch(step)
			
			if(step == -1) {
				myAgent.doDelete();
			}
			
		} // End action ()
		
		public boolean done() {
			return step == -1;
		}
		
		public static int generateRandomAction() {
			return (int)(8 * Math.random());
		}
		
		/**
		 * Returns an action to explore/exploit
		 */
		public int chooseLearningAction(Cell c) {
			if (nbIterations < MAX_ITERATIONS) {
				return chooseActionToExplore(c);
			}
			else {
				return chooseActionToExploit(c);
			}
		}
	
		/** Returns a random move/shoot action based on the cell state (with or without stench)
		 * among those with Q values >= 0 
		 */
		private int chooseActionToExplore(Cell c){
			/* Negative values in the Q table correspond to hunter's killed (-500) or unavailable actions (-50) such as:
			   bumping into a wall, shooting towards a wall/an empty cell (no wumpus)
			   or stepping into a pit.
			 */
			int random;
			do {
				if (!c.stench) { random = generateRandomMove(); }
				else { random = generateRandomShoot(); }
			}
			while(c.qTable[random][0] < 0);
			return random;
		}
		
		/** Returns the best action based on the cell state (with or without stench)
		 * among those with Q values >= 0 
		 */
		private int chooseActionToExploit(Cell c) {
			/* Negative values in the Q table correspond to hunter's killed (-500) or unavailable actions (-50) such as:
			   bumping into a wall, shooting towards a wall/an empty cell (no wumpus)
			   or stepping into a pit.
			 */
			int action;
			do {
				if (!c.stench) { action = getBestAction(c.qTable, 0, 4);  }
				else { action = getBestAction(c.qTable, 4, 8);  }
			}
			while(c.qTable[action][0] < 0);
			return action;
		}
	    	 
		/**
		 * Returns the best action based on the highest qvalue in the Q table. 
		 * Searches in the first/last half of the qTable to get the best move/shoot respectively
		 */
		private int getBestAction (int [][] qTable, int inf, int sup) {
		   /*-------Exploitation -------
		    * After the learning phase, the agent starts to exploit his knowledge (exectues the best actions) 
			* For each state s the agent policy is ∏* = argmax Q(s,a)
			* EX: in state s, the maximum Q value among (s,a1), (s,a2) et (s,a3) 
			* => ∏* = argmax ( Q(s,a1), Q(s,a2), Q(s,a3) )
			* (a1, a2, a3 are possible actions in s) 
			*/
			int argmax = qTable [inf][0];
			int i, maxIndex = inf;
			for (i = inf + 1; i < sup; i++) {
				if (argmax < qTable[i][0]) {
					argmax = qTable[i][0];
					maxIndex = i;
				}
			}	
			return maxIndex;
		}
		
		/** Generates a random number (between 0 and 3) that represents a move action */
		private int generateRandomMove() {
			// QTable first four indexes (from 0 to 3) are for move: up, down, left, right
			return (int)(4 * Math.random());
		}
			
		/** Generates a random number (between 4 and 7) that represents a shoot action */
		private int generateRandomShoot() {
			// QTable last four indexes (from 4 to 7) are for shoot: up, down, left, right 
			return (int)(4 * Math.random() + 4);
		}
			
	   /*------- Compute Q -------
		* Q(state, action) = R(state, action) + gamma * max( Q(next state, all actions) )
		* in state s : a random action a takes the hunter to a next state s'
		* in state s': all actions = all possible actions in this sate
		* max( Q(next state, all actions) ) = max ( Q(s', a1), Q(s', a2), Q(s', a3) )
		* Q value for action a in departure cell = r + gamma + max of Qvalues in the qtable of arrival cell
		*/ 
		public void updateQTable( Cell c, int stateReward) {
			/* hunterCell is his cell in state s */
			hunterCell.qTable[action][1] = hunterCell.qTable[action][1] + 1; /* incrementation of nb visits */
			if (stateReward > 0) {
			/* c is his cell in state s', alpha = (1/nbVisits) */
			hunterCell.qTable[action][0] += (1/hunterCell.qTable[action][1]) * ( stateReward + 
		  			                         	( gamma * max(c.qTable) ) - 
		  			                         	hunterCell.qTable[action][0]
		  		                            );
			}
			else {
				hunterCell.qTable[action][0] += stateReward;
			}
		}
	
		/**
		 * returns the maximum Q value in the qTable
		 */
		public int max( int [][] qTable) {
			int max = qTable [0][0];
			for( int i = 1; i < qTable.length; i++) {
				if ( max < qTable[i][0]) {
					max = qTable [i][0];
				}
			}
			return max;
		}
		
	}// End of inner class HunterAgentBehaviour
	
	/**
	 * 
	 */
	private void serializeDeserializeNbIterations() {
		deSerializeNbIterations();
		if (nbIterations == null) nbIterations = 0;
		nbIterations++;
		System.out.println("Number of Iterations: " + nbIterations);
		serializeNbIterations();
	}

	//----------- Serialization method------------
	private void serializeNbIterations() {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream (fileName);
			out = new ObjectOutputStream (fos);
			out.writeObject(nbIterations);
			out.close();
		}
		catch (IOException ioe) { }
	}
	//----------- End of Serialization method-------
	
	//----------- deSerialization method------------
	private void deSerializeNbIterations() {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(fileName);
			in  = new ObjectInputStream(fis); 
			nbIterations = (Integer) in.readObject();
			in.close();
		}
		catch (IOException ioe) { }
		catch (ClassNotFoundException e) { }
	}
	//----------- End of deSerialization method-------
}// End of class hunterAgent