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
Simple client TCP se connectant a un serveur quelquonque.
******************************************************/

package ens.etsmtl.ca.q1;
import java.io.*;
import java.net.*;

public class Client {
	public static void main(String[] args) throws IOException {

		String serverHostname = new String ("127.0.0.1");

        if (args.length > 0)
        	serverHostname = args[0];
        System.out.println ("Essai de se connecter a l'hote " +
		serverHostname + " au port 10118.");

        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            echoSocket = new Socket(serverHostname, 10118);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Hote inconnu: " + serverHostname);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Ne peut pas se connecter au serveur: " + serverHostname);
            System.exit(1);
        }

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String userInput;
        System.out.print ("Entree: ");
        while ((userInput = stdIn.readLine()) != null) {
        	out.println(userInput);
        	System.out.println("echo: " + in.readLine());
            System.out.print ("Entree: ");
        }

        out.close();
        in.close();
        stdIn.close();
        echoSocket.close();
    }
}

