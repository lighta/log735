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
Class servant a definir un dictionnaire de serveur joignable
******************************************************/

package ens.etsmtl.ca.q6;

import java.util.HashMap;
import java.util.Map;

/**
 * Dictionnaire regroupant un ensemble des definission de connection
 * @author lighta
 */
public class ServDico {
	protected String host_name = "";
	protected int port = 10118;
	
	/**
	 * Objet definissant les information necessaire 
	 * pour la connection a un serveur
	 * host,port,pass,.. ?
	 * @author lighta
	 */
	public class ServerDef {
		public String host_name = "";
		public int port = 10118;
		public ServerDef(String host_name, int port) {
			this(host_name);
			this.port = port;
		}
		public ServerDef(String host_name) {
			this.host_name = host_name;
		}
		
		@Override
		public String toString() {
			return "hostname="+host_name+" port="+port;
		}
	}
	
	public Map<Integer, ServerDef> servers_dico = new HashMap<>(); //liste reel

	/**
	 * Constructeur
	 */
	public ServDico() {
		super();
		FillServerDico();
	}

	/**
	 * Fonction de remplissage du dictionnaire par defaut
	 */
	private void FillServerDico() {
		servers_dico.put(0, new ServerDef("127.0.0.1"));
		servers_dico.put(1, new ServerDef("127.0.0.2"));
	}
}
