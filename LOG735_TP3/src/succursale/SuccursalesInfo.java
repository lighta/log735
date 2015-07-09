package succursale;
import server_access.ConnexionInfo;

public class SuccursalesInfo extends ConnexionInfo {
	int Id = 0; //undefined
	int montant;
	
	public SuccursalesInfo(String hostname, int port, int montant) {
		super(hostname,port);
		this.montant = montant;
	}
	
	public int getMontant() {
		return montant;
	}
	
	public void setMontant(int montant) {
		this.montant = montant;
	}
	
	public void addMontant(int montant) {
		this.montant += montant;
	}

	
	public int getId() {
		return Id;
	}
	
	public void setId(int id) {
		Id = id;
	}
	
	
	@Override
	public String toString() {
		return  
				"Id : "+Id+"\n"+
				"Host : "+hostname+"\n"+
				"Port : "+port+"\n"+
				"Montant : "+montant+"\n";	
	}
}
