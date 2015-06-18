package succursale;

public class Transfert {
	public static enum transfert_state { INIT,SEND,ACK};
	
	SuccursalesInfo s1,s2;
	int montant;
	transfert_state state;
	
	public Transfert(SuccursalesInfo s1, SuccursalesInfo s2, int montant) {
		this.s1 = s1;
		this.s2 = s2;
		this.montant = montant;
		state = transfert_state.INIT;
	}

	public void send() {	
		state = transfert_state.SEND;
	}
	
	public void ack() {	
		state = transfert_state.ACK;
	}
}
