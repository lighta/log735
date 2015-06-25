/**
 * 
 */
package global_state;

/**
 * @author MisterTim
 *
 */
public class State {

	public enum states{
		REC,
		WAIT,
		FIN
	}
	
	private int idGlobalState, idState;
	private states currentState;
	private int montant;
	
	
	/**
	 * 
	 */
	public State(int idGlobalState,int idState) {
		this.idGlobalState = idGlobalState;
		this.idState = idState;
		this.currentState = states.REC;
		this.montant = 0;
	}


	public states getCurrentState() {
		return currentState;
	}


	public void setCurrentState(states currentState) {
		this.currentState = currentState;
	}


	public int getIdGlobalState() {
		return idGlobalState;
	}


	public int getIdState() {
		return idState;
	}


	public int getMontant() {
		return montant;
	}
	
	public void setMontant(int montant) {
		this.montant = montant;
	}

}
