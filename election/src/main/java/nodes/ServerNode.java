/**
 * 
 */
package nodes;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import serverAccess.Commande;
import serverAccess.Commande.CommandeType;
import serverAccess.ConnexionInfo;
import serverAccess.MultiAccesPoint;
import serverAccess.Tunnel;


/**
 * @author MisterTim
 *
 */
public class ServerNode extends MultiAccesPoint {

	private final static Logger log = Logger.getLogger(ServerNode.class);
	
	private final static int CANNOT_OPEN_ALL_CONNEXION_EXIT_CODE = 1000;
	
	private final static String IP_PORT_DELIMITER = ":";
	
	private final static int MASTER_CONSOLE_INDEX_ARGS = 0;
	private final static int BIND_ADDRESS_INDEX_ARGS = 1;
	
	
	private ConnexionInfo myCInfo;
	private ConnexionInfo masterConsoleCInfo; 
	private Tunnel masterConsoleTunnel;
	
	private Map<String,ConnexionInfo> neighboursCInfo;
	private Map<String,Tunnel> neighboursTunnel;
	
	
	/**
	 * 
	 */
	public ServerNode(ConnexionInfo masterConsole, ConnexionInfo myCInfo,
			List<ConnexionInfo> neighboursCInfo) {
		
		try {
			super.openAccesPoint("myBind", myCInfo);
			this.myCInfo = myCInfo;
			
			super.connectTo("masterConsole", masterConsole);
			this.masterConsoleCInfo = masterConsole;
			
			this.neighboursCInfo = new HashMap<String, ConnexionInfo>();
			for (ConnexionInfo connexionInfo : neighboursCInfo) {
				super.connectToWithoutWaiting("neighb" + connexionInfo, connexionInfo);
				this.neighboursCInfo.put(connexionInfo.getHostname(), connexionInfo);
			}
			
		} catch (IOException e) {
			log.debug("IOException", e);
			log.info("something went wrong, need to terminate");
			System.exit(CANNOT_OPEN_ALL_CONNEXION_EXIT_CODE);
		}
		
		
		
	}

	
	@Override
	protected void newTunnelCreated(Tunnel tun) {
		if(this.neighboursCInfo.containsKey(tun.getcInfoDist().getHostname()))
			this.neighboursTunnel.put(tun.getcInfoDist().getHostname(), tun);
	}


	@Override
	protected void commandeReceiveFrom(Commande comm, Tunnel tun) {
		
		Commande c = null;
		switch (comm.getType()) {
			case HELLO:
				c= new Commande(CommandeType.MESS, "HELLO");
				break;
			case RESTART:
				
				break;
			case STATE:
				
				break;
			case STOP:
				
				break;
			case EN_TUN:
				
				break;
	
			default:
				break;
		}
		
		if(c != null){
			try {
				tun.sendCommande(c);
			} catch (IOException e) {
				log.debug("IOException", e);
			}
		}
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ConnexionInfo masterConsole = parseBindAddress(args[MASTER_CONSOLE_INDEX_ARGS]);
		
		ConnexionInfo myCInfo = parseBindAddress(args[BIND_ADDRESS_INDEX_ARGS]);
		
		List<ConnexionInfo> neighboursCInfo = new ArrayList<ConnexionInfo>();
		
		ConnexionInfo cInfo;
		for (int i = BIND_ADDRESS_INDEX_ARGS+1; i < args.length; i++) {
			cInfo = parseBindAddress(args[i]);
			if(cInfo != null)
			neighboursCInfo.add(cInfo);
		}
		
		new ServerNode(masterConsole,myCInfo,neighboursCInfo);

	}
	
	private static ConnexionInfo parseBindAddress(String s) {
		
		String[] info = s.split(IP_PORT_DELIMITER);
		
		if(info[0] != null){
			String hostname = info[0];
			if(info[1] != null){
				int port = Integer.parseInt(info[1]);
				return new ConnexionInfo(hostname, port);
			}
		}
		return null;
	}



}
