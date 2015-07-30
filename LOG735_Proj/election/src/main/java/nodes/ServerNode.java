/**
 * 
 */
package nodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

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
	
	private List<ConnexionInfo> neighboursCInfo; 	//list of nodes to connect to
	private Map<String,Tunnel> neighboursTunnel;	//list de tunnel creer (on est sur d'etre co)
	
	
	public static enum NodeStateElec { prestart,started,chilling }
	public static enum NodeModeElec { bully,ring }
	private List<ConnexionInfo> waitNodesEle;
	private NodeStateElec state_ele;
	private NodeModeElec mode_ele;
	int score = 0; //score for election
	
	//variable methode anneau
	int nm1=-1,np1=-1; //noeuds voisin pour methode anneau
	boolean participant = false;
	boolean elu = false;
	
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
			
			this.neighboursCInfo = new ArrayList<>();
			neighboursTunnel = new HashMap<>();
			ConnInfos(neighboursCInfo);
			
			askId();
			startDefaultConsole();
	}

	private void ConnInfos(List<ConnexionInfo> neighboursCInfo) throws UnknownHostException, IOException{
		for (ConnexionInfo connexionInfo : neighboursCInfo) {
			if(connexionInfo.equals(myCInfo)) continue; //skip self
			super.connectToWithoutWaiting("neighb" + connexionInfo, connexionInfo);
			this.neighboursCInfo.add(connexionInfo);
		}
		ChoixVoisinAnneau();
	}
	
	private void askList() throws IOException {
		final Commande c = new Commande(ServerCommandeType.ASKLIST,"");
		masterConsoleTunnel.sendCommande(c);
	}

	/**
	 * Demande au masterConsoleTunnel son ID
	 * @throws IOException
	 */
	private void askId() throws IOException {
		final Commande c = new Commande(ServerCommandeType.ASKID,""+myCInfo);
		masterConsoleTunnel.sendCommande(c);
	}
	
	private void voteHandle(String messageContent) {
		final String[] part = messageContent.split(":");
		if(part.length < 2) //bad format
			return;
		final int idrec = Integer.parseInt(part[0]);
		final int scorerec = Integer.parseInt(part[1]);
		
		Commande c = null;
		if(scorerec < score)
			c = new Commande(ServerCommandeType.ELE_VOTE,id+":"+score);
		else {
			c = new Commande(ServerCommandeType.ELE_VOTE,idrec+":"+scorerec);
		}
		sendEleScore(c);
	}
	
	private void sendEleScore(final Commande c) {
		switch(mode_ele){
			case bully: //rere
				break;
			case ring:
				final ConnexionInfo cnext = neighboursCInfo.get(np1); //recupe l'info du prochain noeud
				final Tunnel tnext = neighboursTunnel.get(cnext);
				try {
					tnext.sendCommande(c);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.debug("IOException",e);
				}
				break;
		}
	}
	
	/**
	 * Fonction servant a chercher quel est notre numero d'index dans notre liste de node
	 * @return
	 */
	private int SearchSelfIndex(){
		for(int i=0; i<neighboursCInfo.size(); i++){
			if( neighboursCInfo.get(i).equals(myCInfo))
				return i;
		}
		return -1;
	}
	
	private void ChoixVoisinAnneau(){
		final int sz = neighboursCInfo.size();
		final int cur = SearchSelfIndex();
		if(sz >= 2){ //nos voisin sont different
			nm1 = (sz+cur-1)%sz;
			np1 = (cur+1)%sz;
			//attention si sz==2 nm1==np1
			if(sz == 2)
				nm1 = -1;
		}
		else {
			; //on est seul (sob)
		}
	}
	
	/**
	 * ??
	 * Rajoute le tun dans la liste des nodes voisin si non present (gere les doublon)
	 * @param tun : tunnel a rajouter dans liste des voisin 
	 */
	@Override
	protected void newTunnelCreated(Tunnel tun) {
		this.neighboursTunnel.put(tun.getcInfoDist().getHostname()+":"+tun.getcInfoDist().getPort(), tun);
	}

	
	private void startingElection(){
		participant = true;
		voteHandle("msg");
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
			//demandes manuelles
			case ASKID: 
				try {
					askId();
				} catch (IOException e2) {
					log.debug("IOException",e2);
				} 
				break;
			case ASKLIST: 
				try {
					askList();
				} catch (IOException e2) {
					log.debug("IOException",e2);
				} 
				break;
			case ASKELE:
				if(state_ele == NodeStateElec.started) 
					break; //already in election mode
				waitNodesEle = neighboursCInfo;
				state_ele = NodeStateElec.prestart;
				log.debug("prestarting election");
				break;
			case HELLO:
				awnserc= new Commande(ServerCommandeType.MESS, "HELLO !!!");
				break;
				
				
			case ELE_VOTE: voteHandle(comm.getMessageContent()); break;
				
			case ST_ELE:
				if(state_ele == NodeStateElec.started)
					break;
				state_ele = NodeStateElec.started;
				if(state_ele == NodeStateElec.prestart){
					waitNodesEle = null; //no more waiting, an election is already ongoing	
				}
				log.debug("voting to election");
				voteHandle(comm.getMessageContent()); //next step of election
				break;
			case ALIVE: //keep alive
				if(state_ele == NodeStateElec.prestart){
					ConnexionInfo cid = tun.getcInfoDist(); //doubt is the good one
					log.debug("cid="+cid);
					if(waitNodesEle.contains(cid)){
						waitNodesEle.remove(cid);
						if(waitNodesEle.isEmpty()){ //nomore node to wait, we can launch election
							state_ele = NodeStateElec.started;
							log.debug("starting election");
							startingElection(); //start election
						}
					}
				}
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
				try {
					askList();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					log.debug("IOException",e1);
				}
				break;
			case LIST:
				log.debug("List received: "+comm.getMessageContent());
				List<ConnexionInfo> conList = parsenList(comm.getMessageContent());
				try {
					ConnInfos(conList);
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					log.debug("UnknownHostException",e1);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					log.debug("IOException",e1);
				}
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

	private List<ConnexionInfo> parsenList(String messageContent) {
		String nodeinfos[] = messageContent.split("#");
		List<ConnexionInfo> listConInfo = new ArrayList<>();
		for(String conInfo : nodeinfos){
			ConnexionInfo con = utils.parseBindAddress(conInfo,":");
			if(con != null)
				listConInfo.add(con);
		}
		return listConInfo;
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
	 * 				format : mastercnsl:port myadr:port <adrn1:port adrn2:port adrn3:port ...>
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
