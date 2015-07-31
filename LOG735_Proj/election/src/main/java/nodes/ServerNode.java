/**
 * 
 */
package nodes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.PriorityQueue;
import java.util.Properties;

import master.MasterConsole;
import common.utils;

import org.apache.log4j.Logger;
import org.apache.log4j.lf5.util.DateFormatManager;

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
	
	private int id;
	private ConnexionInfo myCInfo;
	private ConnexionInfo masterConsoleCInfo; 
	private Tunnel masterConsoleTunnel;
	
	private List<Tunnel> neighboursInfo; 	//list of nodes to connect to
	//private Map<String,Tunnel> neighboursTunnel;	//list de tunnel creer (on est sur d'etre co)
	
	private ConsoleService defaultConsoleService = null;
	
	public static enum NodeModeElec { Bully,Bully2,Ring }
	private List<ConnexionInfo> waitNodesEle;
	private NodeModeElec mode_ele = NodeModeElec.Ring;
	private long score = (new Date()).getTime(); //score for election
	Long current_ele_timestamp = null;
	
	//variable methode anneau
	int nm1=-1,np1=-1; //noeuds voisin pour methode anneau
	boolean participant = false;
	boolean elu = false;
	/**
	 * null if me
	 */
	Tunnel master_elu = null;
	
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
			
			this.neighboursInfo = new ArrayList<>();
			//neighboursTunnel = new HashMap<>();
			ConnInfos(neighboursCInfo);
			
			askId();
			startDefaultConsole();
	}
	
	private void ConnInfos(List<ConnexionInfo> neighboursCInfo) throws UnknownHostException, IOException{
		log.debug("Called : ConnInfos " + neighboursCInfo);
		for (ConnexionInfo connexionInfo : neighboursCInfo) {
			if(connexionInfo.equals(myCInfo)) continue; //skip self
			Tunnel tun = super.connectTo("neighb" + connexionInfo, connexionInfo);
			tun.sendCommande(new Commande(ServerCommandeType.ID_NODE, "" + this.id));
			tun.setDistId(connexionInfo.getId());
			this.neighboursInfo.add(tun);
			Collections.sort(this.neighboursInfo, new Comparator<Tunnel>() {
				@Override
				public int compare(Tunnel o1, Tunnel o2) {
					return (new Integer(o1.getDistId())).compareTo(o2.getDistId());
				}
			});
			
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
		final Commande c = new Commande(ServerCommandeType.ASKID,"" + myCInfo);
		masterConsoleTunnel.sendCommande(c);
	}
	
	private void voteHandle(String messageContent) {
		log.debug("Called : voteHandle " + messageContent);
		final String[] part = messageContent.split(":");
		if(part.length < 3){ //bad format
			log.debug("Bad Format");
			return;
		}
		
		final long timestamp = Long.parseLong(part[0]);
		final int idrec = Integer.parseInt(part[1]);
		final long scorerec = Long.parseLong(part[2]);
		log.debug("timestamp=" + timestamp + "idrec=" + idrec + "scorerec=" + scorerec);
		Commande c = null;
		
		if(idrec == id){
			if(scorerec == score){
				c = new Commande(ServerCommandeType.ELU,timestamp + ":" + id+":"+score);
				log.info("Je sui l'ELU : " + idrec + scorerec);
			}else{
				log.info("Sale con de Hacker: c'est mon id mais pas mon score");
			}
		}else if(scorerec < score){
			c = new Commande(ServerCommandeType.ELE_VOTE,timestamp + ":" + id+":"+score);
			log.info("Vote for me " + id+":"+score);
		}else {
			c = new Commande(ServerCommandeType.ELE_VOTE,timestamp + ":" + idrec+":"+scorerec);
			log.info("Vote for him :'( " + idrec+":"+scorerec);
		}
		
		sendEleScore(c);
	}
	
	private void eluHandle(String messageContent) {
		final String[] part = messageContent.split(":");
		if(part.length < 3){ //bad format
			log.debug("Bad Format");
			return;
		}
		final long timestamp = Long.parseLong(part[0]);
		final int idrec = Integer.parseInt(part[0]);
		final long scorerec = Long.parseLong(part[1]);
		
		Commande c = null;
		if(idrec != id){
			c = new Commande(ServerCommandeType.ELU,timestamp + ":" + idrec+":"+scorerec);
			log.info("ELU : " + idrec + scorerec);			
			sendEleScore(c);
		}
		
		
	}
	
	private void sendEleScore(final Commande c) {	
		switch(mode_ele){
			case Bully: //rere
				log.info("Bully send ele score");
				break;
			case Ring:
				if (np1 == -1)
					ChoixVoisinAnneau();
				log.info("Ring send ele score");
				final Tunnel tnext = neighboursInfo.get(np1); //recupe l'info du prochain noeud
				//final Tunnel tnext = neighboursTunnel.get(cnext);
				log.info("Send to " + tnext);
				try {
					tnext.sendCommande(c);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.info("IOException",e);
				}
				break;
		case Bully2:
			log.info("Bully2 send ele score");
			break;
		default:
			log.info("No mode ele selected");
			break;
		}
	}
	
//	/**
//	 * Fonction servant a chercher quel est notre numero d'index dans notre liste de node
//	 * @return
//	 */
//	private int SearchSelfIndex(){
//		for(int i=0; i<neighboursInfo.size(); i++){
//			if( neighboursInfo.get(i).equals(myCInfo))
//				return i;
//		}
//		return -1;
//	}
	
	private void ChoixVoisinAnneau(){
		final int sz = neighboursInfo.size();
		final int cur = 0;
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
		log.info("Connected : " + tun);
	}
	
	private void startingElection(String messageContent){
		log.info("I Start an election : " + messageContent);
		participant = true;
		voteHandle(messageContent);
	}
	
	/**
	 * Node command acceptor Handler
	 * @param comm : Command received to handle
	 * @param tun : Tunnel from which the command was received
	 */
	@Override
	protected void commandeReceiveFrom(Commande comm, Tunnel tun) {
		Commande awnserc = null; //if we want to respond to tun
		log.info( "Received : " + comm.getType().name() + " --> { " + comm.getMessageContent() + " }");
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
			case HELLO:
				awnserc= new Commande(ServerCommandeType.MESS, "HELLO !!! I am " + id );
				break;
				
			//ELE	
			case ASKELE:
				startingElection(createEleVote());
				break;
			case ELE_VOTE: 
				voteHandle(comm.getMessageContent()); 
				log.info("I have voted for " + comm.getMessageContent());
				break;
			case ELU: 
				eluHandle(comm.getMessageContent()); 
				break;
			case ST_ELE:
				voteHandle(comm.getMessageContent()); //next step of election
				break;
				
			//SERVER
			case RESTART:		
				break;
			case STATE:
				break;
			case STOP:
				break;
			case EN_TUN:
				break;				
			case ID:
				this.id = Integer.parseInt(comm.getMessageContent());
				try {
					askList();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					log.debug("IOException",e1);
				}
				break;
			case ID_NODE:
				tun.setDistId(Integer.parseInt(comm.getMessageContent()));
				this.neighboursInfo.add(tun);
				Collections.sort(this.neighboursInfo, new Comparator<Tunnel>() {

					@Override
					public int compare(Tunnel o1, Tunnel o2) {
						return (new Integer(o1.getDistId())).compareTo(o2.getDistId());
					}
				});
				ChoixVoisinAnneau();
				log.info("NeighboursInfo : " + neighboursInfo);
				
				break;
			case LIST:
				log.debug("List received: "+comm.getMessageContent());
				List<ConnexionInfo> conList = parsenList(comm.getMessageContent());
				try {
					ConnInfos(conList);
					
					log.info("NeighboursInfo : " + neighboursInfo);
					
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
				if(tun != null)
					tun.sendCommande(awnserc);
				else
					this.defaultConsoleService.print(awnserc.getMessageContent());

			} catch (IOException e) {
				log.debug("IOException", e);
			}
		}
	}
	
	private String createEleVote() {
		Long d = (new Date()).getTime();
		String vote = d + ":" + Integer.MIN_VALUE + ":" + 0;
		return vote;
	}

	@Override
	protected void broken(Tunnel tun) {
		
		log.info("remove tunnel " + tun);
		
		neighboursInfo.remove(tun);
		
		//this.neighboursTunnel.remove(tun.getcInfoDist().getHostname());
		
		ChoixVoisinAnneau();
		
				
	}
	
	private List<ConnexionInfo> parsenList(String messageContent) {
		String nodeinfos[] = messageContent.split("\\|");
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
		defaultConsoleService = new ConsoleService("Console for me");
		defaultConsoleService.addObserver(this);
			try {
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
					commandeReceiveFrom(c, null);
				}
		}else{
			super.update(obj, arg);
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
					
					//configFile.load(MasterConsole.class.getClassLoader().getResourceAsStream("nodes/hostname.properties"));
					configFile.load(new FileReader("./target/classes/nodes/hostname.properties"));
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
					//configFile.load(MasterConsole.class.getClassLoader().getResourceAsStream("nodes/hostname.properties"));
					configFile.load(new FileReader("./target/classes/nodes/hostname.properties"));
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
	

	private class WaitAndStartElection extends Service{
		
		public WaitAndStartElection(String serviceName) {
			super(serviceName);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void loopAction() {
			
			long timeout = (long) (Math.random()*10000+100);
			
			synchronized (this) {
				try {
					this.wait(timeout);
				} catch (InterruptedException e) {
					log.debug("InterruptedException",e);
				}
			}
			Date d = new Date();
			Commande c = new Commande(ServerCommandeType.ELE_VOTE,d.getTime()+":"+id+":"+score);
			startingElection(c.getMessageContent());
		}
		
	}




}
