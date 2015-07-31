/**
 * 
 */
package master;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Properties;

import nodes.ServerNode;

import org.apache.log4j.Logger;

import common.utils;
import console.ConsoleService;
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
	private Map<String,String> nodesCon;
	private Map<Integer,Tunnel> nodesIdTunnel;
	private Map<Tunnel,Integer> nodesTunnelId;
	
	private ConsoleService defaultConsoleService = null;
	
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
		this.nodesCon = new HashMap<>();
		this.nodesIdTunnel = new HashMap<>();
		this.nodesTunnelId = new HashMap<>();
		
		startDefaultConsole();
		
	}

	/**
	 * Demare une DefaultConsole comme un service
	 */
	private void startDefaultConsole() {
		try {
			defaultConsoleService = new ConsoleService("Console for me");
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
					
					Integer idToSend = null;
					try{
						idToSend = Integer.parseInt(content[0]);
					}catch(NumberFormatException nfe){
						//nothing to do
					}
					if(idToSend != null){
						if(idToSend == 0)
							commandeReceiveFrom(c, null);
						else{
							try {
								Tunnel tun = null;
								tun = nodesIdTunnel.get(idToSend);
								if(tun != null){
									log.info("Send : " + c + " to " + tun.getcInfoDist().getHostname() + ":" + tun.getcInfoDist().getPort());
									tun.sendCommande(c);
								}else
									log.info("This id is not register\n"
											+ "\t\tRegistered Ids : " + this.nodesIdTunnel.keySet());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								log.debug("IOException",e);
							}
							
						}
					}
					else{
						if(!this.nodesTunnel.isEmpty())
							log.info("starting to broadcast ... : " + c);
						else
							log.info("Nothing to broadcast");
						
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
		this.nodesTunnel.put(tun.getcInfoDist().getHostname()+":"+tun.getcInfoDist().getPort(), tun);
		log.info("Add node Tunnel : " + tun);
		log.info("Connected : " + tun);
		
	}

	/**
	 * Handler de la masterCommand
	 */
	@Override
	protected void commandeReceiveFrom(Commande comm, Tunnel tun) {
		Commande c = null;
		log.info("Received : " + comm.getType().name() + " --> { " + comm.getMessageContent() + " }");
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
				Integer id = generateID();
				nodesCon.put(tun.getcInfoDist().getHostname()+":"+tun.getcInfoDist().getPort(),comm.getMessageContent() + ":" + id);
				log.info("Add node con : " + tun);
				this.nodesIdTunnel.put(id, tun);
				this.nodesTunnelId.put(tun,id);
				log.info("Add node ID tun : " + id + " for " + tun);
				c = new Commande(ServerCommandeType.ID, "" + id);
				break;
			case ASKLIST:
				log.debug("reply LIST");
				c = new Commande(ServerCommandeType.LIST, generateLIST());
				log.info(c);
				break;
			default:
				log.debug(comm.getType().name());
				break;
		}
		
		if(c != null){
			try {
				if(tun != null)
					tun.sendCommande(c);
				else
					this.defaultConsoleService.print(c.getMessageContent());
					
			} catch (IOException e) {
				log.debug("IOException", e);
			}
		}
	}
	
	@Override
	protected void broken(Tunnel tun) {
		
		log.info("remove tunnel " + tun.getcInfoDist().getHostname()+":"+tun.getcInfoDist().getPort());
		
		this.nodesTunnel.remove(tun.getcInfoDist().getHostname()+":"+tun.getcInfoDist().getPort());
		this.nodesCon.remove(tun.getcInfoDist().getHostname()+":"+tun.getcInfoDist().getPort());
		this.nodesIdTunnel.remove(this.nodesTunnelId.get(tun));
		this.nodesTunnelId.remove(tun);
		log.debug("new map : " + this.nodesTunnel);
		log.debug("new list : " + this.nodesCon);
		
		
		
	}
	
	
	//private Map<String,Tunnel> nodesTunnel;
	//tun.getcInfoDist().getHostname()+":"+tun.getcInfoDist().getPort()
	
	
	private String generateLIST() {
		StringBuilder _sb = new StringBuilder();
		log.info(this.nodesCon.values());
		for ( Entry<String,String> curkey : this.nodesCon.entrySet()  ) {
			_sb.append(curkey.getValue()+"|");
		}	
		return _sb.toString();
	}
	
	/**
	 * Genere un ID unique, et le retourne sous forme de string
	 * @FIXME srsly a string ??...
	 * @return
	 */
	private int generateID() {
		return ++MasterConsole.currentNodeId ;
	}

	/**
	 * @param args
	 * 	format : mastercsnl:port
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
					
					//configFile.load(MasterConsole.class.getClassLoader().getResourceAsStream("master/hostname.properties"));
					//System.out.println("getAbsolutePath()" + new File("./target/classes/master/test.t").getAbsolutePath());
					//System.out.println("getPath()" + new File("./target/classes/master/test.t").getPath());
					configFile.load(new FileReader("./target/classes/master/hostname.properties"));
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
