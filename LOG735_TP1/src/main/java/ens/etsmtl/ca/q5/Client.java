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
Client avance, switchant de serveur apres un certain Timeout
******************************************************/

package ens.etsmtl.ca.q5;
import java.io.*;
import java.net.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ens.etsmtl.ca.q5.ServDico.ServerDef;



public class Client {
	private Socket echoSocket = null;			//Socket de connection au serveur
	private PrintWriter out = null;				//Writter pour l'envoi au serveur
	private BufferedReader in;					//BufferStream pour les entree provenant du serveur
	private int current = -1;					//index d'iteration des serveurs
	private ServDico servs_dico;				//dictionnaire definissant l'ensemble des serveurs
	
	//enumeration des etats possible d'une reponse
	private enum state_echo {
		END,  		//fin de connection par server
		NEXT, 		//prochaine demande du client
		TIMEOUT; 	//timeout atteint, faire strat
	};
	
	final int TIMEOUT_REPONSE = 3*1000; 		// tmp de timeout pour la reponse du serveur
	final int TIMEOUT_CONNECT = 3000; 			// tmp de timeout pour connection au serveur
	private ScheduledExecutorService executor;	//scheduler pour les fonction temporiser

	/**
	 * Fonction servant a itere dans la liste des Serveur definit.
	 * Retourne une definition de serveur offrant le service
	 * @return
	 */
	private ServerDef getNext_host() {
		current++;
		if (current >= servs_dico.servers_dico.size() ) {
			System.err.println("Plus de serveur disponible pour switch");
			System.exit(1);
		}
		return servs_dico.servers_dico.get(current);
	}

	/**
	 * Fonction d'initialisation du socket et autre stream lie lors d'une connection
	 * @param hostname : Adr du serveur a se connecter
	 * @param port : Port du serveur a se connecter
	 * @param switch_host : Quitter la connection active ?
	 * @return true=sucess, false=erreur survenue
	 */
	private boolean init_sock(String hostname, Integer port, boolean switch_host) {
		if(switch_host){
			if (echoSocket != null){
				try {
					echoSocket.close();
				} catch (IOException e) {
					System.err.println("Couldn't close socket");
				}
			}	
		}
		echoSocket = attempt_to_connect(hostname, port);
		if (echoSocket == null) {
			System.out.println("Aucun serveur dispo");
			return false;
		//	System.exit(2);
		}
		try {
			out = new PrintWriter(echoSocket.getOutputStream(), true);
		} catch (IOException e) {
			System.err.println("Couldn't attach to outSocket");
			return false;
		//	e.printStackTrace();
		}
		try {
			in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
		} catch (IOException e) {
			System.err.println("Couldn't attach to outSocket");
			return false;
		//	e.printStackTrace();
		}
		
		return true;
	}
	
	/**
	 * Fonction iterative essayant de se connecter a un serveur (offrant le service).
	 * Utilisera l'hostname et port fournit en parametre comme essai de base 
	 * puis iterera sur la liste du dictionnaire.
	 * @param hostname : Adr d'un serveur
	 * @param port : Port d'un serveur
	 * @return Une connection active a un serveur ou exit du prog
	 */
	private Socket attempt_to_connect(String hostname, Integer port) {
		Socket tmp_Socket = null;
		String serverHostname = "";
		int server_port = 10118;
		ServerDef hostserv;
		
		System.out.println("current="+current);
		if (hostname != null) {
			serverHostname = hostname;
			if (port != null) { //on ne prend pas le port si on a pas de hostname associer
				server_port = port;
			}
		} else {
			hostserv = getNext_host();
			serverHostname = hostserv.host_name;
			server_port = hostserv.port;
		}
		
		while (true) {
			if(echoSocket != null){ //check if already used
				if(echoSocket.getInetAddress().getHostAddress().compareToIgnoreCase(serverHostname)==0){
					System.out.println("Same host");
					if(echoSocket.getPort() == server_port){
						System.out.println("And same port, skipping...");
						//getting next (is skipping)
						hostserv = getNext_host();
						serverHostname = hostserv.host_name;
						server_port = hostserv.port;
					}
				}
			}
			try {
				System.out.println("Essai de se connecter a l'hote "+ serverHostname + " au port " + server_port);
				tmp_Socket = new Socket();
				tmp_Socket.connect(new InetSocketAddress(serverHostname,server_port), TIMEOUT_CONNECT);
				break; //on a trouver un serveur valide, on sort
			} catch (UnknownHostException e) {
				System.err.println("Hote inconnu: " + serverHostname);
			} catch (IOException e) {
				System.err.println("Ne peut pas se connecter au serveur: "+ serverHostname);
			}
			hostserv = getNext_host();
			serverHostname = hostserv.host_name;
			server_port = hostserv.port;
		}

		return tmp_Socket;
	}

	/**
	 * Fonction temporiser attendant le retour d'une requete.
	 * Sert a lancer un timeout pour effectuer une strategy 
	 * si le timeout est atteint avant la reponse
	 * @param in : Stream d'entree pour la reponse
	 * @return un etat de retour, cf state_echo
	 * @throws IOException
	 */
	private state_echo launch(BufferedReader in) throws IOException {	
		final BufferedReader tmp_in = in;
		final Future<state_echo> handler = executor.submit(new Callable<state_echo>() {
		    @Override
		    public state_echo call() throws Exception {
		    	String echo = tmp_in.readLine();
		    	state_echo res;
				System.out.println("echo: " + echo);
				if (echo.contains("BYE")) {
					res = state_echo.END;
				}
				else {
					res = state_echo.NEXT;
				}
				executor.shutdownNow();
				return res;
		    }
		});
		try {
			executor.schedule(new Runnable(){
			    @Override
			    public void run(){
			    	System.out.println("Canceling job");
			        handler.cancel(true);
			        System.out.println("Cancel done");
			        executor.shutdown();
			        //state_echo.TIMEOUT;
			    }      
			}, TIMEOUT_REPONSE, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException rj){
			if(handler.isDone() == false ){
				System.out.println("Error with scheduling timeout");
			}	
		}

		while(!handler.isDone()); // on attend
		
		if(handler.isCancelled()) {
			return state_echo.TIMEOUT;
		}
		try {
			return handler.get();
		} catch (InterruptedException e) {
			return state_echo.TIMEOUT;
		} catch (ExecutionException e) {
			System.err.println("Erreur quelque part dans le thread.. veuillez recommencez !!");
			return state_echo.NEXT; //on reeesaye une nouvelle entree de client
		}
	}
	
	/**
	 * Constructeur
	 * @throws IOException
	 */
	public Client() throws IOException {
		servs_dico = new ServDico();

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Entrez l'ip du serveur");
		String serverHostname = stdIn.readLine();

		if(init_sock(serverHostname, null,false)==false)
			System.exit(2);
		
		String userInput="";
		boolean getinput = true;

		while (true) {
			if(getinput==true){
				System.out.print("Entree: ");
				userInput = stdIn.readLine();
			}
			getinput=true;
			out.println(userInput); 
			executor = Executors.newScheduledThreadPool(2);
			final state_echo res = launch(in);
			if(res == state_echo.END) break;
			else if(res == state_echo.TIMEOUT){
				System.out.println("Timeout reached, need to switch host");
				if(init_sock(null,null,true)==false)
					System.exit(2);
				getinput=false;
				//ok mais est-ce qu'on refait cette p** de requete ??
			}
		}
		stdIn.close();
		
		out.close();
		in.close();
		echoSocket.close();
	}

	public static void main(String[] args) throws IOException {
		new Client();
	}
}
