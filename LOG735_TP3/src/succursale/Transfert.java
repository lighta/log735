package succursale;

import connexion.Tunnel;

public class Transfert extends Thread {
	public static enum transfert_state { INIT,SEND,ACK};
	
	public SuccursalesInfo s1,s2;
	int montant;
	transfert_state state;
	Tunnel tun;
	int transfert_id;
	
	public Transfert(SuccursalesInfo s1, SuccursalesInfo s2, int montant, Tunnel tun, int transfert_id) {
		this.s1 = s1;
		this.s2 = s2;
		this.montant = montant;
		this.transfert_id=transfert_id;
		if ( tun == null || s1 == null || s2 == null) {
		    throw new IllegalArgumentException(
		      String.format("Parameters can't be null: s1=%s, s1=%s, tun=%s", s1,s2,tun));
		}
		this.tun = tun;
		state = transfert_state.INIT;
	}

	@Override
	public void run() {
		super.run();
		try {
			Thread.sleep(5*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(state!= transfert_state.INIT)
			tun.askTransfert(s1.getId(),montant,state,transfert_id);
	}
	
	public void send() {	
		state = transfert_state.SEND;		
	}
	
	public void ack() {	
		state = transfert_state.ACK;
	}
}
