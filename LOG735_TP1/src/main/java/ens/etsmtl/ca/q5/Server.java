package ens.etsmtl.ca.q5;

import java.net.*;
import java.io.*;

import ens.etsmtl.ca.q5.ServDico;
import ens.etsmtl.ca.q5.ServDico.ServerDef;

public class Server {
	static int nb_req=0; //hold the number of request performed
	ServDico servs_dico;

	
	public Server() throws IOException {
		super();
		int second=5; //time to sleep before serving client (for simulate issue)
		String hostname;
		String inputLine = "";

		
		servs_dico = new ServDico();
		
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Entrez l'ip de bind du serveur");
		hostname = stdIn.readLine();
		//check ip
		
		
		System.out.println("Entrez le nb de second");
		inputLine = stdIn.readLine();
		second = Integer.parseInt(inputLine);
		
		ServerTCP serv_th = new ServerTCP(hostname,second);
		serv_th.start();
		
		while (inputLine.equalsIgnoreCase("Q")==false ) {
			System.out.println("Press Q to end server");
			inputLine = stdIn.readLine();
		} //waiting request stop
		System.out.println("Closing server");
		serv_th.stoping();
		while(serv_th.isAlive()); //waiting gracefully closed
		System.out.println("Server closed");
	}



	public class ServerTCP extends Thread {
		boolean run = true; //start/stop server variable
		Socket clientSocket = null;
		HandlerTCP clientjob;
		
		ServerSocket serverSocket = null;
		InetAddress ipAddress;
		
		String hostname;
		int port=10118;
		int nbsleep=0;
		
		public ServerTCP(String hostname, int nbsleep) {
			this.init(hostname,nbsleep);
		}
		
		public ServerTCP(String hostname)  {
			this.init(hostname,0);
		}

		public ServerTCP()  {
			this.init("127.0.0.1",0);
		}
		private void init(String hostname, int nbsleep) {
			this.hostname=hostname;
			this.nbsleep=nbsleep;	
			try {
				ipAddress= InetAddress.getByName(hostname);
			} catch (UnknownHostException e1) {
				System.err.println("On ne peut pas se binder sur : "+hostname+ " invalide");
				System.exit(1);
			}
			try {
				serverSocket = new ServerSocket(10118,0, ipAddress);
			} catch (IOException e) {
				System.err.println("On ne peut pas ecouter au  port: 10118.");
				System.exit(1);
			}
		}
		
		@Override
		public void run() {
				System.out.println("Le serveur est en marche, Attente de la connexion...");
				while (run == true) {
					try {
						clientSocket = serverSocket.accept();
					} catch (IOException e) {
						System.err.println("Accept a echoue.");
						continue;
					}
					
					
					try {
						clientjob = new HandlerTCP(clientSocket,nbsleep,port,hostname);
						clientjob.start();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(Thread.interrupted())
						run = false;
				}
		}
		
		public void stoping() {
			run = false;
			try {
				clientjob.stoping();
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	//end ServerTCP class
	
	
	public class HandlerTCP extends Thread {
		private static final int TIMEOUT_CONNECT = 3000;
		Socket clientSocket = null;
		int second=0;
		int serv_port=0;
		String serv_host;
		
		BufferedReader in;
		PrintWriter out;

		public HandlerTCP(Socket clientSocket, int second, int serv_port, String serv_host) throws IOException {
			this.clientSocket = clientSocket;
			this.second = second;
			this.serv_port = serv_port;
			this.serv_host = "";
			this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			this.out = new PrintWriter(clientSocket.getOutputStream(), true);

		}	
		
		private void reply() throws IOException, InterruptedException {
			
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				
				//simulation long traitement
				System.out.println("Serveur waiting for : " + second);
				Thread.sleep(1000*second);
				System.out.println("Sleep ended");
				//end simulation
				
				//echo + bye
				if (inputLine.equalsIgnoreCase("BYE")){
					out.println(inputLine+" : Connection closed");
					System.out.println("Serveur: Connection client closed" );
					break;
				}

				nb_req++;
				inputLine = "nb_req:"+nb_req+" "+inputLine.toUpperCase();
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
		
		public void stoping() {
			System.out.println("Stopping HandlerTCP");
			out.println("BYE");
			System.out.println("Stopped HandlerTCP");

		}
		
	}
	//end HandlerTCP class

	public static void main(String[] args) throws IOException {
		new Server();
	}
	
}
