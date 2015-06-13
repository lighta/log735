package banque;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import succursale.SuccursalesInfo;
import connexion.Commande;
import connexion.ConnexionInfo;
import connexion.MultiAccesPoint;
import connexion.Tunnel;

public class Banque extends MultiAccesPoint implements IBanque, IConsoleBanque {
	private HashMap<Integer,SuccursalesInfo> suc_Infos;
	
	
	
	public Banque() {
		super();
		ConnexionInfo consoleConnexion = new ConnexionInfo("localhost",9100);
		ConnexionInfo succursaleConnexion = new ConnexionInfo("localhost",9300);
		
		try {
			super.openAccesPoint("Console", consoleConnexion);
			super.openAccesPoint("Console", succursaleConnexion);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	

	@SuppressWarnings("unchecked")
	@Override
	public Map<Integer,SuccursalesInfo> GetSuccursalesInfo() {
		
		if(suc_Infos != null)
			return  (Map<Integer, SuccursalesInfo>) suc_Infos.clone();
		else
			throw new NullPointerException("null suc_info");
				
	}
	
	@Override
	protected void newTunnelCreated(Tunnel tun){
		
	}
	
	
	@Override
	protected void commandeReceiveFrom(Commande comm,Tunnel tun) {
		
	}
	
	
	@Override
	public Integer getTotalAmount() {
		
		int amount = 0;
		
		for (Entry<Integer, SuccursalesInfo> succ : suc_Infos.entrySet()) {
			amount += succ.getValue().getMontant();
		}
		
		return amount;
	}
	
	public Integer GenerateSuccursalId(){
		return null;
	}
	
	public boolean CheckUniqueId(){
		return false;
	}

	@Override
	public void printSuccursale() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void printTotalAmountSuccursale() {
		// TODO Auto-generated method stub
		
	}

	public static void main(String[] args) {
		new Banque();
	}
}
