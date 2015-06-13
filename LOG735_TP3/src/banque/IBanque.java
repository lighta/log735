package banque;

import java.util.Map;
import succursale.SuccursalesInfo;

public interface IBanque {
	
	Map<Integer,SuccursalesInfo> GetSuccursalesInfo();
	
}
