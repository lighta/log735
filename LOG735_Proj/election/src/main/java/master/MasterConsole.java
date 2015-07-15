/**
 * 
 */
package master;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Properties;

import nodes.ServerNode;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import console.ConsoleService;
import serverAccess.AccesPoint;
import serverAccess.Commande;
import serverAccess.ConnexionInfo;
import serverAccess.MultiAccesPoint;
import serverAccess.Tunnel;
import serverAccess.Commande.ServerCommandeType;
import service.Service;
import service.Service.AlreadyStartException;

/**
 * @author MisterTim
 *
 */
public class MasterConsole extends MultiAccesPoint {

private final static Logger log = Logger.getLogger(ServerNode.class);
	
	private final static int CANNOT_OPEN_ALL_CONNEXION_EXIT_CODE = 1000;
	
	private final static String IP_PORT_DELIMITER = ":";
	
	private final static int MASTER_CONSOLE_INDEX_ARGS = 0;

	private static int currentNodeId = 0;
	
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
		
		this.nodesTunnel = new HashMap<>();
		
		startDefaultConsole();
		
	}

	private void startDefaultConsole() {
		
			try {
				Service defaultConsoleService = new ConsoleService("Console for me");
				defaultConsoleService.addObserver(this);
				Service.startService(defaultConsoleService);
			} catch (AlreadyStartException e) {
				// TODO Auto-generated catch block
				log.debug("AlreadyStartException",e);
			}
			
	}
	
	@Override
	public void update(Observable obj, Object arg) {
		
		if(obj instanceof ConsoleService){
			ConsoleService cs = (ConsoleService) obj;
			
				if(arg instanceof Commande){
					Commande c = (Commande) arg;
					String[] content = c.getMessageContent().split(":");
					
					if(content[0].equals("0"))
						commandeReceiveFrom(c, null);
					else{
						log.debug("starting to broadcast ... ");
						for (Entry<String, Tunnel> tunn : this.nodesTunnel.entrySet()) {
							try {
								
								log.debug("Sending to : " + tunn.getKey());
								tunn.getValue().sendCommande(c);
								
							} catch (IOException e) {
								// TODO Auto-generated catch block
								log.debug("IOException",e);
							}
						}
					}				
				}
		}else{
			
			super.update(obj, arg);
			
		}
	}
	
	@Override
	protected void newTunnelCreated(Tunnel tun) {
		log.debug("put new node tunnel : " + tun.getcInfoDist().getHostname());
		this.nodesTunnel.put(tun.getcInfoDist().getHostname(), tun);
	}

	@Override
	protected void commandeReceiveFrom(Commande comm, Tunnel tun) {
		Commande c = null;
		switch (comm.getType()) {
			case HELLO:
				c= new Commande(ServerCommandeType.MESS, "HELLO !!!");
				break;
			case RESTART:
				System.out.println("RESTART");
				break;
			case STATE:
				System.out.println("STATE");
				break;
			case STOP:
				System.out.println("STOP");
				break;
			case EN_TUN:
				System.out.println("STOP");
				break;
			case ASKID:
				log.debug("reply ID");
				c = new Commande(ServerCommandeType.ID, generateID());
			default:
				System.out.println(comm.getType().name());
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
		
	private String generateID() {
		return "" + ++MasterConsole.currentNodeId ;
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
