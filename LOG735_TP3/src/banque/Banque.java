package banque;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import succursale.SuccursalesInfo;

public class Banque implements IBanque, IConsoleBanque {
	private HashMap<Integer,SuccursalesInfo> suc_Infos;

	@Override
	public int AcceptConnexion() {
		// TODO Auto-generated method stub
		return 0;
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
