package banque;

public class AccesPoint extends Observable {
	
	private ConnexionInfo _cInfo;
	private boolean stop;
	
	
	public enum notificationType{
		ACCEPT_CONNEXION,
		NEW_MESSAGE
	}
	
	public AccesPoint(ConnexionInfo cInfo) {
		// TODO Auto-generated constructor stub
		
		stop = false;
	}
	
	public void finalyze(){
		stop = true;
	}
	
	public void accept() {
		while(!stop){
			//accept from socket	
			setChanged();
			notifyObserver(this,ACCEPT_CONNEXION);
		}
	}
	
}
