package connexion;

public class ConnexionInfo {
	
	protected final String hostname;
	protected final int port;
	
	
	public ConnexionInfo(String hostname, int port) {
		super();
		this.hostname = hostname;
		this.port = port;
	}	
	
	public String getHostname() {
		return hostname;
	}
	
	public int getPort() {
		return port;
	}
	
}
