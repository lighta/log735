package ens.etsmtl.ca.q5;

import java.net.*;
import java.io.*;

public class Server {
	static boolean run = true; //start/stop server variable
	static int second=5; //time to sleep before serving client (for simulate issue)
	static int nb_req=0; //hold the number of request performed

	public static class HandlerTCP extends Thread {
		Socket clientSocket = null;

		public HandlerTCP(Socket clientSocket) throws IOException {
			this.clientSocket = clientSocket;
		}

		private void reply() throws IOException, InterruptedException {
			PrintWriter out;
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				nb_req++;
				System.out.println("Serveur waiting for : " + second);
				Thread.sleep(1000*second);
				System.out.println("Sleep ended");
				inputLine = "nb_req:"+nb_req+" "+inputLine.toUpperCase();
				//echo + bye
				if (inputLine.equals("BYE")){
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
			System.out.println("connexion reussie");
			System.out.println("Attente de l'entree.....");
			try {
				reply();
			} catch (IOException e) {
				System.err.println("Reply failed");
				// e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		
		System.out.println("Entrez le nb de second");
		inputLine = stdIn.readLine();
		second = Integer.parseInt(inputLine);
		
		try {
			serverSocket = new ServerSocket(10118,0, ipAddress);
		} catch (IOException e) {
			System.err.println("On ne peut pas ecouter au  port: 10118.");
			System.exit(1);
		}
		finally {
			System.out.println("Le serveur est en marche, Attente de la connexion...");
			
	
			while (run == true) {
	
				try {
					clientSocket = serverSocket.accept();
				} catch (IOException e) {
					System.err.println("Accept a echoue.");
					System.exit(1);
				}
	
				HandlerTCP clientjob = new HandlerTCP(clientSocket);
				clientjob.start();
	
			}
			serverSocket.close();
		}

	}
}