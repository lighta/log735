package ens.etsmtl.ca.q3;

import java.net.*;
import java.io.*;

public class Server {
	static boolean run = true;
	static int second=5;

	public static class HandlerTCP extends Thread {
		Socket clientSocket = null;

		public HandlerTCP(Socket clientSocket) throws IOException {
			this.clientSocket = clientSocket;
		}

		private void reply() throws IOException, InterruptedException {
			PrintWriter out;
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			
			String inputLine = null;
			while (true) {
				while(inputLine == null || inputLine.isEmpty())
					inputLine = in.readLine();
				System.out.println("Serveur waiting for : " + second);
				Thread.sleep(1000*second);
				System.out.println("Sleep ended");
				inputLine = inputLine.toUpperCase();
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

	
	/**
	 * 
	 * @param args[0] programm inputStream (null for standard input)
	 * @param args[1] programm outputStream (null for standard output)
	 * @param args[2] programm errOutputStream (null for standard output)
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException { 
		
		BufferedInputStream input;
		PrintStream output;
		PrintStream errOutput;
		
		//set programm inputStream
		if (args.length >= 1 && args[0] != null){
			FileInputStream fis = new FileInputStream (args [0]);
			input = new BufferedInputStream(fis);
		}
		else{
			input = new BufferedInputStream(System.in);
		}
		
		
		//set programm outputStream
		if (args.length >= 2 && args[1] != null){
			FileOutputStream fos = new FileOutputStream (args [1]);
			output = new PrintStream(fos);
		}
		else{
			output = new PrintStream(System.out);
		}
		
		//set programm outputStream
		if (args.length >= 3 && args[2] != null){
			FileOutputStream fos = new FileOutputStream (args [2]);
			errOutput = new PrintStream(fos);
		}
		else{
			errOutput = new PrintStream(System.err);
		}
		
		System.setIn(input);
		System.setOut(output);
		System.setErr(errOutput);
		
		
		
		
		Socket clientSocket = null;
		ServerSocket serverSocket = null;
		InetAddress ipAddress;
		String inputLine = "";

		
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Entrez l'ip de bind du serveur");
		while(inputLine == null || inputLine.isEmpty())
			inputLine = stdIn.readLine();
		
		System.out.println(inputLine);
		//check ip
		ipAddress= InetAddress.getByName(inputLine);
		
		System.out.println("Entrez le nb de second");
		while(inputLine == null || inputLine.isEmpty())
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