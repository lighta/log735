package succursale;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import connexion.Tunnel;


public class Succursale extends Thread implements ISuccursale {
	ServerSocket serverSocket;
	
	//virez sucinfo?
	private HashMap<Integer,SuccursalesInfo> suc_Infos;
	
	List<SucHandler> clientjobs;
	
	private HashMap<Integer,Tunnel> connections;
	private List<Transfert> transferts;
	private int montant;
	private int Id;
	
	public Succursale(ServerSocket serverSocket, int montant) {
		if (serverSocket==null || montant < 0) {
		    throw new IllegalArgumentException(
		      String.format("Parameters can't be null: serverSocket=%s montant=%d",
		    		  serverSocket,montant));
		}
		
		suc_Infos = new HashMap<Integer,SuccursalesInfo>();
		connections = new HashMap<Integer,Tunnel>();
		transferts = new ArrayList<>();
		clientjobs = new ArrayList<>();
	}
	
	public String toString(){
		return
			"Host:"+suc_Infos.get(Id).hostname
			+"Port:"+suc_Infos.get(Id).port
			+"Montant: "+montant;
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
		return montant < this.montant;
	}
	
	public boolean SendTransfert(SuccursalesInfo s1, SuccursalesInfo s2, int montant){
		Transfert tf = new Transfert(s1,s2,montant);
		if(AuthorizeTransfert(tf)){
			transferts.add(tf);
			tf.send();
			return false;
		}
		return true;
	}
	
	
	public boolean connectTo(SuccursalesInfo info){
		if(info.Id == Id) //on refuse la connection a nous meme !
			return false;
		try {
			Tunnel tun = new Tunnel(info);
			connections.put(info.Id, tun);
		} catch (IOException e) {
			//e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean connectToAll(){
		for(Entry<Integer,SuccursalesInfo> suc : suc_Infos.entrySet()){
			SuccursalesInfo info = suc.getValue();
			if(info.Id != Id){ //on evite le cas d'erreur dela connexion a nous meme
				if(connectTo(info)==false){
					System.err.println("Connection fail for suc:"+info);
					//return false;
				}
			}
		}
		return true;
	}
	
	
	public int ScheduleTransfert(){
		return 0;
	}
	
	public int ScheduleGetSystemStatus(){
		return 0;
	}
	
	public class SucHandler extends Thread {
		Socket clientSocket;
		public SucHandler(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}
		
		@Override
		public void run() {
			super.run();
			try {
				BufferedInputStream in = new BufferedInputStream(clientSocket.getInputStream());
				String rec = in.toString();
				System.out.println("rec="+rec);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		Socket clientSocket = null;
		
		while(true){
			try {
				clientSocket = serverSocket.accept();
				SucHandler job = new SucHandler(clientSocket);
				job.start();
				clientjobs.add(job);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
