package succursale;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import succursale.Transfert.transfert_state;
import connexion.ConnexionInfo;
import connexion.Tunnel;


public class Succursale extends Thread implements ISuccursale {
	private ServerSocket serverSocket;
	private boolean running;
	private ScheduleTf sched_transfert;
	private ScheduleState sched_state;
	
	private int transfert_id=1;
	
	//virez sucinfo?
	private HashMap<Integer,SuccursalesInfo> suc_Infos;
	
	private List<SucHandler> clientjobs;
	private HashMap<Integer,Tunnel> connections;
	private HashMap<Integer,Transfert> transferts;
	
	private SuccursalesInfo infos;
	private int bank_total;
	
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
		transferts = new HashMap<Integer,Transfert>();
		clientjobs = new ArrayList<>();
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
	
	public String getSystemStatus(){
		String sysstatus = getStatus();
		for(Entry<Integer,Tunnel> con : connections.entrySet()){
			Tunnel tun = con.getValue();
			tun.askStatus();
		}
		return sysstatus;
	}
	
	public boolean rmList(int id){
		if(suc_Infos.containsKey(id)==false)
			return false; //not  found
		suc_Infos.remove(id);
		return true;
	}
	public boolean addList(SuccursalesInfo infos){
		if(suc_Infos.containsKey(infos.getId()))
			return false; //duplicate key
		suc_Infos.put(infos.getId(), infos);
		return true;
	}
	public void updateList(HashMap<Integer,SuccursalesInfo> infos){
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
		Tunnel tun = connections.get(s2.getId());
		transfert_id++;
		transfert_id%=1000;
		transfert_id+=infos.getId()*1000; // range of if per suc
		
		Transfert tf = new Transfert(this.infos,s2,montant,tun,transfert_id);
		if(!AuthorizeTransfert(tf)){ //transfert non authorise
			//tf.destroy();
			return false;
		}	
		infos.addMontant(-montant);
		transferts.put(transfert_id, tf);
		tf.send();
		tf.start();
		return true;
	}
	
	
	public boolean connectToBanque(ConnexionInfo banqueinfo){
		try {
			Tunnel tun = new Tunnel(this.infos,banqueinfo);
			SucHandler job = new SucHandler(tun.getSocket());
			clientjobs.add(job);
			job.start();
			connections.put(-1, tun); //banque is -1
		} catch (IOException e) {
			//e.printStackTrace();
			return false;
		}
		return true;	
	}
	
	public int connectTo(SuccursalesInfo info){
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
			
			SucHandler job = new SucHandler(tun.getSocket());
			clientjobs.add(job);
			job.start();
			
			connections.put(info.getId(), tun);
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
	
	public void RegisterSuccursalesList(final String list){
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
			suc_Infos.put(id, suc);
		}
	}
	
	public void setId(int id){
		infos.setId(id);
	}
	
	private void ScheduleTransfert(){
		sched_transfert = new ScheduleTf();
		sched_transfert.start();
	}
	
	private void ScheduleGetSystemStatus(){
		sched_state = new ScheduleState();
		sched_state.start();
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
		
		ScheduleTransfert();
		//ScheduleGetSystemStatus();
		
		while(this.running){
			try {
				System.out.println("Succursale listening on host="+infos.getHostname()+" and port="+infos.getPort());
				clientSocket = serverSocket.accept();
				
				SucHandler job = new SucHandler(clientSocket);
				clientjobs.add(job);
				job.start();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
	private class SucHandler extends Thread {
		Socket clientSocket;
		BufferedReader in;
		boolean running;
		
		public SucHandler(Socket clientSocket) throws IOException {
			this.clientSocket = clientSocket;
			in = new BufferedReader(new InputStreamReader( clientSocket.getInputStream())); 
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
								Tunnel tun = new Tunnel("CONSOLE",clientSocket);
								connections.put(-2, tun);
								tun.sendMsg("Welcome !");
								break;
							}
							case "!CON":{ //received a create connexion request
								final String msg = part[1];
								final String msgpart[] = msg.split(":");
								final String host = msgpart[0];
								int port = Integer.parseInt(msgpart[1]);
								
								System.out.println("Creating connection to banque");
								ConnexionInfo banqueCon = new ConnexionInfo(host, port);
								boolean res = connectToBanque(banqueCon);
								Tunnel tun = connections.get(-2); //recupere la console
								if(tun != null){
									tun.sendCONACK(infos.getId());
								}
								break;
							}
							case "!TUN":{ //received a tunnel connexion request
								final int id = Integer.parseInt(part[1]);
								System.out.println("id="+id);
								Tunnel tun = new Tunnel("SUC"+id,clientSocket);
								connections.put(id, tun);
								break;
							}
							case "!ID":{ //received an ID assignation
								int id = Integer.parseInt(part[1]);
								System.out.println("id="+id);
								setId(id);
								Tunnel tun = connections.get(-1); //recupere le tunnel de la banque
								tun.askList(); //demande de la list
								break;
							}
							case "!ADDLIST":{ //received a list from bank
								final String msg = part[1];
								final String msgpart[] = msg.split(":");
								
								final int id = Integer.parseInt(msgpart[0]);
								final String host = msgpart[1];
								final int port = Integer.parseInt(msgpart[2]);
								SuccursalesInfo suc = new SuccursalesInfo(host, port, -1); //remove montant from succursale info
								suc.setId(id);
								suc_Infos.put(id, suc);
								connectToOthers(); //check si deja connecter
								Tunnel tun = connections.get(-2); //recupere la console
								if(tun != null){
									String list = FormatSuccursalesList("\n");
									tun.sendNList(list);
								}
								break;
							}
							case "!LIST":{ //received a list from bank
								RegisterSuccursalesList(part[1]); //enregistrement des autres succursale dans notre list
								connectToOthers(); //check si deja connecter
								Tunnel tun = connections.get(-2); //recupere la console
								if(tun != null){
									String list = FormatSuccursalesList("\n");
									tun.sendList(list);
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
								
								Tunnel con = connections.get(-2); //recupere la console
								if(con != null){
									con.sendTFACK(id, res);
								}
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
									Tunnel con = connections.get(-2); //recupere la console
									if(con != null){
										con.sendTFDONE(id, true);
									}
									System.out.println("TF DONE Suc_Info="+infos);
								}
								else {
									Tunnel tun = connections.get(id);
									infos.addMontant(montant);
									Transfert tf = new Transfert(infos, suc_Infos.get(id), montant, tun, transfert_id);
									tf.ack();
									tf.start();
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
								Tunnel con = connections.get(-2); //recupere la console
								if(con != null){
									con.sendSETMACK(true);
								}
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
								Tunnel tun = connections.get(-2); //recupere la console
								if(tun != null){
									tun.sendMsg(bnk_total.toString());
								}
								break;
							}
							case "!SHOWSTATE":{
								System.out.print("Suc_Info={\n"+infos+"}\n"+"bank_total="+bank_total+"\n");
								Tunnel tun = connections.get(-2); //recupere la console
								if(tun != null){
									tun.sendMsg(infos.toString());
								}
								break;
							}
							case "!SHOWLIST":{
								String list = FormatSuccursalesList("\n");
								System.out.println("List={\n"+list+"}");
								Tunnel tun = connections.get(-2); //recupere la console
								if(tun != null){
									tun.sendList(list);
								}
								break;
							}
							case "!BUG": //TODO
							default:{
								System.out.println("Unsupported cmd received cmd="+cmd+ "rec="+rec);
							}		
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			clientjobs.remove(this); //remove ourself	
		}
	}
	
	private class ScheduleState extends Thread {
		boolean running;
		
		public ScheduleState() {
			// TODO Auto-generated constructor stub
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
				try {
					final int wait = rand.nextInt((30-10)*1000)+10000;
					Thread.sleep( wait ); //wait entre 10 et 30s
					getSystemStatus();
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}
			}
		}
	}
	
	private class ScheduleTf extends Thread {
		boolean running;
		
		public ScheduleTf() {
			// TODO Auto-generated constructor stub
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
					
				final long wait = rand.nextInt((10-5)*1000)+5000; //wait entre 5 et 10s
				SuccursalesInfo suc = null;
				int suc_indice=-1;
				final int ssize = suc_Infos.size();
				
				System.out.println("ScheduleTf waiting for wait="+wait+"ms");
				try {
					Thread.sleep( wait ); 
					if(ssize > 1) {
						while(suc == null || suc.getId() == infos.getId()) { //redo
							suc_indice= rand.nextInt( ssize-1 )+1;
							suc = suc_Infos.get(suc_indice);  //we need some othersuccursale for this 
						}
						final int montant = rand.nextInt(100-20)+20; //entre 20 et 100
						System.out.println("ScheduleTf sending to suc_id="+suc_indice+" montant="+montant );
						SendTransfert(suc_Infos.get(suc_indice),montant);
					}
					else {
						System.out.println("No succursale available for TF");
					}
				} catch (InterruptedException e) {
					System.err.println("Interrupted ScheduleTf sleep timer");
					//e.printStackTrace();
				}
			}
		}
	}
}
