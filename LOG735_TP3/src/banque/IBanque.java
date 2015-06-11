package banque;

import java.util.Map;

import succursale.SuccursalesInfo;

public interface IBanque {
	
	
	int AcceptConnexion();
	Map<Integer,SuccursalesInfo> GetSuccursalesInfo();
	
	
}
