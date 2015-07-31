package serverAccess;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;





import org.apache.log4j.Logger;

import service.Service;
import service.Service.AlreadyStartException;

public class AccesPoint extends Observable implements Observer {
	
	private static Logger log = Logger.getLogger(AccesPoint.class);
	
	private AcceptConnexionService acceptServ;
	private List<CreateConnexionService> createConnServices;
	
	private String name;
	private ConnexionInfo localcInfo;
	
	private AccesPoint(String name){
		super();
		this.name = name;
		createConnServices = new ArrayList<>();
	}
	
	public AccesPoint(String name,ConnexionInfo localcInfo){
		this(name);
		this.localcInfo = localcInfo;
		log.debug("Acces point created");
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
	
	public void acceptConnexion() throws UnknownHostException, IOException {
		
		log.debug("create service AcceptConnexionService");
		acceptServ = new AcceptConnexionService(name, localcInfo.getHostname(), localcInfo.getPort());
		acceptServ.addObserver(this);
		try {
			log.info("Try to start service ( " + acceptServ.getName() + " )");
			Service.startService(acceptServ);
		} catch (AlreadyStartException e) {
			log.debug("", e);
		}

	}
	
	public Tunnel connectTo(ConnexionInfo cInfo) throws IOException
	{
		log.debug("Try to connect to " + cInfo);
		Socket clientSocket = new Socket(cInfo.getHostname(), cInfo.getPort());
		log.debug("Connected to " + cInfo);
		Tunnel t = new Tunnel("@tun-"+Math.random(),(Socket) clientSocket);
		log.info("Tunnel communication created ( " + t + " )");
		return t;
	}
	
	public void connectToWithoutWaiting(ConnexionInfo cInfo) throws IOException
	{
		log.debug("called : connectToWithoutWaiting");
		CreateConnexionService createConn = new CreateConnexionService(cInfo.getHostname(),cInfo.getPort());
		createConn.addObserver(this);
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
			if(arg instanceof Tunnel){
				log.debug("new tunnel created "+ arg);
				setChanged();
				notifyObservers(arg);

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
			log.info(this.getName());
			while(super.getCurrentState() != ServiceState.ENDING)
			{
				try {
					Socket clientSocket = serverSocket.accept();
					Tunnel t = new Tunnel("@tun-"+Math.random(),clientSocket);
					setChanged();
					notifyObservers(t);
				} catch (IOException e) {
					log.debug("Fail to connect ",e);
					//e.printStackTrace();
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
			this.hostname=hostname;
			this.port=port;
		}
		
		@Override
		public void loopAction() {
			while(super.getCurrentState() != ServiceState.ENDING)
			{
				try {
					Socket clientSocket = new Socket(hostname, port);
					Tunnel t = new Tunnel("@tun-"+Math.random(),(Socket) clientSocket);
					setChanged();
					notifyObservers(t);
					Service.stopService(this);
				} catch (IOException e) {
					log.debug("Tryed con to hostname="+hostname+" port="+port,e);
					System.exit(1002);
				}
			}
			
			
		}
	}

	

	
	
}
