/**
 * 
 */
package master;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Map;

import nodes.ServerNode;

import org.apache.log4j.Logger;

import serverAccess.Commande;
import serverAccess.ConnexionInfo;
import serverAccess.MultiAccesPoint;
import serverAccess.Tunnel;
import serverAccess.Commande.CommandeType;

/**
 * @author MisterTim
 *
 */
public class MasterConsole extends MultiAccesPoint {

private final static Logger log = Logger.getLogger(ServerNode.class);
	
	private final static int CANNOT_OPEN_ALL_CONNEXION_EXIT_CODE = 1000;
	
	private final static String IP_PORT_DELIMITER = ":";
	
	private final static int MASTER_CONSOLE_INDEX_ARGS = 0;
	
	private ConnexionInfo myCInfo;
	
	private Map<String,Tunnel> nodesTunnel;
	
	/**
	 * @param masterConsole 
	 * 
	 */
	public MasterConsole(ConnexionInfo masterConsoleInfo) {
		
		try {
			super.openAccesPoint("myBind", masterConsoleInfo);
			this.myCInfo = masterConsoleInfo;
			
		} catch (IOException e) {
			log.debug("IOException", e);
			log.info("something went wrong, need to terminate");
			System.exit(CANNOT_OPEN_ALL_CONNEXION_EXIT_CODE);
		}

	}

	
	@Override
	protected void newTunnelCreated(Tunnel tun) {
		
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
		
		ConnexionInfo masterConsoleInfo = null;	
		
		if(args.length>=MASTER_CONSOLE_INDEX_ARGS-1)		
			masterConsoleInfo = parseBindAddress(args[MASTER_CONSOLE_INDEX_ARGS]);
		else{
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			
			System.out.println("bind ip address ?");
			String host;
			
			while(true){
				try {
					host = in.readLine();
					masterConsoleInfo = parseBindAddress(host);
					if(masterConsoleInfo != null)
						break;
				} catch (IOException e) {
					continue;
				}
			}

			int port = -1;
			while(true){
				System.out.println("bind port ?");
				try {
					port = Integer.parseInt(in.readLine());
					break;
				} catch (NumberFormatException | IOException e) {
					continue;
				}
			}
			if(masterConsoleInfo == null)
				masterConsoleInfo = new ConnexionInfo(host,port);
			
		}
		new MasterConsole(masterConsoleInfo);

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
