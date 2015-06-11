package banque;

public class ConnexionInfo {
	
	final String hostname;
	final int port;
	
	
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
