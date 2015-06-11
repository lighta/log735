package succursale;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class SuccursaleServer {
	private ServerSocket serverSocket;
	private Succursale sucursale;
	
	public SuccursaleServer(String hostname, int port) {
		InetAddress ipAddress;
		
		try {
			ipAddress= InetAddress.getByName(hostname);
			serverSocket = new ServerSocket(port,0, ipAddress);
			sucursale = new Succursale(serverSocket,1245);
		} catch (UnknownHostException e1) {
			System.err.println("On ne peut pas se binder sur : "+hostname+ " invalide");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("On ne peut pas ecouter au  port: "+port);
			System.exit(1);
		}
	}
}
