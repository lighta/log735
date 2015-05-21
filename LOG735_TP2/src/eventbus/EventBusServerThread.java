package eventbus;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class EventBusServerThread extends Thread {
	private int port;
	private ServerSocket serverSocket;
	private IEventBusThread eventBus;
	
	public EventBusServerThread(int port, IEventBusThread eventBus)
	{
		this.port = port;
		this.eventBus = eventBus;
	}
	
	public void run()
	{ 
		try { 
			serverSocket = new ServerSocket(port); 
        } 
		catch (IOException e) 
        { 
			System.err.println("On ne peut pas écouter au  port: " + port + "."); 
			System.exit(1); 
        } 

		while(true) {
			Socket clientSocket = null; 
			System.out.println ("Le serveur " + port + " est en marche, Attente de la connexion.....");
	
			try { 
				clientSocket = serverSocket.accept(); 
	        } 
			catch (IOException e) 
	        { 
				System.err.println("Accept de " + port + " a échoué."); 
				System.exit(1); 
	        } 
			
			EventBusCommunicator ebc = new EventBusCommunicator(clientSocket, eventBus);
			ebc.start();
			eventBus.attachCommunicator(ebc);
		}
	}
}
