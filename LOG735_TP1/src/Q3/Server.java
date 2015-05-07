package Q3;

import java.net.*;
import java.io.*;

public class Server {

	static boolean run = true;

	public static class HandlerTCP extends Thread {
		Socket clientSocket = null;

		public HandlerTCP(Socket clientSocket) throws IOException {
			this.clientSocket = clientSocket;
		}

		private void reply() throws IOException {
			PrintWriter out;
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				inputLine = inputLine.toUpperCase();
				//echo + bye
				if (inputLine.equalsIgnoreCase("Bye")){
					out.println(inputLine+" : Connection closed");
					System.out.println("Serveur: Connection client closed" );
					break;
				}
				//echo standard
				System.out.println("Serveur: " + inputLine);
				out.println(inputLine);
			}
			
			out.close();
			in.close();
		}

		@Override
		public void run() {
			System.out.println("connexion réussie");
			System.out.println("Attente de l'entrée.....");
			try {
				reply();
			} catch (IOException e) {
				System.err.println("Reply failed");
				// e.printStackTrace();
			}
			try {
				clientSocket.close();
			} catch (IOException e) {
				System.err.println("Closed failed");
				// e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		
		Socket clientSocket = null;
		ServerSocket serverSocket = null;
		InetAddress ipAddress;
		String inputLine = "";
		
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Entrez l'ip de bind du serveur");
		inputLine = stdIn.readLine();
		//check ip
		ipAddress= InetAddress.getByName(inputLine);
		try {
			
			
			serverSocket = new ServerSocket(10118,0, ipAddress);
			
		} catch (IOException e) {
			System.err.println("On ne peut pas écouter au  port: 10118.");
			System.exit(1);
		}
		finally {
			System.out.println("Le serveur est en marche, Attente de la connexion...");
			
	
			while (run == true) {
	
				try {
					clientSocket = serverSocket.accept();
				} catch (IOException e) {
					System.err.println("Accept a échoué.");
					System.exit(1);
				}
	
				HandlerTCP clientjob = new HandlerTCP(clientSocket);
				clientjob.start();
	
			}
			serverSocket.close();
		}

	}
}
