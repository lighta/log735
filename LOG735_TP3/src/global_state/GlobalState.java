/**
 * 
 */
package global_state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import succursale.Transfert;

/**
 * @author MisterTim
 *
 */
public class GlobalState implements Iterable<State> {

	
	
	private int idGlobalState;
	private int idInitiator;
	private HashMap<Integer, State> states;
	private State myState;
	private int remainingStates;
	private List<Transfert> myIncomingTransfert;

	/**
	 * @param idState 
	 * 
	 */
	public GlobalState(int idGlobalState, int idInitiator) {
		this.idGlobalState = idGlobalState;
		this.idInitiator = idInitiator;
		this.states = new HashMap<Integer,State>();
		this.myState = new State(idGlobalState, idInitiator);
		this.myIncomingTransfert = new ArrayList<Transfert>();
	}
	
	public State getState(int idState){
		return states.get(idState);
	}
	
	public boolean addState(State state){
		System.out.println("idst="+state.getIdGlobalState()+ " thisid="+this.idGlobalState);
		if(state.getIdGlobalState() == this.idGlobalState)
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

	/**
	 * Les states n'ayant pas encore repondu, tjr dans l'etat rec
	 * @return
	 */
	public int getRemainingState() {
		for (Entry<Integer, State> states : states.entrySet()) {
			if(states.getValue().getCurrentState() == global_state.State.states.REC)
				remainingStates++;
		}
		
		return remainingStates;
	}

	public List<Transfert> getMyIncomingTransf() {
		return this.myIncomingTransfert;
	}
	
	public boolean addIncomingTransf(Transfert t) {
		return this.myIncomingTransfert.add(t);
	}

	@Override
	public Iterator<State> iterator() {
		return this.states.values().iterator();
	}

}
