package succursale;
import connexion.ConnexionInfo;

public class SuccursalesInfo extends ConnexionInfo {
	final int Id;
	int montant;
	
	public SuccursalesInfo(int id, String hostname, int port, int montant) {
		super(hostname,port);
		Id = id;
		this.montant = montant;
	}
	
	public int getMontant() {
		return montant;
	}
	
	public void setMontant(int montant) {
		this.montant = montant;
	}

	
	public int getId() {
		return Id;
	}
	
	
	@Override
	public String toString() {
		return  
				//"Id : "+Id+
				"Host : "+hostname+
				"Port : "+port+
				"Montant : "+montant;	
	}
}
