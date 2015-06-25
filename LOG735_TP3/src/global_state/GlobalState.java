/**
 * 
 */
package global_state;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * @author MisterTim
 *
 */
public class GlobalState {

	
	
	private int idGlobalState;
	private int idInitiator;
	private HashMap<Integer, State> states;
	private State myState;
	private int remainingStates;

	/**
	 * @param idState 
	 * 
	 */
	public GlobalState(int idGlobalState, int idInitiator) {
		this.idGlobalState = idGlobalState;
		this.idInitiator = idInitiator;
		this.states = new HashMap<Integer,State>();
		this.myState = new State(idGlobalState, idInitiator);
	}
	
	public State getState(int idState){
		return states.get(idState);
	}
	
	public boolean addState(State state){
		if(state.getIdGlobalState() != this.idGlobalState)
			return (states.put(state.getIdState(),state)) != null;
		return false;
	}
	
	public int getIdGlobalState() {
		return idGlobalState;
	}

	public int getIdInitiator() {
		return idInitiator;
	}

	public State getMyState() {
		return myState;
	}

	public int getRemainingState() {
		for (Entry<Integer, State> states : states.entrySet()) {
			if(states.getValue().getCurrentState() != global_state.State.states.REC)
				remainingStates++;
		}
		
		return remainingStates;
	}

}
