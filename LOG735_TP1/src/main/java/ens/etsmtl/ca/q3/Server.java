package ens.etsmtl.ca.q3;

import java.net.*;
import java.io.*;

import ens.etsmtl.ca.q3.ServDico;
import ens.etsmtl.ca.q3.ServDico.ServerDef;

public class Server {
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
					
					HandlerTCP clientjob;
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
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	//end ServerTCP class
	
	
	public class HandlerTCP extends Thread {
		Socket clientSocket = null;
		int second=0;
		int serv_port=0;
		String serv_host;

		public HandlerTCP(Socket clientSocket, int second, int serv_port, String serv_host) throws IOException {
			this.clientSocket = clientSocket;
			this.second = second;
			this.serv_port = serv_port;
			this.serv_host = "";
		}	
		
		private void reply() throws IOException, InterruptedException {
			PrintWriter out;
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			

			String inputLine = null;
			while (true) {
				while(inputLine == null || inputLine.isEmpty())
					inputLine = in.readLine();
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
				inputLine = inputLine.toUpperCase();
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
	//end HandlerTCP class

	
	/**
	 * 
	 * @param args[0] programm inputStream (null for standard input)
	 * @param args[1] programm outputStream (null for standard output)
	 * @param args[2] programm errOutputStream (null for standard output)
	 * @throws IOException 
	 * 
	 * @throws IOException
	 */
	private static void initIO(String[] streams) throws IOException
	{
		BufferedInputStream input;
		PrintStream output;
		PrintStream errOutput;
		
		//set programm inputStream
		if (streams.length >= 1 && streams[0] != null){
			FileInputStream fis = new FileInputStream (streams [0]);
			input = new BufferedInputStream(fis);
		}
		else{
			input = new BufferedInputStream(System.in);
		}
		
		
		//set programm outputStream
		if (streams.length >= 2 && streams[1] != null){
			FileOutputStream fos = new FileOutputStream (streams [1]);
			output = new PrintStream(fos);
		}
		else{
			output = new PrintStream(System.out);
		}
		
		//set programm outputStream
		if (streams.length >= 3 && streams[2] != null){
			FileOutputStream fos = new FileOutputStream (streams [2]);
			errOutput = new PrintStream(fos);
		}
		else{
			errOutput = new PrintStream(System.err);
		}
		
		System.setIn(input);
		System.setOut(output);
		System.setErr(errOutput);
		
	}
	
	public static void main(String[] args) throws IOException {
				
		initIO(args);
		
		new Server();
		
	}
	
}
