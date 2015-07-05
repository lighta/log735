/**
 * 
 */
package master;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;

import nodes.ServerNode;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

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
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * 
	 */
	public MasterConsole(ConnexionInfo masterConsoleInfo) throws UnknownHostException, IOException {
		
		super.openAccesPoint("myBind", masterConsoleInfo);
		this.myCInfo = masterConsoleInfo;

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
		
		try {
			ConnexionInfo masterConsoleInfo = null;	
			System.out.println("use default bind ?");
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String use_default = in.readLine();
			
			if(!use_default.equalsIgnoreCase("N")){
				Properties configFile = new Properties();
				
				configFile.load(MasterConsole.class.getClassLoader().getResourceAsStream("master/hostname.properties"));
				String hostname = configFile.getProperty("bind_hostname");
				int port = Integer.parseInt(configFile.getProperty("bind_port"));
				masterConsoleInfo = new ConnexionInfo(hostname, port);
				
			}else if(args.length>MASTER_CONSOLE_INDEX_ARGS)		
				masterConsoleInfo = parseBindAddress(args[MASTER_CONSOLE_INDEX_ARGS]);
			else{
				
				
				System.out.println("bind ip address ?");
				String host;
				
				while(true){
					try {
						host = in.readLine();
						masterConsoleInfo = parseBindAddress(host);
						break;
					} catch (IOException e) {
						continue;
					}
					
				}
	
				int port = -1;
				while(masterConsoleInfo == null){
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
			
		} catch (IOException e) {
			log.debug("IOException", e);
			log.info("something went wrong, need to terminate");
			System.exit(CANNOT_OPEN_ALL_CONNEXION_EXIT_CODE);
		}
		
	}
	
	private static ConnexionInfo parseBindAddress(String s) {
		
		String[] info = s.split(IP_PORT_DELIMITER);
		if(info.length != 2)
			return null;
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
