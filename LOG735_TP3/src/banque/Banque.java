package banque;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import logs.Logger;
import succursale.SuccursalesInfo;
import connexion.Commande;
import connexion.ConnexionInfo;
import connexion.MultiAccesPoint;
import connexion.Tunnel;
import connexion.Commande.CommandeType;

public class Banque extends MultiAccesPoint implements IBanque, IConsoleBanque {
	private HashMap<Integer, SuccursalesInfo> suc_Infos;

	private static Logger log = Logger.createLog(Banque.class);

	private int sequenceCurrentValue = 1;
	
	public Banque() {
		super();
		log.message("Creating Banque");
		
		suc_Infos = new HashMap<>();
		
		ConnexionInfo consoleConnexion = new ConnexionInfo("localhost", 9100);
		ConnexionInfo succursaleConnexion = new ConnexionInfo("localhost", 9300);

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

	@SuppressWarnings("unchecked")
	@Override
	public Map<Integer, SuccursalesInfo> GetSuccursalesInfo() {

		if (suc_Infos != null)
			return (Map<Integer, SuccursalesInfo>) suc_Infos.clone();
		else
			throw new NullPointerException("null suc_info");

	}

	@Override
	protected void newTunnelCreated(Tunnel tun) {
		tun.addObserver(this);
	}

	@Override
	protected void commandeReceiveFrom(Commande comm, Tunnel tun) {

		log.message("Commande receive : " + comm + " from " + tun);

		if (CommandeType.TUN == comm.getType()) {

		} else if (CommandeType.ID == comm.getType()) {

			Commande c = new Commande(CommandeType.ID, ""
					+ GenerateSuccursalId());
			tun.sendCommande(c);

		} else if (CommandeType.STATE == comm.getType()) {

		} else if (CommandeType.LIST == comm.getType()) {

			Commande c = new Commande(CommandeType.LIST, ""
					+ GetSuccursalesInfo());
			tun.sendCommande(c);

		} else if (CommandeType.MESS == comm.getType()) {

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

	public Integer GenerateSuccursalId() {
		return sequenceCurrentValue++;
	}

	public boolean CheckUniqueId() {
		return true;
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
