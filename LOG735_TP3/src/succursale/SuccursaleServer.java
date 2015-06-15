package succursale;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class SuccursaleServer {
	private ServerSocket serverSocket;
	private Succursale sucursale;
	
	public SuccursaleServer(String hostname, int port, int montant) {
		InetAddress ipAddress;
		String inputLine = "";
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			ipAddress= InetAddress.getByName(hostname);
			serverSocket = new ServerSocket(port,0, ipAddress);
			System.out.println("Starting succursale");
			sucursale = new Succursale(serverSocket,montant);
			sucursale.start();
		} catch (UnknownHostException e1) {
			System.err.println("On ne peut pas se binder sur : "+hostname+ " invalide");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("On ne peut pas ecouter au  port: "+port);
			System.exit(1);
		}
		
		while (inputLine.equalsIgnoreCase("Q")==false ) {
			System.out.println("Press Q to end server");
			try {
				inputLine = stdIn.readLine();
			} catch (IOException e) {
				//e.printStackTrace();
			}
		} //waiting request stop
		System.out.println("Closing server");
		stoping();
		while(serverSocket.isClosed()==false); //waiting gracefully closed
		System.out.println("Server closed");
	}
	
	public void stoping() {
		try {
			sucursale.stop();	//FIXME
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {				
		SuccursaleServer suc_srv = new SuccursaleServer("localhost", 2000, 1000);	
	}
}
