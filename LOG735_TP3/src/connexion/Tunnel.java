package connexion;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Observable;

import connexion.Commande.CommandeType;
import services.Service;
import services.Service.AlreadyStartException;
import succursale.SuccursalesInfo;

public class Tunnel extends Observable{
	private final BufferedOutputStream out;
	private final BufferedInputStream in;
	private final Socket socket;
	private final ConnexionInfo cInfoLocal;
	private final ConnexionInfo cInfoDist;
	private String name_id = "";
	
	private WaitMessageService wMessService = null;
	
	public Tunnel(ConnexionInfo s2) throws IOException {
		super();
		this.socket = new Socket(s2.getHostname(), s2.getPort());
		out = new BufferedOutputStream(socket.getOutputStream());
		in = new BufferedInputStream(socket.getInputStream());
		
		cInfoDist = new ConnexionInfo(socket.getInetAddress().getHostName(), socket.getPort());
		cInfoLocal = new ConnexionInfo(socket.getLocalAddress().getHostName(), socket.getLocalPort());
		
		askList();
	}
	
	//constructeur pour emetteur
	public Tunnel(SuccursalesInfo s1, SuccursalesInfo s2) throws IOException {
		super();
		this.socket = new Socket(s2.getHostname(), s2.getPort());
		out = new BufferedOutputStream(socket.getOutputStream());
		in = new BufferedInputStream(socket.getInputStream());
		
		cInfoDist = new ConnexionInfo(socket.getInetAddress().getHostName(), socket.getPort());
		cInfoLocal = new ConnexionInfo(socket.getLocalAddress().getHostName(), socket.getLocalPort());
		
		askTunnel(s1);
	}
	
	/**
	 * constructeur pour receiver
	 * @deprecated use instead {@link Tunnel(String,Socket)} 
	 * @param socket
	 * @throws IOException
	 */
	public Tunnel(Socket socket) throws IOException {
		super();
		this.socket = socket;
		out = new BufferedOutputStream(socket.getOutputStream());
		in = new BufferedInputStream(socket.getInputStream());
		cInfoDist = new ConnexionInfo(socket.getInetAddress().getHostName(), socket.getPort());
		cInfoLocal = new ConnexionInfo(socket.getLocalAddress().getHostName(), socket.getLocalPort());
		wMessService = new WaitMessageService(this, in);
		
		try {
			Service.startService(wMessService);
		} catch (AlreadyStartException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	
	public BufferedOutputStream getOut() {
		return out;
	}
	public BufferedInputStream getIn() {
		return in;
	}
	
	public Socket getSocket() {
		return socket;
	}	
	
	public String getNameId() {
		return name_id;
	}
	
	/**
	 * @deprecated instead of destroy let the garbage collector do the clean up with finalize() 
	 */
	@Deprecated 
	public void destroy(){ //clean up all ressource
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
		super.finalize();
	}

	private void askList() {
		sendCommande(new Commande(CommandeType.LIST,""));
	}
	
	private void askTunnel(SuccursalesInfo s1) {
		sendCommande(new Commande(CommandeType.TUN, "" + s1.getId()));
	}
	
	public void askStatus() {
		sendCommande(new Commande(CommandeType.STATE ,"Give me your status !"));
	}

	
	protected void sendCommande(Commande comm)
	{
		try {
			out.write(comm.getBytes());
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		
		return  cInfoLocal.toString() + " <==> " + cInfoDist.toString();
	}
	
	private class WaitMessageService extends Service
	{
		
		BufferedInputStream inputStream;
		
		public WaitMessageService(Tunnel tun, BufferedInputStream input) {
			super("waitMessageService for tunnel " + tun.toString() );
			this.inputStream = input;
			
		}

		@Override
		public void loopAction() {
			while(super.getCurrentState() != ServiceState.ENDING)
			{
				Commande m = Commande.ParseCommande(inputStream);
				
				if(m == null)
					try {
						this.wait(30);
					} catch (InterruptedException e) {
						Service.stopService(this);
						e.printStackTrace();	
					}
				
			}
		}
		
		
	}

	
}
