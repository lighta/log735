package Q3;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class Client {

	private class EchoHandler extends Thread
	{
		
		private BufferedReader in;

		public EchoHandler(BufferedReader in) {
			this. in = in;
		}
		
		@Override
		public void run() {
			try {
				String echo = in.readLine();
				System.out.println("echo: " + echo);
				if (echo.contains("BYE")) {
						
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	
	private class ServerDico {

		@SuppressWarnings("unused")
		protected String host_name = "";
		@SuppressWarnings("unused")
		protected int port = 10118;

		@SuppressWarnings("unused")
		public ServerDico(String host_name, int port) {
			this(host_name);
			this.port = port;
		}

		public ServerDico(String host_name) {
			this.host_name = host_name;
		}
	}

	private final Map<Integer, ServerDico> servers_dico = new HashMap<>();

	private final void FillServerDico() {
		servers_dico.put(0, new ServerDico("127.0.0.1"));
		servers_dico.put(1, new ServerDico("127.0.0.2"));
	}

	private ServerDico getNext_host(int current) {
		if (current >= servers_dico.size() - 1) {
			System.err.println("Plus de serveur disponible pour switch");
			System.exit(1);
		}
		return servers_dico.get(current + 1);
	}

	private Socket attempt_to_connect() {
		return attempt_to_connect(null, null);
	}

	private Socket attempt_to_connect(String hostname, Integer port) {
		Socket echoSocket = null;
		String serverHostname = "";
		int server_port = 10118;

		ServerDico hostserv;

		int current = -1;

		if (hostname != null) {
			serverHostname = hostname;
		}
		if (port != null) {
			server_port = port;
		}

		while (true) {
			try {
				System.out.println("Essai de se connecter à l'hôte "
						+ serverHostname + " au port " + server_port);
				echoSocket = new Socket();
				echoSocket.connect(new InetSocketAddress(serverHostname,
						server_port), 3000);
				break;
			} catch (UnknownHostException e) {
				System.err.println("Hôte inconnu: " + serverHostname);
			} catch (IOException e) {
				System.err.println("Ne peut pas se connecter au serveur: "
						+ serverHostname);
			}

			hostserv = getNext_host(current);
			serverHostname = hostserv.host_name;
			server_port = hostserv.port;
			current++;
		}

		return echoSocket;
	}

	public Client() throws IOException {
		Socket echoSocket = null;
		PrintWriter out = null;
		int current = -1;
		int port = 10118;
		ServerDico hostserv;
		EchoHandler echoHand = null;

		FillServerDico();

		BufferedReader stdIn2 = new BufferedReader(new InputStreamReader(
				System.in));
		System.out.println("Entrez l'ip du serveur");
		String serverHostname = stdIn2.readLine();

		echoSocket = attempt_to_connect(serverHostname, port);

		if (echoSocket == null) {
			System.out.println("Aucun serveur dispo");
			System.exit(2);
		}

		out = new PrintWriter(echoSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				echoSocket.getInputStream()));

		echoHand = new EchoHandler(in);
		
		
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(
				System.in));
		
		
		String userInput;
		System.out.print("Entrée: ");

		while ((userInput = stdIn.readLine()) != null) {
			out.println(userInput);
			
			echoHand.start();
			
			try {
				Thread.sleep(3*1000);
								
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			System.out.print("Entrée: ");
		}

		out.close();
		in.close();
		stdIn.close();
		echoSocket.close();
	}

	public static void main(String[] args) throws IOException {
		new Client();
	}
}
