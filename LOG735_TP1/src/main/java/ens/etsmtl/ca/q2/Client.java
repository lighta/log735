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
[Résumé des fonctionnalités et de la raison d’être de la classe]
******************************************************/

package ens.etsmtl.ca.q2;
import java.io.*;
import java.net.*;

public class Client {
	public static void main(String[] args) throws IOException {

		String serverHostname = new String("127.0.0.1");

		if (args.length > 0)
			serverHostname = args[0];
		System.out.println("Essai de se connecter � l'h�te " + serverHostname
				+ " au port 10118.");

		Socket echoSocket = null;
		PrintWriter out = null;
		BufferedReader in = null;

		try {
			echoSocket = new Socket(serverHostname, 10118);
			out = new PrintWriter(echoSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					echoSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("H�te inconnu: " + serverHostname);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Ne pas se connecter au serveur: "
					+ serverHostname);
			System.exit(1);
		}

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(
				System.in));
		String userInput;
		System.out.print("Entr�e: ");
		while ((userInput = stdIn.readLine()) != null) {
			out.println(userInput);
			String echo = in.readLine();
			System.out.println("echo: " + echo);
			if (echo.contains("BYE")) {
				break;
			}

			System.out.print("Entr�e: ");
		}

		out.close();
		in.close();
		stdIn.close();
		echoSocket.close();
	}
}
