package connexion;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

import logs.Logger;
import connexion.Commande.CommandeType;
import services.Service;
import services.Service.AlreadyStartException;
import succursale.SuccursalesInfo;
import succursale.Transfert.transfert_state;

public class Tunnel extends Observable implements Observer{
	
	private static Logger log = Logger.createLog(Tunnel.class);
	
	private final BufferedOutputStream out;
	private final InputStream in;
	private final Socket socket;
	private final ConnexionInfo cInfoLocal;
	private final ConnexionInfo cInfoDist;
	private String name_id = "";
	
	private WaitMessageService wMessService = null;
	
	//connection entre succursale et banque
	public Tunnel(ConnexionInfo s1,ConnexionInfo s2) throws IOException {
		super();
		this.socket = new Socket(s2.getHostname(), s2.getPort());
		out = new BufferedOutputStream(socket.getOutputStream());
		in = new BufferedInputStream(socket.getInputStream());
		
		cInfoDist = new ConnexionInfo(socket.getInetAddress().getHostAddress(), socket.getPort());
		cInfoLocal = new ConnexionInfo(socket.getLocalAddress().getHostAddress(), socket.getLocalPort());
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
		log.message("Try to retrieve socket outputStream");
		out = new BufferedOutputStream(socket.getOutputStream());
		log.message("Try to retrieve socket inputStream");
		in = socket.getInputStream();
		cInfoDist = new ConnexionInfo(socket.getInetAddress().getHostAddress(), socket.getPort());
		cInfoLocal = new ConnexionInfo(socket.getLocalAddress().getHostAddress(), socket.getLocalPort());
		wMessService = new WaitMessageService(this, in);
		wMessService.addObserver(this);
		try {
			log.message("Try start service ( " + wMessService.getName() + " )");
			Service.startService(wMessService);
		} catch (AlreadyStartException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			log.message("Already started service ( " + wMessService.getName() + " )");
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
	public InputStream getIn() {
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

	public void sendMsg(String msg) {
		sendCommande(new Commande(CommandeType.MESS,msg));
	}
	
	public void sendList(String list) {
		sendCommande(new Commande(CommandeType.LIST,list));
	}
	
	public void sendNList(String list) {
		sendCommande(new Commande(CommandeType.NLIST,list));
	}
	
	public void sendCONACK(int id, boolean res) {
		sendCommande(new Commande(CommandeType.CONACK,""+id+" "+(res?"Success":"Fail")));
	}
	
	public void sendBUGACK(boolean res) {
		sendCommande(new Commande(CommandeType.BUGACK,""+(res?"Success":"Fail")));
	}
	
	public void sendTFACK(int id, boolean res) {
		sendCommande(new Commande(CommandeType.TFACK,id+":"+(res?"Success":"Fail")));
	}
	
	public void sendTFDONE(int id, boolean res) {
		sendCommande(new Commande(CommandeType.TFDONE,id+":"+(res?"Success":"Fail")));
	}
	
	public void sendSETMACK(int montant, boolean res) {
		sendCommande(new Commande(CommandeType.SETMACK,(res?"Success":"Fail")+"\n New Montant="+montant));
	}
	
	public void askList() {
		sendCommande(new Commande(CommandeType.GETLIST,""));
	}
	
	public void askTransfert(int id, int montant, transfert_state ack, int transfert_id) {
		sendCommande(new Commande(CommandeType.TFSUC, "" + id+":"+montant+":"+ack+":"+transfert_id ));
	}
	
	public void askRegister(int montant) {
		sendCommande(new Commande(CommandeType.REG, "" + montant ));
	}
	
	public void askTunnel(int id) {
		sendCommande(new Commande(CommandeType.TUN, "" +id));
	}
	
	public void askTotal() {
		sendCommande(new Commande(CommandeType.GETTOTAL, "" ));
	}
	
	public void askStatus() {
		sendCommande(new Commande(CommandeType.STATE ,"Give me your status !"));
	}

	
	public void sendCommande(Commande comm)
	{
		//log.message("Try to send commande : " + comm);
		try {
			out.write(comm.getBytes());
			out.flush();
			log.message("Commande send : " + comm );	
		} catch (IOException e) {
			e.printStackTrace();
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
					log.message("Commande receive : " + arg +" notifyObservers");
					setChanged();
					notifyObservers(arg);
			}
			
		}
	}
	
	private class WaitMessageService extends Service
	{
		private Logger log = Logger.createLog(WaitMessageService.class);
		
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
						Commande m = Commande.ParseCommande(inputStream);
						if(m == null)
						{
							log.message("null Commande" );
						}
						else
						{
							log.message("Notify new commande : " + m );
							setChanged();
							notifyObservers(m);
							
						}
					}
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		}
		
		
	}

	
}
