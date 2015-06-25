package succursale;

import global_state.GlobalState;
import global_state.State.states;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import services.Service;
import services.Service.AlreadyStartException;
import succursale.Transfert.transfert_state;
import connexion.Commande;
import connexion.Commande.CommandeType;
import connexion.ConnexionInfo;
import connexion.Tunnel;


public class Succursale extends Thread implements ISuccursale {
	private final static String DEF_BANK_IP = "192.168.0.20";
	private final static int DEF_BANK_PORT = 9300;
	
	private ServerSocket serverSocket;
	private boolean running;
	private ScheduleTf sched_transfert;
	private ScheduleState sched_state;
	
	private int transfert_id=1;
	
	//virez sucinfo?
	private HashMap<Integer,SuccursalesInfo> suc_Infos;
	
	private List<SucHandler> clientjobs;
	private Tunnel banqueCon = null;
	private HashMap<Integer, Tunnel> consoles;
	private HashMap<Integer,Tunnel> connections;
	private HashMap<Integer,Transfert> transferts;
	
	private SuccursalesInfo infos;
	private int bank_total;
	private int globalStateIdSequence;
	Map<Integer, GlobalState> globalsStates;
	
	public Succursale(ServerSocket serverSocket, int montant) {
		if (serverSocket==null || montant < 0) {
		    throw new IllegalArgumentException(
		      String.format("Parameters can't be null: serverSocket=%s montant=%d",
		    		  serverSocket,montant));
		}
		this.serverSocket = serverSocket;
		int port = serverSocket.getLocalPort();
		String hosname = serverSocket.getInetAddress().getHostName();
		infos = new SuccursalesInfo(hosname,port,montant);
		
		suc_Infos = new HashMap<Integer,SuccursalesInfo>();
		connections = new HashMap<Integer,Tunnel>();
		consoles = new HashMap<Integer,Tunnel>();
		transferts = new HashMap<Integer,Transfert>();
		clientjobs = new ArrayList<>();
		this.globalStateIdSequence = 0;
		globalsStates = new HashMap<>();
	}
	
	public String toString(){
		return
			"Host:"+infos.getHostname()
			+"Port:"+infos.getPort()
			+"Montant: "+infos.getMontant();
	}
	
	public String getStatus(){
		return toString();
	}
	
	private void DoRandomTF(Random rand){
		final long wait = rand.nextInt((10-5)*1000)+5000; //wait entre 5 et 10s
		SuccursalesInfo suc = null;
		int suc_indice=-1;
		final int limit=1000;
		int i=0;
		final int ssize = this.suc_Infos.size();
		
		//System.out.println("ScheduleTf waiting for wait="+wait+"ms");
		try {
			Thread.sleep( wait ); 
			i=0;
			if(ssize > 1) {
				//System.out.println("ScheduleTf searching valid target ssize="+ssize);
				while((suc == null 
					|| suc.getId() == this.infos.getId())
					&& (i++ < limit) ) 
				{ //redo
					suc_indice= rand.nextInt( ssize )+1;
					suc = this.suc_Infos.get(suc_indice);  //we need some othersuccursale for this 
				}
				if(i < limit){
					final int montant = rand.nextInt(100-20)+20; //entre 20 et 100
					System.out.println("ScheduleTf trying to send to suc_id="+suc_indice+" montant="+montant );
					SendTransfert(suc,montant);
				}
			}
			else {
				System.out.println("No succursale available for TF");
			}
		} catch (InterruptedException e) {
			System.err.println("Interrupted ScheduleTf sleep timer");
			//e.printStackTrace();
		}
	}
	
	private synchronized void getSystemStatus(){
		this.globalStateIdSequence++;
		//globalStateIdSequence %= 1000;
		//globalStateIdSequence += infos.Id * 1000;
		if(connections.size() >= 1) {
			System.out.println("starting gstate id="+this.globalStateIdSequence);
			
			final GlobalState gState = new GlobalState(this.globalStateIdSequence,this.infos.getId());
			gState.getMyState().setMontant(infos.getMontant());
			
			for (Entry<Integer, Tunnel> conn : connections.entrySet()) {
				int id_con = conn.getKey();
				if(id_con > 0){ //verif que c,est une suc
					gState.addState(new global_state.State(gState.getIdGlobalState(), id_con));
				}
			}
		
			globalsStates.put(gState.getIdGlobalState(), gState);
			broadcastStateStart(gState);
		}
	}
	
	public synchronized boolean rmList(int id){
		if(suc_Infos.containsKey(id)==false)
			return false; //not  found
		suc_Infos.remove(id);
		return true;
	}
	public synchronized boolean addList(SuccursalesInfo infos){
		if(suc_Infos.containsKey(infos.getId()))
			return false; //duplicate key
		suc_Infos.put(infos.getId(), infos);
		return true;
	}
	public synchronized void updateList(HashMap<Integer,SuccursalesInfo> infos){
		suc_Infos = infos;
	}
	
	public boolean AuthorizeTransfert(Transfert tf){
		if(tf.s1.getId() == tf.s2.getId()) //same node
			return false;
		//check in list
		if(!(suc_Infos.containsValue(tf.s1) || suc_Infos.containsValue(tf.s2)))
			return false;
		if(tf.montant < 0) //verifie montant positif
			return false;
		//check si on a le montant
		return tf.montant < this.infos.montant;
	}
	
	public boolean SendTransfert(SuccursalesInfo s2, int montant){
		if(s2 == null) return false;
		
		Tunnel tun = connections.get(s2.getId());
		if(tun == null) return false;
			
		transfert_id++;
		transfert_id%=1000;
		transfert_id+=infos.getId()*1000; // range of if per suc
		
		try {
			Transfert tf = new Transfert(this.infos,s2,montant,tun,transfert_id);
			if(!AuthorizeTransfert(tf)){ //transfert non authorise
				//tf.destroy();
				return false;
			}
			
			infos.addMontant(-montant);
			transferts.put(transfert_id, tf);
			tf.send();
			tf.start();
		}
		catch (IllegalArgumentException e) {
			System.out.println("Fail to create transfert");
			return false;
		}
		
		return true;
	}
	
	
	public synchronized boolean connectToBanque(ConnexionInfo banqueinfo){
		try {
			Tunnel tun = new Tunnel(this.infos,banqueinfo);
			banqueCon = tun;
			SucHandler job = new SucHandler(tun);
			clientjobs.add(job);
			job.start();
		} catch (IOException e) {
			//e.printStackTrace();
			return false;
		}
		return true;	
	}
	
	public synchronized int connectTo(SuccursalesInfo info){
		if(connections.get(info.getId()) != null)
			return -1; //already connected
		if(info.getId() == this.infos.getId()) //on refuse la connection a nous meme !
			return -2;
		if( serverSocket.getLocalPort() == info.getPort() 
				&& serverSocket.getInetAddress().getHostName().equalsIgnoreCase(info.getHostname()) )
			return -2; //ourself again
		
		try {
			System.out.println("Trying to connect to "+info);
			Tunnel tun = new Tunnel(this.infos,info);
			connections.put(info.getId(), tun);
			SucHandler job = new SucHandler(info.getId());
			clientjobs.add(job);
			job.start();
		} catch (IOException e) {
			//e.printStackTrace();
			return -3;
		}
		return 0;
	}
	
	public boolean connectToOthers(){
		for(Entry<Integer,SuccursalesInfo> suc : suc_Infos.entrySet()){
			SuccursalesInfo info = suc.getValue();		
			if(info.getId() != this.infos.getId()){ //on evite le cas d'erreur dela connexion a nous meme
				final int res = connectTo(info);
				switch(res){
					case 0: break; //sucess
					case -1: System.err.println("Already connected to :"+info+" skipping..."); break;
					case -2: System.err.println("Can't connect to self skipping..."); break;
					case -3: System.err.println("Fail to connect to :"+info); break;
				}
			}
		}
		return true;
	}
	
	
	public String FormatSuccursalesList(String separator){
		StringBuilder sb_ = new StringBuilder();
		for(Entry<Integer,SuccursalesInfo> suc : suc_Infos.entrySet()){
			SuccursalesInfo info = suc.getValue();		
			sb_.append(info.getId()+":"+info.getHostname()+":"+info.getPort()+separator);
		}
		return sb_.toString();
	}
	
	public synchronized void RegisterSuccursalesList(final String list){
		//System.out.println("Entering RegisterSuccursalesList list="+list);
		String[] sucs = list.split("\\|");
		for (int i=0; i<sucs.length; i++) {
			String sucstring=sucs[i];
			if(sucstring == null || sucstring.isEmpty())
				break;
			//sucstring = sucstring.substring(0, sucstring.length()-1); //remove last
			System.out.println("sucstring="+sucstring);
			final String[] part = sucstring.split(":");
			System.out.println("size sucs="+sucs.length+" size sucstring="+part.length);
			
			final int id = Integer.parseInt(part[0]);
			final String host = part[1];
			final int port = Integer.parseInt(part[2]);
			
			
			SuccursalesInfo suc = new SuccursalesInfo(host, port, -1);
			suc.setId(id);
			addList(suc);
		}
	}
	
	public void setId(int id){
		infos.setId(id);
		this.globalStateIdSequence = infos.getId() * 1000;
	}
	
	private synchronized void ScheduleTransfert(){
		if(sched_transfert == null){ //create
			sched_transfert = new ScheduleTf();
			sched_transfert.start();
		}
	}
	
	private synchronized void ScheduleGetSystemStatus(){
		if(sched_state == null){ //create
			sched_state = new ScheduleState();
			sched_state.start();
		}
		//else { //maj de la list
		//	sched_state.setSuccs(this.suc_Infos);
		//}
	}
	
	@Override
	public void interrupt() {	
		super.interrupt();
		this.running=false;
		try {
			serverSocket.close();
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
	
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		Socket clientSocket = null;
		
		
		this.running=true;
		while(this.running){
			try {
				System.out.println("Succursale listening on host="+infos.getHostname()+" and port="+infos.getPort());
				clientSocket = serverSocket.accept();
				Tunnel base = new Tunnel("SucServer",clientSocket);
				SucHandler job = new SucHandler(base);
				clientjobs.add(job);
				job.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Accept fail on Succursale");
				//e.printStackTrace();
			}
		}
	}

	
	private void broadcastStateStart(GlobalState gState) {
		final Commande comm = new Commande(CommandeType.STATE_START, "" + gState.getIdGlobalState()+ ":" + gState.getIdInitiator() + ":" + infos.getId());
		for (Entry<Integer, Tunnel> conn : connections.entrySet()) {
			if(conn.getKey() >= 0){
				final Tunnel tun = conn.getValue();
				(new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(5*1000);
							tun.sendCommande(comm);
						} catch (InterruptedException e) {
							System.out.println("getSystemStatus timer fail");
							//e.printStackTrace();
						}
					}
				})).start();
			}
		}		
	}

	private class SucHandler extends Thread {
		//Socket clientSocket;
		Tunnel tunnel;
		BufferedReader in;
		boolean running;
		int id_suc=-3;
		
		public SucHandler(int id_suc) throws IOException {
			this.id_suc = id_suc;
			init(connections.get(id_suc));
		}
		
		public SucHandler(Tunnel tun) throws IOException {
			init(tun);		
		}
		
		private void init(Tunnel tun) throws IOException{
			if(tun==null){
				throw new IllegalArgumentException(
					      String.format("No tunnel found for SucHandler id_suc%s", id_suc));
			}
			tunnel = tun;
			in = new BufferedReader(new InputStreamReader( tunnel.getIn() ) );
		}

		@Override
		public void interrupt() {	
			super.interrupt();
			this.running=false;
			try {
				in.close();
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			super.run();
			
			this.running=true;
			
			while(this.running){	
				try {
					String rec;
					while ((rec = in.readLine()) != null){
						System.out.println("rec="+rec);
						final String part[] = rec.split("#");
			
						final String cmd = part[0];
						if(part.length < 1){
							System.out.println("Malformed packet received");
							System.out.println("rec="+rec);
							continue;
						}
						if(cmd.startsWith("!")==false
							//&& rec.endsWith("!")
						){
							System.out.println("Invalid cmd received");
							System.out.println("rec="+rec);
							continue;
						}
						
						switch(cmd){
							case "!HELLO":{ //received a create connexion request
								// save tunnel
								//Tunnel tun = new Tunnel("CONSOLE",clientSocket);
								consoles.put(-2, tunnel);
								tunnel.sendMsg("Welcome ! to Succursale");
								break;
							}
							case "!CON":{ //received a create connexion request
								String host = DEF_BANK_IP;
								int port = DEF_BANK_PORT;
								if(part.length > 1){
									final String msg = part[1];
									final String msgpart[] = msg.split(":");
									host = msgpart[0];
									port = Integer.parseInt(msgpart[1]);
								}
								
								System.out.println("Creating connection to banque");
								ConnexionInfo banqueConn = new ConnexionInfo(host, port);
								boolean res = connectToBanque(banqueConn);
								tunnel.sendCONACK(infos.getId(),res);
								break;
							}
							case "!TUN":{ //received a tunnel connexion request
								final int id = Integer.parseInt(part[1]);
								System.out.println("id="+id);
								Tunnel tun = new Tunnel("SUC"+id,tunnel.getSocket());
								connections.put(id, tun);
								break;
							}
							case "!ID":{ //received an ID assignation
								if(tunnel == banqueCon){ //must come from bank
									int id = Integer.parseInt(part[1]);
									System.out.println("id="+id);
									setId(id);
									banqueCon.askList(); //demande de la list
								}
								break;
							}
							case "!ADDLIST":{ //received a list from bank
								if(tunnel == banqueCon){ //must come from bank
									final String msg = part[1];
									final String msgpart[] = msg.split(":");
									
									final int id = Integer.parseInt(msgpart[0]);
									final String host = msgpart[1];
									final int port = Integer.parseInt(msgpart[2]);
									SuccursalesInfo suc = new SuccursalesInfo(host, port, -1); //remove montant from succursale info
									suc.setId(id);
									addList(suc);
									connectToOthers(); //check si deja connecter
									
									ScheduleTransfert();
									//ScheduleGetSystemStatus();
	
									Tunnel tun = consoles.get(-2); //recupere la console
									if(tun != null){
										String list = FormatSuccursalesList("\n");
										tun.sendNList(list);
									}
									
								}
								break;
							}
							case "!LIST":{ //received a list from bank
								if(tunnel == banqueCon){ //must come from bank
									RegisterSuccursalesList(part[1]); //enregistrement des autres succursale dans notre list
									connectToOthers(); //check si deja connecter
									Tunnel tun = consoles.get(-2); //recupere la console
									if(tun != null){
										String list = FormatSuccursalesList("\n");
										tun.sendList(list);
									}
								}
								break;
							}
							case "!TFCON":{
								final String msg = part[1];
								final String msgpart[] = msg.split(":");
								
								final int id = Integer.parseInt(msgpart[0]);
								final int montant = Integer.parseInt(msgpart[1]);
								
								SuccursalesInfo dest_suc = suc_Infos.get(id);
								boolean res = SendTransfert(dest_suc,montant);
								
								tunnel.sendTFACK(id, res);
								break;
							}
							case "!TFSUC":{
								final String msg = part[1];
								final String msgpart[] = msg.split(":");
								
								final int id = Integer.parseInt(msgpart[0]);
								final int montant = Integer.parseInt(msgpart[1]);	
								final String state = msgpart[2];
								final int transfert_id = Integer.parseInt(msgpart[3]);
								
								if( state.compareTo(transfert_state.ACK.toString()) == 0){
									transferts.remove(transfert_id);  //retrait du transfert de notre liste
									Tunnel con = consoles.get(-2); //recupere la console
									if(con != null){
										con.sendTFDONE(id, true);
									}
									System.out.println("TF DONE Suc_Info, new Montant="+infos.getMontant());
								}
								else {
									Tunnel tun = connections.get(id);
									if(tun==null) break;
									infos.addMontant(montant);
									try {
										Transfert tf = new Transfert(infos, suc_Infos.get(id), montant, tun, transfert_id);
										global_state.State succState;
										for (Entry<Integer,GlobalState> gState : globalsStates.entrySet()){
											if((succState = gState.getValue().getState(id)) != null){
												if(succState.getCurrentState() == states.REC){
													gState.getValue().addIncomingTransf(tf);
												}
											}
										}
										tf.ack();
										tf.start();
									}
									catch (IllegalArgumentException e) {
										System.out.println("Fail to create Transfert : "+e.getMessage());
									}
								}
								break;
							}
							case "!SETM":{
								int montant = Integer.parseInt(part[1]);
								if(montant < 1){
									System.out.println("Invalide montant="+montant+ " must be strict positive");
									break;
								}
								infos.setMontant(montant);
								tunnel.sendSETMACK(infos.getMontant(),true);
								break;
							}
							case "!MESS":{
								System.out.println("Received mess="+part[1]);
								break;
							}
							case "!TOTAL":{
								bank_total = Integer.parseInt(part[1]);
								final String bnk_total = "bank_total="+bank_total+"\n";
								System.out.print(bnk_total);
								Tunnel tun = consoles.get(-2); //recupere la console
								if(tun != null){
									tun.sendMsg(bnk_total.toString());
								}
								break;
							}
							case "!SHOWLIST":{
								String list = FormatSuccursalesList("\n");
								System.out.println("List={\n"+list+"}");
								tunnel.sendList(list);
								break;
							}
							case "!SHOWSTATE":{
								System.out.print("Suc_Info={\n"+infos+"}\n"+"bank_total="+bank_total+"\n");
								if(banqueCon!=null)
									banqueCon.askTotal();
								tunnel.sendMsg(infos.toString());
								break;
							}
							case "!GLOBST":{
								if(banqueCon!=null)
									banqueCon.askTotal();
								getSystemStatus();
								break;
							}
							case "!STATE_START":{
								final String msg = part[1];
								final String msgpart[] = msg.split(":");
								final int idGlobalState = Integer.parseInt(msgpart[0]);
								final int idSuccInit = Integer.parseInt(msgpart[1]);
								final int idSuccSender = Integer.parseInt(msgpart[2]);
								GlobalState gState;
								global_state.State succState;
								
								if((gState = globalsStates.get(idGlobalState)) != null)
								{
									if((succState = gState.getState(idSuccSender)) != null)
									{
										succState.setCurrentState(states.WAIT);
									}
								}
								else
								{
									//first capture
									gState = new GlobalState(idGlobalState,idSuccInit);
									gState.getMyState().setMontant(infos.getMontant());
									
									for (Entry<Integer, Tunnel> conn : connections.entrySet()) {
										int id_con = conn.getKey();
										if(id_con > 0){ //verif que c,est une suc
											gState.addState(new global_state.State(gState.getIdGlobalState(), id_con));
										}
									}
									global_state.State st = gState.getState(idSuccSender);
									st.setCurrentState(states.WAIT);								
									globalsStates.put(gState.getIdGlobalState(), gState);	
								}
								
								//Envoi d'une reponse FIN ou Other Start
								if(idSuccInit != infos.Id) {
									System.out.println("sending ST_FIN  or Start to other");
									if(gState.getRemainingState() == 0){
										final Tunnel tun = connections.get(gState.getIdInitiator());
										final Commande comm = new Commande(CommandeType.STATE_FIN, "" + gState.getIdGlobalState()+ ":" + infos.getId() + ":" + gState.getMyState().getMontant());
										(new Thread(new Runnable() {
											@Override
											public void run() {
												try {
													Thread.sleep(5*1000);
													tun.sendCommande(comm);
												} catch (InterruptedException e) {
													System.out.println("getSystemStatus timer fail");
													//e.printStackTrace();
												}
											}
										})).start();
										globalsStates.remove(idGlobalState);
									}
									else {
										broadcastStateStart(gState);
									}
								}
								break;
							}
							case "!STATE_FIN":{
								final String msg = part[1];
								final String msgpart[] = msg.split(":");
								final int idGlobalState = Integer.parseInt(msgpart[0]);
								final int idSuccSender = Integer.parseInt(msgpart[1]);
								final int montant = Integer.parseInt(msgpart[2]);
								
								GlobalState gState;
								global_state.State succState; //marker de MrTim
								if((gState = globalsStates.get(idGlobalState)) != null)
								{
									
									if((succState = gState.getState(idSuccSender)) != null)
									{
										succState.setCurrentState(states.FIN);
										global_state.State st = gState.getState(idSuccSender);
										st.setMontant(montant); //maj du montant pour la suc
									}
									
									if(gState.getRemainingState() == 0){
										int sumSnapshot = gState.getMyState().getMontant();
										
										String c = ""
											+ "Succursale d'origine de la capture : #" + gState.getIdInitiator() + "\n\r"
											+ "Succursale #"+ gState.getIdInitiator() +" : "+ gState.getMyState().getMontant()+"$" + "\n\r";
											
										for (global_state.State state : gState) {
											final int smontant = state.getMontant();
											sumSnapshot += smontant;
											c += "Succursale #"+state.getIdState()+" : "+ smontant + "$" + "\n\r";
										}
											
										for (Transfert t : gState.getMyIncomingTransf()) {
											final int smontant = t.montant;
											sumSnapshot += smontant;
											c += "Canal S" + t.s1.getId()+"-S"+ t.s2.getId() +" : "+t.montant + "$" + "\n\r";
										}											
												
										c += "Somme connue par la Banque : " + bank_total + "$" + "\n\r"
											+ "Somme detectee par la capture : " + sumSnapshot + "$" + "\n\r"
											+ "ETAT GLOBAL "+(sumSnapshot == bank_total?"COHERENT":"INCOHERENT" + "\n\r");
										
										Commande comm = new Commande(CommandeType.SHOWSTATE,c);
										Tunnel tun = consoles.get(-2); //recupere la console
										if(tun != null){
											tun.sendCommande(comm);
										}
										globalsStates.remove(idGlobalState);
									}
								}
								else
								{
									System.err.println("Global State Unknow");
								}
								break;
							}
							case "!BUG":{
								int montant = Integer.parseInt(part[1]);
								infos.setMontant(montant);
								tunnel.sendSETMACK(infos.getMontant(),true);
								break;
							}
							default:{
								System.out.println("Unsupported cmd received cmd="+cmd+ "rec="+rec);
							}		
						}
					}
				} catch (IOException e) {
					System.err.print("BufferStream as an error, Please reconnect to ="+tunnel.getSocket());
					if(tunnel == banqueCon)
						banqueCon = null;
					else if (consoles.get(-2) == tunnel){
						consoles.remove(-2);
					}
					else connections.remove(id_suc);
					break; //stop handler
					//e.printStackTrace();
				}
			}
			clientjobs.remove(this); //remove ourself	
		}
	}
	
	private class ScheduleState extends Thread {
		boolean running;

		@Override
		public void interrupt() {
			super.interrupt();
			this.running=false;
		}
		
		@Override
		public void run() {
			super.run();
			this.running=true;
			Random rand = new Random();
				
			while(this.running){
				try {
					final int wait = rand.nextInt((30-10)*1000)+10000;
					Thread.sleep( wait ); //wait entre 10 et 30s
					getSystemStatus();
				} catch (InterruptedException e) {
					System.err.println("ScheduleState timer error");
					//e.printStackTrace();
				}
			}
			System.err.println("ScheduleState ending loop");
		}
		
		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			System.out.println("Dealloc ScheduleState");
		}
	}
	
	private class ScheduleTf extends Thread {
		boolean running;

		public ScheduleTf() {
			//auto
		}
		
		@Override
		public void interrupt() {	
			super.interrupt();
			this.running=false;
		}
		
		
		
		@Override
		public void run() {
			super.run();
			this.running=true;
			Random rand = new Random();
				
			while(this.running){
				synchronized (suc_Infos) {
					DoRandomTF(rand) ;
				} 
			}
			System.err.println("ScheduleTf ending loop");
		}
		
		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			System.out.println("Dealloc ScheduleTf");
		}
	}
}
