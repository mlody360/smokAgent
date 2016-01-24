package start;

import environment.*;
import hunter.HunterAgent;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;



public class Wumpus {
	
	public static void main(String [] args) {
	
		// Creation of runtime
		Runtime rt = Runtime.instance();
		// Close VM
		rt.setCloseVM(true);
		

		// Launching the platform, creation of main container
		Profile profileMain = new ProfileImpl(null, 8888, null);
		rt.createMainContainer(profileMain); // AgentContainer mainContainer
		
		// Creation of Agents container
		ProfileImpl profileAgentContainer = new ProfileImpl(null, 8888, null);
		AgentContainer agentContainer = rt.createAgentContainer(profileAgentContainer);
		
		// Starting Environment and Hunter agents		
		AgentController enviromentAgent, hunterAgent;
		try {
			enviromentAgent = agentContainer.createNewAgent("Environment", EnvironmentAgent.class.getName(), new Object[0]);
			enviromentAgent.start();
			Thread.sleep(2000);
			hunterAgent = agentContainer.createNewAgent("Hunter", HunterAgent.class.getName(), new Object[0]);
			hunterAgent.start();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}