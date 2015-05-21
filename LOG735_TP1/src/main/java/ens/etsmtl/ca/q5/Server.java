/******************************************************
 Cours : LOG735
 Session : Été 2015
 Groupe : 01
 Projet : Laboratoire 1
 Étudiants : 
 	Max Moreau
 	Charly Simon
 Code(s) perm. : 
	MORM30038905
 	SIMC28069108
 Date création : 7/05/2015
 Date dern. modif. : 16/05/2015
******************************************************
Server echo Multithread TCP 
******************************************************/

package ens.etsmtl.ca.q5;
import java.net.*;
import java.io.*;

/**
 * Objet server general servant a definir la creation d'un serveur TCP specific
 * @author lighta
 */
public class Server {
	static int nb_req=0;	//variable pour compter le nombre de requete servit

	/**
	 * Constructor
	 * Demandea l'usager un ensemble d'information pour la creation du serveur
	 * @throws IOException
	 */
	public Server() throws IOException {
		super();
		int second=5; //time to sleep before serving client (for simulate issue)
		String hostname;
		String inputLine = "";
		
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


	/**
	 * Serveur Echo Multithread
	 * Ecoute et repond au connexion des clients
	 * @author lighta
	 */
	public class ServerTCP extends Thread {
		boolean run = true; 						//start/stop server variable
		Socket clientSocket = null;					//socket des clients
		HandlerTCP clientjob;						//thread des client
		
		ServerSocket serverSocket = null;			//endpoint du servuer
		InetAddress ipAddress;						//adr d'ecoute du serveur
		
		String hostname;							//adr d'ecoute du serveur (forme string)
		static final String DEF_HOST="127.0.0.1";   //adr d'ecoute par defaut
		
		int port=10118;								//port d'ecoute
		int nbsleep=0;								//tps d'attente
		
		/**
		 * Constructeur
		 * Cree un serveur en ecoute a l'adresse hostname avec un temps d'attente nbsleep
		 * @param hostname : Adresse ou le serveur doit ecouter
		 * @param nbsleep : Temps d'attente en second
		 */
		public ServerTCP(String hostname, int nbsleep) {
			this.hostname=hostname;
			this.nbsleep=nbsleep;	
			try {
				ipAddress= InetAddress.getByName(hostname);
			} catch (UnknownHostException e1) {
				System.err.println("On ne peut pas se binder sur : "+hostname+ " invalide");
				System.exit(1);
			}
			try {
				serverSocket = new ServerSocket(port,0, ipAddress);
			} catch (IOException e) {
				System.err.println("On ne peut pas ecouter au  port: "+port);
				System.exit(1);
			}
		}
		
		/**
		 * Constructeur
		 * Cree un serveur en ecoute a l'adresse hostname sans temps d'attente
		 * @param hostname : Adresse ou le serveur doit ecouter
		 */
		public ServerTCP(String hostname)  {
			this(hostname,0);
		}

		/**
		 * Constructeur
		 * Cree un serveur en ecoute a l'adresse DEF_HOST et sans temps d'attente
		 */
		public ServerTCP()  {
			this(DEF_HOST,0);
		}
		
		
		/**
		 * Methode servant a rendre le serveur en mode actif
		 * Ecoute infiniment en attente d'une connection puis transmet a HandlerTCP
		 */
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
		
		/**
		 * Fonction pour arreter l'ecoute infinie du serveur.
		 * Marque la fin de la boucle 
		 * puis force une exeption pour quitter les IO blocants.
		 */
		public void stoping() {
			run = false;
			try {
			//	clientjob.stoping();
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	//end ServerTCP class
	
	/**
	 * Class de traitement du serveur pour chaque connection client
	 * (Ici un simple wait + echo)
	 * @author lighta
	 */
	public class HandlerTCP extends Thread {
		Socket clientSocket = null;			//socket de connection au client
		int second=0;						//nb de seconded'attente avant reponse
		int serv_port=0;					//port du serveur
		String serv_host;					//adr du serveur

		/**
		 * Constructeur
		 * Creer un objet de reponse pour le serveur qui sera envoyer au client du socket
		 * @param clientSocket : Socket du client (pour repondre)
		 * @param second : Temps d'attente avant reponse
		 * @param serv_port : Port du serveur lie
		 * @param serv_host : Adresse du serveur lie
		 * @throws IOException
		 */
		public HandlerTCP(Socket clientSocket, int second, int serv_port, String serv_host) throws IOException {
			this.clientSocket = clientSocket;
			this.second = second;
			this.serv_port = serv_port;
			this.serv_host = "";
		}	
		
		/**
		 * Fonction de reponse, traitement propre sans notions des containers
		 * @throws IOException
		 * @throws InterruptedException
		 */
		private void reply() throws IOException, InterruptedException {
			PrintWriter out;
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			String inputLine = null;
			while (true) {
				while(inputLine == null || inputLine.isEmpty()) //attente blocante pour fichier
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

				nb_req++;
				inputLine = "nb_req:"+nb_req+" "+inputLine.toUpperCase();
				//echo standard
				System.out.println("Serveur: " + inputLine);
				out.println(inputLine);
				
				inputLine = null; //raz pour fichier
			}	
			out.close();
			in.close();
		}

		/**
		 * Debut de traitement de requete.
		 * Container pour gestions des sockets et autres IO
		 */
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
	 * Fonction servant a definir les stream de defaut pour les entree et sortie standard
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
