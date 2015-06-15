package succursale;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import connexion.ConnexionInfo;
import connexion.Tunnel;


public class Succursale extends Thread implements ISuccursale {
	private ServerSocket serverSocket;
	private boolean running;
	private ScheduleTf sched_transfert;
	private ScheduleState sched_state;
	
	//virez sucinfo?
	private HashMap<Integer,SuccursalesInfo> suc_Infos;
	
	private List<SucHandler> clientjobs;
	private HashMap<Integer,Tunnel> connections;
	private List<Transfert> transferts;
	
	private SuccursalesInfo infos;
	

	
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
		transferts = new ArrayList<>();
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
		//check in list
		if(!(suc_Infos.containsValue(tf.s1) || suc_Infos.containsValue(tf.s2)))
			return false;
		//check si on a le montant
		return tf.montant < this.infos.montant;
	}
	
	public boolean SendTransfert(SuccursalesInfo s2, int montant){
		Transfert tf = new Transfert(this.infos,s2,montant);
		if(AuthorizeTransfert(tf)){
			transferts.add(tf);
			tf.send();
			return false;
		}
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
		return running;	
	}
	
	public boolean connectTo(SuccursalesInfo info){
		if(info.getId() == this.infos.getId()) //on refuse la connection a nous meme !
			return false;
		try {
			Tunnel tun = new Tunnel(this.infos,info);
			
			SucHandler job = new SucHandler(tun.getSocket());
			clientjobs.add(job);
			job.start();
			
			connections.put(info.getId(), tun);
		} catch (IOException e) {
			//e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean connectToAll(){
		for(Entry<Integer,SuccursalesInfo> suc : suc_Infos.entrySet()){
			SuccursalesInfo info = suc.getValue();
			if(info.getId() != this.infos.getId()){ //on evite le cas d'erreur dela connexion a nous meme
				if(connectTo(info)==false){
					System.err.println("Connection fail for suc:"+info);
					//return false;
				}
			}
		}
		return true;
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
		
		//ScheduleTransfert();
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
						String part[] = rec.split("#");
						
						if(part.length < 2){
							System.out.println("Malformed packet received");
							continue;
						}
						if(part[0].compareTo("CON")==0){
							//received a create connexion request
							String host = part[1];
							int port = Integer.parseInt(part[2]);
							ConnexionInfo banqueCon = new ConnexionInfo(host, port);
							connectToBanque(banqueCon);
						}
						if(part[0].compareTo("TUN")==0){ 	
							//received a tunnel connexion request
							int id = Integer.parseInt(part[1]);
							System.out.println("id="+id);
							Tunnel tun = new Tunnel(clientSocket);
							connections.put(id, tun);
						}
						if(part[0].compareTo("ID")==0){
							//received an ID assignation
							int id = Integer.parseInt(part[1]);
							System.out.println("id="+id);
							setId(id);
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
				
			while(this.running){
				double rand = Math.random();
				try {
					Thread.sleep( (long) (10000*(rand+2)) ); //wait entre 10 et 30s
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
					
			while(this.running){
				double rand = Math.random();
				try {
					Thread.sleep( (long) (5000*(rand+1)) ); //wait entre 5 et 10s
					if(suc_Infos.size()>0){ //we need some othersuccursale for this
						int i = (int)(suc_Infos.size() * rand); //take a rnd indice
						int montant = (int) ((int) 20 * (1+ 4*(rand) )); //entre 20 et 100
						SendTransfert(suc_Infos.get(i),montant);
					}
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}
			}
		}
	}
}
