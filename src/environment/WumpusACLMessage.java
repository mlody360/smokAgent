package environment;

import jade.lang.acl.ACLMessage;


public class WumpusACLMessage extends ACLMessage{
	
	public final static int REQUEST_POSITION = 50000;
	public final static int INFORM_POSITION = 50001;
	public final static int REQUEST_ACTION = 50002;
	public final static int CONFIRM_ACTION = 50003;
	public final static int UPDATE_TABLE = 50004;
	
	public WumpusACLMessage(int performative) {
		super(performative);
	}
	
}