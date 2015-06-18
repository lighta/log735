package banque;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import logs.Logger;
import services.Service;
import services.Service.AlreadyStartException;
import succursale.SuccursalesInfo;
import connexion.Commande;
import connexion.Commande.CommandeType;
import connexion.ConnexionInfo;
import connexion.MultiAccesPoint;
import connexion.Tunnel;

public class Banque extends MultiAccesPoint implements IBanque, IConsoleBanque {
	private HashMap<Integer, SuccursalesInfo> suc_Infos;

	private static Logger log = Logger.createLog(Banque.class);
	
	private final static String _CONSOLE_CONNECTION_HOSTNAME = "localhost";
	private final static int _CONSOLE_CONNECTION_PORT = 9100;
	
	private final static String _SUCC_CONNECTION_HOSTNAME = "0.0.0.0";
	private final static int _SUCC_CONNECTION_PORT = 9300;
	
	private int sequenceCurrentValue = 1;
	
	public Banque() {
		super();
		log.message("Creating Banque");
		
		suc_Infos = new HashMap<>();
		
		ConnexionInfo consoleConnexion = new ConnexionInfo(Banque._CONSOLE_CONNECTION_HOSTNAME,Banque._CONSOLE_CONNECTION_PORT);
		ConnexionInfo succursaleConnexion = new ConnexionInfo(Banque._SUCC_CONNECTION_HOSTNAME,Banque._SUCC_CONNECTION_PORT);

		try {
			log.message("Try to open access points");
			super.openAccesPoint("Console", consoleConnexion);
			super.openAccesPoint("Console", succursaleConnexion);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		log.message("Start Console interpreter");
//		(new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				new ConsoleInterpreter();
//
//			}
//		})).start();
	}

	@Override
	public String GetSuccursalesInfo() {

		if (suc_Infos != null)
			return FormatSuccursalesList("|");
		else
			throw new NullPointerException("null suc_info");

	}

	public String FormatSuccursalesList(String separator){
		StringBuilder sb_ = new StringBuilder();
		for(Entry<Integer,SuccursalesInfo> suc : suc_Infos.entrySet()){
			SuccursalesInfo info = suc.getValue();	
			sb_.append(info.getId()+":"+info.getHostname()+":"+9200+separator);
		}
		return sb_.toString();
	}
	
	@Override
	protected void newTunnelCreated(Tunnel tun) {
		tun.addObserver(this);
	}

	@Override
	protected void commandeReceiveFrom(Commande comm, Tunnel tun) {

		log.message("Commande receive : " + comm + " from " + tun);

		Commande c = null;
		
		switch (comm.getType()) {
		case HELLO:
			c = new Commande(CommandeType.MESS, "Welcome !!!");
			break;
		case BUG:
			System.out.println("Not supported yet !!!!!!!!!!!!");
			break;
		case CRCON:
			c = new Commande(CommandeType.CON, Banque._SUCC_CONNECTION_HOSTNAME + ":" + Banque._SUCC_CONNECTION_PORT);
			break;
		case MESS:
			System.out.println("" + comm.getMessageContent());
			break;
		case REG:

			SuccursalesInfo succ_info = new SuccursalesInfo(tun.getcInfoDist().getHostname(), 9200, Integer.parseInt(comm.getMessageContent()));
			int succ_id = GenerateSuccursalId(succ_info);
			succ_info.setId(succ_id);
			
			this.suc_Infos.put(succ_id, succ_info);	//register succ
			printToConsoles("" + succ_info.getMontant()+":"+ this.getTotalAmount());			
			c = new Commande(CommandeType.ID, "" + succ_id);	//send id to succ
			notifyAllSuccOfNewOne(succ_info);
			break;
		case STATE:
			System.out.println("Not supported yet !!!!!!!!!!!!");
			break;
		case GETLIST:
			c = new Commande(CommandeType.LIST, ""	+ GetSuccursalesInfo());
			break;		
		default:
			System.out.println("Unrecognized Command !!!!!!!!!");
		}
		
		if(c != null){
			tun.sendCommande(c);
		}
		
	}

	@Override
	public Integer getTotalAmount() {

		int amount = 0;

		for (Entry<Integer, SuccursalesInfo> succ : suc_Infos.entrySet()) {
			amount += succ.getValue().getMontant();
		}

		return amount;
	}

	public Integer GenerateSuccursalId(ConnexionInfo info) {
		
		log.message("compare " + info + " to : ");
		
		for (Entry<Integer,SuccursalesInfo> sInfo : suc_Infos.entrySet()) {
			log.message("--> " + info);
			if(sInfo.getValue().getHostname().equalsIgnoreCase(info.getHostname())
					&& sInfo.getValue().getPort() == info.getPort()
					){
				return sInfo.getKey();
			}
				
		}	
		
		return sequenceCurrentValue++;
	}

	public boolean CheckUniqueId() {
		return true;
	}

	private void notifyAllSuccOfNewOne(final SuccursalesInfo succ_info){
		
		try {
			Service.startService(new Service("notifyAllSuccOfNewOne : " + succ_info) {
				
				Commande c = null;
				
				@Override
				public void loopAction() {
					c = new Commande(CommandeType.ADDLIST, succ_info.getId()+":"+succ_info.getHostname()+":"+9200);
					for (Tunnel tun : getTunnelsbyPort(_SUCC_CONNECTION_PORT)) {
						tun.sendCommande(c);
					}
				}
			});
		} catch (AlreadyStartException e) {
			log.message("" + e.getStackTrace());
		}
		
		
	}
	
	private void printToConsoles(final String message){
		
		try {
			Service.startService(new Service("printToConsoles : " + Math.random()) {
				
				Commande c = null;
				
				@Override
				public void loopAction() {
					c = new Commande(CommandeType.HAM, message);
					for (Tunnel tun : getTunnelsbyPort(_CONSOLE_CONNECTION_PORT)) {
						tun.sendCommande(c);
					}
				}
			});
		} catch (AlreadyStartException e) {
			log.message("" + e.getStackTrace());
		}
		
		
	}
	
	@Override
	public void printSuccursale() {
		
	}

	@Override
	public void printTotalAmountSuccursale() {
		
	}

	public static void main(String[] args) {
		new Banque();
	}
}
