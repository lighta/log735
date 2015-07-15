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
 *
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
	 * @throws IOException 
	 * 
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

	
	private void askId() throws IOException {
		Commande c = new Commande(ServerCommandeType.ASKID,"");
		masterConsoleTunnel.sendCommande(c);
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
				c= new Commande(ServerCommandeType.MESS, "HELLO !!!");
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
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			ConnexionInfo masterConsoleInfo = null;
			ConnexionInfo myCInfo = null;
			String hostname = "";
			int port = -1;
						
			System.out.println("use default bind ?");
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String use_default = in.readLine();
			
			if(!use_default.equalsIgnoreCase("N")){
				Properties configFile = new Properties();
				
				configFile.load(MasterConsole.class.getClassLoader().getResourceAsStream("nodes/hostname.properties"));
				hostname = configFile.getProperty("bind_hostname");
				port = Integer.parseInt(configFile.getProperty("bind_port"));
				myCInfo = new ConnexionInfo(hostname, port);
				
			}else if(args.length>BIND_ADDRESS_INDEX_ARGS)	// don't use 	args.length>=MASTER_CONSOLE_INDEX_ARGS-1
				myCInfo = parseBindAddress(args[BIND_ADDRESS_INDEX_ARGS]);
			else{
	
				System.out.println("bind ip address ?");
				
				while(true){
					try {
						hostname = in.readLine();
						myCInfo = parseBindAddress(hostname);
						break;
					} catch (IOException e) {
						continue;
					}
				}
	
				while(myCInfo == null){
					System.out.println("bind port ?");
					try {
						port = Integer.parseInt(in.readLine());
						break;
					} catch (NumberFormatException | IOException e) {
						continue;
					}
				}
				if(myCInfo == null)
					myCInfo = new ConnexionInfo(hostname,port);
			
			}
			
			
			hostname = "";
			port = -1;
			
			System.out.println("use default master ?");
			use_default = in.readLine();
			
			if(!use_default.equalsIgnoreCase("N")){
				Properties configFile = new Properties();
				
				configFile.load(MasterConsole.class.getClassLoader().getResourceAsStream("nodes/hostname.properties"));
				hostname = configFile.getProperty("masterConsole_hostname");
				port = Integer.parseInt(configFile.getProperty("masterConsole_port"));
				masterConsoleInfo = new ConnexionInfo(hostname, port);
				
			}else if(args.length>MASTER_CONSOLE_INDEX_ARGS)		
				masterConsoleInfo = parseBindAddress(args[MASTER_CONSOLE_INDEX_ARGS]);
			else{
				
				System.out.println("masterConsole ip address ?");
				
				while(true){
					try {
						hostname = in.readLine();
						masterConsoleInfo = parseBindAddress(hostname);
						break;
					} catch (IOException e) {
						continue;
					}
				}
	
				while(masterConsoleInfo ==  null){
					System.out.println("masterConsole port ?");
					try {
						port = Integer.parseInt(in.readLine());
						break;
					} catch (NumberFormatException | IOException e) {
						continue;
					}
				}
				if(masterConsoleInfo == null)
					masterConsoleInfo = new ConnexionInfo(hostname,port);
			
			}
			List<ConnexionInfo> neighboursCInfo = new ArrayList<ConnexionInfo>();
			
			ConnexionInfo cInfo;
			for (int i = BIND_ADDRESS_INDEX_ARGS+1; i < args.length; i++) {
				cInfo = parseBindAddress(args[i]);
				if(cInfo != null)
				neighboursCInfo.add(cInfo);
			}
			
			new ServerNode(masterConsoleInfo,myCInfo,neighboursCInfo);
			
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
