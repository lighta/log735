package serverAccess;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import serverAccess.Commande.ServerCommandeType;
import service.Service;
import service.Service.AlreadyStartException;


public class Tunnel extends Observable implements Observer{
	
	private static Logger log = Logger.getLogger(Tunnel.class);
	
	private final static int SOCKET_TIMEOUT = 5000;

	public static final String BROKEN = "BROKEN";
	
	private final BufferedOutputStream out;
	private final InputStream in;
	private final Socket socket;
	private final ConnexionInfo cInfoLocal;
	private final ConnexionInfo cInfoDist;
	private String name_id = "";
	private int dist_id = 0;
	
	private WaitMessageService wMessService = null;
	private AliveService aliveService = null;
	
	/**
	 * constructeur pour receiver
	 * @deprecated use instead {@link Tunnel(String,Socket)} 
	 * @param socket
	 * @throws IOException
	 */
	public Tunnel(Socket socket) throws IOException {
		super();
		this.socket = socket;
		
		this.socket.setSoTimeout(SOCKET_TIMEOUT);
		
		log.debug("Try to retrieve socket outputStream");
		out = new BufferedOutputStream(socket.getOutputStream());
		log.debug("Try to retrieve socket inputStream");
		in = socket.getInputStream();
		cInfoDist = new ConnexionInfo(socket.getInetAddress().getHostAddress(), socket.getPort());
		cInfoLocal = new ConnexionInfo(socket.getLocalAddress().getHostAddress(), socket.getLocalPort());
		wMessService = new WaitMessageService(this, in);
		wMessService.addObserver(this);
		
		aliveService = new AliveService(this);
		aliveService.addObserver(this);
		
		
		
		try {
			log.debug("Try start service ( " + wMessService.getName() + " )");
			Service.startService(wMessService);
			log.debug("Try start service ( " + aliveService.getName() + " )");
			Service.startService(aliveService);
		} catch (AlreadyStartException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			log.debug("Already started service ( " + wMessService.getName() + " )");
		}
		
	}
	
	//constructeur pour receiver
	public Tunnel(String name_id, Socket socket) throws IOException {
		this(socket);
		this.name_id = name_id;				
	}
	
	
	public ConnexionInfo getcInfoDist() {
			return new ConnexionInfo(cInfoDist);
	}

	public ConnexionInfo getcInfoLocal() {
		return new ConnexionInfo(cInfoLocal);
	}
	
	public int getDistId() {
		return this.dist_id;
	}
	
	public void setDistId(int id){
		this.dist_id = id;
	}
	
	public String getNameId() {
		return name_id;
	}
	
	private void closeSocket() {
		try {
			if (out != null)
				out.close();
		} catch (Exception e) {
			// On ignore
		}
		try {
			if (in != null)
				in.close();
		} catch (Exception e) {
			// On ignore
		}
		try {
			if (socket != null)
				socket.close();
		} catch (Exception e) {
			// On ignore
		}
		
	}

	@Override
	protected void finalize() throws Throwable {
		closeSocket();
		super.finalize();
	}
	
	public void sendCommande(Commande comm) throws IOException
	{
		//log.message("Try to send commande : " + comm);

		try {
			out.write(comm.getBytes());
			out.flush();
			log.debug("Commande send : " + comm );	
		} catch (IOException e) {
			log.debug("Connection reset");
			closeSocket();
			throw e;
		}
		

	}
	
	@Override
	public String toString() {
		
		return  cInfoLocal.toString() + " <==> " + cInfoDist.toString();
	}
	
	@Override
	public void update(Observable o, Object arg) {
		
		if(o instanceof WaitMessageService){
			//AcceptConnexionService acs = (AcceptConnexionService) o;
			if(arg instanceof Commande){
				log.debug("Commande receive : " + arg +" notifyObservers");
				setChanged();
				notifyObservers(arg);
			}
			
		}
	}
	
	private class WaitMessageService extends Service
	{
		private Logger log = Logger.getLogger(WaitMessageService.class);
		
		InputStream inputStream;
		
		public WaitMessageService(Tunnel tun, InputStream in) {
			super("waitMessageService for tunnel " + tun.toString() );
			this.inputStream = in;
			
		}

		@Override
		public void loopAction() {
			while(super.getCurrentState() != ServiceState.ENDING)
			{
				
				//log.message("Try to parse Commande" );
				
				try {
					if(inputStream.available() > 1){
						
						final Commande c = Commande.ParseCommande(inputStream);
						if(c == null)
						{
							log.debug("null Commande" );
						}
						else
						{
							if(!c.getType().equals(ServerCommandeType.ALIVE)){
								log.debug("Notify new commande : " + c );
								setChanged();
								notifyObservers(c);							
							}
						}
					}
					
				} catch (IOException e1) {
					log.debug("Stream closed");
					closeSocket();
					Service.stopService(this);
					notifybroken();
				}catch (Exception ex) {
	               log.debug("", ex);
	               
	            }

			}
		}
		
		
		
	}

	private class AliveService extends Service
	{
		private static final long WAKEUP_TIMEOUT = SOCKET_TIMEOUT - SOCKET_TIMEOUT/3;

		private Logger log = Logger.getLogger(WaitMessageService.class);
				
		private Tunnel tun;
		
		public AliveService(Tunnel tun) {
			super("AliveService for tunnel " + tun.toString() );
			this.tun = tun;
		}

		@Override
		public void loopAction() {
			while(super.getCurrentState() != ServiceState.ENDING)
			{
				try {
					synchronized (this) {
						this.wait(WAKEUP_TIMEOUT);
					}
					this.tun.sendCommande(new Commande(ServerCommandeType.ALIVE, ""));
				} catch (InterruptedException e) {
					log.debug("InterruptedException", e);
				} catch (IOException e) {
					log.debug("Connection reset");
					Service.stopService(this);
					notifybroken();
				}
			}
		}
	}
	
	private void notifybroken() {
		setChanged();
		notifyObservers(Tunnel.BROKEN);	
	}

	
	@Override
	public boolean equals(Object obj) {
		
		if(super.equals(obj))
			return true;
		
		if(obj != null)
			return false;
		
		if(!(obj instanceof Tunnel))
			return false;
		
		Tunnel o = (Tunnel) obj;
		
		if(this.getcInfoDist().getHostname().equalsIgnoreCase(o.getcInfoDist().getHostname()))
			if(this.getcInfoDist().getPort() == o.getcInfoDist().getPort())
				return true;
		
		return false;
	}

}
