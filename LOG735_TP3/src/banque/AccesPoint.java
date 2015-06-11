package banque;

import java.util.Observable;
import connexion.ConnexionInfo;

public class AccesPoint extends Observable {
	
	private ConnexionInfo _cInfo;
	private boolean stop;
	
	public enum notificationType{
		ACCEPT_CONNEXION,
		NEW_MESSAGE
	}
	
	public AccesPoint(String name,ConnexionInfo cInfo) {
		stop = false;
	}
	
	public void finalyze(){
		stop = true;
	}
	
	public void accept() {
		new Thread
		while(!stop){
			//accept from socket	
			setChanged();
			notifyObservers(notificationType.ACCEPT_CONNEXION);
		}
	}
	
}
