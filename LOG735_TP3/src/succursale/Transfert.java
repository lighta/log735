package succursale;

public class Transfert {
	static enum state_ { INIT,SEND,ACK};
	
	SuccursalesInfo s1,s2;
	int montant;
	state_ state;
	
	public Transfert(SuccursalesInfo s1, SuccursalesInfo s2, int montant) {
		this.s1 = s1;
		this.s2 = s2;
		this.montant = montant;
		state = state_.INIT;
	}

	public void send() {	
		state = state_.SEND;
	}
	
	public void ack() {	
		state = state_.ACK;
	}

}
