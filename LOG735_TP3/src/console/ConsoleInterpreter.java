package console;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Scanner;

import logs.Logger;
import connexion.AccesPoint;
import connexion.Commande;
import connexion.Commande.CommandeType;
import connexion.ConnexionInfo;
import connexion.MultiAccesPoint;
import connexion.Tunnel;

public class ConsoleInterpreter extends MultiAccesPoint {

	private static final Logger log = Logger.createLog(ConsoleInterpreter.class);
	
	private Tunnel tun;

	public static void main(String[] args) {
		new ConsoleInterpreter();
	}

	public ConsoleInterpreter() {
		ConnexionInfo banquecInfo = new ConnexionInfo("127.0.0.1", 9100);
		log.message("Try to connect to " + banquecInfo);

		try {
			tun = this.connectTo("BanqueAccess", banquecInfo);

			log.message("ask Succusrales list");
			tun.sendCommande(new Commande(CommandeType.LIST, ""));

			Scanner scanIn = new Scanner(System.in);
			
			while (true) {
				
				System.out.println("Supported Commandes are :");
				System.out.println("\t LIST");
				System.out.println("\t ID");
				
				System.out.println("Enter Commande :");
				
				String comm = scanIn.nextLine();
				
				Commande c = null;
				if(comm.equals(CommandeType.LIST.name()))
					c=new Commande(CommandeType.LIST, "");
				else if(comm.equals(CommandeType.ID.name()))
					c=new Commande(CommandeType.ID, "");
				else
					System.out.println("Unreconized Commande. Try again !");
				
				if(c != null)
					sendcommande(c);

			}
			
			//scanIn.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendcommande(Commande c) {
		tun.sendCommande(c);
	}

	@Override
	protected void newTunnelCreated(Tunnel tun) {

	}

	@Override
	protected void commandeReceiveFrom(Commande comm, Tunnel tun) {

		System.out.println(comm.getMessageContent());
	}

}
