package succursale;

public class SuccursalesInfo {
	final int Id;
	final String hostname;
	final int port;
	int montant;
	
	
	
	public SuccursalesInfo(int id, String hostname, int port, int montant) {
		super();
		Id = id;
		this.hostname = hostname;
		this.port = port;
		this.montant = montant;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public int getPort() {
		return port;
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
		return  "Id : "+Id+
				"Host : "+hostname+
				"Port : "+port+
				"Montant : "+montant;	
	}
}
