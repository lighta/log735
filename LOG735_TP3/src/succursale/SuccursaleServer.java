package succursale;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class SuccursaleServer {
	public static final String DEF_HOST = "0.0.0.0"; 
	public static final int DEF_PORT = 9200;
	public static final int DEF_MONTANT = 1000; //useless
	
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
		String host = DEF_HOST;
		int port = DEF_PORT;
		int montant = DEF_MONTANT;
		
		if(args.length > 2){ // host port montant
			host = args[0];
			port = Integer.parseInt(args[1]);
			montant = Integer.parseInt(args[2]);
		}			
		SuccursaleServer suc_srv = new SuccursaleServer(host, port, montant);	
	}
}
