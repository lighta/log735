package banque;

import succursale.SuccursalesInfo;

public interface IBanque {
	int AcceptConnexion();
	SuccursalesInfo GetSuccursalesInfo();
}
