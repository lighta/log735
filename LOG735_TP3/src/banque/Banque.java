package banque;

import java.util.HashMap;
import java.util.Map;

import succursale.SuccursalesInfo;
import connexion.Message;
import connexion.MultiAccesPoint;
import connexion.Tunnel;

public class Banque extends MultiAccesPoint implements IBanque, IConsoleBanque {
	private HashMap<Integer,SuccursalesInfo> suc_Infos;
	
	
	
	public Banque() {
		// TODO Auto-generated constructor stub
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
	protected void messageReceiveFrom(Tunnel tun,Message mess) {
		
	}


	@Override
	public Integer getTotalAmount() {
		// TODO Auto-generated method stub
		return null;
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

	
}
