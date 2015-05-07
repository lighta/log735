package ens.etsmtl.ca.Q1;
import java.net.*; 
import java.io.*; 

public class Server { 
	public static void main(String[] args) throws IOException { 
    
		ServerSocket serverSocket = null; 

		try { 
			serverSocket = new ServerSocket(10118); 
        } 
		catch (IOException e) 
        { 
			System.err.println("On ne peut pas �couter au  port: 10118."); 
			System.exit(1); 
        } 

		Socket clientSocket = null; 
		System.out.println ("Le serveur est en marche, Attente de la connexion.....");

		try { 
			clientSocket = serverSocket.accept(); 
        } 
		catch (IOException e) 
        { 
			System.err.println("Accept a �chou�."); 
			System.exit(1); 
        } 

		System.out.println ("connexion r�ussie");
		System.out.println ("Attente de l'entr�e.....");

		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); 
		BufferedReader in = new BufferedReader(new InputStreamReader( clientSocket.getInputStream())); 

		String inputLine;

		while ((inputLine = in.readLine()) != null) 
        { 
			System.out.println ("Serveur: " + inputLine);
			inputLine = inputLine.toUpperCase();
        	out.println(inputLine);
        	if (inputLine.equals("Bye.")) 
        		break; 
        } 

		out.close(); 
		in.close(); 
		clientSocket.close(); 
		serverSocket.close(); 
	} 
} 
