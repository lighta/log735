/**
 * 
 */
package connexion;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import logs.Logger;

/**
 * @author AJ98150
 *
 */
public abstract class MultiAccesPoint implements Observer {
	
	private static Logger log = Logger.createLog(MultiAccesPoint.class);
	
	private Map<String,AccesPoint> _accesPoints;
	
	/**
	 * port number for key
	 */
	private Map<Integer,Map<String,Tunnel>> _tunnels;
	
	public MultiAccesPoint() {
		// TODO Auto-generated constructor stub
		
		_accesPoints = new HashMap<>();
		_tunnels = new HashMap<>();
	}
	
	private void useLocalPort(int port) {
		if(_tunnels.get(port) == null){
			_tunnels.put(port, new HashMap<String,Tunnel>());
		}
	}
	
	/**
	 * start service connexion acceptation
	 * @param name
	 * @param LocalcInfo
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void openAccesPoint(String name,ConnexionInfo LocalcInfo) throws UnknownHostException, IOException {
		log.message("Try to open access point");
		AccesPoint ap = new AccesPoint(name,LocalcInfo);
		ap.addObserver(this);
		ap.acceptConnexion();
		_accesPoints.put(name,ap);
		useLocalPort(LocalcInfo.getPort());
	}
	
	

	/**
	 * Try to connect to c_info
	 * @param name_id
	 * @param cInfo
	 * @return the tunnel connection
	 * @throws IOException
	 */
	public Tunnel connectTo(String name_id,ConnexionInfo cInfo) throws IOException {
		AccesPoint ap = new AccesPoint(name_id,cInfo);
		
		Tunnel tun = ap.connectTo(cInfo);
		useLocalPort(tun.getcInfoLocal().getPort());
		return tun;
	}
	
	
	public void connectToWithoutWaiting(String name_id,ConnexionInfo cInfo) throws UnknownHostException, IOException{
		AccesPoint ap = new AccesPoint(name_id,cInfo);
		ap.addObserver(this);
		ap.connectToWithoutWaiting(cInfo);
		_accesPoints.put(name_id,ap);
	}
	
	@Override
	public void update(Observable obj, Object arg) {
		
		if(obj instanceof AccesPoint){
			AccesPoint ap = (AccesPoint) obj;
			
			if(arg instanceof Tunnel){

				Tunnel tun = (Tunnel) arg;
				log.message("new tunnel "+ tun + " arg created from " + ap);
				
				int localPort = tun.getcInfoLocal().getPort();
				useLocalPort(localPort);
				_tunnels.get(localPort).put(tun.getNameId(), tun);
				
				newTunnelCreated(tun);
			}

		}else if(obj instanceof Tunnel){
			Tunnel tun = (Tunnel) obj;
			useLocalPort(tun.getcInfoLocal().getPort());
			
			if(arg instanceof Commande){
				log.message("Commande " + arg + " receive from tunnel" + tun);
				Commande comm = (Commande) arg;
				commandeReceiveFrom(comm,tun);
			}
		}
		
	}
	
	protected abstract void newTunnelCreated(Tunnel tun);
	protected abstract void commandeReceiveFrom(Commande comm,Tunnel tun);
	
//	protected Tunnel getTunnelByNameId(String tunnelNameId){
//		return _tunnels.get(tunnelNameId);
//	}
	
	protected Collection<Tunnel> getTunnelsbyPort(int port){
		return _tunnels.get(port).values();
	}
	
}
