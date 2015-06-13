package connexion;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import services.Service;
import services.Service.AlreadyStartException;

public class AccesPoint extends Observable implements Observer {
	
	
	private AcceptConnexionService acceptServ;
	private List<CreateConnexionService> createConnServices;
	
	private String name;
	
	private AccesPoint(String name){
		super();
		this.name = name;
		createConnServices = new ArrayList<>();
	}
	
	public AccesPoint(String name,ConnexionInfo localcInfo) throws UnknownHostException, IOException {
		this(name);
		acceptServ = new AcceptConnexionService(name, localcInfo.getHostname(), localcInfo.getPort());
		
	}
	
	public void finalyze(){
		Service.stopService(acceptServ);
		for (CreateConnexionService createConnexionService : createConnServices) {
			Service.stopService(createConnexionService);
		}
	}
	
	public String getName() {
		return name;
	}	
	
	public void acceptConnexion() {
		try {
			Service.startService(acceptServ);
		} catch (AlreadyStartException e) {
			e.printStackTrace();
		}

	}
	
	
	public void connectTo(ConnexionInfo cInfo) throws IOException
	{
		CreateConnexionService createConn = new CreateConnexionService(cInfo.getHostname(),cInfo.getPort());
		this.createConnServices.add(createConn);
		
		try {
			Service.startService(createConn);
		} catch (AlreadyStartException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if(o == null)
			return;
		
		if(o instanceof AcceptConnexionService){
			//AcceptConnexionService acs = (AcceptConnexionService) o;
			if(arg instanceof Socket){
				try {
					Tunnel t = new Tunnel("@tun-"+this.getName(),(Socket) arg);
					setChanged();
					notifyObservers(t);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}
	}
	
	private class AcceptConnexionService extends Service
	{
		private ServerSocket serverSocket = null;
		
		@SuppressWarnings("unused")
		public AcceptConnexionService(String hostname, int port) throws UnknownHostException, IOException {
			this(null,hostname,port);
		}
		
		public AcceptConnexionService(String name, String hostname, int port) throws UnknownHostException, IOException {
			super("AcceptConnexion" + (name == null ? "" : " for " + name) + " over "  + hostname + " on port " + port);
			InetAddress ipAddress= InetAddress.getByName(hostname);
			serverSocket = new ServerSocket(port,0, ipAddress);
		}
		
		@Override
		public void loopAction() {
			while(super.getCurrentState() != ServiceState.ENDING)
			{
				try {
					Socket clientSocket = serverSocket.accept();
					setChanged();
					notifyObservers(clientSocket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			
			}
		}
	}

	private class CreateConnexionService extends Service
	{
		private String hostname;
		private int port;
		
		public CreateConnexionService(String hostname, int port) throws UnknownHostException, IOException {
			this(null,hostname,port);
		}
		
		public CreateConnexionService(String name, String hostname, int port) throws UnknownHostException, IOException {
			super("CreateConnexion" + (name == null ? "" : " for " + name) + " over "  + hostname + " on port " + port);

		}
		
		@Override
		public void loopAction() {
			while(super.getCurrentState() != ServiceState.ENDING)
			{
				try {
					Socket clientSocket = new Socket(hostname, port);
					setChanged();
					notifyObservers(clientSocket);
					Service.stopService(this);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			
		}
	}

	

	
	
}
