/**
 * 
 */
package serverAccess;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import org.apache.log4j.Logger;


/**
 * @author AJ98150
 *
 */
public abstract class MultiAccesPoint implements Observer {
	
	private static Logger log = Logger.getLogger(MultiAccesPoint.class);
	
	private Map<String,AccesPoint> _accesPoints;
	
	/**
	 * port number for key
	 */
	private Map<Integer,Map<String,Tunnel>> _tunnels;
	
	public MultiAccesPoint() {
		_accesPoints = new HashMap<>();
		_tunnels = new HashMap<>();
	}
	
	private void useLocalPort(int port) {
		if(_tunnels.get(port) == null){
			_tunnels.put(port, new HashMap<String,Tunnel>());
		}
	}
	
	/**
	 * start service server_access acceptation
	 * @param name
	 * @param LocalcInfo
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void openAccesPoint(String name,ConnexionInfo LocalcInfo) throws UnknownHostException, IOException {
		log.debug("Try to open access point");
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
		tun.addObserver(this);
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
				tun.addObserver(this);
				log.debug("new tunnel "+ tun + " arg created from " + ap);
				
				int localPort = tun.getcInfoLocal().getPort();
				useLocalPort(localPort);
				_tunnels.get(localPort).put(tun.getNameId(), tun);
				
				newTunnelCreated(tun);
			}

		}else if(obj instanceof Tunnel){
			Tunnel tun = (Tunnel) obj;
			useLocalPort(tun.getcInfoLocal().getPort());
			log.debug("receive from tunnel" + tun);
			if(arg instanceof Commande){
				log.debug("Commande " + arg + " receive from tunnel" + tun);
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
