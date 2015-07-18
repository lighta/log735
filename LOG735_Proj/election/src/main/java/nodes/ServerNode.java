/**
 * 
 */
package nodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import master.MasterConsole;
import common.utils;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import console.ConsoleService;
import serverAccess.Commande;
import serverAccess.Commande.ServerCommandeType;
import serverAccess.ConnexionInfo;
import serverAccess.MultiAccesPoint;
import serverAccess.Tunnel;
import service.Service;
import service.Service.AlreadyStartException;


/**
 * @author MisterTim
 */
public class ServerNode extends MultiAccesPoint {

	private final static Logger log = Logger.getLogger(ServerNode.class);
	
	private final static int CANNOT_OPEN_ALL_CONNEXION_EXIT_CODE = 1000;
	
	private final static String IP_PORT_DELIMITER = ":";
	
	private final static int MASTER_CONSOLE_INDEX_ARGS = 0;
	private final static int BIND_ADDRESS_INDEX_ARGS = 1;
	
	private String id;
	private ConnexionInfo myCInfo;
	private ConnexionInfo masterConsoleCInfo; 
	private Tunnel masterConsoleTunnel;
	
	private Map<String,ConnexionInfo> neighboursCInfo;
	private Map<String,Tunnel> neighboursTunnel;
	
	
	/**
	 * ServerNode constructor
	 * ???
	 * Creer un accesspoint sur myCInfo, 
	 * se connectes a tous ses voisins 
	 * puis demande un ID et demarre une console
	 * 
	 * @param masterConsoleInfo : adresse de la masterconsole 
	 * @param myCInfo : adresse de bind du node
	 * @param neighboursCInfo : liste d'adresse des voisins
	 * @throws IOException 
	 */
	public ServerNode(ConnexionInfo masterConsoleInfo, ConnexionInfo myCInfo,
			List<ConnexionInfo> neighboursCInfo) throws IOException {
		
			super.openAccesPoint("myBind", myCInfo);
			this.myCInfo = myCInfo;
			
			masterConsoleTunnel = super.connectTo("masterConsole", masterConsoleInfo);
			this.masterConsoleCInfo = masterConsoleInfo;
			
			this.neighboursCInfo = new HashMap<String, ConnexionInfo>();
			for (ConnexionInfo connexionInfo : neighboursCInfo) {
				super.connectToWithoutWaiting("neighb" + connexionInfo, connexionInfo);
				this.neighboursCInfo.put(connexionInfo.getHostname(), connexionInfo);
			}
			
			askId();	
			startDefaultConsole();
	}

	/**
	 * Demande au masterConsoleTunnel son ID
	 * @throws IOException
	 */
	private void askId() throws IOException {
		final Commande c = new Commande(ServerCommandeType.ASKID,"");
		masterConsoleTunnel.sendCommande(c);
	}
	
	/**
	 * ??
	 * Rajoute le tun dans la liste des nodes voisin si non present (gere les doublon)
	 * @FIXME le check sur hostname creera de nombreux false positif (lighta)
	 * @param tun : tunnel a rajouter dans liste des voisin 
	 */
	@Override
	protected void newTunnelCreated(Tunnel tun) {
		if(this.neighboursCInfo.containsKey(tun.getcInfoDist().getHostname()))
			this.neighboursTunnel.put(tun.getcInfoDist().getHostname(), tun);
	}

	
	/**
	 * Node command acceptor Handler
	 * @param comm : Command received to handle
	 * @param tun : Tunnel from which the command was received
	 */
	@Override
	protected void commandeReceiveFrom(Commande comm, Tunnel tun) {
		Commande awnserc = null; //if we want to respond to tun
		switch (comm.getType()) {
			case HELLO:
				awnserc= new Commande(ServerCommandeType.MESS, "HELLO !!!");
				break;
			case RESTART:		
				break;
			case STATE:
				break;
			case STOP:
				break;
			case EN_TUN:
				break;				
			case ID:
				this.id = comm.getMessageContent();
				break;
			default:
				log.debug("Unhandled command received: "+comm);
				break;
		}
		
		if(awnserc != null){
			try {
				tun.sendCommande(awnserc);
			} catch (IOException e) {
				log.debug("IOException", e);
			}
		}
	}
	
	/**
	 * Fonction to start a DefaultConsole as a deamon
	 */
	private void startDefaultConsole() {
		Service defaultConsoleService = new ConsoleService("Console for me");
		defaultConsoleService.addObserver(this);
			try {
				Service.startService(defaultConsoleService);
			} catch (AlreadyStartException e) {
				// TODO Auto-generated catch block
				log.debug("AlreadyStartException",e);
			}

	}


	/**
	 * ServerNode Entry point
	 * @param args : Programs arguments
	 * 				format : myadr:port <adrn1:port adrn2:port adrn3:port ...>
	 */
	public static void main(String[] args) {
		
		try {
			ConnexionInfo masterConsoleInfo = null;
			ConnexionInfo myCInfo = null;
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			
			//read myCInfo from, args,console or ressource
			if(args.length>BIND_ADDRESS_INDEX_ARGS)	// don't use 	args.length>=MASTER_CONSOLE_INDEX_ARGS-1
				myCInfo = utils.parseBindAddress(args[BIND_ADDRESS_INDEX_ARGS],IP_PORT_DELIMITER);
			else{
				System.out.println("use default bind ?");
				final String use_default = in.readLine();
				
				if(!use_default.equalsIgnoreCase("N")){
					Properties configFile = new Properties();
					
					configFile.load(MasterConsole.class.getClassLoader().getResourceAsStream("nodes/hostname.properties"));
					final String hostname = configFile.getProperty("bind_hostname");
					final int port = Integer.parseInt(configFile.getProperty("bind_port"));
					myCInfo = new ConnexionInfo(hostname, port);
					
				}
				else {
					System.out.println("bind ip address ?");
					myCInfo = utils.readAddress(in,IP_PORT_DELIMITER,"Parsing fail, please reinter in proper format (host:port) : ");
				}
			
			}	
			
			//read masterConsoleInfo from, args,console or ressource
			List<ConnexionInfo> neighboursCInfo = new ArrayList<ConnexionInfo>();
			if(args.length>MASTER_CONSOLE_INDEX_ARGS) {	
				masterConsoleInfo = utils.parseBindAddress(args[MASTER_CONSOLE_INDEX_ARGS],IP_PORT_DELIMITER);
				
				ConnexionInfo cInfo;
				//lecture des autre noeuds et ajout dans liste
				for (int i = BIND_ADDRESS_INDEX_ARGS+1; i < args.length; i++) {
					cInfo = utils.parseBindAddress(args[i],IP_PORT_DELIMITER);
					if(cInfo != null)
						neighboursCInfo.add(cInfo);
				}
			} else { //no args given
				System.out.println("use default master ?");
				final String use_default = in.readLine();
			
				if(!use_default.equalsIgnoreCase("N")){	
					Properties configFile = new Properties();
					configFile.load(MasterConsole.class.getClassLoader().getResourceAsStream("nodes/hostname.properties"));
					final String hostname = configFile.getProperty("masterConsole_hostname");
					final int port = Integer.parseInt(configFile.getProperty("masterConsole_port"));
					masterConsoleInfo = new ConnexionInfo(hostname, port);
				}
				else{
					System.out.println("masterConsole ip address ? (host:port)");
					final String failquestion = "Parsing fail, please reinter in proper format (host:port) : ";
					//demande une ip valide sur le reseau
					masterConsoleInfo = utils.readAddress(in,IP_PORT_DELIMITER,failquestion);
				}
			} 
			
			new ServerNode(masterConsoleInfo,myCInfo,neighboursCInfo);
			
		} catch (IOException e) {
			log.debug("IOException", e);
			log.info("something went wrong, need to terminate");
			System.exit(CANNOT_OPEN_ALL_CONNEXION_EXIT_CODE);
		}
	}
	





}
