/**
 * 
 */
package connexion;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * @author AJ98150
 *
 */
public abstract class MultiAccesPoint implements Observer {
	
	private Map<String,AccesPoint> _accesPoints;
	private Map<String,Tunnel> _tunnels;
	
	public MultiAccesPoint() {
		// TODO Auto-generated constructor stub
		
		_accesPoints = new HashMap<>();
		_tunnels = new HashMap<>();
	}
	
	public void openAccesPoint(String name,ConnexionInfo LocalcInfo) throws UnknownHostException, IOException{
		AccesPoint ap = new AccesPoint(name,LocalcInfo);
		ap.acceptConnexion();
		_accesPoints.put(name,ap);
	}
	
	public void connectTo(String name_id,ConnexionInfo cInfo) throws UnknownHostException, IOException{
		AccesPoint ap = new AccesPoint(name_id,cInfo);
		ap.connectTo(cInfo);
		_accesPoints.put(name_id,ap);
	}
	
	@Override
	public void update(Observable obj, Object arg) {
		
		if(obj instanceof AccesPoint){
			//AccesPoint ap = (AccesPoint) obj;
			
			if(arg instanceof Tunnel){
				Tunnel tun = (Tunnel) arg;
				
				_tunnels.put(tun.getNameId(), tun);
				newTunnelCreated(tun);
			}

		}else if(obj instanceof Tunnel){
			Tunnel tun = (Tunnel) obj;
			
			
			if(arg instanceof Commande){
				Commande comm = (Commande) arg;
				commandeReceiveFrom(comm,tun);
			}
		}
		
	}
	
	protected abstract void newTunnelCreated(Tunnel tun);
	protected abstract void commandeReceiveFrom(Commande comm,Tunnel tun);
	
	protected Tunnel getTunnelByNameId(String tunnelNameId){
		return _tunnels.get(tunnelNameId);
	}
		
	
}
