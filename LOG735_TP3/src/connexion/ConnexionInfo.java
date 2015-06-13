package connexion;

public class ConnexionInfo {
	
	protected final String hostname;
	protected final int port;
	
	
	public ConnexionInfo(String hostname, int port) {
		super();
		this.hostname = hostname;
		this.port = port;
	}	
	
	protected ConnexionInfo(ConnexionInfo connexionInfo) {
		this.hostname = connexionInfo.hostname;
		this.port = connexionInfo.port;
	}

	public String getHostname() {
		return hostname;
	}
	
	public int getPort() {
		return port;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		
		if(obj == null)
			return false;
		
		if(!(obj instanceof ConnexionInfo))
			return false;
		
		ConnexionInfo cobj = (ConnexionInfo) obj;
		
		if(this.hostname == null || cobj.hostname == null)
			return false;
		
		return this.hostname.equals(cobj.hostname) && this.port == cobj.port;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new ConnexionInfo(this);
	}
	
	@Override
	public String toString() {
		return this.hostname + ":" + this.port;
	}
}
