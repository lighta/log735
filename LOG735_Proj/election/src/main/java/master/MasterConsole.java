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

import common.utils;

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
	 * @param masterConsoleInfo : Information de connection de masterConsole 
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

	/**
	 * Demare une DefaultConsole comme un service
	 */
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
	
	/**
	 * Recoit une commande par observation
	 */
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
	
	/**
	 * Ajoute le tunnel dans la liste des nodetunnels
	 */
	@Override
	protected void newTunnelCreated(Tunnel tun) {
		log.debug("put new node tunnel : " + tun.getcInfoDist().getHostname());
		this.nodesTunnel.put(tun.getcInfoDist().getHostname(), tun);
	}

	/**
	 * Handler de la masterCommand
	 */
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
		
	/**
	 * Genere un ID unique, et le retourne sous forme de string
	 * @FIXME srsly a string ??...
	 * @return
	 */
	private String generateID() {
		return "" + ++MasterConsole.currentNodeId ;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		try {
			ConnexionInfo masterConsoleInfo = null;	

			if(args.length>MASTER_CONSOLE_INDEX_ARGS)		
				masterConsoleInfo = utils.parseBindAddress(args[MASTER_CONSOLE_INDEX_ARGS],IP_PORT_DELIMITER);
			else{
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				System.out.println("use default bind ?");
				final String use_default = in.readLine();
				
				if(!use_default.equalsIgnoreCase("N")){
					Properties configFile = new Properties();
					
					configFile.load(MasterConsole.class.getClassLoader().getResourceAsStream("master/hostname.properties"));
					final String hostname = configFile.getProperty("bind_hostname");
					final int port = Integer.parseInt(configFile.getProperty("bind_port"));
					masterConsoleInfo = new ConnexionInfo(hostname, port);		
				}
				else {
					System.out.println("bind ip address (adr:port) ?");
					masterConsoleInfo = utils.readAddress(in, IP_PORT_DELIMITER, "fail, bind ip address (adr:port) ?");
				}
			}
			new MasterConsole(masterConsoleInfo);
		} catch (IOException e) {
			log.debug("IOException", e);
			log.info("something went wrong, need to terminate");
			System.exit(CANNOT_OPEN_ALL_CONNEXION_EXIT_CODE);
		}
		
	}

}
